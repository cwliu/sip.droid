package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.wiadvance.sip.db.ContactDbSchema.ContactTable;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactDbHelper {

    private static final Object lock = new Object();

    private static SQLiteDatabase mDatabase;
    private static ContactDbHelper sDbHelper;

    private ContactDbHelper(Context context) {
        mDatabase = new ContactSqliteOpenHelper(context).getWritableDatabase();
    }

    public static ContactDbHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new ContactDbHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    public void addContact(Contact contact) {
        ContentValues cv = getContentsValue(contact);

        mDatabase.insert(ContactTable.NAME, null, cv);
    }

    public void addContactList(List<Contact> contactList) {

        // TODO
        for (Contact c : contactList) {
            addContact(c);
        }
    }

    public List<Contact> getAllContacts() {

        ContactCursorWrapper contactCursorWrapper = queryContacts(null, null);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getPhoneContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_PHONE)};

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getCompanyContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_COMPANY)};

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs);
        return getContacts(contactCursorWrapper);
    }

    public void removeContacts() {
        // TODO
    }

    public void removePhoneContacts() {
        mDatabase.delete(ContactTable.NAME,
                ContactTable.Cols.TYPE + " = ?", new String[]{String.valueOf(Contact.TYPE_PHONE)});
    }

    public void removeCompanyContacts() {
        mDatabase.delete(ContactTable.NAME,
                ContactTable.Cols.TYPE + " = ?", new String[]{String.valueOf(Contact.TYPE_COMPANY)});
    }


    @NonNull
    private List<Contact> getContacts(ContactCursorWrapper contactCursorWrapper) {
        List<Contact> contacts = new ArrayList<>();
        try {
            contactCursorWrapper.moveToFirst();
            while (!contactCursorWrapper.isAfterLast()) {
                Contact contact = contactCursorWrapper.getContact();
                contacts.add(contact);
                contactCursorWrapper.moveToNext();
            }

        } finally {
            contactCursorWrapper.close();
        }
        return contacts;
    }

    private static ContentValues getContentsValue(Contact contact) {
        ContentValues cv = new ContentValues();
        cv.put(ContactTable.Cols.NAME, contact.getName());
        cv.put(ContactTable.Cols.SIP, contact.getSip());
        cv.put(ContactTable.Cols.PHONE, contact.getPhone());
        cv.put(ContactTable.Cols.EMAIL, contact.getEmail());
        cv.put(ContactTable.Cols.PHOTO, contact.getPhotoUri());
        cv.put(ContactTable.Cols.TYPE, contact.getType());

        return cv;
    }

    private static ContactCursorWrapper queryContacts(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                ContactTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        cursor.moveToFirst();
        return new ContactCursorWrapper(cursor);
    }
}
