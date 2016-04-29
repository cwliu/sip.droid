package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class NotificationUtil {

    private static final String TAG = "NotificationUtil";

    public static final String ACTION_NOTIFICATION = "com.wiadvance.sipdemo.notification";
    public static final String ACTION_FAVORITE_NOTIFICATION = "com.wiadvance.sipdemo.favorite_notification";
    public static final String ACTION_PHONE_CONTACT_LOAD_COMPLETE = "com.wiadvance.sip.phone_contact_load_complete";
    public static final String ACTION_CALL_STATUS_CHANGED = "com.wiadvance.sipdemo.call";
    public static final String ACTION_CALL_MSG = "com.wiadvance.sipdemo.call_msg";
    public static final String ACTION_COMPANY_UPDATE_NOTIFICATION = "com.wiadvance.sipdemo.company.update";

    public static final String GLOBAL_NOTIFY_MESSAGE = "notify_message";

    public static final String NOTIFY_CALL_ON = "notify_call_on";
    public static final String NOTIFY_CALL_STATUS = "notify_call_status";
    public static final String NOTIFY_CALL_IS_SIP = "is_sip_call";
    public static final String NOTIFY_CALL_MSG = "notify_call_msg";

    public static void displayStatus(Context context, String s) {
        Log.d(TAG, "displayStatus: " + s);
        Intent intent = new Intent(ACTION_NOTIFICATION);
        intent.putExtra(GLOBAL_NOTIFY_MESSAGE, s);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void notifyCallStatus(Context context, boolean on, String message, boolean isSip){
        Intent intent = new Intent(ACTION_CALL_STATUS_CHANGED);
        intent.putExtra(NOTIFY_CALL_ON, on);
        intent.putExtra(NOTIFY_CALL_STATUS, message);
        intent.putExtra(NOTIFY_CALL_IS_SIP, isSip);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void notifyCallMsg(Context context, String message){
        Intent intent = new Intent(ACTION_CALL_MSG);
        intent.putExtra(NOTIFY_CALL_MSG, message);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void favoriteUpdate(Context context){
        Intent intent = new Intent(ACTION_FAVORITE_NOTIFICATION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void phoneContactUpdate(Context context){
        Intent intent = new Intent(ACTION_PHONE_CONTACT_LOAD_COMPLETE);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
