package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AbstractToolbarContactActivity {

    private List<Contact> mSearchResultList = new ArrayList<>();
    private SearchContactAdapter mRecyclerViewAdapter;

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mRecyclerViewAdapter = new SearchContactAdapter(this);

        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
        searchView.requestFocusFromTouch();

//        // Remove search icon
//        ImageView searchImage = (ImageView) searchView.findViewById(
//                android.support.v7.appcompat.R.id.search_mag_icon);
//        searchImage.setVisibility(View.GONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                query(newText);
                return true;
            }
        });

        return true;
    }

    private void query(String text) {

        mSearchResultList.clear();
        for (Contact c : ContactTableHelper.getInstance(this).getAllContacts()) {
            if (c.getName().toLowerCase().contains(text.toLowerCase())) {
                mSearchResultList.add(c);
            }
        }
        createRecyclerViewAdapter().notifyDataSetChanged();
    }

    @Override
    int getLayout() {
        return R.layout.activity_search;
    }

    @Override
    public AbstractContactAdapter createRecyclerViewAdapter() {
        return mRecyclerViewAdapter;
    }

    class SearchContactAdapter extends AbstractContactAdapter {

        public SearchContactAdapter(Context context) {
            super(context);
            mSearchResultList.addAll(ContactTableHelper.getInstance(SearchActivity.this).getAllContacts());
            setContactList(mSearchResultList);
        }
    }
}
