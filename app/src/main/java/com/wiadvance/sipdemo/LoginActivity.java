package com.wiadvance.sipdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationCancelError;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.UserInfo;
import com.wiadvance.sipdemo.office365.AuthenticationManager;
import com.wiadvance.sipdemo.office365.Constants;

import io.fabric.sdk.android.Fabric;
import java.net.URI;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int REQUEST_SIP_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private Button mLoginButton;
    private ProgressBar mLoginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

        initializeViews();

        checkPermissions(
                this,
                android.Manifest.permission.USE_SIP,
                android.Manifest.permission.RECORD_AUDIO
        );

    }

    private void initializeViews() {
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
    }

    private void showLoading(boolean on) {
        if(on){
            mLoginButton.setVisibility(View.GONE);
            mLoginProgressBar.setVisibility(View.VISIBLE);
        }else{
            mLoginButton.setVisibility(View.VISIBLE);
            mLoginProgressBar.setVisibility(View.GONE);
        }
    }

    private void resetUI() {
        showLoading(false);
    }

    public void onLoginButtonClick(View view) {
        Log.d(TAG, "onLoginButtonClick() called with: " + "view = [" + view + "]");
        login();
    }

    private void login() {
        checkO365Config();

        showLoading(true);

        AuthenticationManager.getInstance().setContextActivity(this);
        AuthenticationManager.getInstance().connect(
                new AuthenticationCallback<AuthenticationResult>() {
                    @Override
                    public void onSuccess(AuthenticationResult result) {
                        //Need to get the new access token to the RESTHelper instance
                        Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

                        UserInfo info = result.getUserInfo();
                        String sip;
                        if(info.getDisplayableId().equals("mgr@wiadvance.net")){
                            sip = "0702552501";
                        }else{
                            sip = "0702552500";
                        }
                        Intent intent = SipActivity.newIntent(
                                LoginActivity.this,
                                info.getGivenName(),
                                info.getDisplayableId(),
                                sip
                        );
                        startActivity(intent);

                        resetUI();
                    }

                    @Override
                    public void onError(final Exception e) {
                        Log.e(TAG, "onConnectButtonClick onError() - " + e.getMessage());
                        if (!(e instanceof AuthenticationCancelError)) {
                            showConnectErrorUI(e.getMessage());
                        } else {
                            resetUI();
                        }
                    }
                });
    }

    private void showConnectErrorUI(String errorMessage) {
        String msg = String.format(getResources().getString(
                R.string.connect_toast_text_error), errorMessage
        );

        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        AuthenticationManager
                .getInstance()
                .getAuthenticationContext()
                .onActivityResult(requestCode, resultCode, data);
    }

    private void checkO365Config() {
        //check that client id and redirect have been set correctly
        try {
            UUID.fromString(Constants.CLIENT_ID);
            URI.create(Constants.REDIRECT_URI);
        } catch (IllegalArgumentException e) {
            Toast.makeText(
                    this
                    , getString(R.string.warning_clientid_redirecturi_incorrect)
                    , Toast.LENGTH_LONG).show();

            resetUI();
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                    requestPermissions(permissions, requestCode);
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
                mLoginButton.setEnabled(true);
            } else {
                // No permission
                final String permission = Manifest.permission.USE_SIP;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                mLoginButton.setEnabled(false);
            }
        }
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Has permission Todo
                mLoginButton.setEnabled(true);
            } else {
                // No permission
                final String permission = Manifest.permission.RECORD_AUDIO;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(permission)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                mLoginButton.setEnabled(false);
            }
        }
    }

}
