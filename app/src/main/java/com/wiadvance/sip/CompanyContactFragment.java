package com.wiadvance.sip;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.wiadvance.sip.model.Contact;
import com.wiadvance.sip.model.UserRaw;
import com.wiadvance.sip.office365.AuthenticationManager;
import com.wiadvance.sip.office365.Constants;
import com.wiadvance.sip.office365.MSGraphAPIController;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CompanyContactFragment extends Fragment {

    private static final String TAG = "CompanyContactFragment";
    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingProgress;

    public static Fragment newInstance(){
        CompanyContactFragment fragment = new CompanyContactFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

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

        if (UserData.sCompanyContactList.size() == 0) {
            showLoading(true);
        }

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().connect(mAuthenticationCallback);
    }

    private void refreshContacts() {
        for (Contact c : UserData.sCompanyContactList) {
            String email = c.getEmail();
            String phone = UserData.sEmailtoPhoneBiMap.get(email);
            String sip = UserData.sEmailtoSipBiMap.get(email);

            if (phone != null) {
                c.setPhone(phone);
            }

            if (sip != null) {
                c.setSip(sip);
            }
        }

        if(getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mRecyclerView.getAdapter() != null) {
                        mRecyclerView.getAdapter().notifyDataSetChanged();
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

                    UserData.sCompanyContactList.clear();
                    for (UserRaw.InnerDict user : userRaw.value) {
                        if (user.mail == null || user.mail.equals(UserData.getEmail(getActivity()))) {
                            continue;
                        }

                        // TODO
                        Contact contact = new Contact(user.displayName, user.mail);
                        Log.d(TAG, "user: " + user.displayName + ", mobilePhone: " + user.mobilePhone);
                        for (String phone : user.businessPhones) {
                            Log.d(TAG, "user: " + user.displayName + ", businessPhone: " + phone);
                        }
                        String photoUrl = String.format(Constants.USER_PHOTO_URL_FORMAT, user.mail);
                        contact.setPhotoUri(Uri.parse(photoUrl));
                        UserData.sCompanyContactList.add(contact);
                    }

                    mRecyclerView.setAdapter(new CompanyContactAdapter(getActivity()));
                    refreshContacts();

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
}
