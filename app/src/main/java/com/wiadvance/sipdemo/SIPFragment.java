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
import android.widget.Toast;

import java.text.ParseException;

public class SIPFragment extends Fragment {

    private static final String TAG = "SIPFragment";
    private static final int REQUEST_SIP_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private SipManager mSipManager;
    private SipProfile mSipProfile;

    public static SIPFragment newInstance() {

        Bundle args = new Bundle();

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

        String username = "0702552500";
        String domain = "210.202.37.33";
        String password = "123456789";

        try {
            SipProfile.Builder sipBuilder = new SipProfile.Builder(username, domain);
            sipBuilder.setPassword(password);
            mSipProfile = sipBuilder.build();

            Intent intent = new Intent();
            intent.setAction("android.SipDemo.INCOMING_CALL");

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    getContext(), 0, intent, Intent.FILL_IN_DATA
            );

            mSipManager.open(mSipProfile, pendingIntent, null);

            mSipManager.setRegistrationListener(mSipProfile.getUriString(), new SipRegistrationListener() {

                public void onRegistering(String localProfileUri) {
                    updateStatus("Registering with SIP Server...");
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    updateStatus("Ready");
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                                                 String errorMessage) {
                    updateStatus("Registration failed.  Please check settings.");
                }
            });

            SipAudioCall.Listener audioListener = new SipAudioCall.Listener() {
                @Override
                public void onCallEstablished(SipAudioCall call) {
                    super.onCallEstablished(call);
                    call.startAudio();
                }
            };

        } catch (ParseException | SipException e) {
            Log.e(TAG, "onCreate: ", e);
            updateStatus("Error: " + e.toString());
        }
    }

    private void updateStatus(String s) {
        Log.d(TAG, "updateStatus: " + s);
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sip, container, false);
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
                    Log.d(TAG, "checkPermissions: requestCode " + requestCode);
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
}
