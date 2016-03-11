package com.wiadvance.sipdemo;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Date;

public class SIPFragment extends Fragment {

    private static final String TAG = "SIPFragment";
    private static final int REQUEST_SIP_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;
    private static final String ARG_NAME = "name";
    private static final String ARG_EMAIL = "email";
    private static final String ARG_SIP = "sip";

    public static String ACTION_INCOMING_CALL = "com.wiadvance.sipdemo.incoming_call";

    private SipManager mSipManager;
    private SipProfile mCallerProfile;

    private Button registerButton1;
    private Button registerButton2;
    private Button callButton0;
    private Button callButton1;
    private Button callButton2;
    private Button endButton;
    private String mName;
    private String mEmail;
    private String mSipNumber;

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
        checkPermissions(
                getContext(),
                android.Manifest.permission.USE_SIP,
                android.Manifest.permission.RECORD_AUDIO
        );

        if (mSipManager == null) {
            mSipManager = SipManager.newInstance(getContext());
        }

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

        registerButton1 = (Button) rootView.findViewById(R.id.register_button_1);
        registerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    register("0702552500");
                } catch (ParseException e) {
                    Log.e(TAG, "onClick: ", e);
                }
            }
        });

        registerButton2 = (Button) rootView.findViewById(R.id.register_button_2);
        registerButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    register("0702552501");
                } catch (ParseException e) {
                    Log.e(TAG, "onClick: ", e);
                }
            }
        });

        callButton0 = (Button) rootView.findViewById(R.id.call_button_0);
        callButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall("0702555000");
            }
        });

        callButton1 = (Button) rootView.findViewById(R.id.call_button_1);
        callButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall("0702552500");
            }
        });

        callButton2 = (Button) rootView.findViewById(R.id.call_button_2);
        callButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeCall("0702552501");
            }
        });

        endButton = (Button) rootView.findViewById(R.id.end_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLocalProfile(mCallerProfile);
            }
        });
        return rootView;
    }

    private boolean checkPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {

            for (final String permission : permissions) {
                int requestCode = 0;

                if (permission.equals(android.Manifest.permission.USE_SIP)) {
                    requestCode = REQUEST_SIP_PERMISSION;
                } else if (permission.equals(android.Manifest.permission.RECORD_AUDIO)) {
                    requestCode = REQUEST_RECORD_AUDIO_PERMISSION;
                }
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{permission}, requestCode);
                }
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_SIP_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Has permission Todo
            } else {
                // No permission
                final String permission = android.Manifest.permission.USE_SIP;
                if (shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("We need you to grant SIP permission");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, REQUEST_SIP_PERMISSION);
                        }
                    });
                    builder.show();
                }
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Has permission Todo
            } else {
                // No permission
                final String permission = android.Manifest.permission.RECORD_AUDIO;
                if (shouldShowRequestPermissionRationale(permission)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("We need you to grant audio record permission");
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{permission}, REQUEST_SIP_PERMISSION);
                        }
                    });
                    builder.show();
                }
            }
        }
    }
    private void register(String account) throws ParseException {
        String username = account;
        String domain = "210.202.37.33";
        String password = "123456789";

        SipProfile.Builder sipBuilder;
        sipBuilder = new SipProfile.Builder(username, domain);

        sipBuilder.setPassword(password);
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
                    Notification.updateStatus(getContext(), "Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    Notification.updateStatus(getContext(), "Ready, expiryTime: " + new Date(expiryTime));
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    Notification.updateStatus(getContext(), "Registration failed. " +
                            "errorCode: " + errorCode + ", errorMessage: " + errorMessage);
                }
            });
        } catch (SipException e) {
            Log.e(TAG, "register: ", e);
        }
    }

    private void makeCall(String account) {

        try {

            if(mCallerProfile == null){
                Notification.updateStatus(getContext(), "Please register first!");
                return;
            }

            SipAudioCall.Listener audioListener = new SipAudioCall.Listener() {
                @Override
                public void onCalling(SipAudioCall call) {
                    Log.d(TAG, "onCalling() called with: " + "call = [" + call + "]");
                    Notification.updateStatus(getContext(), "onCalling");
                    super.onCalling(call);
                }

                @Override
                public void onChanged(SipAudioCall call) {
                    Log.d(TAG, "onChanged() called with: " + "call = [" + call + "]");
                    Notification.updateStatus(getContext(), "onChanged");
                    super.onChanged(call);
                }

                @Override
                public void onRingingBack(SipAudioCall call) {
                    Log.d(TAG, "onRingingBack() called with: " + "call = [" + call + "]");
                    Notification.updateStatus(getContext(), "onRingingBack");
                    super.onRingingBack(call);
                }

                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    Log.d(TAG, "onCallEstablished() called with: " + "call = [" + call + "]");
                    Notification.updateStatus(getContext(), "onCallEstablished");
                    call.startAudio();
                    call.setSpeakerMode(true);
                }

                @Override
                public void onError(SipAudioCall call, int errorCode, String errorMessage) {
                    super.onError(call, errorCode, errorMessage);
                    Log.d(TAG, "onError() called with: " + "call = [" + call + "], errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");
                    Notification.updateStatus(getContext(), "onError: errorCode = [" + errorCode + "], errorMessage = [" + errorMessage + "]");

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
            Notification.updateStatus(getContext(), "Error: " + e.toString());
        }
    }

    public void closeLocalProfile(SipProfile sipProfile) {
        if (mSipManager == null) {
            return;
        }
        try {
            if (sipProfile != null) {
                mSipManager.close(sipProfile.getUriString());
                Notification.updateStatus(getContext(), "Call end");
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to close local profile.", e);
        }
    }

    public SipManager getSipManager() {
        return mSipManager;
    }
}
