package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactDbHelper;

public class FavoriteContactAdapter extends AbstractContactAdapter {

    public FavoriteContactAdapter(Context context) {
        super(context);

        setContactList(ContactDbHelper.getInstance(context).getFavoriteContacts());
    }
}
