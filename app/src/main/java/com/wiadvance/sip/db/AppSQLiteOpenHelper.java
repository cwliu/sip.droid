package com.wiadvance.sip.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wiadvance.sip.db.AppDbSchema.CallLogTable;
import com.wiadvance.sip.db.AppDbSchema.ContactTable;
import com.wiadvance.sip.db.AppDbSchema.FavoriteContactTable;
import com.wiadvance.sip.db.AppDbSchema.PhoneTable;
import com.wiadvance.sip.db.AppDbSchema.RegularContactTable;

public class AppSQLiteOpenHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "sip";
    private static int DB_VERSION = 10;

    public AppSQLiteOpenHelper(Context context) {
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
                ContactTable.Cols.EMAIL + "," +
                ContactTable.Cols.PHOTO + "," +
                ContactTable.Cols.TYPE + " INTEGER DEFAULT 0," +
                ContactTable.Cols.ANDROID_CONTACT_ID + "," +
                ContactTable.Cols.CREATED_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP )"
        );

        db.execSQL("CREATE TABLE " + CallLogTable.NAME + "(" +
                CallLogTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CallLogTable.Cols.CALL_TIME + " INTEGER," +
                CallLogTable.Cols.CALL_DURATION + " INTEGER," +
                CallLogTable.Cols.CALL_TYPE + " INTEGER," +
                CallLogTable.Cols.CONTACT + " INTEGER," +
                "FOREIGN KEY(" + CallLogTable.Cols.CONTACT + ") REFERENCES " +
                ContactTable.NAME + "(" + ContactTable.Cols.ID + ") ON DELETE CASCADE )"
        );

        db.execSQL("CREATE TABLE " + RegularContactTable.NAME + "(" +
                RegularContactTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                RegularContactTable.Cols.CONTACT + " INTEGER NOT NULL," +
                RegularContactTable.Cols.COUNT + " INTEGER," +
                RegularContactTable.Cols.UPDATED_TIME + " INTEGER," +
                "FOREIGN KEY(" + RegularContactTable.Cols.CONTACT + ") REFERENCES " +
                ContactTable.NAME + "(" + ContactTable.Cols.ID + ") ON DELETE CASCADE )"
        );

        db.execSQL("CREATE TABLE " + FavoriteContactTable.NAME + "(" +
                FavoriteContactTable.Cols.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteContactTable.Cols.CONTACT + " INTEGER NOT NULL," +
                "FOREIGN KEY(" + FavoriteContactTable.Cols.CONTACT + ") REFERENCES " +
                ContactTable.NAME + "(" + ContactTable.Cols.ID + ") ON DELETE CASCADE )"
        );

        db.execSQL("CREATE TABLE " + PhoneTable.NAME + "(" +
                PhoneTable.Cols.CONTACT + " INTEGER NOT NULL," +
                PhoneTable.Cols.PHONE + " NOT NULL," +
                PhoneTable.Cols.TYPE + " INTEGER DEFAULT 0," +
                "FOREIGN KEY(" + PhoneTable.Cols.CONTACT + ") REFERENCES " +
                ContactTable.NAME + "(" + ContactTable.Cols.ID + ") ON DELETE CASCADE," +
                "PRIMARY KEY ( " + PhoneTable.Cols.CONTACT + ", " +PhoneTable.Cols.PHONE + ")" +
                ")"
        );
    }

    private void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + PhoneTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + FavoriteContactTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + RegularContactTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CallLogTable.NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ContactTable.NAME);
    }
}
