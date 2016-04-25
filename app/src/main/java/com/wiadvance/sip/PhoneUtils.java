package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.Contact;

public class PhoneUtils {

    public static void call(Context context, Contact contact) {
        Intent intent = MakeCallActivity.newIntent(context, contact);
        context.startActivity(intent);
    }

    public static Contact getCompanyContactByAccount(Context context, String account) {

        for (Contact c : ContactDbHelper.getInstance(context).getAllContacts()) {
            String sipUsername = c.getSip();
            if (sipUsername != null && account.equals(sipUsername)) {
                return c;
            }

            String phone = c.getPhone();
            Log.d("PhoneUtils", "account = [" + account + "]" + "phone = [" + phone + "]");
            if (phone != null && account.equals(phone)) {
                return c;
            }
        }
        return null;
    }

    public static String normalizedPhone(String phone) {
        return phone != null ? phone.replace(" ", "").replace("+886", "0") : null;
    }
}
