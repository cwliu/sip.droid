package com.wiadvance.sipdemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.wiadvance.sipdemo.office365.AuthenticationManager;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
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

    public static String ACTION_INCOMING_CALL = "com.wiadvance.sipdemo.incoming_call";

    private SipManager mSipManager;
    private SipProfile mCallerProfile;

    private Button endButton;
    private String mName;
    private String mEmail;
    private String mSipNumber;

    private RecyclerView mRecyclerView;
    private List<Contact> mContactList = new ArrayList<>();
    private ProgressBar mLoadingProgress;

    private SipAudioCall mCall;

    private boolean mConnected = false;
    public static final String SIP_DOMAIN = "210.202.37.33";
    public static final String SIP_PASSWORD = "123456789";

    public static SIPFragment newInstance(String name, String email, String sipNumber) {

        Bundle args = new Bundle();
        args.putString(ARG_NAME, name);
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_SIP, sipNumber);

        SIPFragment fragment = new SIPFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(getContext());

            initializeViews();

            setHasOptionsMenu(true);
        }

        setRetainInstance(true);
    }

    private void initializeViews() {
        mName = getArguments().getString(ARG_NAME);
        mEmail = getArguments().getString(ARG_EMAIL);
        mSipNumber = getArguments().getString(ARG_SIP);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sip, container, false);

        TextView nameTextView = (TextView)  rootView.findViewById(R.id.name);
        TextView emailTextView = (TextView) rootView.findViewById(R.id.email);
        TextView sipNumberTextView = (TextView) rootView.findViewById(R.id.sip_number);

        nameTextView.setText(mName);
        emailTextView.setText(mEmail);
        sipNumberTextView.setText(mSipNumber);

        endButton = (Button) rootView.findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCall != null){
                    try {
                        mCall.endCall();
                    } catch (SipException e) {
                        Log.e(TAG, "SipException", e);
                    }
                }
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mLoadingProgress = (ProgressBar) rootView.findViewById(R.id.loading_progress_bar);
        return rootView;
    }

    private void register(String account) {
        if(!isSipSupported()){
            return;
        }

        String username = account;

        SipProfile.Builder sipBuilder;
        try {
            sipBuilder = new SipProfile.Builder(username, SIP_DOMAIN);
        } catch (ParseException e) {
            Log.e(TAG, "ParseException: ", e);
            return;
        }

        sipBuilder.setPassword(SIP_PASSWORD);
        mCallerProfile = sipBuilder.build();
        Log.d(TAG, "Caller uri: " + mCallerProfile.getUriString());

        Intent intent = new Intent();
        intent.setAction(ACTION_INCOMING_CALL);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), 0, intent, Intent.FILL_IN_DATA
        );

        try {
            mSipManager.open(mCallerProfile, pendingIntent, new SipRegistrationListener() {

                public void onRegistering(String localProfileUri) {
                    NotificationUtil.updateStatus(getContext(), "Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    mConnected = true;
                    NotificationUtil.updateStatus(getContext(), "Ready to make or receive a SIP call !");
                    Log.d(TAG, "onRegistrationDone: Expiry Time: " + new Date(expiryTime));
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    if(errorCode == -10){
                        if(mConnected){
                            NotificationUtil.updateStatus(getContext(), "Disconnected from SIP Server" );
                        }
                    }else{
                        NotificationUtil.updateStatus(getContext(), "Registration failed.\n" +
                                "ErrorCode: " + errorCode + "\nErrorMessage: " + errorMessage);
                    }
                    mConnected = false;
                }
            });
        } catch (SipException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    private boolean isSipSupported() {
        return SipManager.isVoipSupported(getContext());
    }

    private void makeCall(String account) {
        if(!isSipSupported()){
            return;
        }

        try {

            if(mCallerProfile == null){
                NotificationUtil.updateStatus(getContext(), "Please register first!");
                return;
            }

            SipAudioCall.Listener audioListener = new SipAudioCall.Listener() {
                @Override
                public void onCalling(SipAudioCall call) {
                    setCall(call);
                    Log.d(TAG, "onCalling() called with: " + "call = [" + call + "]");
                    setCall(call);
                    NotificationUtil.updateStatus(getContext(), "onCalling");
                    super.onCalling(call);
                }

                @Override
                public void onChanged(SipAudioCall call) {
                    Log.d(TAG, "onChanged() called with: " + "call = [" + call + "]");
                    super.onChanged(call);
                }

                @Override
                public void onRingingBack(SipAudioCall call) {
                    super.onRingingBack(call);
                    Log.d(TAG, "onRingingBack() called with: " + "call = [" + call + "]");
                    NotificationUtil.updateStatus(getContext(), "onRingingBack");
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    Log.d(TAG, "onCallEstablished() called with: " + "call = [" + call + "]");
                    NotificationUtil.updateStatus(getContext(), "onCallEstablished");
                    call.startAudio();
                    call.setSpeakerMode(false);

                    setAudioVolume();

                }

                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    super.onError(call, errorCode, errorMessage);
                    Log.d(TAG, "onError() called with: " + "call = [" + call + "], errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");
                    NotificationUtil.updateStatus(getContext(), "onError: errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");

                }

                @Override
                public void onCallBusy(SipAudioCall call) {
                    super.onCallBusy(call);
                    Log.d(TAG, "onCallBusy() called with: " + "call = [" + call + "]");
                }

                @Override
                public void onCallEnded(SipAudioCall call) {
                    Log.d(TAG, "onCallEnded() called with: " + "call = [" + call + "]");
                    super.onCallEnded(call);
                }

                @Override
                public void onCallHeld(SipAudioCall call) {
                    Log.d(TAG, "onCallHeld() called with: " + "call = [" + call + "]");
                    super.onCallHeld(call);
                }

                @Override
                public void onReadyToCall(SipAudioCall call) {
                    Log.d(TAG, "onReadyToCall() called with: " + "call = [" + call + "]");
                    super.onReadyToCall(call);
                }

                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    Log.d(TAG, "onRinging() called with: " + "call = [" + call + "], caller = [" + caller + "]");
                    super.onRinging(call, caller);
                }

            };

            String peerProfileUri = "sip:"+ account +"@210.202.37.33";
            mSipManager.makeAudioCall(mCallerProfile.getUriString(), peerProfileUri, audioListener, 30);

        } catch (SipException e) {
            Log.e(TAG, "onCreate: ", e);
            NotificationUtil.updateStatus(getContext(), "Error: " + e.toString());
        }
    }

    private void setAudioVolume() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);

        Log.d(TAG, "Max volume: " + audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL));
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, 4, 0);
    }

    public SipAudioCall getCall() {
        return mCall;
    }

    public void setCall(SipAudioCall call) {
        mCall = call;
    }

    public SipManager getSipManager() {
        return mSipManager;
    }

    public class ContactHolder extends RecyclerView.ViewHolder{

        private final TextView mNameTextView;
        private final View mItemView;
        private final ImageView mPhoneImageview;
        private final ImageView mZoiperImageview;

        public ContactHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
            mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
            mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
            mZoiperImageview = (ImageView) itemView.findViewById(R.id.zoiper_image_view);

        }

        public void bindViewHolder(final Contact contact){
            mNameTextView.setText(contact.getName());

            if(isSipSupported()) {
                mPhoneImageview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeCall(contact.getSip());
                    }
                });
            }else{
                mPhoneImageview.setVisibility(View.GONE);
            }

            mZoiperImageview.setOnClickListener(new View.OnClickListener(){

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

    public class ContactAdapter extends  RecyclerView.Adapter<ContactHolder>{

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

    private void showLoading(boolean on){
        if(on){
            mLoadingProgress.setVisibility(View.VISIBLE);
        }else{
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
                                for(ContactRaw.InnerDict person: contactRaw.value){

                                    Log.d(TAG, "person: " + person.displayName);
                                    for(String phone: person.businessPhones){
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

                                NotificationUtil.updateStatus(getContext(), "Please re-login.\nDownload contact failed: " + error.toString());

                                getActivity().finish();
                            }
                        });
                    }

                    @Override
                    public void onError(final Exception e) {
                        Log.e(TAG, "onConnectButtonClick onError() - " + e.getMessage());
                    };
                });


        register(mSipNumber);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.fragment_sip, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_logout:
                removeSipAccount();
                AuthenticationManager.getInstance().setContextActivity(getActivity());
                AuthenticationManager.getInstance().disconnect();
                getActivity().finish();
                return true;

            case R.id.action_manual_register:
                register(mSipNumber);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean removeSipAccount() {
        if(!isSipSupported()){
            return false;
        }

        try {
            if (mCallerProfile != null) {
                mSipManager.close(mCallerProfile.getUriString());
                NotificationUtil.updateStatus(getContext(), "Unregister device from SIP Server");
            }
        } catch (Exception ee) {
            Log.d(TAG, "Failed to close local profile.", ee);
        }
        return false;
    }
}
