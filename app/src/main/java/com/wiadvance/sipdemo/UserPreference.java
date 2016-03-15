package com.wiadvance.sipdemo;

import android.content.Context;
import android.preference.PreferenceManager;

public class UserPreference {
    private static final String PREF_NAME = "name";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_SIP = "sip";


    public static String getName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME, null);
    }

    public static String getEmail(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_EMAIL, null);
    }

    public static String getSip(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SIP, null);
    }

    public static void setName(Context context, String name){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_NAME, name).apply();
    }

    public static void setEmail(Context context, String email){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_EMAIL, email).apply();
    }

    public static void setSip(Context context, String sip){
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SIP, sip).apply();
    }
}
