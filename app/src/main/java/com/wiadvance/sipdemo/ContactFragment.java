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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.picasso.Picasso;
import com.wiadvance.sipdemo.linphone.LinphoneCoreHelper;
import com.wiadvance.sipdemo.linphone.LinphoneSipManager;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.model.ContactRaw;
import com.wiadvance.sipdemo.office365.AuthenticationManager;
import com.wiadvance.sipdemo.office365.MSGraphAPIController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_SIP = "sip";
    private static final String ARG_DOMAIN = "domain";
    private static final String ARG_PASSWORD = "password";

    private String mSipNumber;
    private String mDomain;
    private String mPassword;

    private RecyclerView mRecyclerView;
    private List<Contact> mContactList = new ArrayList<>();
    private ProgressBar mLoadingProgress;
    private LinphoneSipManager mWiSipManager;
    private DrawerItemAdapter mAdapter;

    public static ContactFragment newInstance(String name, String email, String sipNumber, String domain, String password) {

        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_SIP, sipNumber);
        args.putString(ARG_DOMAIN, domain);
        args.putString(ARG_PASSWORD, password);

        ContactFragment fragment = new ContactFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeViews();
        setRetainInstance(true);

        mWiSipManager = new LinphoneSipManager(getContext());
    }

    private void initializeViews() {
        mSipNumber = getArguments().getString(ARG_SIP);
        mDomain = getArguments().getString(ARG_DOMAIN);
        mPassword = getArguments().getString(ARG_PASSWORD);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sip, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);

        DrawerLayout drawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        ListView drawerList = (ListView) rootView.findViewById(R.id.left_drawer);

        List<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem("Header", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Information", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Logout", R.drawable.ic_exit_to_app_black_24dp));
        mAdapter = new DrawerItemAdapter(getContext(), items);
        drawerList.setAdapter(mAdapter);

        drawerList.setBackgroundColor(getResources().getColor(R.color.beige));
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        final String message = "Version: " + versionName + "." + versionCode + "\n"
                + "Sip Number: " + UserPreference.getSip(getContext()) + "\n"
                + "Email: " + UserPreference.getEmail(getContext()) + "\n";

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
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

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

//                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };

        drawerToggle.syncState();
        drawerLayout.setDrawerListener(drawerToggle);
        return rootView;
    }

    public class ContactHolder extends RecyclerView.ViewHolder {

        private final TextView mNameTextView;
        private final ImageView mPhoneImageview;
        private final ImageView mAvatar;

        public ContactHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
            mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
            mAvatar = (ImageView) itemView.findViewById(R.id.list_item_avatar);
        }

        public void bindViewHolder(final Contact contact, int position) {
            mNameTextView.setText(contact.getName());

            int randomAvatar[] = {R.drawable.avatar_1_120dp, R.drawable.avatar_2_120dp, R.drawable.avatar_3_120dp};

            mAvatar.setImageResource(randomAvatar[position % randomAvatar.length]);
            mPhoneImageview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = MakeCallActivity.newIntent(getContext(), contact);
                    startActivity(intent);
                }
            });
        }
    }

    public class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        @Override
        public ContactHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = LayoutInflater.from(getContext()).inflate(R.layout.list_item_contact, parent, false);
            return new ContactHolder(inflate);
        }

        @Override
        public void onBindViewHolder(ContactHolder holder, int position) {
            holder.bindViewHolder(mContactList.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mContactList.size();
        }
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

        if (mContactList.size() == 0) {
            showLoading(true);
        }

        mWiSipManager.register(mSipNumber, mPassword, mDomain);

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().connect(mAuthenticationCallback);
    }

    private void logout() {
        mWiSipManager.unregister(mSipNumber);
        LinphoneCoreHelper.destroyLinphoneCore(getContext());

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(getContext(), BuildConfig.MIXPANL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
            props.put("INIT_TIME", LinphoneCoreHelper.getInitTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("LOGOUT", props);

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().disconnect();
        getActivity().finish();
    }

    private AuthenticationCallback<AuthenticationResult> mAuthenticationCallback = new AuthenticationCallback<AuthenticationResult>() {
        @Override
        public void onSuccess(AuthenticationResult result) {
            //Need to get the new access token to the RESTHelper instance
            Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

            MSGraphAPIController.getInstance().showContacts(new Callback<ContactRaw>() {
                @Override
                public void success(ContactRaw contactRaw, Response response) {
                    mContactList.clear();
                    for (ContactRaw.InnerDict person : contactRaw.value) {
                        Contact contact = new Contact(person.displayName);

                        for (String phone : person.businessPhones) {
                            if (!phone.startsWith("070")) {
                                contact.setPhone(phone);
                            } else {
                                contact.setSip(phone);
                            }
                        }
                        mContactList.add(contact);
                    }

                    mRecyclerView.setAdapter(new ContactAdapter());
                    mRecyclerView.getAdapter().notifyDataSetChanged();

                    showLoading(false);
                }

                @Override
                public void failure(RetrofitError error) {
                    NotificationUtil.displayStatus(getContext(), "Please re-login.\nDownload contact data failed: " + error.toString());

                    showLoading(false);
                }
            });



            Picasso.with(getContext()).setLoggingEnabled(true);
        }

        @Override
        public void onError(final Exception e) {
            Log.e(TAG, "onConnectButtonClick onError() - " + e.getMessage());
        }
    };
}
