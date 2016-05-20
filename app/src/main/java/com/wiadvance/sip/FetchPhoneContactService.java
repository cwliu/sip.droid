package com.wiadvance.sip;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;


public class FetchPhoneContactService extends IntentService {

    public FetchPhoneContactService() {
        super("FetchPhoneContactService");
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, FetchPhoneContactService.class);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String orderBy = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        Cursor phones = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, orderBy);

        if (phones == null) {
            return;
        }

        String lastContactId = "";

        Contact c = new Contact();
        try {
            while (phones.moveToNext()) {
                String androidContactId = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

                String name = phones.getString(phones.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                String phoneNumber = PhoneUtils.normalizedPhone(phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));

                String phoneType = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));


                if (androidContactId.equals(lastContactId)) {
                    c.getPhoneList().add(phoneNumber);
                } else {
                    if (!lastContactId.equals("")) {
                        ContactTableHelper.getInstance(this).updatePhoneContactByAndroidContactId(c);
                    }

                    c = new Contact(name);
                    List<String> list = new ArrayList<>();
                    list.add(phoneNumber);
                    c.setPhoneList(list);

                    Uri uri = getPhotoUri(androidContactId);
                    if (uri != null) {
                        c.setPhotoUri(uri);
                    }
                    c.setType(Contact.TYPE_PHONE);
                    c.setAndroidContactId(androidContactId);
                }
                lastContactId = androidContactId;
            }
        } finally {
            phones.close();
        }

        if (!lastContactId.equals("")) {
            ContactTableHelper.getInstance(this).updatePhoneContactByAndroidContactId(c);
        }


        NotificationUtil.phoneContactUpdate(this);
    }

    public Uri getPhotoUri(String id) {
        Cursor cur = null;
        try {
            cur = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                .parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}
