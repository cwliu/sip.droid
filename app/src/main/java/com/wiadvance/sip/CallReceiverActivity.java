package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Picasso;
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

public class CallReceiverActivity extends AppCompatActivity {

    private static final String ARG_CALLER_NUM = "caller_num";

    private static String TAG = "CallReceiverActivity";

    private LinphoneCore mLc;
    private LinphoneCall mLinephoneCall;
    private NotificationReceiver mNotificationReceiver;
    private BroadcastReceiver mCallStatusReceiver;
    private ImageView call_receiver_avatar;

    public static Intent newLinephoneIntnet(Context context, String caller) {
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

        String caller_address = getIntent().getStringExtra(ARG_CALLER_NUM);
        TextView name = (TextView) findViewById(R.id.call_receiver_sip);
        if (name != null) {
            name.setText(LinphoneUtils.getUsernameFromAddress(caller_address));
        }


        for (Contact c : UserData.sCompanyContactList) {
            if (caller_address.contains(c.getSip())) {
                if (c.getEmail() != null) {
                    ImageView avatar = (ImageView) findViewById(R.id.call_receiver_avatar);
                    Picasso.with(this).load(c.getPhotoUri()).placeholder(R.drawable.avatar_120dp).into(avatar);
                }

                if (name != null) {
                    name.setText(c.getName());
                }

                UserData.updateRecentContact(this, c);
                break;
            }
        }

        // TODO
//        for(Contact c: UserData.sPhoneContactList){
//            if(caller_address.contains(c.getPhone())){
//                if(c.getEmail() != null){
//                    ImageView avatar = (ImageView) findViewById(R.id.call_receiver_avatar);
//                    Picasso.with(this).load(c.getPhotoUri()).placeholder(R.drawable.avatar_120dp).into(avatar);
//                }
//
//                if (name != null) {
//                    name.setText(c.getName());
//                }
//
//                UserData.updateRecentContact(this, c);
//                break;
//            }
//        }


        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANL_TOKEN);
        mixpanel.track(TAG, null);
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
                NotificationUtil.displayStatus(this, "Call is established");
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
}