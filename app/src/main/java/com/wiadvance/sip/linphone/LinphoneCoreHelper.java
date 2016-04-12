package com.wiadvance.sip.linphone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sip.BuildConfig;
import com.wiadvance.sip.FileUtils;
import com.wiadvance.sip.R;

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

            LinphoneCoreFactory.instance().setDebugMode(true, "WiCore");

            sInitTime = new Date().toString();

            mLinphoneCore = LinphoneCoreFactory.instance().createLinphoneCore(
                    new WiLinPhoneCoreListener(context, "createLinphoneCore"), context
            );

            initLinephoneResource(context);

            mLinphoneCore.setMaxCalls(3);
            mLinphoneCore.setNetworkReachable(true);
            mLinphoneCore.enableVideo(false, false);

            setUserAgent(context);
            startIterate(context);
        }
        return mLinphoneCore;
    }

    public static synchronized void destroyLinphoneCore(Context context) {
        try {
            sTimer.cancel();
            mLinphoneCore.destroy();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mLinphoneCore = null;
        }
    }

    private static void startIterate(final Context context) {
        TimerTask lTask = new TimerTask() {
            @Override
            public synchronized void run() {

                if (mLinphoneCore != null) {
                    mLinphoneCore.iterate();

                    if (count == 0) {
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
                    if (count >= 6000) {
                        count = 0;
                    }
                }
            }
        };

        sTimer = new Timer("My scheduler");
        sTimer.schedule(lTask, 0, 20);
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

    private static void setUserAgent(Context context) {
        try {
            String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            if (versionName == null) {
                versionName = String.valueOf(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode);
            }
            mLinphoneCore.setUserAgent("Wiadvance", versionName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    private static void initLinephoneResource(Context context) {
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

        mLinphoneCore.setRing(basePath + "/oldphone_mono.wav");
        mLinphoneCore.setRingback(basePath + "/ringback.wav");
        mLinphoneCore.setCallErrorTone(Reason.Busy, basePath + "/laser.wav");
    }
}
