package com.wiadvance.sip;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.wiadvance.sip.db.ContactDbHelper;
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

        List<Contact> contactList = new ArrayList<>();

        String orderBy = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, orderBy);
        if (phones == null) {
            return;
        }

        int phone_index = 1;
        String lastName = "";
        try {
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                if (name.equals(lastName)) {
                    name = lastName + "-" + ++phone_index;
                } else {
                    lastName = name;
                    phone_index = 1;
                }

                String phoneNumber = phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Contact c = new Contact(name);
                c.setPhone(phoneNumber);
                Uri uri = getPhotoUri(phones.getString(
                        phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
                if (uri != null) {
                    c.setPhotoUri(uri);
                }
                c.setType(Contact.TYPE_PHONE);
                contactList.add(c);
            }
        } finally {
            phones.close();
        }

        ContactDbHelper.getInstance(this).removePhoneContacts();
        ContactDbHelper.getInstance(this).addContactList(contactList);

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
