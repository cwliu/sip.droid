package com.wiadvance.sipdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PhoneContactFragment extends Fragment {

    private RecyclerView mRecyclerView;

    public static Fragment newInstance() {
        return new PhoneContactFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new WiLinearLayoutManager(getActivity()));

        mRecyclerView.setAdapter(new PhoneContactAdapter(getActivity()));

        return rootView;
    }


}
