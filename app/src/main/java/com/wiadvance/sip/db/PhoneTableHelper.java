package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

import com.wiadvance.sip.db.AppDbSchema.PhoneTable;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class PhoneTableHelper {
    private static final Object lock = new Object();

    private static PhoneTableHelper sDbHelper;

    private static SQLiteDatabase mDatabase;
    private Context mContext;

    private PhoneTableHelper(Context context) {
        mDatabase = new AppSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static PhoneTableHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new PhoneTableHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    private static ContentValues getContentsValue(int contact_id, String phone) {
        ContentValues cv = new ContentValues();
        cv.put(PhoneTable.Cols.CONTACT, contact_id);
        cv.put(PhoneTable.Cols.PHONE, phone);
        return cv;
    }

    private PhoneCursorWrapper query(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                AppDbSchema.PhoneTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();
        return new PhoneCursorWrapper(cursor);
    }

    public List<String> getPhoneList(int contact_id) {
        String whereClause = AppDbSchema.PhoneTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contact_id)};

        PhoneCursorWrapper cursorWrapper = query(whereClause, whereArgs, null);
        List<String> phones = new ArrayList<>();
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                String phone = cursorWrapper.getPhone();
                phones.add(phone);
                cursorWrapper.moveToNext();
            }

        } finally {
            cursorWrapper.close();
        }

        return phones;
    }


    public void add(int contact_id, List<String> phoneList) {
        for (String phone : phoneList) {
            ContentValues cv = getContentsValue(contact_id, phone);
            try {
                mDatabase.insertOrThrow(PhoneTable.NAME, null, cv);
            } catch (SQLiteConstraintException e) {
                // ignore
            }
        }
    }

    public void delete(int contact_id) {
        String whereClause = AppDbSchema.PhoneTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contact_id)};

        mDatabase.delete(AppDbSchema.PhoneTable.NAME, whereClause, whereArgs);
    }

    public void setCompanyContactPhoneByEmail(String email, String phone) {
        Contact contact = ContactTableHelper.getInstance(mContext).getContactByEmail(email);
        if (contact == null) {
            return;
        }
        List<String> list = new ArrayList<>();
        list.add(phone);
        contact.setPhoneList(list);

        delete(contact.getId());
        add(contact.getId(), list);
    }

    private class PhoneCursorWrapper extends CursorWrapper {
        public PhoneCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public String getPhone() {
            if (getCount() == 0) {
                return null;
            }

            String phone = getString(getColumnIndex(PhoneTable.Cols.PHONE));

            return phone;
        }
    }
}
