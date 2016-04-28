package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class AddContactActivity extends AppCompatActivity {

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_PHONE = "ARG_PHONE";

    private static final List<View> phoneEditTextList = new ArrayList<>();

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

    private void setupViewEvents() {

        final EditText nameEditText = (EditText) findViewById(R.id.add_contact_name_edittext);
//        final EditText phoneEditText = (EditText) findViewById(R.id.add_contact_phone_edittext);
        final ImageButton addPhoneImageButton = (ImageButton) findViewById(R.id.add_contact_add_phone_image_button);
        final GridLayout contactGridLayout = (GridLayout) findViewById(R.id.add_contact_gridlayout);
        final LinearLayout phoneListLinearLayout = (LinearLayout) findViewById(R.id.phone_list_linear_layout);

        if (contactGridLayout == null || phoneListLinearLayout == null) {
            return;
        }

        Button saveButton = (Button) findViewById(R.id.add_contact_create_button);

        String name = getIntent().getStringExtra(ARG_NAME);
        if (nameEditText != null) {
            nameEditText.setText(name);
        }
//        String phone = getIntent().getStringExtra(ARG_PHONE);
//        if (phoneEditText != null) {
//            phoneEditText.setText(phone);
//        }
//
//        if (saveButton != null) {
//            saveButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    String name = nameEditText != null ? nameEditText.getText().toString() : "";
//                    String phone = phoneEditText != null ? phoneEditText.getText().toString() : "";
//
//                    if (name.equals("") || phone.equals("")) {
//                        Toast.makeText(getApplicationContext(), "請輸入名稱與電話", LENGTH_LONG).show();
//                        return;
//                    }
//
//                    Contact c = new Contact(name);
//                    c.setPhone(phone);
//                    c.setType(Contact.TYPE_PHONE_MANUAL);
//                    ContactTableHelper.getInstance(getApplicationContext()).addContact(c);
//
//                    Toast.makeText(getApplicationContext(), "已成功將 " + c.getName() + " 加到通訊錄", LENGTH_LONG).show();
//
//                    NotificationUtil.phoneContactUpdate(getApplicationContext());
//                    finish();
//                }
//            });
//        }


        //noinspection ConstantConditions
        addPhoneImageButton.setOnClickListener(new View.OnClickListener() {
            public static final int VIEW_GROUP_KEY = 1;
            public static final int EDIT_TEXT_KEY = 2;

            @Override
            public void onClick(View v) {

                View phoneViewGroup = LayoutInflater.from(AddContactActivity.this).inflate(R.layout.list_item_phone_field, phoneListLinearLayout, false);
                phoneListLinearLayout.addView(phoneViewGroup);

                EditText phoneEditText = (EditText) phoneViewGroup.findViewById(R.id.list_item_phone_field_edittext);
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
        });
    }

    private void addPhoneField(GridLayout contactGridLayout) {
        GridLayout.Spec rowSpan1 = GridLayout.spec(GridLayout.UNDEFINED, 1);
        GridLayout.Spec colspan1 = GridLayout.spec(GridLayout.UNDEFINED, 1);
        GridLayout.Spec colspan2 = GridLayout.spec(GridLayout.UNDEFINED, 2);
        GridLayout.Spec colspan3 = GridLayout.spec(GridLayout.UNDEFINED, 3);

        EditText et = new EditText(AddContactActivity.this);
        GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(rowSpan1, colspan3);
        et.setLayoutParams(gridParam);
        et.setHint(R.string.hint_phone_numbers);
        et.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        et.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        contactGridLayout.addView(et);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_contact_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
        }
    }
}
