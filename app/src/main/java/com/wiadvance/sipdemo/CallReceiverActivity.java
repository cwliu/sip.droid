package com.wiadvance.sipdemo;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.wiadvance.sipdemo.linphone.LinphoneCoreHelper;

import org.linphone.LinphoneUtils;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallParams;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.Reason;

import java.util.Iterator;
import java.util.List;

public class CallReceiverActivity extends AppCompatActivity {

    public static final String ARG_SIP_INTENT = "SIP_INTENT";
    private static final String ARG_ANSWER_CALL = "ANSWER_CALL";

    private static String TAG = "CallReceiverActivity";

    private LinphoneCore mLc;
    private LinphoneCall mLinephoneCall;
    private NotificationReceiver mNotificationReceiver;

    public static Intent newNativeSipIntent(Context context, Intent sipIntent, Boolean answerCall){
        Intent intent = new Intent(context, CallReceiverActivity.class);

        intent.putExtra(ARG_SIP_INTENT, sipIntent);
        intent.putExtra(ARG_ANSWER_CALL, answerCall);
        return intent;
    }

    public static Intent newLinephoneIntnet(Context context, Boolean answerCall){
        Intent intent = new Intent(context, CallReceiverActivity.class);
        intent.putExtra(ARG_ANSWER_CALL, answerCall);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_receiver);

        Log.d(TAG, "onCreate() called with: " + "savedInstanceState = [" + savedInstanceState + "]");

        try {
            mLc = LinphoneCoreHelper.getLinphoneCoreInstance(this);
        } catch (LinphoneCoreException e) {
            throw new RuntimeException("Can't get linphone core");
        }

        NotificationUtil.cancelNotification(this);

        answerCall();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_NOTIFICATION);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mNotificationReceiver, notify_filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mNotificationReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    public void answerCall() {

        List address = LinphoneUtils.getLinphoneCalls(mLc);
        Log.d(TAG, "Number of call: " + address.size());
        Iterator contact = address.iterator();


        while (contact.hasNext()) {
            mLinephoneCall = (LinphoneCall) contact.next();
            if (LinphoneCall.State.IncomingReceived == mLinephoneCall.getState()) {
                break;
            }
        }

        if (mLinephoneCall == null) {
            Log.e(TAG, "Couldn\'t find incoming call");
        } else {
            Boolean answerCall = getIntent().getBooleanExtra(ARG_ANSWER_CALL, true);
            LinphoneCallParams params = mLc.createDefaultCallParameters();
            params.enableLowBandwidth(false);

            if(answerCall){
                LinphoneAddress address1 = mLinephoneCall.getRemoteAddress();
                Log.d(TAG, "Find a incoming call, number: " + address1.asStringUriOnly());
                try {
                    mLc.acceptCallWithParams(mLinephoneCall, params);
                    NotificationUtil.displayStatus(this, "Call is established");
                } catch (LinphoneCoreException e) {
                    Log.e(TAG, "Accept Call exception: ", e);
                    NotificationUtil.displayStatus(this, "Failed to accept the call");
                }
            }else{
                mLc.declineCall(mLinephoneCall, Reason.Declined);
                finish();
            }
        }
    }

    public void onEndCallButtonClick(View view){
        if(mLinephoneCall != null){
            mLc.terminateCall(mLinephoneCall);
            finish();
        }
    }
}
