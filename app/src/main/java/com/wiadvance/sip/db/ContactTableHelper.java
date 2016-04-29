package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.wiadvance.sip.db.AppDbSchema.ContactTable;
import com.wiadvance.sip.db.AppDbSchema.ContactTable.Cols;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactTableHelper {

    private static final Object lock = new Object();

    private static SQLiteDatabase mDatabase;
    private static ContactTableHelper sDbHelper;
    private Context mContext;

    private ContactTableHelper(Context context) {
        mDatabase = new AppSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static ContactTableHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new ContactTableHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    private static ContentValues getContentsValue(Contact contact) {
        ContentValues cv = new ContentValues();
        cv.put(Cols.NAME, contact.getName());
        cv.put(Cols.SIP, contact.getSip());
        cv.put(Cols.EMAIL, contact.getEmail());
        cv.put(Cols.PHOTO, contact.getPhotoUri());
        cv.put(Cols.TYPE, contact.getType());
        cv.put(Cols.ANDROID_CONTACT_ID, contact.getAndroidContactId()); // Optional

        return cv;
    }

    private ContactCursorWrapper queryContacts(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                ContactTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();
        return new ContactCursorWrapper(cursor);
    }

    public long addContact(Contact contact) {
        ContentValues cv = getContentsValue(contact);
        long id = mDatabase.insert(ContactTable.NAME, null, cv);

        List<String> list = contact.getPhoneList();
        if (list.size() != 0) {
            PhoneTableHelper.getInstance(mContext).delete((int) id);
            PhoneTableHelper.getInstance(mContext).add((int) id, list);
        }

        return id;
    }

    public Contact getContactById(int contact_id) {
        String whereClause = Cols.ID + " = ? ";

        String[] whereArgs = new String[]{String.valueOf(contact_id)};
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, null);
        List<Contact> contacts = getContacts(contactCursorWrapper);
        if (contacts.size() == 0) {
            return null;
        } else {
            return contacts.get(0);
        }
    }

    public Contact getContactByEmail(String email) {
        String whereClause = Cols.EMAIL + " = ? ";

        String[] whereArgs = new String[]{String.valueOf(email)};
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, null);

        List<Contact> contacts = getContacts(contactCursorWrapper);
        if (contacts.size() == 0) {
            return null;
        } else {
            return contacts.get(0);
        }
    }

    private Contact getContactByAndroidContactId(String id) {
        String whereClause = Cols.ANDROID_CONTACT_ID + " = ? ";

        String[] whereArgs = new String[]{String.valueOf(id)};
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, null);

        List<Contact> contacts = getContacts(contactCursorWrapper);
        if (contacts.size() == 0) {
            return null;
        } else {
            return contacts.get(0);
        }
    }

    public List<Contact> getAllContacts() {
        String whereClause = Cols.TYPE + " = ? OR " +
                Cols.TYPE + " = ? OR " +
                Cols.TYPE + " = ?";

        String orderBy = Cols.NAME + " ASC";

        String[] whereArgs = new String[]{
                String.valueOf(Contact.TYPE_COMPANY),
                String.valueOf(Contact.TYPE_PHONE),
                String.valueOf(Contact.TYPE_PHONE_MANUAL)
        };
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getPhoneContacts() {
        String whereClause = Cols.TYPE + " = ? OR " + Cols.TYPE + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(Contact.TYPE_PHONE),
                String.valueOf(Contact.TYPE_PHONE_MANUAL)
        };

        String orderBy = Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getManualPhoneContacts() {
        String whereClause = Cols.TYPE + " = ? ";
        String[] whereArgs = new String[]{
                String.valueOf(Contact.TYPE_PHONE_MANUAL)
        };

        String orderBy = Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }


    public List<Contact> getCompanyContacts() {
        String whereClause = Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_COMPANY)};
        String orderBy = Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
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


    public void updateCompanyContactByEmail(Contact c) {
        String whereClause = Cols.EMAIL + " = ? AND " + Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{c.getEmail(), String.valueOf(Contact.TYPE_COMPANY)};

        Contact contact = getContactByEmail(c.getEmail());
        if (contact == null) {
            addContact(c);
        } else {
            mDatabase.update(ContactTable.NAME, getContentsValue(c), whereClause, whereArgs);
        }
    }

    public void updatePhoneContactByAndroidContactId(Contact newContact) {
        String whereClause = Cols.ANDROID_CONTACT_ID + " = ? AND " + Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{newContact.getAndroidContactId(), String.valueOf(Contact.TYPE_PHONE)};

        Contact queriedContact = getContactByAndroidContactId(newContact.getAndroidContactId());
        if (queriedContact == null) {
            addContact(newContact);
        } else {
            mDatabase.update(ContactTable.NAME, getContentsValue(newContact), whereClause, whereArgs);
        }

        queriedContact = getContactByAndroidContactId(newContact.getAndroidContactId());
        if (queriedContact != null) {
            PhoneTableHelper.getInstance(mContext).delete(queriedContact.getId());
            PhoneTableHelper.getInstance(mContext).add(queriedContact.getId(), newContact.getPhoneList());
        }
    }

    public void updateCompanyContactSipByEmail(String email, String sipAccount) {
        String whereClause = Cols.EMAIL + " = ? AND " + Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{email, String.valueOf(Contact.TYPE_COMPANY)};

        ContentValues cv = new ContentValues();
        cv.put(Cols.SIP, sipAccount);
        mDatabase.update(ContactTable.NAME, cv, whereClause, whereArgs);
    }

    class ContactCursorWrapper extends CursorWrapper {
        public ContactCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Contact getContact() {

            if (getCount() == 0) {
                return null;
            }

            int id = getInt(getColumnIndex(Cols.ID));
            String name = getString(getColumnIndex(Cols.NAME));
            String sip = getString(getColumnIndex(Cols.SIP));
            String email = getString(getColumnIndex(Cols.EMAIL));
            String photo = getString(getColumnIndex(Cols.PHOTO));
            Integer type = getInt(getColumnIndex(Cols.TYPE));

            Contact contact = new Contact(name);
            contact.setId(id);
            contact.setEmail(email);
            contact.setSip(sip);
            List<String> phoneList = PhoneTableHelper.getInstance(mContext).getPhoneList(id);
            contact.setPhoneList(phoneList);
            contact.setPhotoUri(photo);
            contact.setType(type);

            return contact;
        }
    }
}
