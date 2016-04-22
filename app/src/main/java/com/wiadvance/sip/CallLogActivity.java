package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.CallLogEntry;

import java.util.ArrayList;
import java.util.List;

public class CallLogActivity extends AbstractToolbarContactActivity {

    private final List<CallLogEntry> mCallLogList = new ArrayList<>();
    private CallLogAdapter mRecyclerViewAdapter;

    public static Intent newIntent(Context context){
        return new Intent(context, CallLogActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CallLogEntry call = new CallLogEntry();
        call.contact = ContactDbHelper.getInstance(this).getPhoneContacts().get(0);
        mCallLogList.add(call);
        mCallLogList.add(call);
        mCallLogList.add(call);
        mCallLogList.add(call);
        mCallLogList.add(call);

    }

    @Override
    int getLayout() {
        return R.layout.activity_calllog;
    }

    @Override
    public AbstractContactAdapter createRecyclerViewAdapter() {
        return new CallLogAdapter(this);
    }

    class CallLogAdapter extends AbstractContactAdapter {

        public CallLogAdapter(Context context) {
            super(context);
        }

        @Override
        public int getLayout() {
            return R.layout.list_item_calllog;
        }

        @Override
        public int getItemCount() {
            return mCallLogList.size();
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.bindCallLogContactViewHolder(mCallLogList.get(position));
        }
    }
}
