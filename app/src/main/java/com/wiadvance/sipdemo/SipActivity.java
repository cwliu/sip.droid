package com.wiadvance.sipdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class SipActivity extends SingleFragmentActivity {

    private String TAG = "SipActivity";
    private BroadcastReceiver mNotificationReceiver;

    private SIPFragment mSipFragment;
    private IncomingCallReceiver callReceiver;

    private static String EXTRA_NAME = "name";
    private static String EXTRA_EMAIL = "email";
    private static String EXTRA_SIP_NUMBER = "sip_number";

    public SIPFragment getSipFragment() {
        return mSipFragment;
    }

    public static Intent newIntent(Context context, String name, String email, String sipNumber){
        Intent intent = new Intent(context, SipActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_SIP_NUMBER, sipNumber);
        return intent;
    }

    @Override
    protected Fragment createFragment() {

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String email = getIntent().getStringExtra(EXTRA_EMAIL);
        String sipNumber = getIntent().getStringExtra(EXTRA_SIP_NUMBER);
        mSipFragment = SIPFragment.newInstance(name, email, sipNumber);
        return mSipFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, notify_filter);

        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter receiver_filter = new IntentFilter();
        receiver_filter.addAction(SIPFragment.ACTION_INCOMING_CALL);
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, receiver_filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mNotificationReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mNotificationReceiver);
        }
        if (callReceiver != null) {
            this.unregisterReceiver(callReceiver);
        }
    }

    class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "NotificationReceiver, onReceive()");
            String message = intent.getStringExtra(NotificationUtil.NOTIFY_MESSAGE);
            Toast.makeText(SipActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
