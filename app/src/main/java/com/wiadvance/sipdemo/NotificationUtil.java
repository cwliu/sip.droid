package com.wiadvance.sipdemo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NotificationUtil {

    private static final String TAG = "NotificationUtil";

    public static String ACTION_NOTIFICATION = "com.wiadvance.sipdemo.notification";
    public static String ACTION_CALL = "com.wiadvance.sipdemo.call";

    public static final String NOTIFY_MESSAGE = "notify_message";
    public static final String NOTIFY_CALL_ON = "notify_call_online";

    public static final int INCOMING_CALL_NOTIFICATION_ID = 1;

    public static void displayStatus(Context context, String s) {
        Log.d(TAG, "displayStatus: " + s);
        Intent intent = new Intent(ACTION_NOTIFICATION);
        intent.putExtra(NOTIFY_MESSAGE, s);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void notifyCallStatus(Context context, boolean on){
        Intent intent = new Intent(ACTION_CALL);
        intent.putExtra(NOTIFY_CALL_ON, on);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void cancelNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID);
    }
}
