package com.wiadvance.sipdemo.linphone;

import android.content.Context;
import android.util.Log;

import com.wiadvance.sipdemo.BuildConfig;
import com.wiadvance.sipdemo.WiSipManager;

import org.linphone.core.LinphoneAuthInfo;
import org.linphone.core.LinphoneCall;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.LinphoneProxyConfig;

public class LinphoneSipManager extends WiSipManager {

    private static final String TAG = "LinephoneSIPManager";

    private final Context mContext;
    private boolean mIsCalling;
    private LinphoneCall call = null;


    public LinphoneCore mLinphoneCore;

    public LinphoneSipManager(Context context) {

        mContext = context;
        try {
            mLinphoneCore = LinphoneCoreHelper.getInstance(context);
        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public void register(String account) {

        String identity = "sip:" + account + "@" + BuildConfig.SIP_DOMAIN;
        LinphoneProxyConfig proxyConfig;
        try {
            proxyConfig = mLinphoneCore.createProxyConfig(identity, BuildConfig.SIP_DOMAIN, null, true);
            mLinphoneCore.addProxyConfig(proxyConfig);

            LinphoneAuthInfo authInfo = LinphoneCoreFactory.instance().createAuthInfo(
                    account, BuildConfig.SIP_PASSWORD, null, BuildConfig.SIP_DOMAIN);
            mLinphoneCore.addAuthInfo(authInfo);
            mLinphoneCore.setDefaultProxyConfig(proxyConfig);
            mLinphoneCore.addListener(new WiLinPhoneCoreListener("OnRegister"));

        } catch (LinphoneCoreException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    @Override
    public boolean unregister(String account) {
        return false;
    }


    @Override
    public void makeCall(final String account) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    LinphoneCore lc = LinphoneCoreHelper.getInstance(mContext);
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
}
