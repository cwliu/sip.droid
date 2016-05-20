package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactTableHelper;

public class PhoneContactAdapter extends AbstractContactAdapter {

    public PhoneContactAdapter(Context context) {
        super(context);
        setContactList(ContactTableHelper.getInstance(context).getPhoneContacts());
    }
}
