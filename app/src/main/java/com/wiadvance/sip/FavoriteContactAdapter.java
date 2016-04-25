package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.ContactTableHelper;

public class FavoriteContactAdapter extends AbstractContactAdapter {

    public FavoriteContactAdapter(Context context) {
        super(context);

        setContactList(ContactTableHelper.getInstance(context).getFavoriteContacts());
    }
}
