package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.wiadvance.sip.db.AppDbSchema.CallLogTable.Cols;
import com.wiadvance.sip.model.CallLogEntry;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogTableHelper {

    private static final Object lock = new Object();

    private static CallLogTableHelper sDbHelper;

    private static SQLiteDatabase mDatabase;
    private static Context mContext;

    private CallLogTableHelper(Context context) {
        mDatabase = new AppSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static CallLogTableHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new CallLogTableHelper(context);
                }
                return sDbHelper;
            }
        } else {
            return sDbHelper;
        }
    }

    private static ContentValues getContentsValue(CallLogEntry log) {
        ContentValues cv = new ContentValues();
        cv.put(Cols.CALL_TIME, log.getCallTime().getTime());
        cv.put(Cols.CALL_DURATION, log.getCallDurationInSeconds());
        cv.put(Cols.CALL_TYPE, log.getCallType());

        Contact contact = log.getContact();
        if (contact != null) {

            if(contact.getId() != 0){
                cv.put(Cols.CONTACT, contact.getId());
            }else{
                contact.setType(Contact.TYPE_EXTERNAL);
                long id = ContactTableHelper.getInstance(mContext).addContact(contact);
                cv.put(Cols.CONTACT, id);
            }
        }

        return cv;
    }


    private CallLogCursorWrapper query(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                AppDbSchema.CallLogTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                orderBy
        );
        cursor.moveToFirst();
        return new CallLogCursorWrapper(cursor);
    }

    public List<CallLogEntry> getAllCallLogs() {
        String orderBy = Cols.CALL_TIME + " DESC";

        CallLogCursorWrapper cursorWrapper = query(null, null, orderBy);

        List<CallLogEntry> logs = new ArrayList<>();
        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                CallLogEntry log = cursorWrapper.getCallLog();
                logs.add(log);
                cursorWrapper.moveToNext();
            }

        } finally {
            cursorWrapper.close();
        }
        return logs;
    }

    public void addCallLog(CallLogEntry log) {
        ContentValues cv = getContentsValue(log);

        mDatabase.insert(AppDbSchema.CallLogTable.NAME, null, cv);
    }

    class CallLogCursorWrapper extends CursorWrapper {
        public CallLogCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public CallLogEntry getCallLog() {
            if (getCount() == 0) {
                return null;
            }

            int id = getInt(getColumnIndex(AppDbSchema.CallLogTable.Cols.ID));
            long callTime = getLong(getColumnIndex(AppDbSchema.CallLogTable.Cols.CALL_TIME));
            int callDuration = getInt(getColumnIndex(AppDbSchema.CallLogTable.Cols.CALL_DURATION));
            int callType = getInt(getColumnIndex(AppDbSchema.CallLogTable.Cols.CALL_TYPE));
            int contact_id = getInt(getColumnIndex(AppDbSchema.CallLogTable.Cols.CONTACT));


            CallLogEntry log = new CallLogEntry();
            log.setId(id);
            log.setCallTime(new Date(callTime));
            log.setCallDurationInSeconds(callDuration);
            log.setCallType(callType);

            Contact contact = ContactTableHelper.getInstance(mContext).getContactById(contact_id);
            log.setContact(contact);
            return log;
        }
    }
}
