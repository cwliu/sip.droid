package com.wiadvance.sipdemo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sipdemo.linphone.LinphoneCoreHelper;
import com.wiadvance.sipdemo.linphone.LinphoneSipManager;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.model.UserRaw;
import com.wiadvance.sipdemo.office365.AuthenticationManager;
import com.wiadvance.sipdemo.office365.MSGraphAPIController;

import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.core.LinphoneCore;
import org.linphone.core.LinphoneCoreException;
import org.linphone.core.LinphoneCoreListenerBase;
import org.linphone.core.LinphoneProxyConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";

    public static final String HTTPS_SIP_SERVER_HEROKUAPP_COM_API_V1_SIPS =
            "https://sip-server.herokuapp.com/api/v1/sips/";

    private RecyclerView mRecyclerView;
    private ProgressBar mLoadingProgress;
    private LinphoneSipManager mWiSipManager;
    private DrawerItemAdapter mDrawerAdapter;
    private LinphoneCoreListenerBase mLinPhoneListener;
    private View mRootView;

    public static ContactFragment newInstance() {

        Bundle args = new Bundle();
        ContactFragment fragment = new ContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        mWiSipManager = new LinphoneSipManager(getContext());
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_sip, container, false);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLoadingProgress = (ProgressBar) mRootView.findViewById(R.id.loading_progress_bar);

        List<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem("Header", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Information", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Logout", R.drawable.ic_exit_to_app_black_24dp));
        mDrawerAdapter = new DrawerItemAdapter(getContext(), items);

        setupNavigationDrawer(mRootView);

        return mRootView;
    }

    private void setupNavigationDrawer(View rootView) {
        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) rootView.findViewById(R.id.left_drawer);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                (DrawerLayout) rootView.findViewById(R.id.drawer_layout),
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);

        drawerList.setAdapter(mDrawerAdapter);

        drawerList.setBackgroundColor(getResources().getColor(R.color.beige));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        int versionCode = BuildConfig.VERSION_CODE;
                        String versionName = BuildConfig.VERSION_NAME;

                        final String message = "Version: " + versionName + "." + versionCode + "\n"
                                + "Sip Number: " + UserPreference.getSip(getContext()) + "\n"
                                + "Email: " + UserPreference.getEmail(getContext()) + "\n";

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getContext()).setTitle("Information")
                                .setMessage(message)
                                .setPositiveButton("ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    case 2:
                        logout();
                        break;
                }
            }
        });
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

        if (UserPreference.sContactList.size() == 0) {
            showLoading(true);
        }

        try {
            LinphoneCore lc = LinphoneCoreHelper.getLinphoneCoreInstance(getContext());
            mLinPhoneListener = new LinphoneCoreListenerBase() {
                @Override
                public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg, LinphoneCore.RegistrationState state, final String message) {

                    if (state.equals(LinphoneCore.RegistrationState.RegistrationOk)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDrawerAdapter != null) {
                                    mDrawerAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }

                    if (state.equals(LinphoneCore.RegistrationState.RegistrationFailed)) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDrawerAdapter != null) {
                                    mDrawerAdapter.notifyDataSetChanged();
                                }
                                NotificationUtil.displayStatus(getContext(), "Sip registration error: " + message);
                            }
                        });
                    }
                }
            };
            lc.addListener(mLinPhoneListener);

        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }


        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().connect(mAuthenticationCallback);

        syncSipAccountList();

    }

    @Override
    public void onStop() {
        super.onStop();

        try {
            LinphoneCore lc = LinphoneCoreHelper.getLinphoneCoreInstance(getContext());
            lc.removeListener(mLinPhoneListener);

        } catch (LinphoneCoreException e) {
            e.printStackTrace();
        }
    }

    private void syncSipAccountList() {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("email", UserPreference.getEmail(getContext()))
                .add("access_token", AuthenticationManager.getInstance().getAccessToken())
                .build();

        Request request = new Request.Builder()
                .url(HTTPS_SIP_SERVER_HEROKUAPP_COM_API_V1_SIPS)
                .post(body)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
                NotificationUtil.displayStatus(getContext(),
                        "Sip backend server error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");

                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(getContext(),
                            "Sip backend server error: " + response.body().string());
                    return;
                }

                String rawBodyString = response.body().string();

                SipApiResponse sip_data = new Gson().fromJson(rawBodyString, SipApiResponse.class);

                String sip_domain = sip_data.proxy_address + ":" + sip_data.proxy_port;
                if (getContext() != null) {
                    UserPreference.setSip(getContext(), sip_data.sip_account);
                    UserPreference.setPassword(getContext(), sip_data.sip_password);
                    UserPreference.setDomain(getContext(), sip_domain);

                    for (SipApiResponse.SipAccount acc : sip_data.sip_list) {
                        UserPreference.sEmailtoSipBiMap.forcePut(acc.email, acc.sip_account);
                        UserPreference.sEmailtoPhoneBiMap.forcePut(acc.email, acc.phone);
                    }

                    mWiSipManager.register(sip_data.sip_account, sip_data.sip_password, sip_domain);
                }
                refreshContacts();
            }
        });
    }

    private void refreshContacts() {
        for (Contact c : UserPreference.sContactList) {
            String email = c.getEmail();
            String phone = UserPreference.sEmailtoPhoneBiMap.get(email);
            String sip = UserPreference.sEmailtoSipBiMap.get(email);

            if (phone != null) {
                c.setPhone(phone);
            }

            if (sip != null) {
                c.setSip(sip);
            }
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView.getAdapter() != null) {
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
    }

    private void logout() {

        String sipNumber = UserPreference.getSip(getContext());
        if (sipNumber != null) {
            mWiSipManager.unregister(sipNumber);
        }

        UserPreference.clean(getContext());

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(getContext(), BuildConfig.MIXPANL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
            props.put("INIT_TIME", LinphoneCoreHelper.getInitTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("LOGOUT", props);

        LinphoneCoreHelper.destroyLinphoneCore(getContext());

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().disconnect();

        // Back to login activity
        Intent intent = LoginActivity.newIntent(getContext());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private AuthenticationCallback<AuthenticationResult> mAuthenticationCallback = new AuthenticationCallback<AuthenticationResult>() {
        @Override
        public void onSuccess(AuthenticationResult result) {
            //Need to get the new access token to the RESTHelper instance
            Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

            MSGraphAPIController.getInstance().showContacts(new Callback<UserRaw>() {
                @Override
                public void success(UserRaw userRaw, Response response) {

                    UserPreference.sContactList.clear();
                    for (UserRaw.InnerDict user : userRaw.value) {
                        if (user.mail == null || user.mail.equals(UserPreference.getEmail(getContext()))) {
                            continue;
                        }

                        // TODO
                        Contact contact = new Contact(user.displayName, user.mail);
                        Log.d(TAG, "user: " + user.displayName + ", mobilePhone: " + user.mobilePhone);
                        for (String phone : user.businessPhones) {
                            Log.d(TAG, "user: " + user.displayName + ", businessPhone: " + phone);
                        }

                        UserPreference.sContactList.add(contact);
                    }

                    mRecyclerView.setAdapter(new ContactAdapter(getContext()));
                    refreshContacts();

                    showLoading(false);
                }

                @Override
                public void failure(RetrofitError error) {
                    NotificationUtil.displayStatus(getContext(), "Microsoft Graph Server error: " + error.toString());

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
