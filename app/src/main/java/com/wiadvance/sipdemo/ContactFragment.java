package com.wiadvance.sipdemo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
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
import retrofit.mime.TypedByteArray;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";
    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_SIP = "sip";
    private static final String ARG_DOMAIN = "domain";
    private static final String ARG_PASSWORD = "password";

    private Button endButton;
    private String mName;
    private String mEmail;
    private String mSipNumber;
    private String mDomain;
    private String mPassword;

    private RecyclerView mRecyclerView;
    private List<Contact> mContactList = new ArrayList<>();
    private ProgressBar mLoadingProgress;

    private LinphoneSipManager mWiSipManager;
    private BroadcastReceiver mCallStatusReceiver;

    private boolean mDisplayEndButton = false;
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;

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


        mWiSipManager = new LinphoneSipManager(getContext());

        initializeViews();

        setHasOptionsMenu(true);

        setRetainInstance(true);
    }

    private void initializeViews() {
        mName = getArguments().getString(ARG_NAME);
        mEmail = getArguments().getString(ARG_EMAIL);
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

        endButton = (Button) rootView.findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWiSipManager.endCall();
            }
        });
        if (mDisplayEndButton) {
            endButton.setVisibility(View.VISIBLE);
        } else {
            endButton.setVisibility(View.GONE);
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);

        mDrawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) rootView.findViewById(R.id.left_drawer);

        List<DrawerItem> items = new ArrayList<>();
        items.add(new DrawerItem("Header", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Information", R.drawable.ic_info_outline_black_24dp));
        items.add(new DrawerItem("Logout", R.drawable.ic_exit_to_app_black_24dp));
        mDrawerList.setAdapter(new DrawerItemAdapter(getContext(), items));

        mDrawerList.setBackgroundColor(getResources().getColor(R.color.beige));
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        final String message = "Version: " + versionName + "." + versionCode + "\n"
                + "Sip Number: " + UserPreference.getSip(getContext()) + "\n"
                + "Email: " + UserPreference.getEmail(getContext()) + "\n";

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:

                        AlertDialog.Builder builder = new AlertDialog.Builder(
                                getContext()).setTitle("Version")
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
                mDrawerLayout,
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
        mDrawerLayout.setDrawerListener(drawerToggle);
        return rootView;
    }

    public class ContactHolder extends RecyclerView.ViewHolder {

        private final TextView mNameTextView;
        private final ImageView mPhoneImageview;
        private final ImageView mZoiperImageview;

        public ContactHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
            mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
            mZoiperImageview = (ImageView) itemView.findViewById(R.id.zoiper_image_view);

        }

        public void bindViewHolder(final Contact contact) {
            mNameTextView.setText(contact.getName());

            if (mWiSipManager.isSupported()) {
                mPhoneImageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWiSipManager.makeCall(contact);
                    }
                });
            } else {
                mPhoneImageview.setVisibility(View.GONE);
            }

            mZoiperImageview.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("tel://" + contact.getSip()));
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
            holder.bindViewHolder(mContactList.get(position));
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

        showLoading(true);

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().connect(
                new AuthenticationCallback<AuthenticationResult>() {
                    @Override
                    public void onSuccess(AuthenticationResult result) {
                        //Need to get the new access token to the RESTHelper instance
                        Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

                        MSGraphAPIController.getInstance().showContacts(new Callback<ContactRaw>() {
                            @Override
                            public void success(ContactRaw contactRaw, Response response) {
                                Log.d(TAG, "success() called with: " + "contactRaw = [" + contactRaw + "], response = [" + response + "]");
                                Log.d(TAG, "response.getStatus()" + response.getStatus());

                                String s = new String(((TypedByteArray) response.getBody()).getBytes());
                                Log.d(TAG, "response: " + s);
                                Log.d(TAG, "contactRaw.data: " + contactRaw.data);
                                Log.d(TAG, "contactRaw.value: " + contactRaw.value);
                                mContactList.clear();
                                for (ContactRaw.InnerDict person : contactRaw.value) {

                                    Log.d(TAG, "person: " + person.displayName);
                                    Contact contact = new Contact(person.displayName);

                                    for (String phone : person.businessPhones) {
                                        if (!phone.startsWith("070")) {
                                            contact.setPhone(phone);
                                        } else {
                                            contact.setSip(phone);
                                        }
                                        Log.d(TAG, "phone: " + phone);
                                    }
                                    String sipUri = "sip:" + contact.getSip() + "@" + "210.202.37.33";
                                    mWiSipManager.addFriend(sipUri);
                                    mContactList.add(contact);
                                }

                                mRecyclerView.setAdapter(new ContactAdapter());
                                mRecyclerView.getAdapter().notifyDataSetChanged();

                                showLoading(false);
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                showLoading(false);

                                NotificationUtil.displayStatus(getContext(), "Please re-login.\nDownload contact failed: " + error.toString());

                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception e) {
                        Log.e(TAG, "onConnectButtonClick onError() - " + e.getMessage());
                    }
                });

        mWiSipManager.register(mSipNumber, mPassword, mDomain);

        mWiSipManager.setOnlineStatus();
        mWiSipManager.displayFriendStatus();
    }

    private void logout() {
        mWiSipManager.unregister(mSipNumber);
        LinphoneCoreHelper.destroyLinphoneCore(getContext());

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(getContext(), BuildConfig.MIXPANL_TOKEN);
        JSONObject props = new JSONObject();
        try {
            props.put("SIP_NUMBER", LinphoneCoreHelper.getSipNumber());
            props.put("INIT_TIME", LinphoneCoreHelper.getInitTime().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mixpanel.track("LOGOUT", props);

        AuthenticationManager.getInstance().setContextActivity(getActivity());
        AuthenticationManager.getInstance().disconnect();
        getActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();

        mCallStatusReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                boolean on = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_ON, false);
                if (on) {
                    mDisplayEndButton = true;
                    endButton.setVisibility(View.VISIBLE);
                } else {
                    mDisplayEndButton = false;
                    endButton.setVisibility(View.GONE);
                }
            }
        };
        IntentFilter notify_filter = new IntentFilter(NotificationUtil.ACTION_CALL);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(mCallStatusReceiver, notify_filter);

    }

    @Override
    public void onStop() {
        super.onStop();

        if (mCallStatusReceiver != null) {
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
            manager.unregisterReceiver(mCallStatusReceiver);
        }
    }
}
