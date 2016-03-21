package com.wiadvance.sipdemo.nativesip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wiadvance.sipdemo.NotificationUtil;

/***
 * Listens for incoming SIP calls, intercepts and hands them off to SipActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private static final String TAG = "IncomingCallReceiver";

    /**
     * Processes the incoming call, answers it, and hands it over to the SipActivity.
     *
     * @param context   The context under which the receiver is running.
     * @param sipIntent The intent being received.
     */
    @Override
    public void onReceive(final Context context, Intent sipIntent) {
        Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], sipIntent = [" + sipIntent + "]");
        NotificationUtil.displayStatus(context, "Incoming call !");

        NativeSipManager.showIncomingCallNotification(context, sipIntent);
    }
}
