package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Picasso;
import com.wiadvance.sip.db.RegularContactTableHelper;
import com.wiadvance.sip.linphone.LinphoneCoreHelper;
import com.wiadvance.sip.model.Contact;

import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.Reason;

import java.util.Iterator;
import java.util.List;

public class CallReceiverActivity extends AppCompatActivity implements SensorEventListener {

    private static final String ARG_CALLER_NUM = "caller_num";

    private static String TAG = "CallReceiverActivity";

    private LinphoneCore mLc;
    private LinphoneCall mLinephoneCall;
    private NotificationReceiver mNotificationReceiver;
    private BroadcastReceiver mCallStatusReceiver;

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private float originalBrightness;

    private ImageButton mEndCallButton;
    private TextView mCallStatus;

    public static Intent newIntent(Context context, String caller) {
        Intent intent = new Intent(context, CallReceiverActivity.class);
        intent.putExtra(ARG_CALLER_NUM, caller);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_receiver);

        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");

        try {
            mLc = LinphoneCoreHelper.getLinphoneCoreInstance(this);
        } catch (LinphoneCoreException e) {
            throw new RuntimeException("Can't get linphone core");
        }

        String callerAddress = getIntent().getStringExtra(ARG_CALLER_NUM);

        TextView name = (TextView) findViewById(R.id.call_receiver_sip);
        if (name != null) {
            name.setText(LinphoneUtils.getUsernameFromAddress(callerAddress));
        }

        String callerUsername = LinphoneUtils.getUsernameFromAddress(callerAddress);
        Contact matchContact = PhoneUtils.getCompanyContactByAccount(this, callerUsername);
        if(matchContact != null) {
            // Set Office 365 Avatar
            if (matchContact.getEmail() != null) {
                ImageView avatar = (ImageView) findViewById(R.id.call_receiver_avatar);
                Picasso.with(this).load(matchContact.getPhotoUri()).placeholder(R.drawable.avatar_120dp).into(avatar);
            }

            if (name != null) {
                name.setText(matchContact.getName());
            }
            RegularContactTableHelper.getInstance(this).addContactCountByOne(matchContact);
            UserData.sCurrentContact = matchContact;
        }else{
            Contact c = new Contact(callerUsername);
            c.setType(Contact.TYPE_EXTERNAL);
            c.setSip(callerUsername);
            UserData.sCurrentContact = c;
        }

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANL_TOKEN);
        mixpanel.track(TAG, null);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        originalBrightness = lp.screenBrightness;

        mEndCallButton = (ImageButton) findViewById(R.id.end_call_button);
        mCallStatus = (TextView) findViewById(R.id.call_status);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_NOTIFICATION);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mNotificationReceiver, notify_filter);

        mCallStatusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                boolean on = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_ON, false);
                if (!on) {
                    finish();
                }
            }
        };

        IntentFilter call_notify_filter = new IntentFilter(NotificationUtil.ACTION_CALL_STATUS_CHANGED);
        LocalBroadcastManager call_manager = LocalBroadcastManager.getInstance(this);
        call_manager.registerReceiver(mCallStatusReceiver, call_notify_filter);

        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mNotificationReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mNotificationReceiver);
        }

        if (mCallStatusReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mCallStatusReceiver);
        }

        mSensorManager.unregisterListener(this, mProximitySensor);

        UserData.recordCallLog(this);
    }

    public void onEndCallButtonClick(View view) {
        if (mLinephoneCall != null) {
            mLc.terminateCall(mLinephoneCall);
        }
        finish();
    }

    public void onAnswerButtonClick(View view) {
        List address = LinphoneUtils.getLinphoneCalls(mLc);
        Log.d(TAG, "Number of call: " + address.size());
        Iterator contact = address.iterator();


        while (contact.hasNext()) {
            mLinephoneCall = (LinphoneCall) contact.next();
            if (LinphoneCall.State.IncomingReceived == mLinephoneCall.getState()) {
                break;
            }
        }

        if (mLinephoneCall == null) {
            Log.e(TAG, "Couldn\'t find incoming call");
        } else {
            LinphoneCallParams params = mLc.createDefaultCallParameters();
            params.enableLowBandwidth(false);

            LinphoneAddress address1 = mLinephoneCall.getRemoteAddress();
            Log.d(TAG, "Find a incoming call, number: " + address1.asStringUriOnly());
            try {
                mLc.acceptCallWithParams(mLinephoneCall, params);
                mCallStatus.setText(R.string.call_established_msg);

            } catch (LinphoneCoreException e) {
                Log.e(TAG, "Accept Call exception: ", e);
                NotificationUtil.displayStatus(this, "Failed to accept the call");
            }
        }

        findViewById(R.id.answer_call_button).setVisibility(View.GONE);
        findViewById(R.id.deny_call_button).setVisibility(View.GONE);
        findViewById(R.id.end_call_button).setVisibility(View.VISIBLE);

    }

    public void onDenyCallButtonClick(View view) {
        List address = LinphoneUtils.getLinphoneCalls(mLc);
        Log.d(TAG, "Number of call: " + address.size());
        Iterator contact = address.iterator();

        while (contact.hasNext()) {
            mLinephoneCall = (LinphoneCall) contact.next();
            if (LinphoneCall.State.IncomingReceived == mLinephoneCall.getState()) {
                break;
            }
        }

        if (mLinephoneCall == null) {
            Log.e(TAG, "Couldn\'t find incoming call");
        } else {
            mLc.declineCall(mLinephoneCall, Reason.Declined);
        }
        finish();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] < mProximitySensor.getMaximumRange()) {
            dimScreen(true);
        } else {
            dimScreen(false);
        }
    }

    private void dimScreen(boolean dim) {
        Log.d(TAG, "dimScreen() called with: ");

        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        if (dim) {
            lp.screenBrightness = 0.0f;
            mEndCallButton.setEnabled(false);
        } else {
            lp.screenBrightness = originalBrightness;
            mEndCallButton.setEnabled(true);
        }
        this.getWindow().setAttributes(lp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}