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

import com.wiadvance.sip.db.FavoriteContactTableHelper;

public class FavoriteContactFragment extends Fragment {

    private NotificationReceiver mNotificationReceiver;

    public static Fragment newInstance() {
        return new FavoriteContactFragment();
    }

    private FavoriteContactAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new FavoriteContactAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mAdapter != null) {
            mAdapter.setContactList(FavoriteContactTableHelper.getInstance(getContext()).getAll());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_FAVORITE_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, notify_filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mNotificationReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mAdapter != null) {
                mAdapter.setContactList(FavoriteContactTableHelper.getInstance(getContext()).getAll());
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
