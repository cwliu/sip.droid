package com.wiadvance.sip.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ContactSqliteOpenHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "sip";
    private static int DB_VERSION = 2;

    public ContactSqliteOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db);
        createTable(db);
    }

    private void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ContactDbSchema.ContactTable.NAME + "(" +
                ContactDbSchema.ContactTable.Cols.ID + " integer primary key autoincrement, " +
                ContactDbSchema.ContactTable.Cols.NAME + "," +
                ContactDbSchema.ContactTable.Cols.SIP + "," +
                ContactDbSchema.ContactTable.Cols.PHONE + "," +
                ContactDbSchema.ContactTable.Cols.EMAIL + "," +
                ContactDbSchema.ContactTable.Cols.PHOTO + "," +
                ContactDbSchema.ContactTable.Cols.TYPE + " INTEGER DEFAULT 0," +
                ContactDbSchema.ContactTable.Cols.CREATED_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP )"
        );

    }

    private void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ContactDbSchema.ContactTable.NAME);
    }
}
