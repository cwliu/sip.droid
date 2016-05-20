package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactTableHelper;

public class CompanyContactAdapter extends AbstractContactAdapter {

    public CompanyContactAdapter(Context context) {
        super(context);
        setContactList(ContactTableHelper.getInstance(context).getCompanyContacts());
    }
}