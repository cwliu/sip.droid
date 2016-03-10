package com.wiadvance.sipdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.util.Log;

/*** Listens for incoming SIP calls, intercepts and hands them off to SipActivity.
 */
public class IncomingCallReceiver extends BroadcastReceiver {

    private static final String TAG = "IncomingCallReceiver";
    private SipAudioCall mIncomingCall;

    /**
     * Processes the incoming call, answers it, and hands it over to the SipActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        mIncomingCall = null;
        Log.d(TAG, "onReceive() called with: " + "context = [" + context + "], intent = [" + intent + "]");
        Notification.updateStatus(context, "Incomming call");

        try {
            SipActivity activity = (SipActivity) context;
            SIPFragment fragment = activity.getSipFragment();
            SipManager manager = fragment.getSipManager();


            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    Notification.updateStatus(context, "Receive call onRinging()");
                    try {
                        Log.d(TAG, "onRinging() called with: " + "call = [" + call + "], caller = [" + caller + "]");
                        call.answerCall(30);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    super.onCallEnded(call);
                    Notification.updateStatus(context, "Receive call onCallEnded()");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    Notification.updateStatus(context, "Receive call onCallEstablished()");

                    call.startAudio();
                    call.setSpeakerMode(true);
                    if(call.isMuted()) {
                        call.toggleMute();
                    }
                }
            };

            mIncomingCall = manager.takeAudioCall(intent, listener);
            mIncomingCall.answerCall(30);

        } catch (Exception e) {
            Log.e(TAG, "onReceive() exception",e);
            if (mIncomingCall != null) {
                mIncomingCall.close();
            }
        }
    }
}
