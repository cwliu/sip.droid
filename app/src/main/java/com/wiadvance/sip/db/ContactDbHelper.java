package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.wiadvance.sip.NotificationUtil;
import com.wiadvance.sip.db.AppDbSchema.ContactTable;
import com.wiadvance.sip.db.AppDbSchema.ContactTable.Cols;
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
        mDatabase = new GWSQLiteOpenHelper(context).getWritableDatabase();
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

    private static ContentValues getContentsValue(Contact contact) {
        ContentValues cv = new ContentValues();
        cv.put(Cols.NAME, contact.getName());
        cv.put(Cols.SIP, contact.getSip());
        cv.put(Cols.PHONE, contact.getPhone());
        cv.put(Cols.EMAIL, contact.getEmail());
        cv.put(Cols.PHOTO, contact.getPhotoUri());
        cv.put(Cols.TYPE, contact.getType());

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

    public void addContact(Contact contact) {
        ContentValues cv = getContentsValue(contact);

        mDatabase.insert(ContactTable.NAME, null, cv);
    }

    public void addContactList(List<Contact> contactList) {

        // TODO Bulk insert
        for (Contact c : contactList) {
            addContact(c);
        }
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

    public List<Contact> getAllContacts() {
        String whereClause = Cols.TYPE + " = ? OR " +
                Cols.TYPE + " = ? OR " +
                Cols.TYPE + " = ?";

        String[] whereArgs = new String[]{
                String.valueOf(Contact.TYPE_COMPANY),
                String.valueOf(Contact.TYPE_PHONE),
                String.valueOf(Contact.TYPE_PHONE_MANUAL)
        };
        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, null);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getPhoneContacts() {
        String whereClause = Cols.TYPE + " = ? OR " +
                Cols.TYPE + " = ? ";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_PHONE),
                String.valueOf(Contact.TYPE_PHONE_MANUAL)};

        String orderBy = Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getPhoneManualContacts() {
        String whereClause = Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_PHONE_MANUAL)};
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

    public List<Contact> getRecentContacts() {
        String whereClause = Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_RECENT)};
        String orderBy = "datetime(" + Cols.CREATED_TIME + ") DESC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public List<Contact> getFavoriteContacts() {
        String whereClause = Cols.TYPE + " = ?";
        String[] whereArgs = new String[]{String.valueOf(Contact.TYPE_FAVORITE)};
        String orderBy = Cols.NAME + " ASC";

        ContactCursorWrapper contactCursorWrapper = queryContacts(whereClause, whereArgs, orderBy);
        return getContacts(contactCursorWrapper);
    }

    public void addRecentContact(Contact contact) {
        List<Contact> list = getRecentContacts();

        // FIX ME
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
        mDatabase.delete(ContactTable.NAME, Cols.ID + " = ?", new String[]{dbId});
    }

    public void removeAllContacts() {
        mDatabase.delete(ContactTable.NAME, null, null);
    }

    public void removePhoneContacts() {
        mDatabase.delete(ContactTable.NAME,
                Cols.TYPE + " = ?", new String[]{String.valueOf(Contact.TYPE_PHONE)});
    }

    public void removeCompanyContacts() {
        mDatabase.delete(ContactTable.NAME,
                Cols.TYPE + " = ?", new String[]{String.valueOf(Contact.TYPE_COMPANY)});
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

        String whereClause = Cols.NAME + " = ? AND " + Cols.TYPE + " = ?";
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
            String phone = getString(getColumnIndex(Cols.PHONE));
            String email = getString(getColumnIndex(Cols.EMAIL));
            String photo = getString(getColumnIndex(Cols.PHOTO));
            Integer type = getInt(getColumnIndex(Cols.TYPE));

            Contact contact = new Contact(name);
            contact.setId(id);
            contact.setEmail(email);
            contact.setSip(sip);
            contact.setPhone(phone);
            contact.setPhotoUri(photo);
            contact.setType(type);

            return contact;
        }
    }
}
