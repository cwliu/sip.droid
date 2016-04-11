package com.wiadvance.sipdemo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CompanyContactAdapter extends RecyclerView.Adapter<ContactHolder> {

    private final Context mContext;

    public CompanyContactAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rootView = LayoutInflater.from(mContext).inflate(R.layout.list_item_contact, parent, false);
        return new ContactHolder(mContext, rootView);
    }

    @Override
    public void onBindViewHolder(ContactHolder holder, int position) {
        holder.bindViewHolder(UserData.sCompanyContactList.get(position));
    }

    @Override
    public int getItemCount() {
        return UserData.sCompanyContactList.size();
    }
}