package com.wiadvance.sipdemo.linphone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sipdemo.BuildConfig;
import com.wiadvance.sipdemo.FileUtils;
import com.wiadvance.sipdemo.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreFactory;
import org.linphone.core.Reason;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneCoreHelper {

    private static final String TAG = "LinphoneCoreHelper";
    private static LinphoneCore mLinphoneCore = null;
    private static Timer sTimer;
    private static int count = 0;
    private static String sInitTime;
    private static String sSipNumber = "N/A";

    private LinphoneCoreHelper() {
    }

    public synchronized static LinphoneCore getLinphoneCoreInstance(Context context) throws LinphoneCoreException {
        if (mLinphoneCore == null) {

            sInitTime = new Date().toString();
            LinphoneCoreFactory.instance().setDebugMode(true, "WiAdvance");

            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(
                    new WiLinPhoneCoreListener(context, "createLinphoneCore"), context
            );
            String basePath = copyResourceFile(context);

            mLinphoneCore.setRing(basePath + "/oldphone_mono.wav");
            mLinphoneCore.setRingback(basePath + "/ringback.wav");
            mLinphoneCore.setCallErrorTone(Reason.Busy, basePath + "/laser.wav");

            mLinphoneCore.setMaxCalls(5);

            mLinphoneCore.setNetworkReachable(true);
            setUserAgent(context);
            startIterate(context);

        }
        return mLinphoneCore;
    }

    public static synchronized void destroyLinphoneCore(Context context){
        try {
            sTimer.cancel();
            mLinphoneCore.destroy();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        finally {
            mLinphoneCore = null;
        }

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
            props.put("INIT_TIME", LinphoneCoreHelper.getInitTime().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("LOGOUT", props);

    }

    private static void startIterate(final Context context) {
        TimerTask lTask = new TimerTask() {
            @Override
            public void run() {
                if(mLinphoneCore != null){
                    mLinphoneCore.iterate();

                    if(count == 0){
                        MixpanelAPI mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANL_TOKEN);

                        JSONObject props = new JSONObject();
                        try {
                            props.put("SIP_NUMBER", sSipNumber);
                            props.put("INIT_TIME", sInitTime.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mixpanel.track("STANDBY", props);
                    }

                    count += 1;
                    if(count >= 6000){
                        count = 0;
                    }
                }
            }
        };

        sTimer = new Timer("My scheduler");
        sTimer.schedule(lTask, 0, 20);
    }

    private static void setUserAgent(Context context) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("Myphone", versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }
    }

    @NonNull
    private static String copyResourceFile(Context context) {
        // Init LinPhoneCore settings
        String basePath = context.getFilesDir().getAbsolutePath();
        try {
            FileUtils.copyIfNotExist(context, R.raw.toy_mono, basePath + "/toy_mono.wav");
            FileUtils.copyIfNotExist(context, R.raw.ringback, basePath + "/ringback.wav");
            FileUtils.copyIfNotExist(context, R.raw.oldphone_mono, basePath + "/oldphone_mono.wav");
            FileUtils.copyIfNotExist(context, R.raw.laser, basePath + "/laser.wav");
            FileUtils.copyIfNotExist(context, R.raw.linphonerc_default, basePath + "/.linphonerc");
            FileUtils.copyFromPackage(context, R.raw.linphonerc_factory, new File(basePath + "/linphonerc").getName());

        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            e.printStackTrace();
        }
        return basePath;
    }

    public static void setSipNumber(String sipNumber) {
        sSipNumber = sipNumber;
    }

    public static String getInitTime() {
        return sInitTime;
    }

    public static String getSipNumber() {
        return sSipNumber;
    }
}
