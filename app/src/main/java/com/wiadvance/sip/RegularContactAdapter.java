package com.wiadvance.sip;

import android.content.Context;

import com.wiadvance.sip.db.RegularContactTableHelper;

public class RegularContactAdapter extends AbstractContactAdapter {
    public RegularContactAdapter(Context context) {
        super(context);

        setContactList(RegularContactTableHelper.getInstance(context).getRegularContactList());
    }
}
