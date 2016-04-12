package com.wiadvance.sipdemo;

import android.content.Context;

public class RecentContactAdapter extends AbstractContactAdapter {
    public RecentContactAdapter(Context context) {
        super(context);

        setContactList(UserData.getRecentContactList(getContext()));
    }
}
