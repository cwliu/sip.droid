package com.wiadvance.sip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContactPagerAdapter extends FragmentPagerAdapter {
    public ContactPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return FavoriteContactFragment.newInstance();
            case 1:
                return RegularContactFragment.newInstance();
            case 2:
                return PhoneContactFragment.newInstance();
            default:
                return CompanyContactFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return "我的最愛";

            case 1:
                return "經常聯絡";

            case 2:
                return "聯絡人";

            case 3:
                return "公司通訊";

            default:
                return "N/A";
        }
    }
}
