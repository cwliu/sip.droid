package com.wiadvance.sip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.wiadvance.sip.db.ContactTableHelper;
import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.model.UserRaw;
import com.wiadvance.sip.office365.AuthenticationManager;
import com.wiadvance.sip.office365.Constants;
import com.wiadvance.sip.office365.MSGraphAPIController;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CompanyContactFragment extends Fragment {

    private static final String TAG = "CompanyContactFragment";
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingProgress;
    private CompanyContactAdapter mAdapter;
    private NotificationReceiver mNotificationReceiver;


    public static Fragment newInstance() {
        return new CompanyContactFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.contacts_loading_progress_bar);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recyclerview);
        mAdapter = new CompanyContactAdapter(getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private void showLoading(boolean on) {
        if (on) {
            mLoadingProgress.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mLoadingProgress.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContactTableHelper.getInstance(getContext()).getCompanyContacts().size() == 0) {
            showLoading(true);
        }

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().connect(mAuthenticationCallback);
    }

    @Override
    public void onStart() {
        super.onStart();

        mNotificationReceiver = new NotificationReceiver();
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());

        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_COMPANY_UPDATE_NOTIFICATION);
        manager.registerReceiver(mNotificationReceiver, notify_filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mNotificationReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
            manager.unregisterReceiver(mNotificationReceiver);
        }
    }

    private void updateContactsSipPhone(List<Contact> list) {

        ContactTableHelper dbHelper = ContactTableHelper.getInstance(getContext());
        for (Contact c : list) {
            String email = c.getEmail();
            String phone = UserData.sEmailToPhoneHashMap.get(email);
            String sip = UserData.sEmailToSipHashMap.get(email);

            if (phone != null) {
                List<String> phoneList = new ArrayList<>();
                phoneList.add(phone);
                c.setPhoneList(phoneList);
            }

            if (sip != null) {
                c.setSip(sip);
            }
            c.setType(Contact.TYPE_COMPANY);
            dbHelper.updateCompanyContactByEmail(c);
        }

        refreshRecyclerView();
    }

    private void refreshRecyclerView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mAdapter != null){
                        mAdapter.setContactList(
                                ContactTableHelper.getInstance(getContext()).getCompanyContacts());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private AuthenticationCallback<AuthenticationResult> mAuthenticationCallback = new AuthenticationCallback<AuthenticationResult>() {
        @Override
        public void onSuccess(AuthenticationResult result) {
            //Need to get the new access token to the RESTHelper instance
            Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

            MSGraphAPIController.getInstance().showContacts(new Callback<UserRaw>() {
                @Override
                public void success(UserRaw userRaw, Response response) {

                    List<Contact> list = new ArrayList<>();
                    for (UserRaw.InnerDict user : userRaw.value) {
                        if (getContext() == null) {
                            break;
                        }

                        if (user.mail == null || user.mail.equals(UserData.getEmail(getContext()))) {
                            continue;
                        }

                        // TODO
                        Contact contact = new Contact(user.displayName, user.mail);
//                        Log.d(TAG, "user: " + user.displayName + ", mobilePhone: " + user.mobilePhone);
//                        for (String phone : user.businessPhones) {
//                            Log.d(TAG, "user: " + user.displayName + ", businessPhone: " + phone);
//                        }
                        String photoUrl = String.format(Constants.USER_PHOTO_URL_FORMAT, user.mail);
                        contact.setPhotoUri(Uri.parse(photoUrl));
                        list.add(contact);
                    }

                    updateContactsSipPhone(list);
                    UserData.updateCompanyAccountData(getContext());
                    showLoading(false);
                }

                @Override
                public void failure(RetrofitError error) {
                    NotificationUtil.displayStatus(getActivity(), "Microsoft Graph Server error: " + error.toString());
                    showLoading(false);
                }
            });
        }

        @Override
        public void onError(final Exception e) {
            Log.e(TAG, "onConnectButtonClick onError() - " + e.getMessage());
        }
    };

    class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshRecyclerView();
        }
    }
}
