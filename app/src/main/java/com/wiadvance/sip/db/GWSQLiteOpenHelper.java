package com.wiadvance.sip.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wiadvance.sip.db.CallLogDbSchema.CallLogTable;
import com.wiadvance.sip.db.ContactDbSchema.ContactTable;

public class GWSQLiteOpenHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "sip";
    private static int DB_VERSION = 3;

    public GWSQLiteOpenHelper(Context context) {
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
        db.execSQL("CREATE TABLE " + ContactTable.NAME + "(" +
                ContactTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ContactTable.Cols.NAME + "," +
                ContactTable.Cols.SIP + "," +
                ContactTable.Cols.PHONE + "," +
                ContactTable.Cols.EMAIL + "," +
                ContactTable.Cols.PHOTO + "," +
                ContactTable.Cols.TYPE + " INTEGER DEFAULT 0," +
                ContactTable.Cols.CREATED_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP )"
        );

        db.execSQL("CREATE TABLE " + CallLogTable.NAME + "(" +
                CallLogTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallLogTable.Cols.CALL_TIME + " INTEGER," +
                CallLogTable.Cols.CALL_DURATION + " INTEGER," +
                CallLogTable.Cols.CALL_TYPE + " INTEGER," +
                CallLogTable.Cols.CONTACT + " INTEGER," +
                "FOREIGN KEY(" + CallLogTable.Cols.CONTACT + ") REFERENCES " +
                ContactTable.NAME + "(" + ContactTable.Cols.ID + ") )"
        );
    }

    private void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + CallLogTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ContactTable.NAME);
    }
}
