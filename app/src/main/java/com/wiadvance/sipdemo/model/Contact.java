package com.wiadvance.sipdemo.model;

import android.net.Uri;

public class Contact {

    private String mName;
    private String mSip;
    private String mPhone;
    private String mEmail;
    private String mPhotoUri;

    public Contact(String name) {
        this(name, null);
    }

    public Contact(String name, String email) {
        mName = name;
        mEmail = email;
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

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setPhotoUri(Uri photoUri) {
        mPhotoUri = photoUri.toString();
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }
}
