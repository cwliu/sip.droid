package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationReceiver, onReceive()");
        String message = intent.getStringExtra(NotificationUtil.GLOBAL_NOTIFY_MESSAGE);
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}