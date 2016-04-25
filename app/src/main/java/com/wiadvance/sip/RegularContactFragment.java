package com.wiadvance.sip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wiadvance.sip.db.RegularContactTableHelper;

public class RegularContactFragment extends Fragment {

    private RegularContactAdapter mAdapter;

    public static Fragment newInstance() {
        return new RegularContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAdapter = new RegularContactAdapter(getActivity());
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mAdapter != null) {
            mAdapter.setContactList(RegularContactTableHelper.getInstance(getContext()).getRegularContactList());
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
