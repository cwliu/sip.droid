package com.wiadvance.sip;

import android.content.Context;

public class FavoriteContactAdapter extends AbstractContactAdapter {

    public FavoriteContactAdapter(Context context) {
        super(context);

        setContactList(UserData.getFavorateContactList(getContext()));
    }
}
