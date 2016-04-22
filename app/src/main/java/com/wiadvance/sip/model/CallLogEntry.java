package com.wiadvance.sip.model;

import java.util.Date;

public class CallLogEntry {

    public static int TYPE_OUTGOING_CALL_ANSWERED = 1;
    public static int TYPE_OUTGOING_CALL_NO_ANSWER = 2;
    public static int TYPE_INCOMING_CALL_ANSWERED = 3;
    public static int TYPE_IMCOMING_CALL_NO_ANSWER = 4;

    private Contact contact;
    private Date callTime;
    private int callDurationInSeconds;
    private int callType;

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
}
