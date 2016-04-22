package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;

import com.wiadvance.sip.model.Contact;

public class PhoneUtils {

    public static void call(Context context, Contact contact) {
        Intent intent = MakeCallActivity.newIntent(context, contact);
        context.startActivity(intent);
    }
}
