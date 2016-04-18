package com.wiadvance.sip;

import android.content.Context;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserData {

    private static final String PREF_NAME = "name";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_SIP = "sip";
    private static final String PREF_REGISTRATION_OK = "registration_ok";
    private static final String PREF_RECENT_CONTACT = "recent_contact";
    private static final String PREF_FAVORATE_CONTACT = "favorite_contact";

    public static HashBiMap<String, String> sEmailtoSipBiMap = HashBiMap.create();
    public static HashBiMap<String, String> sEmailtoPhoneBiMap = HashBiMap.create();
    public static List<Contact> sFavoriteContactListCache = new ArrayList<>();


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

    public static List<Contact> getFavoriteContactList(Context context) {
        Gson gson = new Gson();
        String json = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_FAVORATE_CONTACT, new Gson().toJson(new ArrayList<Contact>()));
        return gson.fromJson(json, new TypeToken<List<Contact>>() {
        }.getType());
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

    public static void setRecentContactList(Context context, List<Contact> contacts) {
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_RECENT_CONTACT, json).apply();
    }

    public static void setFavoriteContactList(Context context, List<Contact> contacts) {
        Gson gson = new Gson();
        String json = gson.toJson(contacts);
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_FAVORATE_CONTACT, json).apply();
    }

    public static void clean(Context context) {
        setName(context, null);
        setEmail(context, null);
        setSip(context, null);
        setRegistrationStatus(context, false);
        setRecentContactList(context, new ArrayList<Contact>());
        setFavoriteContactList(context, new ArrayList<Contact>());

        sEmailtoSipBiMap.clear();
        sEmailtoPhoneBiMap.clear();

        ContactDbHelper.getInstance(context).removeContacts();
    }

    public static void addFavoriteContact(Context context, Contact contact) {
        List<Contact> list = getFavoriteContactList(context);
        boolean isExist = false;

        Iterator<Contact> i = list.iterator();
        while (i.hasNext()) {
            Contact c = i.next(); // must be called before you can call i.remove()
            if (c.equals(contact)) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            list.add(0, contact);
            setFavoriteContactList(context, list);
        }
        NotificationUtil.favoriteUpdate(context);
        Toast.makeText(context, "已將" + contact.getName() + "加入我的最愛", Toast.LENGTH_SHORT).show();

        sFavoriteContactListCache = list;
    }

    public static void removeFavoriteContact(Context context, Contact contact) {
        List<Contact> list = getFavoriteContactList(context);

        Iterator<Contact> i = list.iterator();
        while (i.hasNext()) {
            Contact c = i.next(); // must be called before you can call i.remove()
            if (c.equals(contact)) {
                i.remove();
            }
        }

        NotificationUtil.favoriteUpdate(context);
        setFavoriteContactList(context, list);
        Toast.makeText(context, "已將" + contact.getName() + "移出我的最愛", Toast.LENGTH_SHORT).show();

        sFavoriteContactListCache = list;
    }

    public static boolean isFavoriteContact(Context context, Contact contact) {
        if (sFavoriteContactListCache.size() == 0) {
            sFavoriteContactListCache = getFavoriteContactList(context);
        }

        List<Contact> list = sFavoriteContactListCache;

        Iterator<Contact> i = list.iterator();
        while (i.hasNext()) {
            Contact c = i.next(); // must be called before you can call i.remove()
            if (c.equals(contact)) {
                return true;
            }
        }

        return false;
    }

    public static List<Contact> getAllContact(Context context) {
        return ContactDbHelper.getInstance(context).getAllContacts();
    }
}
