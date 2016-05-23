package com.wiadvance.sip;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ContactUtils {

    private static final String TAG = "ContactUtils";

    @NonNull
    public static Contact addManualContact(Context context, String name, List<String> phoneList) {
        Contact c = new Contact(name);
        c.setPhoneList(phoneList);
        c.setType(Contact.TYPE_PHONE_MANUAL);
        ContactTableHelper.getInstance(context).addContact(c);
        addContactToBackend(context, phoneList, c);
        return c;
    }

    @NonNull
    private static FormBody addContactToBackend(final Context context, List<String> phoneList, Contact c) {
        // Add this contact to backend
        OkHttpClient client = new OkHttpClient();
        OkHttpClient clientWith60sTimeout = client.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        FormBody body = new FormBody.Builder()
                .add("email", UserData.getEmail(context))
                .add("backend_access_token", UserData.getBackendAccessToken(context))
                .add("contact_name", c.getName())
                .add("contact_phone_list", new Gson().toJson(phoneList))
                .build();

        Request request = new Request.Builder()
                .url(BuildConfig.BACKEND_API_SERVER_CONACT)
                .post(body)
                .build();


        Log.d(TAG, "addContactToBackend start");
        clientWith60sTimeout.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
                NotificationUtil.displayStatus(context, "Backend error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, "addContactToBackend finished");
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");

                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(context, "Backend error: " + response.body().string());
                    return;
                }
            }
        });
        return body;
    }
}
