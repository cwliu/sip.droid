package com.wiadvance.sip.model;

import android.content.Context;
import android.net.Uri;

import com.wiadvance.sip.db.FavoriteContactTableHelper;

import java.util.ArrayList;
import java.util.List;

public class Contact {

    private String mName;
    private String mSip;
    private List<String> mPhoneList = new ArrayList<>();
    private String mPreferredPhone;
    private String mEmail;
    private String mPhotoUri;

    private String mAndroidContactId;
    private int mType;
    private int mId;

    public static int TYPE_COMPANY = 1;
    public static int TYPE_PHONE = 2;
    public static int TYPE_PHONE_MANUAL = 3;
    public static int TYPE_EXTERNAL = 4;


    public Contact(){
        this(null, null);
    }
    public Contact(String name) {
        this(name, null);
    }

    public Contact(String name, String email) {
        mName = name;
        mEmail = email;
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

    public void setPhoneList(List<String> list) {
        mPhoneList = list;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public void setPhotoUri(Uri photoUri) {
        mPhotoUri = photoUri.toString();
    }

    public void setPhotoUri(String photoUri) {
        mPhotoUri = photoUri;
    }

    public String getName() {
        return mName;
    }

    public List<String> getPhoneList() {
        return mPhoneList;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPhotoUri() {
        return mPhotoUri;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getAndroidContactId() {
        return mAndroidContactId;
    }

    public void setAndroidContactId(String androidContactId) {
        mAndroidContactId = androidContactId;
    }

    public boolean isFavorite(Context context) {
        return FavoriteContactTableHelper.getInstance(context).isFavoriteContact(this);
    }

    @Override
    public boolean equals(Object o) {

        boolean isEqual = true;

        if (!(o instanceof Contact)) {
            return false;
        }

        Contact rhs = (Contact) o;

        if (getName() != null && (rhs.getName() == null || !getName().equals(rhs.getName()))) {
            isEqual = false;
        }

        if (getEmail() != null && (rhs.getEmail() == null || !getEmail().equals(rhs.getEmail()))) {
            isEqual = false;
        }

        if (getSip() != null && (rhs.getSip() == null || !getSip().equals(rhs.getSip()))) {
            isEqual = false;
        }

//        if (getPhone() != null && (rhs.getPhone() == null || !getPhone().equals(rhs.getPhone()))) {
//            isEqual = false;
//        }

        return isEqual;
    }

    public String getPreferredPhone() {
        return mPreferredPhone;
    }

    public void setPreferredPhone(String preferredPhone) {
        mPreferredPhone = preferredPhone;
    }
}
