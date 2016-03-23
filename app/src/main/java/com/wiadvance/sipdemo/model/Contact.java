package com.wiadvance.sipdemo.model;

public class Contact {

    private String mName;
    private String mSip;
    private String mPhone;

    public Contact(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getSip() {
        return mSip;
    }

    public void setSip(String sip) {
        mSip = sip;
    }

    public void setPhone(String phone){
        mPhone = phone;
    }
}
