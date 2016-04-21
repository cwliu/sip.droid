package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wiadvance.sip.db.ContactDbHelper;
import com.wiadvance.sip.model.Contact;

import static android.widget.Toast.LENGTH_LONG;

public class AddContactActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, AddContactActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_contact_toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.app_name);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        Button button = (Button) findViewById(R.id.add_contact_create_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText nameEditText = (EditText) findViewById(R.id.add_contact_name_edittext);
                    EditText phoneEditText = (EditText) findViewById(R.id.add_contact_phone_edittext);

                    String name = nameEditText != null ? nameEditText.getText().toString() : null;
                    String phone = phoneEditText != null ? phoneEditText.getText().toString() : null;

                    if (name.equals("") || phone.equals("")) {
                        Toast.makeText(getApplicationContext(), "請輸入名稱與電話", LENGTH_LONG).show();
                        return;
                    }

                    Contact c = new Contact(name);
                    c.setPhone(phone);
                    c.setType(Contact.TYPE_PHONE_MANUAL);
                    ContactDbHelper.getInstance(getApplicationContext()).addContact(c);

                    Toast.makeText(getApplicationContext(), "已成功將 " + c.getName() + " 加到通訊錄", LENGTH_LONG).show();
                    finish();
                }
            });
        }
    }
}
