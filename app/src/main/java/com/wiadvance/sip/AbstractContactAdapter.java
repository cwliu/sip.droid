package com.wiadvance.sip;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wiadvance.sip.model.Contact;

import java.util.List;

abstract public class AbstractContactAdapter extends RecyclerView.Adapter<ContactHolder> {

    private final Context mContext;
    private List<Contact> mContactList;

    public AbstractContactAdapter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public List<Contact> getContactList() {
        return mContactList;
    }

    public void setContactList(List<Contact> contactList) {
        mContactList = contactList;
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(getLayout(), parent, false);
        return new ContactHolder(mContext, rootView);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.bindContactViewHolder(getContactList().get(position));
    }

    @Override
    public int getItemCount() {
        return getContactList().size();
    }

    public int getLayout() {
        return R.layout.list_item_contact;
    }
}
