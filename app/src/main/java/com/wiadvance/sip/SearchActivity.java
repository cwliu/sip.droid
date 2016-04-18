package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private String TAG = "SearchActivity";

    private SearchContactAdapter mRecyclerViewAdapter;
    private List<Contact> mSearchResultList = new ArrayList<>();

    public static Intent newIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.search_activity_toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.search_activity_contacts_recycler_view);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerViewAdapter = new SearchContactAdapter(this);
            recyclerView.setAdapter(mRecyclerViewAdapter);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search_view);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

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
        for (Contact c : UserData.getAllContact(this)) {
            if (c.getName().toLowerCase().contains(text.toLowerCase())) {
                mSearchResultList.add(c);
            }
        }
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    class SearchContactAdapter extends AbstractContactAdapter {

        public SearchContactAdapter(Context context) {
            super(context);
            mSearchResultList.addAll(UserData.getAllContact(SearchActivity.this));
            setContactList(mSearchResultList);
        }
    }
}
