package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wiadvance.sip.model.Contact;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class AddContactActivity extends AppCompatActivity {

    private static final String TAG = "AddContactActivity";

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_PHONE_LIST = "ARG_PHONE_LIST";
    private static final String ARG_THUMBNAIL = "ARG_THUMBNAIL";

    private static final List<EditText> phoneEditTextList = new ArrayList<>();

    public static Intent newIntent(Context context) {
        return new Intent(context, AddContactActivity.class);
    }

    public static Intent newIntent(Context context, String name, String phoneGson, String thumbnail) {
        Intent intent = new Intent(context, AddContactActivity.class);
        intent.putExtra(ARG_NAME, name);
        intent.putExtra(ARG_PHONE_LIST, phoneGson);
        intent.putExtra(ARG_THUMBNAIL, thumbnail);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        setupToolbar();
        setupViewEvents();
        setupPhoneField();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setupPhoneField() {
        phoneEditTextList.clear();

        String phoneGson = getIntent().getStringExtra(ARG_PHONE_LIST);
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        List<String> phoneList = new Gson().fromJson(phoneGson, type);
        if (phoneList != null) {
            for (String phone : phoneList) {
                addPhoneField(phone);
            }
        } else {
            addPhoneField("");
        }
    }

    private void setupViewEvents() {

        final EditText nameEditText = (EditText) findViewById(R.id.add_contact_name_edittext);
//        final EditText phoneEditText = (EditText) findViewById(R.id.add_contact_phone_edittext);
        final ImageButton addPhoneImageButton = (ImageButton) findViewById(R.id.add_contact_add_phone_image_button);
//        final GridLayout contactGridLayout = (GridLayout) findViewById(R.id.add_contact_gridlayout);
        final Button saveButton = (Button) findViewById(R.id.add_contact_create_button);
        Button deleteButton = (Button) findViewById(R.id.add_contact_delete_button);

        if (saveButton == null || deleteButton == null) {
            return;
        }

        setThumbnail();

        String name = getIntent().getStringExtra(ARG_NAME);
        if (nameEditText != null) {
            nameEditText.setText(name);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameEditText != null ? nameEditText.getText().toString() : "";
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.name_empty_error), LENGTH_LONG).show();
                    return;
                }

                if (phoneEditTextList.size() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.no_phone_input_error, LENGTH_LONG).show();
                    return;
                }

                String phone;
                List<String> phoneList = new ArrayList<>();
                for (EditText et : phoneEditTextList) {
                    if (et.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), R.string.phone_empty_error, LENGTH_SHORT).show();
                        return;
                    }
                    phone = et.getText().toString();
                    phoneList.add(phone);
                }

                Contact c = addContact(name, phoneList);

                Toast.makeText(getApplicationContext(), "已成功將 " + c.getName() + " 加到通訊錄", LENGTH_LONG).show();
                NotificationUtil.phoneContactUpdate(getApplicationContext());
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //noinspection ConstantConditions
        addPhoneImageButton.setOnClickListener(new View.OnClickListener() {
            public static final int VIEW_GROUP_KEY = 1;
            public static final int EDIT_TEXT_KEY = 2;

            @Override
            public void onClick(View v) {

                addPhoneField(null);
            }
        });
    }

    @NonNull
    private Contact addContact(String name, List<String> phoneList) {
        getBizSocialRecommendation(phoneList, name);
        return ContactUtils.addManualContact(this, name, phoneList);
    }

    private void getBizSocialRecommendation(List<String> phoneList, final String contactName) {

        OkHttpClient client = new OkHttpClient();
        OkHttpClient clientWith60sTimeout = client.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        String phoneListString = phoneList.get(0);
        for (int i = 1; i < phoneList.size(); i++) {
            phoneListString += "," + phoneList.get(i);
        }

        Request request = new Request.Builder()
                .url(BuildConfig.BACKEND_API_SERVER_BIZ_SOCIAL + "?email=" + UserData.getEmail(this)
                    + "&backend_access_token=" + UserData.getBackendAccessToken(this)
                    + "&phone_list=" + phoneListString
                )

                .get()
                .build();

        Log.d(TAG, "getBizSocialRecommendation start");

        clientWith60sTimeout.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
                NotificationUtil.displayStatus(AddContactActivity.this, "Backend error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, "getBizSocialRecommendation finished");

                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");
                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(AddContactActivity.this, "Backend error: " + response.body().string());
                    return;
                }

                String rawString = response.body().string();
                try {
                    JSONObject responseJson = new JSONObject(rawString);

                    int total_user_count = responseJson.getInt("total_user_count");
                    if(total_user_count > 0){
                        Intent intent = SocialActivity.newIntent(AddContactActivity.this, rawString, contactName);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    NotificationUtil.displayStatus(AddContactActivity.this, "JSONException: " + rawString);
                }
            }
        });
    }

    private void addPhoneField(String phone) {
        final LinearLayout phoneListLinearLayout = (LinearLayout) findViewById(R.id.phone_list_linear_layout);

        View phoneViewGroup = LayoutInflater.from(AddContactActivity.this).inflate(R.layout.list_item_phone_field, phoneListLinearLayout, false);
        //noinspection ConstantConditions
        phoneListLinearLayout.addView(phoneViewGroup);

        EditText phoneEditText = (EditText) phoneViewGroup.findViewById(R.id.list_item_phone_field_edittext);
        phoneEditText.setText(phone);
        View removeButton = phoneViewGroup.findViewById(R.id.list_item_phone_field_remove);
        removeButton.setTag(R.string.tag_viewgroup, phoneViewGroup);
        removeButton.setTag(R.string.tag_edittext, phoneEditText);

        phoneEditTextList.add(phoneEditText);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout viewGroup = (LinearLayout) v.getTag(R.string.tag_viewgroup);
                EditText editText = (EditText) v.getTag(R.string.tag_edittext);

                phoneEditTextList.remove(editText);
                phoneListLinearLayout.removeView(viewGroup);
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_contact_toolbar);
        if (toolbar != null) {
            toolbar.setTitle("編輯聯格人");
            setSupportActionBar(toolbar);
            //noinspection ConstantConditions
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setThumbnail() {
        String imagePath = getIntent().getStringExtra(ARG_THUMBNAIL);
        if (imagePath == null) {
            return;
        }

        ImageView imageView = (ImageView) findViewById(R.id.namecard_thumbnail_image_view);
        if (imageView == null) {
            return;
        } else {
            imageView.setVisibility(View.VISIBLE);
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 5;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }
}
