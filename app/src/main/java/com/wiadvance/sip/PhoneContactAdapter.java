package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactDbHelper;

public class PhoneContactAdapter extends AbstractContactAdapter {

    public PhoneContactAdapter(Context context) {
        super(context);
        setContactList(ContactDbHelper.getInstance(context).getPhoneContacts());
    }
}
