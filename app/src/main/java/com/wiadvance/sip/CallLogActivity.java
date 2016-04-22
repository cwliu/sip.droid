package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class CallLogActivity extends AbstractToolbarContactActivity {

    private final List<Contact> mCallLogList = new ArrayList<>();
    private CallLogAdapter mRecyclerViewAdapter;

    public static Intent newIntent(Context context){
        return new Intent(context, CallLogActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRecyclerViewAdapter = new CallLogAdapter(this);

        setContentView(R.layout.activity_calllog);

        super.onCreate(savedInstanceState);
    }

    @Override
    int getLayout() {
        return R.layout.activity_calllog;
    }

    @Override
    public AbstractContactAdapter getRecyclerViewAdapter() {
        return mRecyclerViewAdapter;
    }

    class CallLogAdapter extends AbstractContactAdapter {

        public CallLogAdapter(Context context) {
            super(context);
            mCallLogList.addAll(ContactDbHelper.getInstance(CallLogActivity.this).getAllContacts());
            setContactList(mCallLogList);
        }

        @Override
        public int getLayout() {
            return R.layout.list_item_calllog;
        }
    }
}
