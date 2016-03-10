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

    public SIPFragment getSipFragment() {
        return mSipFragment;
    }

    @Override
    protected Fragment createFragment() {

        mSipFragment = SIPFragment.newInstance();
        return mSipFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter notify_filter = new IntentFilter(Notification.ACTION_NOTIFICATION);
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
            String message = intent.getStringExtra(Notification.NOTIFY_MESSAGE);
            Toast.makeText(SipActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
