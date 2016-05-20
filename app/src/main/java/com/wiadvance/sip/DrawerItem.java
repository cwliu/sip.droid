package com.wiadvance.sip;

import android.support.annotation.DrawableRes;

public class DrawerItem {
    private String mName;
    private int mIcon;


    public DrawerItem(String name, @DrawableRes int icon) {
        this.mName = name;
        this.mIcon = icon;
    }

    public String getName() {
        return mName;
    }

    public int getIcon() {
        return mIcon;
    }
}
