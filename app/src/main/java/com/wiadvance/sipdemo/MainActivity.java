package com.wiadvance.sipdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends SingleFragmentActivity {

    private String TAG = "MainActivity";
    private BroadcastReceiver mNotificationReceiver;

    @Override
    protected Fragment createFragment() {
        return SIPFragment.newInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter filter = new IntentFilter(SIPFragment.ACTION_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mNotificationReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    class NotificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            String message = intent.getStringExtra(SIPFragment.NOTIFY_MESSAGE);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
