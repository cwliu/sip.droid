package com.wiadvance.sipdemo;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wiadvance.sipdemo.model.Contact;

public class PhoneContactAdapter extends RecyclerView.Adapter<ContactHolder> {

    private static final String TAG = "PhoneContactAdapter";
    private final Context mContext;

    public PhoneContactAdapter(Context context) {
        mContext = context;

        String orderBy = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, orderBy);
        if (phones != null) {
            {
                int i = 1;
                String lastName = "";
                try {
                    while (phones.moveToNext()) {
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        if (name.equals(lastName)) {
                            name = lastName + "-" + ++i;
                        } else {
                            lastName = name;
                            i = 1;
                        }

                        String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Contact c = new Contact(name);
                        c.setPhone(phoneNumber);
                        Uri uri = getPhotoUri(phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
                        if (uri != null) {
                            c.setPhotoUri(uri);
                        }
                        UserData.sPhoneContactList.add(c);
                    }
                } finally {
                    phones.close();
                }
            }
        }
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.list_item_contact, parent, false);
        return new ContactHolder(mContext, rootView);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.bindViewHolder(UserData.sPhoneContactList.get(position));
    }

    @Override
    public int getItemCount() {
        return UserData.sPhoneContactList.size();
    }

    public Uri getPhotoUri(String id) {
        try {
            Cursor cur = mContext.getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.CONTACT_ID + "=" + id + " AND "
                            + ContactsContract.Data.MIMETYPE + "='"
                            + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                    null);
            if (cur != null) {
                if (!cur.moveToFirst()) {
                    return null; // no photo
                }
            } else {
                return null; // error in cursor process
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                .parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}
