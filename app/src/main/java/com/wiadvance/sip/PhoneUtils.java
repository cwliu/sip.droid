package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;

import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

public class PhoneUtils {

    public static void call(Context context, Contact contact) {
        Intent intent = MakeCallActivity.newIntent(context, contact);
        context.startActivity(intent);
    }

    public static Contact getCompanyContactByAccount(Context context, String account) {

        for (Contact c : ContactTableHelper.getInstance(context).getAllContacts()) {
            String sipUsername = c.getSip();
            if (sipUsername != null && account.equals(sipUsername)) {
                return c;
            }
        }
        return null;
    }

    public static String normalizedPhone(String phone) {
        return phone != null ? phone.replace(" ", "").replace("+886", "0") : null;
    }
}
