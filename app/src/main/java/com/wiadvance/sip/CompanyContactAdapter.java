package com.wiadvance.sip;

import android.content.Context;

public class CompanyContactAdapter extends AbstractContactAdapter {

    public CompanyContactAdapter(Context context) {
        super(context);
        setContactList(UserData.sCompanyContactList);
    }
}