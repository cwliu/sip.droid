package com.wiadvance.sip;

import android.content.Context;

public class PhoneContactAdapter extends AbstractContactAdapter {

    public PhoneContactAdapter(Context context) {
        super(context);
        setContactList(UserData.sPhoneContactList);
    }
}
