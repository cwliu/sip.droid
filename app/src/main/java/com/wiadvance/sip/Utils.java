package com.wiadvance.sip;

import android.content.Context;
import android.media.AudioManager;

public class Utils {

    private static final String TAG = "Utils";

    public static void setAudioVolume(Context context, float percent) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, (int)(volume * percent), 0);
    }
}
