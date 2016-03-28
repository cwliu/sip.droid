package com.wiadvance.sipdemo.office365;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wiadvance.sipdemo.R;
import com.wiadvance.sipdemo.model.Contact;

public class MakeCallActivity extends AppCompatActivity {


    private static final String ARG_CONTACT = "arg_contact";

    public static Intent newIntent(Context context, Contact contact) {
        Intent intent = new Intent(context, MakeCallActivity.class);
        intent.putExtra(ARG_CONTACT, new Gson().toJson(contact));
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_call);

        initView();
    }

    private void initView() {
        String json = getIntent().getStringExtra(ARG_CONTACT);
        Contact contact = (Contact) new Gson().fromJson(json, Contact.class);

        TextView name = (TextView) findViewById(R.id.callee_name);
//        (ImageView) findViewById(R.id.callee_avatar);

        name.setText(contact.getName());

        ImageButton endcall = (ImageButton) findViewById(R.id.make_call_end_call_button);
        endcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
