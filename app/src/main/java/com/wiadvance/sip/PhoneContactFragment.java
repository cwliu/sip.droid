package com.wiadvance.sip;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class PhoneContactFragment extends Fragment {

    private PhoneContactAdapter mContactAdapter;

    public static Fragment newInstance() {
        return new PhoneContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        if(UserData.sPhoneContactList.size() == 0){
            UserData.sPhoneContactList = getPhoneContacts(getContext());
        }

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mContactAdapter = new PhoneContactAdapter(getActivity());
        mContactAdapter.setContactList(UserData.sPhoneContactList);
        recyclerView.setAdapter(mContactAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private List<Contact> getPhoneContacts(Context context) {

        List<Contact> list = new ArrayList<>();

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
                        list.add(c);
                    }
                } finally {
                    phones.close();
                }
            }
        }

        return list;
    }

    public Uri getPhotoUri(String id) {
        Cursor cur = null;
        try {
            cur = getContext().getContentResolver().query(
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
        } finally {
            if (cur != null) {
                cur.close();
            }
        }
        Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long
                .parseLong(id));
        return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
    }
}
