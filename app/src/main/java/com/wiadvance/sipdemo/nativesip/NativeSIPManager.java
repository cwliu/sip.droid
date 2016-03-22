package com.wiadvance.sipdemo.nativesip;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.wiadvance.sipdemo.BuildConfig;
import com.wiadvance.sipdemo.CallReceiverActivity;
import com.wiadvance.sipdemo.NotificationUtil;
import com.wiadvance.sipdemo.R;
import com.wiadvance.sipdemo.Utils;
import com.wiadvance.sipdemo.WiSipManager;

import java.text.ParseException;
import java.util.Date;

public class NativeSipManager extends WiSipManager {

    private static final String TAG = "NativeSipManager";

    public static String ACTION_INCOMING_CALL = "com.wiadvance.sipdemo.incoming_call";

    private final SipManager mSipManager;
    private Context mContext;
    private SipProfile mCallerProfile;
    private boolean mConnected = false;
    private SipAudioCall mCall;
    private IncomingCallReceiver callReceiver;

    private static final int ANSWER_REQUEST_CODE = 1;
    private static final int DECLINE_REQUEST_CODE = 2;

    public NativeSipManager(Context context) {
        mSipManager = SipManager.newInstance(context);
        mContext = context;
    }

    @Override
    public void register(String account) {
        if (!isSupported()) {
            return;
        }

        String username = account;

        SipProfile.Builder sipBuilder;
        try {
            sipBuilder = new SipProfile.Builder(username, BuildConfig.SIP_DOMAIN);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: ", e);
            return;
        }

        sipBuilder.setPassword(BuildConfig.SIP_PASSWORD);
        mCallerProfile = sipBuilder.build();
        Log.d(TAG, "Caller uri: " + mCallerProfile.getUriString());

        Intent intent = new Intent();
        intent.setAction(ACTION_INCOMING_CALL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, Intent.FILL_IN_DATA);

        try {
            mSipManager.open(mCallerProfile, pendingIntent, new SipRegistrationListener() {

                public void onRegistering(String localProfileUri) {
                    NotificationUtil.displayStatus(mContext, "Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    mConnected = true;
                    NotificationUtil.displayStatus(mContext, "Ready to make or receive a SIP call !");
                    Log.d(TAG, "onRegistrationDone: Expiry Time: " + new Date(expiryTime));
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    if (errorCode == -10) {
                        if (mConnected) {
                            NotificationUtil.displayStatus(mContext, "Disconnected from SIP Server");
                        }
                    } else {
                        NotificationUtil.displayStatus(mContext, "Registration failed.\n" +
                                "ErrorCode: " + errorCode + "\nErrorMessage: " + errorMessage);
                    }
                    mConnected = false;
                }
            });
        } catch (SipException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    @Override
    public boolean unregister(String account) {
        if (!isSupported()) {
            return false;
        }

        try {
            if (mCallerProfile != null) {
                mSipManager.close(mCallerProfile.getUriString());
                NotificationUtil.displayStatus(mContext, "Unregister device from SIP Server");
            }
        } catch (Exception ee) {
            Log.d(TAG, "Failed to close local profile.", ee);
        }
        return false;
    }

    public boolean isSupported() {
        return SipManager.isVoipSupported(mContext);
    }

    public void makeCall(String account) {
        if (!isSupported()) {
            return;
        }

        try {

            if (mCallerProfile == null) {
                NotificationUtil.displayStatus(mContext, "Please register first!");
                return;
            }

            SipAudioCall.Listener audioListener = new SipAudioCall.Listener() {
                @Override
                public void onCalling(SipAudioCall call) {
                    setCall(call);
                    Log.d(TAG, "onCalling() called with: " + "call = [" + call + "]");
                    setCall(call);
                    NotificationUtil.displayStatus(mContext, "onCalling");
                    super.onCalling(call);
                }

                @Override
                public void onChanged(SipAudioCall call) {
                    Log.d(TAG, "onChanged() called with: " + "call = [" + call + "]");
                    super.onChanged(call);
                }

                @Override
                public void onRingingBack(SipAudioCall call) {
                    super.onRingingBack(call);
                    Log.d(TAG, "onRingingBack() called with: " + "call = [" + call + "]");
                    NotificationUtil.displayStatus(mContext, "onRingingBack");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    Log.d(TAG, "onCallEstablished() called with: " + "call = [" + call + "]");
                    NotificationUtil.displayStatus(mContext, "onCallEstablished");
                    call.startAudio();
                    call.setSpeakerMode(false);

                    Utils.setAudioVolume(mContext, (float) 0.75);

                }

                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    super.onError(call, errorCode, errorMessage);
                    Log.d(TAG, "onError() called with: " + "call = [" + call + "], errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");
                    NotificationUtil.displayStatus(mContext, "onError: errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");

                }

                @Override
                public void onCallBusy(SipAudioCall call) {
                    super.onCallBusy(call);
                    Log.d(TAG, "onCallBusy() called with: " + "call = [" + call + "]");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    Log.d(TAG, "onCallEnded() called with: " + "call = [" + call + "]");
                    super.onCallEnded(call);
                }

                @Override
                public void onCallHeld(SipAudioCall call) {
                    Log.d(TAG, "onCallHeld() called with: " + "call = [" + call + "]");
                    super.onCallHeld(call);
                }

                @Override
                public void onReadyToCall(SipAudioCall call) {
                    Log.d(TAG, "onReadyToCall() called with: " + "call = [" + call + "]");
                    super.onReadyToCall(call);
                }

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    Log.d(TAG, "onRinging() called with: " + "call = [" + call + "], caller = [" + caller + "]");
                    super.onRinging(call, caller);
                }

            };

            String peerProfileUri = "sip:" + account + "@210.202.37.33";
            mSipManager.makeAudioCall(mCallerProfile.getUriString(), peerProfileUri, audioListener, 30);

        } catch (SipException e) {
            Log.e(TAG, "onCreate: ", e);
            NotificationUtil.displayStatus(mContext, "Error: " + e.toString());
        }
    }

    @Override
    public void endCall() {
        if (mCall != null) {
            try {
                mCall.endCall();
            } catch (SipException e) {
                Log.e(TAG, "SipException", e);
            }
        }
    }

    @Override
    public void listenIncomingCall() {
        // Set up the intent filter.  This will be used to fire an
        // IncomingCallReceiver when someone calls the SIP address used by this
        // application.
        IntentFilter receiver_filter = new IntentFilter();
        receiver_filter.addAction(ACTION_INCOMING_CALL);
        callReceiver = new IncomingCallReceiver();
        mContext.registerReceiver(callReceiver, receiver_filter);

    }

    @Override
    public void unlistenIncomingCall() {
        if (callReceiver != null) {
            mContext.unregisterReceiver(callReceiver);
        }
    }

    public SipAudioCall getCall() {
        return mCall;
    }

    public void setCall(SipAudioCall call) {
        mCall = call;
    }

    public static void showIncomingCallNotification(Context context, Intent sipIntent) {

        String caller = getCallerNameFromSipIntent(context, sipIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.phone_icon)
                .setContentTitle("Incoming SIP call")
                .setContentText(caller + " is calling you");

        Intent answerIntent = CallReceiverActivity.newNativeSipIntent(context, sipIntent, true);
        Intent declineIntent = CallReceiverActivity.newNativeSipIntent(context, sipIntent, false);
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

    private static String getCallerNameFromSipIntent(Context context, Intent sipIntent) {
        SipManager sipManager = SipManager.newInstance(context);
        String caller = "N/A";
        try {
            SipAudioCall call = sipManager.takeAudioCall(sipIntent, new SipAudioCall.Listener() {
            });
            caller = call.getPeerProfile().getUserName();
        } catch (SipException e) {
            e.printStackTrace();
        }
        return caller;
    }

    private static PendingIntent createPendingIntent(Context context, int requestCode, Intent nextIntent) {
        nextIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(CallReceiverActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        return stackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}