package com.wiadvance.sipdemo;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class CallReceiverActivity extends AppCompatActivity {

    public static final String ARG_SIP_INTENT = "SIP_INTENT";
    private static final String ARG_ANSWER_CALL = "ANSWER_CALL";
    public static final int INCOMING_CALL_NOTIFICATION_ID = 1;

    private static String TAG = "CallReceiverActivity";

    private SipAudioCall mIncomingCall;

    public static Intent newIntent(Context context, Intent sipIntent, Boolean answerCall){
        Intent intent = new Intent(context, CallReceiverActivity.class);
        intent.putExtra(ARG_SIP_INTENT, sipIntent);
        intent.putExtra(ARG_ANSWER_CALL, answerCall);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_receiver);

        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");


        cancelNotification();

        takeAudioCall();
    }

    public void onEndCallButtonClick(View view){
        if(mIncomingCall != null){
            try {
                mIncomingCall.endCall();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(INCOMING_CALL_NOTIFICATION_ID);
    }

    private void takeAudioCall() {
        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {

                @Override
                public void onCallEnded(SipAudioCall call) {
                    super.onCallEnded(call);
                    NotificationUtil.updateStatus(CallReceiverActivity.this, "Call ended");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    NotificationUtil.updateStatus(CallReceiverActivity.this, "Connected");

                    call.startAudio();
                    call.setSpeakerMode(true);
                    if (call.isMuted()) {
                        call.toggleMute();
                    }
                }
            };

            Intent sipIntent = getIntent().getParcelableExtra(ARG_SIP_INTENT);
            Boolean answerCall = getIntent().getBooleanExtra(ARG_ANSWER_CALL, true);

            // @TODO Check support
            SipManager sipManager = SipManager.newInstance(this);

            mIncomingCall = sipManager.takeAudioCall(sipIntent, listener);
            if(answerCall){
                Log.d(TAG, "takeAudioCall() answer call");
                mIncomingCall.answerCall(30);
            }else{
                Log.d(TAG, "takeAudioCall() decline call");
                mIncomingCall.endCall();
            }

        } catch (Exception e) {
            Log.e(TAG, "onReceive() exception", e);
            if (mIncomingCall != null) {
                mIncomingCall.close();
            }
        }
    }
}
