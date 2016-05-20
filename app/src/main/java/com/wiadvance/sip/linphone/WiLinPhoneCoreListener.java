package com.wiadvance.sip.linphone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sip.BuildConfig;
import com.wiadvance.sip.CallReceiverActivity;
import com.wiadvance.sip.NotificationUtil;
import com.wiadvance.sip.R;
import com.wiadvance.sip.UserData;
import com.wiadvance.sip.model.CallLogEntry;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneAddress;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCallStats;
import org.linphone.core.LinphoneChatMessage;
import org.linphone.core.LinphoneChatRoom;
import org.linphone.core.LinphoneContent;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreListener;
import org.linphone.core.LinphoneEvent;
import org.linphone.core.LinphoneFriend;
import org.linphone.core.LinphoneInfoMessage;
import org.linphone.core.LinphoneProxyConfig;
import org.linphone.core.PublishState;
import org.linphone.core.SubscriptionState;

import java.nio.ByteBuffer;

public class WiLinPhoneCoreListener implements LinphoneCoreListener {
    private String TAG = "LinPhoneCoreListener";
    private Context mContext;

    public WiLinPhoneCoreListener(Context context, String name) {
        TAG = TAG + "-" + name;
        mContext  = context;
    }

    @Override
    public void displayStatus(LinphoneCore core, String s) {
        Log.d(TAG, "displayStatus() called with: " + "core = [" + core + "], s = [" + s + "]");


        MixpanelAPI mixpanel = MixpanelAPI.getInstance(mContext, BuildConfig.MIXPANL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
            props.put("INIT_TIME", LinphoneCoreHelper.getInitTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(s.contains("Registration") && s.contains("successful")){
            mixpanel.track("REGISTER-OK", props);
        }

        if(s.contains("Registration") && !s.contains("successful")){
            mixpanel.track("REGISTER-FAIL", props);
        }

        if(s.contains("Call answered by")){
            UserData.sCurrentLogEntry.setCallType(CallLogEntry.TYPE_OUTGOING_CALL_ANSWERED);
        }

        if(s.contains("You have missed")){
            UserData.setUncheckedMissCall(mContext, true);
        }
    }
    
    @Override
    public void callState(LinphoneCore core, LinphoneCall call, LinphoneCall.State state, String s) {
        Log.d(TAG, "callState() called with: " + "core = [" + core + "], call = [" + call + "], state = [" + state + "], s = [" + s + "]");

        if(state.equals(LinphoneCall.State.IncomingReceived)) {

            UserData.sCurrentLogEntry = new CallLogEntry();
            UserData.sCurrentLogEntry.setCallType(CallLogEntry.TYPE_INCOMING_CALL_NO_ANSWER);

            Intent intent = CallReceiverActivity.newIntent(mContext, call.getRemoteAddress().toString());

            mContext.startActivity(intent);
        }

        if(state.equals(LinphoneCall.State.OutgoingInit)){
            UserData.sCurrentLogEntry = new CallLogEntry();
            UserData.sCurrentLogEntry.setCallType(CallLogEntry.TYPE_OUTGOING_CALL_NO_ANSWER);

            NotificationUtil.notifyCallStatus(mContext, true, null, true);
        }

        if(state.equals(LinphoneCall.State.Connected)){
            if(UserData.sCurrentLogEntry.getCallType() == CallLogEntry.TYPE_INCOMING_CALL_NO_ANSWER){
                UserData.sCurrentLogEntry.setCallType(CallLogEntry.TYPE_INCOMING_CALL_ANSWERED);

            }else{
                UserData.sCurrentLogEntry.setCallType(CallLogEntry.TYPE_OUTGOING_CALL_ANSWERED);
            }

            String msg = mContext.getResources().getString(R.string.call_established_msg);
            NotificationUtil.notifyCallMsg(mContext, msg);
        }

        if(state.equals(LinphoneCall.State.CallEnd)){
            NotificationUtil.notifyCallStatus(mContext, false, null, false);
        }

        if(state.equals(LinphoneCall.State.Error)){
            NotificationUtil.displayStatus(mContext, state.toString() + ": " + s);
        }
    }

    @Override
    public void registrationState(LinphoneCore core, LinphoneProxyConfig config, LinphoneCore.RegistrationState state, String s) {
        Log.d(TAG, "registrationState() called with: " + "core = [" + core + "], config = [" + config + "], state = [" + state + "], s = [" + s + "]");


        if(state.equals(LinphoneCore.RegistrationState.RegistrationOk)){
            UserData.setRegistrationStatus(mContext, true);
        }

        if(state.equals(LinphoneCore.RegistrationState.RegistrationFailed)){
            UserData.setRegistrationStatus(mContext, false);
        }

    }
    @Override
    public void authInfoRequested(LinphoneCore core, String s, String s1, String s2) {
        Log.d(TAG, "authInfoRequested() called with: " + "core = [" + core + "], s = [" + s + "], s1 = [" + s1 + "], s2 = [" + s2 + "]");

    }

    @Override
    public void callStatsUpdated(LinphoneCore core, LinphoneCall call, LinphoneCallStats stats) {
        Log.d(TAG, "callStatsUpdated() called with: " + "core = [" + core + "], call = [" + call + "], stats = [" + stats + "]");

    }

    @Override
    public void newSubscriptionRequest(LinphoneCore core, LinphoneFriend friend, String s) {
        Log.d(TAG, "newSubscriptionRequest() called with: " + "core = [" + core + "], friend = [" + friend + "], s = [" + s + "]");

    }

    @Override
    public void notifyPresenceReceived(LinphoneCore core, LinphoneFriend friend) {
        Log.d(TAG, "notifyPresenceReceived() called with: " + "core = [" + core + "], friend = [" + friend + "]");
        Log.d(TAG, "notifyPresenceReceived() Friend: " + friend.getAddress() + ", status: " + friend.getPresenceModel().getActivity().getType());
    }

    @Override
    public void dtmfReceived(LinphoneCore core, LinphoneCall call, int i) {
        Log.d(TAG, "dtmfReceived() called with: " + "core = [" + core + "], call = [" + call + "], i = [" + i + "]");

    }

    @Override
    public void notifyReceived(LinphoneCore core, LinphoneCall call, LinphoneAddress address, byte[] bytes) {
        Log.d(TAG, "notifyReceived() called with: " + "core = [" + core + "], call = [" + call + "], address = [" + address + "], bytes = [" + bytes + "]");

    }

    @Override
    public void transferState(LinphoneCore core, LinphoneCall call, LinphoneCall.State state) {
        Log.d(TAG, "transferState() called with: " + "core = [" + core + "], call = [" + call + "], state = [" + state + "]");

    }

    @Override
    public void infoReceived(LinphoneCore core, LinphoneCall call, LinphoneInfoMessage message) {
        Log.d(TAG, "infoReceived() called with: " + "core = [" + core + "], call = [" + call + "], message = [" + message + "]");
    }

    @Override
    public void subscriptionStateChanged(LinphoneCore core, LinphoneEvent event, SubscriptionState state) {
        Log.d(TAG, "subscriptionStateChanged() called with: " + "core = [" + core + "], event = [" + event + "], state = [" + state + "]");
    }

    @Override
    public void publishStateChanged(LinphoneCore core, LinphoneEvent event, PublishState state) {
        Log.d(TAG, "publishStateChanged() called with: " + "core = [" + core + "], event = [" + event + "], state = [" + state + "]");
    }

    @Override
    public void show(LinphoneCore core) {
        Log.d(TAG, "show() called with: " + "core = [" + core + "]");
    }

    @Override
    public void displayMessage(LinphoneCore core, String s) {
        Log.d(TAG, "displayMessage() called with: " + "core = [" + core + "], s = [" + s + "]");
    }

    @Override
    public void displayWarning(LinphoneCore core, String s) {
        Log.d(TAG, "displayWarning() called with: " + "core = [" + core + "], s = [" + s + "]");
    }

    @Override
    public void fileTransferProgressIndication(LinphoneCore core, LinphoneChatMessage message, LinphoneContent content, int i) {
        Log.d(TAG, "fileTransferProgressIndication() called with: " + "core = [" + core + "], message = [" + message + "], content = [" + content + "], i = [" + i + "]");
    }

    @Override
    public void fileTransferRecv(LinphoneCore core, LinphoneChatMessage message, LinphoneContent content, byte[] bytes, int i) {
        Log.d(TAG, "fileTransferRecv() called with: " + "core = [" + core + "], message = [" + message + "], content = [" + content + "], bytes = [" + bytes + "], i = [" + i + "]");
    }

    @Override
    public int fileTransferSend(LinphoneCore core, LinphoneChatMessage message, LinphoneContent content, ByteBuffer buffer, int i) {
        Log.d(TAG, "fileTransferSend() called with: " + "core = [" + core + "], message = [" + message + "], content = [" + content + "], buffer = [" + buffer + "], i = [" + i + "]");
        return 0;
    }

    @Override
    public void globalState(LinphoneCore core, LinphoneCore.GlobalState state, String s) {
        Log.d(TAG, "globalState() called with: " + "core = [" + core + "], state = [" + state + "], s = [" + s + "]");
    }

    @Override
    public void configuringStatus(LinphoneCore core, LinphoneCore.RemoteProvisioningState state, String s) {
        Log.d(TAG, "configuringStatus() called with: " + "core = [" + core + "], state = [" + state + "], s = [" + s + "]");
    }

    @Override
    public void messageReceived(LinphoneCore core, LinphoneChatRoom room, LinphoneChatMessage message) {
        Log.d(TAG, "messageReceived() called with: " + "core = [" + core + "], room = [" + room + "], message = [" + message + "]");
    }

    @Override
    public void callEncryptionChanged(LinphoneCore core, LinphoneCall call, boolean b, String s) {
        Log.d(TAG, "callEncryptionChanged() called with: " + "core = [" + core + "], call = [" + call + "], b = [" + b + "], s = [" + s + "]");
    }

    @Override
    public void notifyReceived(LinphoneCore core, LinphoneEvent event, String s, LinphoneContent content) {
        Log.d(TAG, "notifyReceived() called with: " + "core = [" + core + "], event = [" + event + "], s = [" + s + "], content = [" + content + "]");
    }

    @Override
    public void isComposingReceived(LinphoneCore core, LinphoneChatRoom room) {
        Log.d(TAG, "isComposingReceived() called with: " + "core = [" + core + "], room = [" + room + "]");
    }

    @Override
    public void ecCalibrationStatus(LinphoneCore core, LinphoneCore.EcCalibratorStatus status, int i, Object o) {
        Log.d(TAG, "ecCalibrationStatus() called with: " + "core = [" + core + "], status = [" + status + "], i = [" + i + "], o = [" + o + "]");
    }

    @Override
    public void uploadProgressIndication(LinphoneCore core, int i, int i1) {
        Log.d(TAG, "uploadProgressIndication() called with: " + "core = [" + core + "], i = [" + i + "], i1 = [" + i1 + "]");
    }

    @Override
    public void uploadStateChanged(LinphoneCore core, LinphoneCore.LogCollectionUploadState state, String s) {
        Log.d(TAG, "uploadStateChanged() called with: " + "core = [" + core + "], state = [" + state + "], s = [" + s + "]");
    }
}
