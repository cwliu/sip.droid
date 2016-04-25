package com.wiadvance.sip.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;

import com.wiadvance.sip.db.CallLogDbSchema.CallLogTable.Cols;
import com.wiadvance.sip.model.CallLogEntry;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallLogDbHelper {

    private static final Object lock = new Object();

    private static CallLogDbHelper sDbHelper;

    private static SQLiteDatabase mDatabase;
    private Context mContext;

    private CallLogDbHelper(Context context) {
        mDatabase = new GWSQLiteOpenHelper(context).getWritableDatabase();
        mContext = context;
    }

    public static CallLogDbHelper getInstance(Context context) {
        if (sDbHelper == null) {
            synchronized (lock) {
                if (sDbHelper == null) {
                    sDbHelper = new CallLogDbHelper(context);
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

        if (log.getContact() != null) {
            cv.put(Cols.CONTACT, log.getContact().getId());
        }

        return cv;
    }


    private CallLogCursorWrapper query(String whereClause, String[] whereArgs, String orderBy) {
        Cursor cursor = mDatabase.query(
                CallLogDbSchema.CallLogTable.NAME,
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

        mDatabase.insert(CallLogDbSchema.CallLogTable.NAME, null, cv);
    }

    class CallLogCursorWrapper extends CursorWrapper {
        public CallLogCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public CallLogEntry getCallLog() {
            if (getCount() == 0) {
                return null;
            }

            int id = getInt(getColumnIndex(Cols.ID));
            int callTime = getInt(getColumnIndex(Cols.CALL_TIME));
            int callDuration = getInt(getColumnIndex(Cols.CALL_DURATION));
            int callType = getInt(getColumnIndex(Cols.CALL_TYPE));
            int contact_id = getInt(getColumnIndex(Cols.CONTACT));


            CallLogEntry log = new CallLogEntry();
            log.setId(id);
            log.setCallTime(new Date(callTime));
            log.setCallDurationInSeconds(callDuration);
            log.setCallType(callType);

            Contact contact = ContactDbHelper.getInstance(mContext).getContactById(contact_id);
            log.setContact(contact);
            return log;
        }
    }
}
