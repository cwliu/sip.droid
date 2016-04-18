package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactDbHelper;

public class RecentContactAdapter extends AbstractContactAdapter {
    public RecentContactAdapter(Context context) {
        super(context);

        setContactList(ContactDbHelper.getInstance(context).getRecentContacts());
    }
}
