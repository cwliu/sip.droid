package com.wiadvance.sip.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactSqliteOpenHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "sip";
    private static int DB_VERSION = 1;

    public ContactSqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ContactDbSchema.ContactTable.NAME + "(" +
                ContactDbSchema.ContactTable.Cols.ID + " integer primary key autoincrement, " +
                ContactDbSchema.ContactTable.Cols.NAME + "," +
                ContactDbSchema.ContactTable.Cols.SIP + "," +
                ContactDbSchema.ContactTable.Cols.PHONE + "," +
                ContactDbSchema.ContactTable.Cols.EMAIL + "," +
                ContactDbSchema.ContactTable.Cols.PHOTO + "," +
                ContactDbSchema.ContactTable.Cols.TYPE + " INTEGER DEFAULT 0," +
                ContactDbSchema.ContactTable.Cols.CREATED_TIME + " DATETIME )"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
