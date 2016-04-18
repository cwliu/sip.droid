package com.wiadvance.sip.db;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.wiadvance.sip.model.Contact;

public class ContactCursorWrapper extends CursorWrapper {
    public ContactCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Contact getContact(){

        if (getCount() == 0) {
            return null;
        }

        String name = getString(getColumnIndex(ContactDbSchema.ContactTable.Cols.NAME));
        String sip = getString(getColumnIndex(ContactDbSchema.ContactTable.Cols.SIP));
        String phone = getString(getColumnIndex(ContactDbSchema.ContactTable.Cols.PHONE));
        String email = getString(getColumnIndex(ContactDbSchema.ContactTable.Cols.EMAIL));
        String photo = getString(getColumnIndex(ContactDbSchema.ContactTable.Cols.PHOTO));
        Integer type = getInt(getColumnIndex(ContactDbSchema.ContactTable.Cols.TYPE));

        Contact contact = new Contact(name);
        contact.setEmail(email);
        contact.setSip(sip);
        contact.setPhone(phone);
        contact.setPhotoUri(photo);
        contact.setType(type);

        return contact;
    }
}
