package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.wiadvance.sip.db.CallLogTableHelper;
import com.wiadvance.sip.model.CallLogEntry;

import java.util.List;

public class CallLogActivity extends AbstractToolbarContactActivity {

    private List<CallLogEntry> mCallLogList;

    public static Intent newIntent(Context context) {
        return new Intent(context, CallLogActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle("History");
        }

        mCallLogList = CallLogTableHelper.getInstance(this).getAllCallLogs();
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
