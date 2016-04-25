package com.wiadvance.sip.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CallLogEntry {

    public static int TYPE_OUTGOING_CALL_ANSWERED = 1;
    public static int TYPE_OUTGOING_CALL_NO_ANSWER = 2;
    public static int TYPE_INCOMING_CALL_ANSWERED = 3;
    public static int TYPE_INCOMING_CALL_NO_ANSWER = 4;

    private int id;
    private Contact contact;
    private Date callTime;
    private int callDurationInSeconds;
    private int callType;

    public CallLogEntry() {
        contact = new Contact("Unknown");
        callTime = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Date getCallTime() {
        return callTime;
    }

    public void setCallTime(Date callTime) {
        this.callTime = callTime;
    }

    public int getCallDurationInSeconds() {
        return callDurationInSeconds;
    }

    public void setCallDurationInSeconds(int callDurationInSeconds) {
        this.callDurationInSeconds = callDurationInSeconds;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }

    public String getCallTimeString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        return sdf.format(getCallTime());
    }
}
