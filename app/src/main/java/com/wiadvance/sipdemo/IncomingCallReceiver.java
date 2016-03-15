package com.wiadvance.sipdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/***
 * Listens for incoming SIP calls, intercepts and hands them off to SipActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private static final String TAG = "IncomingCallReceiver";

    /**
     * Processes the incoming call, answers it, and hands it over to the SipActivity.
     *
     * @param context The context under which the receiver is running.
     * @param sipIntent  The intent being received.
     */
    @Override
    public void onReceive(final Context context, Intent sipIntent) {
        Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], sipIntent = [" + sipIntent + "]");
        NotificationUtil.updateStatus(context, "Incoming call !");

        showNotification(context, sipIntent);
    }

    private void showNotification(Context context, Intent sipIntnet) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.phone_icon)
                .setContentTitle("Incoming SIP call")
                .setContentText("Someone is calling you");

        Intent answerIntent = CallReceiverActivity.newIntent(context, sipIntnet, true);
        Intent declineIntent = CallReceiverActivity.newIntent(context, sipIntnet, false);
        PendingIntent answerPendingIntent = createPendingIntent(context, answerIntent);
        PendingIntent declinePendingIntent = createPendingIntent(context, declineIntent);

        builder.setContentIntent(answerPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder.addAction(new NotificationCompat.Action(
                R.drawable.answer, "Answer", answerPendingIntent));
        builder.addAction(new NotificationCompat.Action(
                R.drawable.decline, "Decline", declinePendingIntent));

        Notification notif = builder.build();
        notif.defaults |= Notification.DEFAULT_VIBRATE;
        mNotificationManager.notify(CallReceiverActivity.INCOMING_CALL_NOTIFICATION_ID, notif);
    }

    private PendingIntent createPendingIntent(Context context, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
