package com.wiadvance.sipdemo;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ContactPagerAdapter extends FragmentPagerAdapter {
    public ContactPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = CompanyContactFragment.newInstance();
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "聯絡人";
            case 1:
                return "公司通訊";

            default:
                return "N/A";
        }
    }
}
