package com.wiadvance.sipdemo.linphone;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sipdemo.BuildConfig;
import com.wiadvance.sipdemo.CallReceiverActivity;
import com.wiadvance.sipdemo.NotificationUtil;
import com.wiadvance.sipdemo.WiSipManager;
import com.wiadvance.sipdemo.model.Contact;

import org.json.JSONException;
import org.json.JSONObject;
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
    private static final long MAX_WAIT_TIME_IN_SECONDS = 10 * 1000;

    private final Context mContext;
    private boolean mIsCalling;

    public LinphoneCore mLinphoneCore;
    private LinphoneProxyConfig mProxyConfig;

    private static final int ANSWER_REQUEST_CODE = 1;
    private static final int DECLINE_REQUEST_CODE = 2;
    private boolean mCancelAllCall;

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
        LinphoneCoreHelper.setSipNumber(identity);

        try {

            mProxyConfig = mLinphoneCore.createProxyConfig(identity, domain, null, true);
            mProxyConfig.setExpires(300);

            mLinphoneCore.addProxyConfig(mProxyConfig);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(
                    account, password, null, domain);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(mProxyConfig);

            setOnlineStatus();

            MixpanelAPI mixpanel = MixpanelAPI.getInstance(mContext, BuildConfig.MIXPANL_TOKEN);
            JSONObject props = new JSONObject();
            try {
                props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
                props.put("INIT_TIME", LinphoneCoreHelper.getInitTime().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mixpanel.track("REGISTER", props);

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

            for (LinphoneProxyConfig config : mLinphoneCore.getProxyConfigList()) {
                mLinphoneCore.removeProxyConfig(config);
            }
        }
        return true;
    }

    @Override
    public void makeCall(final Contact contact) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {

                    Boolean isConnected = false;
                    String sipNumber = contact.getSip();
                    if (sipNumber == null) {
                        NotificationUtil.displayStatus(mContext, "This user has no sip number");
                    } else {
                        NotificationUtil.notifyCallStatus(mContext, true, "SIP Dialing...", true);
                        isConnected = call(sipNumber, true);
                    }

                    if (mCancelAllCall) {
                        NotificationUtil.notifyCallStatus(mContext, false, null, false);
                        return;
                    }

                    String phone = contact.getPhone();
                    if (phone == null) {
                        NotificationUtil.displayStatus(mContext, "This user has no phone number");
                    } else if (!isConnected) {
                        NotificationUtil.notifyCallStatus(mContext, true, "Dialing to: " + phone, false);
                        call(phone, false);
                    }

                    NotificationUtil.notifyCallStatus(mContext, false, null, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                    Log.e(TAG, "LinphoneCoreException", e);
                }
            }

            private boolean call(String account, Boolean hasMaxWaitTime) throws LinphoneCoreException {
                if (account == null) {
                    return false;
                }

                LinphoneCore lc = LinphoneCoreHelper.getLinphoneCoreInstance(mContext);
                LinphoneCall call = lc.invite(account);

                boolean isConnected = false;

                if (call == null) {
                    Log.d(TAG, "Could not place call to");
                } else {
                    Log.d(TAG, "Call to: " + account);
                    mIsCalling = true;

                    long callingTime = 0;
                    long iterateInterval = 50L;
                    boolean hasRinging = false;
                    while (mIsCalling) {
                        lc.iterate();

                        try {
                            Thread.sleep(iterateInterval);

                            if (call.getState().toString().equals("CallEnd") || call.getState().toString().equals("Released")) {
                                mIsCalling = false;
                                Log.d(TAG, "Call end");
                            }

                            if (call.getState().toString().equals("Connected") || call.getState().toString().equals("Connected")) {
                                isConnected = true;
                            }

                            if (call.getState().toString().equals("OutgoingRinging")) {
                                hasRinging = true;
                            }


                        } catch (InterruptedException var8) {
                            Log.d(TAG, "Interrupted! Aborting");
                        }


                        if (hasRinging) {
                            callingTime += iterateInterval;
                        }
                        Log.d(TAG, "call: callingTime: " + callingTime);
                        if (hasMaxWaitTime && callingTime >= MAX_WAIT_TIME_IN_SECONDS) {
                            mIsCalling = false;
                        }
                    }
                    if (!LinphoneCall.State.CallEnd.equals(call.getState())) {
                        Log.d(TAG, "Terminating the call");
                        lc.terminateCall(call);
                    }
                }
                return isConnected;
            }
        }).start();
    }

    @Override
    public void endCall() {
        mIsCalling = false;
    }

    public void endAllCall() {
        endCall();
        mCancelAllCall = true;
    }

    @Override
    public void listenIncomingCall() {

    }

    @Override
    public void unlistenIncomingCall() {

    }

    private static PendingIntent createPendingIntent(Context context, int requestCode, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void addFriend(String sipUri) {
        LinphoneFriend friend = LinphoneCoreFactory.instance().createLinphoneFriend(sipUri);
        friend.enableSubscribes(true); /*configure this friend to emit SUBSCRIBE message after being added to LinphoneCore*/
        friend.setIncSubscribePolicy(LinphoneFriend.SubscribePolicy.SPAccept); /* accept Incoming subscription request for this friend*/

        try {
            LinphoneFriend previous_friend = mLinphoneCore.findFriendByAddress(sipUri);
            if (previous_friend != null) {
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
        for (LinphoneFriend friend : mLinphoneCore.getFriendList()) {
            PresenceModel presenceModel = friend.getPresenceModel();
            if (presenceModel != null) {
                PresenceActivityType presenceActivity = presenceModel.getActivity().getType();
                Log.d(TAG, "Friend: " + friend.getAddress() + ", status: " + presenceActivity.toString() + ", time: " + new Date(presenceModel.getTimestamp()));
            } else {
                Log.d(TAG, "Friend: " + friend.getAddress() + ", status not available");

            }
        }

    }
}
