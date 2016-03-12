package com.wiadvance.sipdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationCancelError;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.UserInfo;
import com.wiadvance.sipdemo.office365.AuthenticationManager;
import com.wiadvance.sipdemo.office365.Constants;

import java.net.URI;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private Button mLoginButton;
    private Button mLogoutButton;
    private ProgressBar mLoginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
    }

    private void initializeViews() {
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLogoutButton = (Button) findViewById(R.id.logout_button);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
    }

    private void showConnectingInProgressUI() {
        mLoginButton.setVisibility(View.GONE);
        mLogoutButton.setVisibility(View.GONE);
        mLoginProgressBar.setVisibility(View.VISIBLE);
    }

    private void resetUI() {
        mLoginButton.setVisibility(View.VISIBLE);
        mLogoutButton.setVisibility(View.VISIBLE);
        mLoginProgressBar.setVisibility(View.GONE);
    }

    public void onLoginButtonClick(View view) {
        Log.d(TAG, "onLoginButtonClick() called with: " + "view = [" + view + "]");

        showConnectingInProgressUI();

        checkO365Config();

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

    public void onLogoutButtonClick(View view) {
        AuthenticationManager.getInstance().setContextActivity(this);
        AuthenticationManager.getInstance().disconnect();
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
}
