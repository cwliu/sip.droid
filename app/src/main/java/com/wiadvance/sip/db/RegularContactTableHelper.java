package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.model.RegularContact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegularContactTableHelper {

    private static final Object lock = new Object();

    private static RegularContactTableHelper sDbHelper;

    private static SQLiteDatabase mDatabase;
    private Context mContext;

    private RegularContactTableHelper(Context context) {
        mDatabase = new AppSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static RegularContactTableHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new RegularContactTableHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    private static ContentValues getContentsValue(RegularContact regularContact) {
        ContentValues cv = new ContentValues();
        cv.put(AppDbSchema.RegularContactTable.Cols.CONTACT, regularContact.getContact().getId());
        cv.put(AppDbSchema.RegularContactTable.Cols.COUNT, regularContact.getCount());
        cv.put(AppDbSchema.RegularContactTable.Cols.UPDATED_TIME,
                regularContact.getUpdatedTime().getTime());

        return cv;
    }

    private RegularCursorWrapper query(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                AppDbSchema.RegularContactTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();
        return new RegularCursorWrapper(cursor);
    }

    public List<Contact> getRegularContactList(){
        String orderBy = AppDbSchema.RegularContactTable.Cols.COUNT + " DESC, " +
                AppDbSchema.RegularContactTable.Cols.UPDATED_TIME + " DESC";

        RegularCursorWrapper cursorWrapper = query(null, null, orderBy);
        List<Contact> contacts = new ArrayList<>();
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                Contact contact = cursorWrapper.getRegularContact().getContact();
                contacts.add(contact);
                cursorWrapper.moveToNext();
            }

        } finally {
            cursorWrapper.close();
        }
        return contacts;
    }

    private void addRegularContact(RegularContact rc) {
        ContentValues cv = getContentsValue(rc);

        mDatabase.insert(AppDbSchema.RegularContactTable.NAME, null, cv);
    }


    public void addContactCountByOne(Contact contact){
        String whereClause = AppDbSchema.RegularContactTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contact.getId())};

        RegularContact rc = getRegularContactByContactId(contact.getId());
        if (rc == null) {
            RegularContact newRc = new RegularContact(contact);
            addRegularContact(newRc);
        } else {
            rc.setCount(rc.getCount() +1);
            rc.setUpdatedTime(new Date());
            mDatabase.update(AppDbSchema.RegularContactTable.NAME, getContentsValue(rc), whereClause, whereArgs);
        }
    }

    private RegularContact getRegularContactByContactId(int id) {
        String whereClause = AppDbSchema.RegularContactTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(id)};

        RegularCursorWrapper cursorWrapper = query(whereClause, whereArgs, null);
        return cursorWrapper.getRegularContact();
    }

    private class RegularCursorWrapper extends CursorWrapper {
        public RegularCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public RegularContact getRegularContact() {
            if (getCount() == 0) {
                return null;
            }

            int id = getInt(getColumnIndex(AppDbSchema.RegularContactTable.Cols.ID));
            int contact_id = getInt(getColumnIndex(AppDbSchema.RegularContactTable.Cols.CONTACT));
            int count = getInt(getColumnIndex(AppDbSchema.RegularContactTable.Cols.COUNT));

            Contact contact = ContactTableHelper.getInstance(mContext).getContactById(contact_id);

            RegularContact regular = new RegularContact(contact);
            regular.setId(id);
            regular.setCount(count);
            regular.setUpdatedTime(new Date());
            return regular;
        }
    }
}