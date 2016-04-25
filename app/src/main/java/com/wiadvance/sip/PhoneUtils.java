package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;

import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.Contact;

import org.linphone.LinphoneUtils;

public class PhoneUtils {

    public static void call(Context context, Contact contact) {
        Intent intent = MakeCallActivity.newIntent(context, contact);
        context.startActivity(intent);
    }

    public static Contact getCompanyContactBySipAddress(Context context, String inputSipAddress) {
        for (Contact c : ContactDbHelper.getInstance(context).getCompanyContacts()) {
            String targetUsername = c.getSip();
            String inputUsername = LinphoneUtils.getUsernameFromAddress(inputSipAddress);

            if (inputUsername.equals(targetUsername)) {
                return c;
            }
        }

        return null;
    }
}
