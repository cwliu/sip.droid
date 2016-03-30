package com.wiadvance.sipdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.wiadvance.sipdemo.linphone.LinphoneSipManager;
import com.wiadvance.sipdemo.model.Contact;

public class MakeCallActivity extends AppCompatActivity {


    private static final String ARG_CONTACT = "ARG_CONTACT";
    private LinphoneSipManager mWiSipManager;
    private Contact mCallee;
    private BroadcastReceiver mCallStatusReceiver;
    private TextView mCallStatus;
    private boolean isEndedByCaller;

    public static Intent newIntent(Context context, Contact contact) {
        Intent intent = new Intent(context, MakeCallActivity.class);
        intent.putExtra(ARG_CONTACT, new Gson().toJson(contact));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_call);

        initView();

        mWiSipManager = new LinphoneSipManager(this);
        mWiSipManager.makeCall(mCallee);

    }

    private void initView() {
        String json = getIntent().getStringExtra(ARG_CONTACT);
        mCallee = new Gson().fromJson(json, Contact.class);

        TextView name = (TextView) findViewById(R.id.callee_name);
        mCallStatus = (TextView) findViewById(R.id.call_status);
        ImageView avatar = (ImageView) findViewById(R.id.callee_avatar);

        name.setText(mCallee.getName());

        ImageButton endcall = (ImageButton) findViewById(R.id.make_call_end_call_button);
        endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEndedByCaller = true;
                mWiSipManager.endAllCall();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();

        mCallStatusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(final Context context, Intent intent) {
                boolean on = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_ON, false);
                if (on) {
                    String status = intent.getStringExtra(NotificationUtil.NOTIFY_CALL_STATUS);
                    if (status != null) {
                        mCallStatus.setText(status);
                    }

                    boolean isSip = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_IS_SIP, true);
                    if (status != null && !isSip) {
                        findViewById(R.id.sip_phone_icon_imageView).setVisibility(View.GONE);
                    }

                } else {
                    if(mWiSipManager.triedSip()){
                        mCallStatus.setText("Call Ended");

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mWiSipManager.isHasConnected()){
                                    Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show();
                                }else if(isEndedByCaller) {
                                    //Nothing need to do
                                }else{
                                    Toast.makeText(context, "No one answered the phone", Toast.LENGTH_SHORT).show();
                                }

                                finish();
                            }
                        }, 1200);
                    }
                }
            }
        };
        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_CALL_STATUS_CHANGED);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mCallStatusReceiver, notify_filter);

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mCallStatusReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mCallStatusReceiver);
        }

        mWiSipManager.endAllCall();
    }
}
