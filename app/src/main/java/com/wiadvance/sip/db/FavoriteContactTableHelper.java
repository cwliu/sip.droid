package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.wiadvance.sip.NotificationUtil;
import com.wiadvance.sip.db.AppDbSchema.FavoriteContactTable;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;


public class FavoriteContactTableHelper {


    private static final Object lock = new Object();

    private static FavoriteContactTableHelper sDbHelper;

    private static SQLiteDatabase mDatabase;
    private Context mContext;

    private FavoriteContactTableHelper(Context context) {
        mDatabase = new AppSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static FavoriteContactTableHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new FavoriteContactTableHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    private static ContentValues getContentsValue(Contact contact) {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteContactTable.Cols.CONTACT, contact.getId());
        return cv;
    }


    public void add(Contact contact) {
        ContentValues cv = getContentsValue(contact);

        mDatabase.insert(FavoriteContactTable.NAME, null, cv);

        NotificationUtil.favoriteUpdate(mContext);
        Toast.makeText(mContext, "已將 " + contact.getName() + " 加入我的最愛", Toast.LENGTH_SHORT).show();
    }

    public void remove(Contact contact) {
        String whereClause = FavoriteContactTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contact.getId())};

        mDatabase.delete(AppDbSchema.FavoriteContactTable.NAME, whereClause, whereArgs);

        NotificationUtil.favoriteUpdate(mContext);
        Toast.makeText(mContext, "已將 " + contact.getName() + " 移出我的最愛", Toast.LENGTH_SHORT).show();
    }

    private FavoriteCursorWrapper query(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                FavoriteContactTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();
        return new FavoriteCursorWrapper(cursor);
    }

    public List<Contact> getAll() {
        FavoriteCursorWrapper cursorWrapper = query(null, null, null);
        List<Contact> contacts = new ArrayList<>();
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                Contact contact = cursorWrapper.getFavoriteContact();
                contacts.add(contact);
                cursorWrapper.moveToNext();
            }

        } finally {
            cursorWrapper.close();
        }
        return contacts;
    }

    public boolean isFavoriteContact(Contact contact) {
        String whereClause = AppDbSchema.FavoriteContactTable.Cols.CONTACT + " = ?";
        String[] whereArgs = new String[]{String.valueOf(contact.getId())};

        FavoriteCursorWrapper cursorWrapper = query(whereClause, whereArgs, null);
        if (cursorWrapper.getCount() > 0) {
            return true;
        } else {
            return false;
        }
    }

    private class FavoriteCursorWrapper extends CursorWrapper {
        public FavoriteCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public Contact getFavoriteContact() {
            if (getCount() == 0) {
                return null;
            }

            int contact_id = getInt(getColumnIndex(FavoriteContactTable.Cols.CONTACT));

            return ContactTableHelper.getInstance(mContext).getContactById(contact_id);
        }
    }
}
