package com.wiadvance.sip;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.common.collect.HashBiMap;
import com.wiadvance.sip.db.ContactDbHelper;

public class UserData {

    private static final String PREF_NAME = "name";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_SIP = "sip";
    private static final String PREF_REGISTRATION_OK = "registration_ok";

    public static HashBiMap<String, String> sEmailToSipBiMap = HashBiMap.create();
    public static HashBiMap<String, String> sEmailToPhoneBiMap = HashBiMap.create();

    public static String getName(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_NAME, null);
    }

    public static String getEmail(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_EMAIL, null);
    }

    public static String getSip(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_SIP, null);
    }

    public static boolean getRegistrationStatus(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_REGISTRATION_OK, false);
    }

    public static void setName(Context context, String name) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_NAME, name).apply();
    }

    public static void setEmail(Context context, String email) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_EMAIL, email).apply();
    }

    public static void setSip(Context context, String sip) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_SIP, sip).apply();
    }

    public static void setRegistrationStatus(Context context, boolean isOk) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(PREF_REGISTRATION_OK, isOk).apply();
    }

    public static void clean(Context context) {
        setName(context, null);
        setEmail(context, null);
        setSip(context, null);
        setRegistrationStatus(context, false);

        sEmailToSipBiMap.clear();
        sEmailToPhoneBiMap.clear();

        ContactDbHelper.getInstance(context).removeAllContacts();
    }
}
