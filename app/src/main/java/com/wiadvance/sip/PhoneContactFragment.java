package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wiadvance.sip.db.ContactTableHelper;

public class PhoneContactFragment extends Fragment {

    private PhoneContactAdapter mContactAdapter;
    private NotificationReceiver mNotificationReceiver;
    private View mProgressBar;

    public static Fragment newInstance() {
        return new PhoneContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mProgressBar = rootView.findViewById(R.id.contacts_loading_progress_bar);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mContactAdapter = new PhoneContactAdapter(getActivity());
        recyclerView.setAdapter(mContactAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContactTableHelper.getInstance(getContext()).getPhoneContacts().size() == 0) {
            setLoadProgressbar(View.VISIBLE);
        }

        Intent intent = FetchPhoneContactService.newIntent(getContext());
        getContext().startService(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_PHONE_CONTACT_LOAD_COMPLETE);
        manager.registerReceiver(mNotificationReceiver, notify_filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mNotificationReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mContactAdapter != null) {
                mContactAdapter.setContactList(
                        ContactTableHelper.getInstance(getContext()).getPhoneContacts());
                mContactAdapter.notifyDataSetChanged();

                setLoadProgressbar(View.GONE);
            }
        }
    }

    private void setLoadProgressbar(int visible) {
        mProgressBar.setVisibility(visible);
    }
}
