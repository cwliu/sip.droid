package com.wiadvance.sip;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;

import com.wiadvance.sip.db.AppDbSchema;
import com.wiadvance.sip.db.AppSQLiteOpenHelper;
import com.wiadvance.sip.db.CallLogTableHelper;
import com.wiadvance.sip.model.CallLogEntry;
import com.wiadvance.sip.model.Contact;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class UserData {

    private static final String PREF_NAME = "name";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_SIP = "sip";
    private static final String PREF_REGISTRATION_OK = "registration_ok";

    public static HashMap<String, String> sEmailToSipHashMap = new HashMap<>();
    public static HashMap<String, String> sEmailToPhoneHashMap = new HashMap<>();

    public static HashSet<String> sAvatar404Cache = new HashSet<>();

    public static CallLogEntry sCurrentLogEntry = new CallLogEntry();
    public static Contact sCurrentContact;

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

        sEmailToSipHashMap.clear();
        sEmailToPhoneHashMap.clear();

        SQLiteDatabase database = new AppSQLiteOpenHelper(context).getWritableDatabase();
        database.delete(AppDbSchema.FavoriteContactTable.NAME, null, null);
        database.delete(AppDbSchema.ContactTable.NAME, null, null);
        database.delete(AppDbSchema.RegularContactTable.NAME, null, null);
        database.delete(AppDbSchema.CallLogTable.NAME, null, null);
    }

    public static void recordCallLog(Context context) {
        long seconds = (new Date().getTime() - sCurrentLogEntry.getCallTime().getTime()) / 1000;
        sCurrentLogEntry.setCallDurationInSeconds((int) seconds);

        CallLogTableHelper.getInstance(context).addCallLog(sCurrentLogEntry);
    }
}
