package com.wiadvance.sipdemo.linphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.wiadvance.sipdemo.CallReceiverActivity;
import com.wiadvance.sipdemo.NotificationUtil;
import com.wiadvance.sipdemo.R;
import com.wiadvance.sipdemo.WiSipManager;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PresenceActivityType;
import org.linphone.core.PresenceModel;

import java.util.Date;

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
    public void register(String account, String password, String domain) {

        String identity = "sip:" + account + "@" + domain;
        try {

            mProxyConfig = mLinphoneCore.createProxyConfig(identity, domain, null, true);
            mProxyConfig.setExpires(60);

            mLinphoneCore.addProxyConfig(mProxyConfig);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(
                    account, password, null, domain);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(mProxyConfig);

            setOnlineStatus();

        } catch (LinphoneCoreException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    public void setOnlineStatus() {
        PresenceModel model = LinphoneCoreFactory.instance().createPresenceModel(PresenceActivityType.Online, null);
        mLinphoneCore.setPresenceModel(model);
        Log.d(TAG, "SetOnlineStatus");
    }

    @Override
    public boolean unregister(String account) {
        if (mProxyConfig != null) {
            mLinphoneCore.removeProxyConfig(mProxyConfig);

            for(LinphoneProxyConfig config :mLinphoneCore.getProxyConfigList()) {
                mLinphoneCore.removeProxyConfig(config);
            }
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
        mNotificationManager.notify(NotificationUtil.INCOMING_CALL_NOTIFICATION_ID, notif);
    }

    private static PendingIntent createPendingIntent(Context context, int requestCode, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void addFriend(String sipUri){
        LinphoneFriend friend = LinphoneCoreFactory.instance().createLinphoneFriend(sipUri);
        friend.enableSubscribes(true); /*configure this friend to emit SUBSCRIBE message after being added to LinphoneCore*/
        friend.setIncSubscribePolicy(LinphoneFriend.SubscribePolicy.SPAccept); /* accept Incoming subscription request for this friend*/

        try {
            LinphoneFriend previous_friend = mLinphoneCore.findFriendByAddress(sipUri);
            if(previous_friend != null){
                mLinphoneCore.removeFriend(previous_friend);
            }
            mLinphoneCore.addFriend(friend);

        } catch (LinphoneCoreException e) {
            e.printStackTrace();
            Log.e(TAG, "addFriend exception ", e);
        }

    }

    public void displayFriendStatus() {
        Log.d(TAG, "Number of friend: " + mLinphoneCore.getFriendList().length);
        for(LinphoneFriend friend: mLinphoneCore.getFriendList()){
            PresenceModel presenceModel = friend.getPresenceModel();
            if(presenceModel != null){
                PresenceActivityType presenceActivity = presenceModel.getActivity().getType();
                Log.d(TAG, "Friend: "+ friend.getAddress() +", status: " + presenceActivity.toString() + ", time: " + new Date(presenceModel.getTimestamp()));
            }else{
                Log.d(TAG, "Friend: "+ friend.getAddress() +", status not available");

            }
        }

    }

//    private boolean isPresenceModelActivitySet() {
//        return mLinphoneCore.getPresenceModel() != null && mLinphoneCore.getPresenceModel().getActivity() != null;
//    }

}
