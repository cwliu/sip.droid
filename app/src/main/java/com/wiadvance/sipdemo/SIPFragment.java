package com.wiadvance.sipdemo;

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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;
import com.wiadvance.sipdemo.linphone.LinphoneSipManager;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.model.ContactRaw;
import com.wiadvance.sipdemo.office365.AuthenticationManager;
import com.wiadvance.sipdemo.office365.MSGraphAPIController;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class SIPFragment extends Fragment {

    private static final String TAG = "SIPFragment";
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

    private WiSipManager wiSipManager;
    private BroadcastReceiver mCallStatusReceiver;

    private boolean mDisplayEndButton = false;

    public static SIPFragment newInstance(String name, String email, String sipNumber, String domain, String password) {

        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_SIP, sipNumber);
        args.putString(ARG_DOMAIN, domain);
        args.putString(ARG_PASSWORD, password);

        SIPFragment fragment = new SIPFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        wiSipManager = new LinphoneSipManager(getContext());

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

        TextView nameTextView = (TextView) rootView.findViewById(R.id.name);
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email);
        TextView sipNumberTextView = (TextView) rootView.findViewById(R.id.sip_number);

        nameTextView.setText(mName);
        emailTextView.setText(mEmail);
        sipNumberTextView.setText(mSipNumber);

        endButton = (Button) rootView.findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wiSipManager.endCall();
            }
        });
        if(mDisplayEndButton){
            endButton.setVisibility(View.VISIBLE);
        }else{
            endButton.setVisibility(View.GONE);
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);
        return rootView;
    }

    private void makeCall(String account) {
        wiSipManager.makeCall(account);
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

            if (wiSipManager.isSupported()) {
                mPhoneImageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeCall(contact.getSip());
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
        } else {
            mLoadingProgress.setVisibility(View.GONE);
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
                                    for (String phone : person.businessPhones) {
                                        if(!phone.startsWith("070")){
                                            continue;
                                        }
                                        Contact contact = new Contact(person.displayName, phone);
                                        mContactList.add(contact);
                                        Log.d(TAG, "phone: " + phone);
                                    }
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

                    ;
                });

        wiSipManager.register(mSipNumber, mPassword, mDomain);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_sip, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                wiSipManager.unregister(mSipNumber);
                AuthenticationManager.getInstance().setContextActivity(getActivity());
                AuthenticationManager.getInstance().disconnect();
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mCallStatusReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {
                boolean on = intent.getBooleanExtra(NotificationUtil.NOTIFY_CALL_ON, false);
                if(on){
                    mDisplayEndButton = true;
                    endButton.setVisibility(View.VISIBLE);
                }else{
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

        if(mCallStatusReceiver != null){
            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
            manager.unregisterReceiver(mCallStatusReceiver);
        }
    }
}
