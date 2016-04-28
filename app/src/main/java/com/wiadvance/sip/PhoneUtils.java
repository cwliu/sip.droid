package com.wiadvance.sip;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

import java.util.List;

public class PhoneUtils {

    public static void call(final Context context, final Contact contact) {
        final Intent intent = MakeCallActivity.newIntent(context, contact);

        if (contact.getType() == Contact.TYPE_PHONE || contact.getType() == Contact.TYPE_PHONE_MANUAL) {

            List<String> list = contact.getPhoneList();
            if (list.size() > 1) {

                final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.select_dialog_item);
                for (String phone : list) {
                    adapter.add(phone);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.msg_please_choose_phone))
                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String phone = adapter.getItem(which);
                                contact.setPreferredPhone(phone);
                                Intent intent = MakeCallActivity.newIntent(context, contact);
                                context.startActivity(intent);
                            }
                        });
                builder.create().show();
            } else {
                context.startActivity(intent);
            }
        } else {
            context.startActivity(intent);
        }
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
