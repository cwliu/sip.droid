package com.wiadvance.sipdemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
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

//        // The stack builder object will contain an artificial back stack for the
//        // started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(LoginActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent answerPendingIntent = PendingIntent.getActivity(context, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent declinePendingIntent = PendingIntent.getActivity(context, 1, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(answerPendingIntent);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        builder.addAction(new NotificationCompat.Action(
                R.drawable.answer, "Answer", answerPendingIntent));
        builder.addAction(new NotificationCompat.Action(
                R.drawable.decline, "Decline", declinePendingIntent));

        mNotificationManager.notify(
                CallReceiverActivity.INCOMING_CALL_NOTIFICATION_ID, builder.build()
        );
    }
}
