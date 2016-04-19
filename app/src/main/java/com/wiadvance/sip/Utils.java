package com.wiadvance.sip;

import android.content.Context;

public class Utils {

    public static int getDeviceScale(Context context){
       return (int) context.getResources().getDisplayMetrics().density;
    }
}
