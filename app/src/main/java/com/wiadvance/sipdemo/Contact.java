package com.wiadvance.sipdemo;

public class Contact {

    private String mName;
    private String mSip;

    public Contact(String name, String sip) {
        mName = name;
        mSip = sip;
    }

    public String getName() {
        return mName;
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
}
