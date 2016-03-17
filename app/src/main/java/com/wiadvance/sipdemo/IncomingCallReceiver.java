package com.wiadvance.sipdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/***
 * Listens for incoming SIP calls, intercepts and hands them off to SipActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private static final String TAG = "IncomingCallReceiver";
    private static final int ANSWER_REQUEST_CODE = 1;
    private static final int DECLINE_REQUEST_CODE = 2;

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

    private void showNotification(Context context, Intent sipIntent) {

        SipAudioCall.Listener listener = new SipAudioCall.Listener() {
        };

        SipManager sipManager = SipManager.newInstance(context);
        String name = "N/A";
        try {
            SipAudioCall call = sipManager.takeAudioCall(sipIntent, listener);
            name = call.getPeerProfile().getUserName();
        } catch (SipException e) {
            e.printStackTrace();
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.phone_icon)
                .setContentTitle("Incoming SIP call")
                .setContentText(name +" is calling you");

        Intent answerIntent = CallReceiverActivity.newIntent(context, sipIntent, true);
        Intent declineIntent = CallReceiverActivity.newIntent(context, sipIntent, false);
        PendingIntent answerPendingIntent = createPendingIntent(context, ANSWER_REQUEST_CODE, answerIntent);
        PendingIntent declinePendingIntent = createPendingIntent(context, DECLINE_REQUEST_CODE, declineIntent);

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

    private PendingIntent createPendingIntent(Context context, int requestCode, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
