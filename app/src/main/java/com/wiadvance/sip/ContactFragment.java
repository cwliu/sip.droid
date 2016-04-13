package com.wiadvance.sip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.wiadvance.sip.linphone.LinphoneCoreHelper;
import com.wiadvance.sip.linphone.LinphoneSipManager;
import com.wiadvance.sip.office365.AuthenticationManager;

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

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";

    public static final String HTTPS_SIP_API_SERVER = BuildConfig.HTTPS_SIP_API_SERVER;

    private LinphoneSipManager mWiSipManager;
    private DrawerItemAdapter mDrawerAdapter;
    private LinphoneCoreListenerBase mLinPhoneListener;

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
        View rootView = inflater.inflate(R.layout.fragment_sip, container, false);


        List<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem("Header", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Information", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Logout", R.drawable.ic_exit_to_app_black_24dp));
        mDrawerAdapter = new DrawerItemAdapter(getContext(), items);

        setupNavigationDrawer(rootView);

        ViewPager viewPager = (ViewPager) rootView.findViewById(R.id.contacts_view_pager);
        viewPager.setAdapter(new ContactPagerAdapter(getFragmentManager()));

        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.contacts_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        if(UserData.getRecentContactList(getContext()).size() == 0){
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) {
                tab.select();
            }
        }
        return rootView;
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
                                + "Sip Number: " + UserData.getSip(getContext()) + "\n"
                                + "Email: " + UserData.getEmail(getContext()) + "\n";

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getContext()).setTitle("Information")
                                .setMessage(message)
                                .setPositiveButton("ok", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        break;
                    case 2:

                        AlertDialog.Builder logoutBuilder = new AlertDialog.Builder(getContext())
                            .setTitle("Logout")
                            .setMessage("Are you sure you want to logout?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    logout();
                                }
                            })
                            .setNegativeButton("No", null);
                        logoutBuilder.create().show();
                        break;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

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

        getSipAccounts();

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

    private void getSipAccounts() {
        OkHttpClient client = new OkHttpClient();

        FormBody body = new FormBody.Builder()
                .add("email", UserData.getEmail(getContext()))
                .add("access_token", AuthenticationManager.getInstance().getAccessToken())
                .build();

        Request request = new Request.Builder()
                .url(HTTPS_SIP_API_SERVER)
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
                    UserData.setSip(getContext(), sip_data.sip_account);
                    UserData.setPassword(getContext(), sip_data.sip_password);
                    UserData.setDomain(getContext(), sip_domain);

                    for (SipApiResponse.SipAccount acc : sip_data.sip_list) {
                        UserData.sEmailtoSipBiMap.forcePut(acc.email, acc.sip_account);
                        UserData.sEmailtoPhoneBiMap.forcePut(acc.email, acc.phone);
                    }

                    mWiSipManager.register(sip_data.sip_account, sip_data.sip_password, sip_domain);
                }
            }
        });
    }

    private void logout() {

        String sipNumber = UserData.getSip(getContext());
        if (sipNumber != null) {
            mWiSipManager.unregister(sipNumber);
        }

        UserData.clean(getContext());

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
}
