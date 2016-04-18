package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.wiadvance.sip.NotificationUtil;
import com.wiadvance.sip.db.ContactDbSchema.ContactTable;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContactDbHelper {

    private static final Object lock = new Object();

    private static SQLiteDatabase mDatabase;
    private static ContactDbHelper sDbHelper;
    private Context mContext;

    private ContactDbHelper(Context context) {
        mDatabase = new ContactSqliteOpenHelper(context).getWritableDatabase();
        mContext = context;
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
        String whereClause = ContactTable.Cols.TYPE + " = ? OR " +
                ContactTable.Cols.TYPE + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_COMPANY),
                String.valueOf(Contact.TYPE_PHONE)};
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, null);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getPhoneContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_PHONE)};
        String orderBy = ContactTable.Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getCompanyContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_COMPANY)};
        String orderBy = ContactTable.Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getRecentContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_RECENT)};
        String orderBy = "datetime(" + ContactTable.Cols.CREATED_TIME + ") DESC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getFavoriteContacts() {
        String whereClause = ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_FAVORITE)};
        String orderBy = ContactTable.Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public void addRecentContact(Contact contact) {
        List<Contact> list = getRecentContacts();

        Iterator<Contact> i = list.iterator();
        while (i.hasNext()) {
            Contact dbContact = i.next();
            if (dbContact.equals(contact)) {
                removeContactById(dbContact.getId());
            }
        }

        contact.setType(Contact.TYPE_RECENT);
        addContact(contact);
    }

    private void removeContactById(int id) {
        String dbId = String.valueOf(id);
        mDatabase.delete(ContactTable.NAME, ContactTable.Cols.ID + " = ?", new String[]{dbId});
    }

    public void removeAllContacts() {
        mDatabase.delete(ContactTable.NAME, null, null);
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

    private static ContactCursorWrapper queryContacts(String whereClause, String[] whereArgs, String orderBy) {
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

    public void addFavoriteContact(Contact contact) {

        List<Contact> list = getFavoriteContacts();
        boolean isExist = false;
        contact.setType(Contact.TYPE_FAVORITE);

        Iterator<Contact> i = list.iterator();
        while (i.hasNext()) {
            Contact c = i.next(); // must be called before you can call i.remove()
            if (c.equals(contact)) {
                isExist = true;
                break;
            }
        }

        if (!isExist) {
            addContact(contact);
        }

        NotificationUtil.favoriteUpdate(mContext);
        Toast.makeText(mContext, "已將 " + contact.getName() + " 加入我的最愛", Toast.LENGTH_SHORT).show();

    }

    public void removeFavoriteContact(Contact contact) {

        String whereClause = ContactTable.Cols.NAME + " = ? AND " + ContactTable.Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{contact.getName(), String.valueOf(Contact.TYPE_FAVORITE)};
        mDatabase.delete(ContactTable.NAME, whereClause, whereArgs);

        NotificationUtil.favoriteUpdate(mContext);
        Toast.makeText(mContext, "已將 " + contact.getName() + " 移出我的最愛", Toast.LENGTH_SHORT).show();
    }

    public boolean isFavoriteContact(Contact contact) {
        for (Contact c : getFavoriteContacts()) {
            if (c.equals(contact)) {
                return true;
            }
        }
        return false;
    }
}
