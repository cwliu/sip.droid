package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.FavoriteContactTableHelper;

public class FavoriteContactAdapter extends AbstractContactAdapter {

    public FavoriteContactAdapter(Context context) {
        super(context);

        setContactList(FavoriteContactTableHelper.getInstance(getContext()).getAll());
    }
}
