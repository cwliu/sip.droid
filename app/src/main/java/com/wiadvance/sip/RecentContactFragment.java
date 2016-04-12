package com.wiadvance.sip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecentContactFragment extends Fragment {

    private RecentContactAdapter mAdapter;

    public static Fragment newInstance() {
        return new RecentContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        recyclerView.setLayoutManager(new WiLinearLayoutManager(getActivity()));

        mAdapter = new RecentContactAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mAdapter != null) {
            mAdapter.setContactList(UserData.getRecentContactList(getContext()));
            mAdapter.notifyDataSetChanged();
        }
    }
}
