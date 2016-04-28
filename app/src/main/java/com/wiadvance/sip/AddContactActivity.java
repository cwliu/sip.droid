package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

public class AddContactActivity extends AppCompatActivity {

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_PHONE = "ARG_PHONE";

    private static final List<EditText> phoneEditTextList = new ArrayList<>();

    public static Intent newIntent(Context context) {
        return new Intent(context, AddContactActivity.class);
    }

    public static Intent newIntent(Context context, String name, String phone) {
        Intent intent = new Intent(context, AddContactActivity.class);
        intent.putExtra(ARG_NAME, name);
        intent.putExtra(ARG_PHONE, phone);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        setupToolbar();
        setupViewEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();

        phoneEditTextList.clear();

        String phone = getIntent().getStringExtra(ARG_PHONE);
        addPhoneField(phone);
    }

    private void setupViewEvents() {

        final EditText nameEditText = (EditText) findViewById(R.id.add_contact_name_edittext);
//        final EditText phoneEditText = (EditText) findViewById(R.id.add_contact_phone_edittext);
        final ImageButton addPhoneImageButton = (ImageButton) findViewById(R.id.add_contact_add_phone_image_button);
        final GridLayout contactGridLayout = (GridLayout) findViewById(R.id.add_contact_gridlayout);
        final Button saveButton = (Button) findViewById(R.id.add_contact_create_button);
        Button deleteButton = (Button) findViewById(R.id.add_contact_delete_button);

        if (contactGridLayout == null || saveButton == null || deleteButton == null) {
            return;
        }

        String name = getIntent().getStringExtra(ARG_NAME);
        if (nameEditText != null) {
            nameEditText.setText(name);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameEditText != null ? nameEditText.getText().toString() : "";
                List<String> phoneList = new ArrayList<String>();
                if (name.equals("")) {
                    Toast.makeText(getApplicationContext(), getString(R.string.name_empty_error), LENGTH_LONG).show();
                    return;
                }

                if (phoneEditTextList.size() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.no_phone_input_error, LENGTH_LONG).show();
                    return;
                }

                String phone = null;
                for (EditText et : phoneEditTextList) {
                    if (et.getText().toString().equals("")) {
                        Toast.makeText(getApplicationContext(), R.string.phone_empty_error, LENGTH_SHORT).show();
                        return;
                    }
                    phone = et.getText().toString();
                }

                Contact c = new Contact(name);
                c.setPhone(phone);
                c.setType(Contact.TYPE_PHONE_MANUAL);
                ContactTableHelper.getInstance(getApplicationContext()).addContact(c);

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

    private void addPhoneField(String phone) {
        final LinearLayout phoneListLinearLayout = (LinearLayout) findViewById(R.id.phone_list_linear_layout);

        View phoneViewGroup = LayoutInflater.from(AddContactActivity.this).inflate(R.layout.list_item_phone_field, phoneListLinearLayout, false);
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
