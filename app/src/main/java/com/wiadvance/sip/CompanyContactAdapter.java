package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactDbHelper;

public class CompanyContactAdapter extends AbstractContactAdapter {

    public CompanyContactAdapter(Context context) {
        super(context);
        setContactList(ContactDbHelper.getInstance(context).getCompanyContacts());
    }
}