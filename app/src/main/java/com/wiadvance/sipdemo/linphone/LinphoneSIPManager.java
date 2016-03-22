package com.wiadvance.sipdemo.linphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.wiadvance.sipdemo.BuildConfig;
import com.wiadvance.sipdemo.CallReceiverActivity;
import com.wiadvance.sipdemo.R;
import com.wiadvance.sipdemo.WiSipManager;

import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

import java.util.Iterator;
import java.util.List;

public class LinphoneSipManager extends WiSipManager {

    private static final String TAG = "LinephoneSIPManager";

    private final Context mContext;
    private boolean mIsCalling;
    private LinphoneCall call = null;

    public LinphoneCore mLinphoneCore;
    private LinphoneProxyConfig mProxyConfig;

    private static final int ANSWER_REQUEST_CODE = 1;
    private static final int DECLINE_REQUEST_CODE = 2;

    public LinphoneSipManager(Context context) {

        mContext = context;
        try {
            mLinphoneCore = LinphoneCoreHelper.getLinphoneCoreInstance(context);
        } catch (LinphoneCoreException e) {
            throw new RuntimeException("Can't init LinphoneCore");
        }
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void register(String account) {

        String identity = "sip:" + account + "@" + BuildConfig.SIP_DOMAIN;
        try {
            mProxyConfig = mLinphoneCore.createProxyConfig(identity, BuildConfig.SIP_DOMAIN, null, true);
            mLinphoneCore.addProxyConfig(mProxyConfig);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(
                    account, BuildConfig.SIP_PASSWORD, null, BuildConfig.SIP_DOMAIN);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(mProxyConfig);

        } catch (LinphoneCoreException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    @Override
    public boolean unregister(String account) {
        if(mProxyConfig != null){
            mLinphoneCore.removeProxyConfig(mProxyConfig);
        }
        return true;
    }

    @Override
    public void makeCall(final String account) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    LinphoneCore lc = LinphoneCoreHelper.getLinphoneCoreInstance(mContext);
                    call = lc.invite(account);
                    if (call == null) {
                        Log.d(TAG, "Could not place call to");
                    } else {
                        Log.d(TAG, "Call to: " + account);
                        mIsCalling = true;

                        while (mIsCalling) {
                            lc.iterate();
                            try {
                                Thread.sleep(50L);
                                if (call.getState().toString().equals("CallEnd") || call.getState().toString().equals("Released")) {
                                    mIsCalling = false;
                                    Log.d(TAG, "Call end");
                                }

                            } catch (InterruptedException var8) {
                                Log.d(TAG, "Interrupted! Aborting");
                            }
                        }
                        if (!LinphoneCall.State.CallEnd.equals(call.getState())) {
                            Log.d(TAG, "Terminating the call");
                            lc.terminateCall(call);
                        }
                    }
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                    Log.e(TAG, "LinphoneCoreException", e);
                }
            }
        }).start();
    }

    @Override
    public void endCall() {
        mIsCalling = false;
    }

    @Override
    public void listenIncomingCall() {

    }

    @Override
    public void unlistenIncomingCall() {

    }

    public static void showIncomingCallNotification(Context context, String caller) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.phone_icon)
                .setContentTitle("Incoming SIP call")
                .setContentText(caller + " is calling you");

        Intent answerIntent = CallReceiverActivity.newLinephoneIntnet(context, true);
        Intent declineIntent = CallReceiverActivity.newLinephoneIntnet(context, false);
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

    private static PendingIntent createPendingIntent(Context context, int requestCode, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void answerCall() {

        LinphoneCall call = null;
        List address = LinphoneUtils.getLinphoneCalls(mLinphoneCore);
        Log.d(TAG, "Number of call: " + address.size());
        Iterator contact = address.iterator();

        while (contact.hasNext()) {
            call = (LinphoneCall) contact.next();
            if (LinphoneCall.State.IncomingReceived == call.getState()) {
                break;
            }
        }

        if (call == null) {
            Log.e(TAG, "Couldn\'t find incoming call");
        } else {

            LinphoneCallParams params = mLinphoneCore.createDefaultCallParameters();
            params.enableLowBandwidth(false);
            LinphoneAddress address1 = call.getRemoteAddress();
            Log.d(TAG, "Find a incoming call, number: " + address1.asStringUriOnly());
            try {
                mLinphoneCore.acceptCallWithParams(call, params);
            } catch (LinphoneCoreException e) {
                Log.e(TAG, "onReceiveButtonClick: ", e);
                e.printStackTrace();
            }
        }

    }
}
