package com.wiadvance.sip;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.wiadvance.sip.db.RegularContactTableHelper;
import com.wiadvance.sip.linphone.LinphoneSipManager;
import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.office365.Constants;

public class MakeCallActivity extends AppCompatActivity implements SensorEventListener {

    private static final String TAG = "MakeCallActivity";

    private static final String ARG_CONTACT = "ARG_CONTACT";

    private LinphoneSipManager mWiSipManager;
    private Contact mCallee;

    private boolean isEndedByCaller;
    private boolean isCalling;

    private TextView mCallStatus;
    private TextView mCallStatusAnimation;
    private ImageButton mEndcallButton;

    private SensorManager mSensorManager;
    private Sensor mProximitySensor;
    private float originalBrightness;

    public static Intent newIntent(Context context, Contact contact) {
        Intent intent = new Intent(context, MakeCallActivity.class);
        intent.putExtra(ARG_CONTACT, new Gson().toJson(contact));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_call);

        String json = getIntent().getStringExtra(ARG_CONTACT);
        mCallee = new Gson().fromJson(json, Contact.class);

        if(mCallee.getId() != 0){ // Make sure it's existed contact in database;
            RegularContactTableHelper.getInstance(this).addContactCountByOne(mCallee);
        }

        initView();

        mWiSipManager = new LinphoneSipManager(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        originalBrightness = lp.screenBrightness;

    }

    private void initView() {

        TextView name = (TextView) findViewById(R.id.callee_name);
        if (mCallee != null && name != null) {
            name.setText(mCallee.getName());
        }

        mCallStatus = (TextView) findViewById(R.id.call_status);
        mCallStatusAnimation = (TextView) findViewById(R.id.call_status_dot_animation);

        ImageView avatar = (ImageView) findViewById(R.id.callee_avatar);

        String photoUrl = String.format(Constants.USER_PHOTO_URL_FORMAT, mCallee.getEmail());
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.avatar_120dp).into(avatar);


        mEndcallButton = (ImageButton) findViewById(R.id.make_call_end_call_button);
        if (mEndcallButton != null) {
            mEndcallButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isEndedByCaller = true;
                    mWiSipManager.endCall();
                    finish();
                }
            });
        }

        isCalling = true;
        final Handler dotAnimationHandler = new Handler();
        Runnable task = new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                String s = mCallStatusAnimation.getText().toString();
                if (s.length() == 3) {
                    mCallStatusAnimation.setText(".");
                } else {
                    mCallStatusAnimation.setText(s + ".");
                }
                if (isCalling) {
                    dotAnimationHandler.postDelayed(this, 800);
                }else{
                    mCallStatusAnimation.setText("");
                }
            }
        };
        dotAnimationHandler.post(task);

    }

    @Override
    public void onStart() {
        super.onStart();


        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_CALL_STATUS_CHANGED);
        notify_filter.addAction(NotificationUtil.ACTION_CALL_MSG);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mCallStatusReceiver, notify_filter);

        mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

        UserData.sCurrentContact = mCallee;
        mWiSipManager.makeCall(mCallee);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mCallStatusReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mCallStatusReceiver);
        }

        mWiSipManager.endCall();

        mSensorManager.unregisterListener(this, mProximitySensor);
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
            mEndcallButton.setEnabled(false);
        } else {
            lp.screenBrightness = originalBrightness;
            mEndcallButton.setEnabled(true);
        }
        this.getWindow().setAttributes(lp);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private BroadcastReceiver mCallStatusReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {

            if(intent.getAction().equals(NotificationUtil.ACTION_CALL_MSG)){

                isCalling = false;
                String status = intent.getStringExtra(NotificationUtil.NOTIFY_CALL_MSG);
                if (status != null) {
                    mCallStatus.setText(status);
                }

            }else{
                boolean on = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_ON, false);
                if (on) {
                    isCalling = true;
                    String status = intent.getStringExtra(NotificationUtil.NOTIFY_CALL_STATUS);
                    if (status != null) {
                        mCallStatus.setText(status);
                    }

                    boolean isSip = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_IS_SIP, true);
                    if (status != null && !isSip) {
                        removeSipIcon();
                    }
                } else {
                    isCalling = false;
                    if (mWiSipManager.triedSip()) {
                        finishActivity(context);
                    }
                }
            }
        }
    };

    private void finishActivity(final Context context) {
        mCallStatus.setText(R.string.call_end);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mWiSipManager.isHasConnected()) {
                    Toast.makeText(context, R.string.call_end, Toast.LENGTH_SHORT).show();
                } else if (isEndedByCaller) {
                    //Nothing need to do
                } else {
                    Toast.makeText(context, "No one answered the phone", Toast.LENGTH_SHORT).show();
                }

                finish();
            }
        }, 1200);
    }

    private void removeSipIcon() {
        View v = findViewById(R.id.sip_phone_icon_imageView);
        if(v != null){
            v.setVisibility(View.GONE);
        }
    }
}
