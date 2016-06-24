package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.linphone.LinphoneSipManager;
import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.office365.AuthenticationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;


public class ContactActivity extends SingleFragmentActivity {

    private String TAG = "ContactActivity";
    private BroadcastReceiver mNotificationReceiver;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANL_TOKEN);
        mixpanel.track(TAG, null);

    }

    @Override
    protected Fragment createFragment() {

        ContactFragment sipFragment = ContactFragment.newInstance();
        return sipFragment;
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSipAccounts();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, notify_filter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mNotificationReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    private void getSipAccounts() {
        OkHttpClient client = new OkHttpClient();
        OkHttpClient clientWith60sTimeout = client.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        FormBody body = new FormBody.Builder()
                .add("email", UserData.getEmail(this))
                .add("access_token", AuthenticationManager.getInstance().getAccessToken())
                .build();

        Request request = new Request.Builder()
                .url(BuildConfig.BACKEND_API_SERVER_SIP)
                .post(body)
                .build();

        clientWith60sTimeout.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");

                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(ContactActivity.this, "Backend error: " + response.body().string());
                    return;
                }

                String rawBodyString = response.body().string();
                SipApiResponse sip_data = new Gson().fromJson(rawBodyString, SipApiResponse.class);

                String sip_domain = sip_data.proxy_address + ":" + sip_data.proxy_port;

                LinphoneSipManager mWiSipManager = new LinphoneSipManager(ContactActivity.this);

                mWiSipManager.register(sip_data.sip_account, sip_data.sip_password, sip_domain);

                UserData.setBackendAccessToken(ContactActivity.this, sip_data.backend_access_token);

                UserData.setSip(ContactActivity.this, sip_data.sip_account);

                for (SipApiResponse.SipAccount acc : sip_data.sip_list) {
                    UserData.sEmailToPhoneHashMap.put(acc.email, acc.phone);
                    UserData.sEmailToSipHashMap.put(acc.email, acc.sip_account);
                }

                UserData.updateCompanyAccountData(ContactActivity.this);
                Intent intent = new Intent(NotificationUtil.ACTION_COMPANY_UPDATE_NOTIFICATION);
                sendBroadcast(intent);

                if(ContactTableHelper.getInstance(ContactActivity.this).getManualPhoneContacts().size() == 0) {
                    downloadManualContact();
                }
            }
        });
    }

    private void downloadManualContact() {
        OkHttpClient client = new OkHttpClient();
        OkHttpClient clientWith60sTimeout = client.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(BuildConfig.BACKEND_API_SERVER_CONACT + "?email=" + UserData.getEmail(this) + "&backend_access_token=" + UserData.getBackendAccessToken(this))
                .get()
                .build();

        clientWith60sTimeout.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
                NotificationUtil.displayStatus(ContactActivity.this, "Backend error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");
                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(ContactActivity.this, "Backend error: " + response.body().string());
                    return;
                }

                String rawString = response.body().string();


                try {
                    JSONArray contactList = new JSONArray(rawString);
                    for (int i = 0; i < contactList.length(); i++) {
                        JSONObject contact = (JSONObject) contactList.get(i);
                        String name = contact.getString("name");
                        JSONArray downloadPhoneList = contact.getJSONArray("phone_list");
                        List<String> phoneList = new ArrayList<>();
                        for(int j = 0; j < downloadPhoneList.length(); j++){
                            String phone = downloadPhoneList.getString(j);
                            phoneList.add(phone);
                        }

                        Contact c = new Contact(name);
                        c.setPhoneList(phoneList);
                        c.setType(Contact.TYPE_PHONE_MANUAL);
                        ContactTableHelper.getInstance(getApplicationContext()).addContact(c);
                    }

                    NotificationUtil.phoneContactUpdate(ContactActivity.this);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "NotificationReceiver, onReceive()");
            String message = intent.getStringExtra(NotificationUtil.GLOBAL_NOTIFY_MESSAGE);
            Toast.makeText(ContactActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }
}
