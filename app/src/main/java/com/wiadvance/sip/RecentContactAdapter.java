package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactTableHelper;

public class RecentContactAdapter extends AbstractContactAdapter {
    public RecentContactAdapter(Context context) {
        super(context);

        setContactList(ContactTableHelper.getInstance(context).getRecentContacts());
    }
}
