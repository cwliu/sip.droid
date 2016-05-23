package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.model.RecommendContact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SocialActivity extends AbstractToolbarContactActivity {

    private List<RecommendContact> mSocialContactList = new ArrayList<>();

    private static final String ARG_RECOMMEND_JSON_STRING = "ARG_RECOMMEND_JSON_STRING";
    private static final String ARG_CONTACT_NAME = "ARG_CONTACT_NAME";

    public static Intent newIntent(Context context, String jsonString, String contactName){
        Intent intent = new Intent(context, SocialActivity.class);
        intent.putExtra(ARG_RECOMMEND_JSON_STRING, jsonString);
        intent.putExtra(ARG_CONTACT_NAME, contactName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.recommend_contact_title);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        String rawString = getIntent().getStringExtra(ARG_RECOMMEND_JSON_STRING);
        String contactName = getIntent().getStringExtra(ARG_CONTACT_NAME);

        TextView messageTv = (TextView) findViewById(R.id.contact_name_message);
        assert messageTv != null;
        messageTv.setText(String.format(getString(R.string.contact_social_message), contactName));

        try{
            JSONObject responseJson = new JSONObject(rawString);
            JSONArray recommendList = responseJson.getJSONArray("recommend_list");
            for (int i = 0; i < recommendList.length(); i++) {
                Contact c = new Contact();
                JSONObject contact = (JSONObject) recommendList.get(i);
                String name = contact.getString("name");
                double percent = contact.getDouble("percent");
                JSONArray downloadPhoneList = contact.getJSONArray("phone_list");
                List<String> phoneList = new ArrayList<>();
                for(int j = 0; j < downloadPhoneList.length(); j++){
                    String phone = downloadPhoneList.getString(j);
                    phoneList.add(phone);
                }

                RecommendContact rc = new RecommendContact(name, percent, phoneList);
                mSocialContactList.add(rc);
            }

        } catch (JSONException exception){
            NotificationUtil.displayStatus(SocialActivity.this, "Json parse error");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_social_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.soical_close_button:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    int getLayout() {
        return R.layout.activity_social;
    }

    @Override
    public AbstractContactAdapter createRecyclerViewAdapter() {
        return new SocialContactAdapter(this);
    }

    class SocialContactAdapter extends AbstractContactAdapter {

        public SocialContactAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemCount() {
            return mSocialContactList.size();
        }

        @Override
        public int getLayout() {
            return R.layout.list_item_recommend;
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.bindRecommendContactViewHolder(mSocialContactList.get(position));
        }
    }
}
