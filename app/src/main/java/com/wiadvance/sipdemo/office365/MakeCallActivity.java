package com.wiadvance.sipdemo.office365;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wiadvance.sipdemo.NotificationUtil;
import com.wiadvance.sipdemo.R;
import com.wiadvance.sipdemo.linphone.LinphoneSipManager;
import com.wiadvance.sipdemo.model.Contact;

public class MakeCallActivity extends AppCompatActivity {


    private static final String ARG_CONTACT = "ARG_CONTACT";
    private LinphoneSipManager mWiSipManager;
    private Contact mCallee;
    private BroadcastReceiver mCallStatusReceiver;
    private TextView mCallStatus;

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
                mWiSipManager.endAllCall();
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
            public void onReceive(Context context, Intent intent) {
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
                        finish();
                    }
                }
            }
        };
        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_CALL);
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
