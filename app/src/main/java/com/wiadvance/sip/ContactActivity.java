package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mixpanel.android.mpmetrics.MixpanelAPI;


public class ContactActivity extends SingleFragmentActivity {

    private String TAG = "ContactActivity";
    private BroadcastReceiver mNotificationReceiver;

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANL_TOKEN);
        mixpanel.track(TAG, null);
    }

    @Override
    protected Fragment createFragment() {

        ContactFragment sipFragment = ContactFragment.newInstance();
        return sipFragment;
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, notify_filter);

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
            Log.d(TAG, "NotificationReceiver, onReceive()");
            String message = intent.getStringExtra(NotificationUtil.GLOBAL_NOTIFY_MESSAGE);
            Toast.makeText(ContactActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
