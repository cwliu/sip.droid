package com.wiadvance.sip.linphone;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sip.BuildConfig;
import com.wiadvance.sip.CallReceiverActivity;
import com.wiadvance.sip.NotificationUtil;
import com.wiadvance.sip.UserData;
import com.wiadvance.sip.WiSipManager;
import com.wiadvance.sip.model.Contact;

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
import java.util.List;

public class LinphoneSipManager extends WiSipManager {

    private static final String TAG = "LinephoneSIPManager";
    private static final long MAX_WAIT_TIME_IN_SECONDS = 10 * 1000;

    private final Context mContext;
    private boolean mIsCalling;

    public LinphoneCore mLinphoneCore;
    private LinphoneProxyConfig mProxyConfig;

    private boolean mCancelAllCall;
    private boolean mTriedAllCall;
    private boolean mHasConnected;

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
                    String sipNumber = contact.getSip();
                    if (sipNumber != null) {
                        NotificationUtil.notifyCallStatus(mContext, true, "SIP Dialing", true);
                        mHasConnected = call(sipNumber, true);
                    }

                    if (mCancelAllCall) {
                        UserData.recordCallLog(mContext);
                        NotificationUtil.notifyCallStatus(mContext, false, null, false);
                        return;
                    }

                    List<String> phoneList = contact.getPhoneList();

                    String phone;
                    if (contact.getPreferredPhone() != null) {
                        phone = contact.getPreferredPhone();
                    } else {
                        phone = phoneList.size() > 0 ? phoneList.get(0) : null;
                    }

                    if (phone == null) {
                        NotificationUtil.displayStatus(mContext, "This user has no phone number");
                    } else if (!mHasConnected) {
                        NotificationUtil.notifyCallStatus(mContext, true, "Dialing to: " + phone, false);
                        call(phone, false);
                    }

                    mTriedAllCall = true;
                    NotificationUtil.notifyCallStatus(mContext, false, null, false);
                } catch (LinphoneCoreException e) {
                    e.printStackTrace();
                    Log.e(TAG, "LinphoneCoreException", e);
                }

                UserData.recordCallLog(mContext);
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

                            if (call.getState().equals(LinphoneCall.State.CallEnd)
                                    || call.getState().equals(LinphoneCall.State.CallReleased)) {
                                mIsCalling = false;
                            }

                            if (call.getState().equals(LinphoneCall.State.StreamsRunning)) {
                                isConnected = true;
                            }

                            if (call.getState().equals(LinphoneCall.State.OutgoingRinging)) {
                                hasRinging = true;
                            }


                        } catch (InterruptedException var8) {
                            Log.d(TAG, "Interrupted! Aborting");
                        }

                        if (hasRinging) {
                            callingTime += iterateInterval;
                        }

                        if (!isConnected && hasMaxWaitTime && callingTime >= MAX_WAIT_TIME_IN_SECONDS) {
                            Log.d(TAG, "CallingTime >= MAX_WAIT_TIME_IN_SECONDS, " + callingTime);
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

    public void endCall() {
        mIsCalling = false;
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

    public boolean triedSip() {
        return mTriedAllCall;
    }

    public boolean isHasConnected() {
        return mHasConnected;
    }

    public void setHasConnected(boolean hasConnected) {
        mHasConnected = hasConnected;
    }
}
