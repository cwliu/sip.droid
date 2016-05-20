package com.wiadvance.sip.model;

import java.util.Date;

public class RegularContact {

    private Contact mContact;
    private int count;
    private Date updatedTime;
    private int mId;

    public RegularContact(Contact contact) {
        mContact = contact;
        count = 1;
        updatedTime = new Date();
    }

    public Contact getContact() {
        return mContact;
    }

    public void setContact(Contact contact) {
        mContact = contact;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updated_time) {
        this.updatedTime = updated_time;
    }

    public void setId(int id) {
        mId = id;
    }
}
