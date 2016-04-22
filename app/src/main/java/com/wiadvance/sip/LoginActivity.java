package com.wiadvance.sip;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationCancelError;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.UserInfo;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.wiadvance.sip.office365.AuthenticationManager;
import com.wiadvance.sip.office365.Constants;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import io.fabric.sdk.android.Fabric;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int REQUEST_SIP_PERMISSION = 1;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 2;

    private Button mLoginButton;
    private ProgressBar mLoginProgressBar;
    private TextView mLoginAppNameTextView;
    private ImageView mLoginLogoImageView;

    public static final int PICASSO_CACHE_SIZE_100_MB = 100 * 1000 * 1000;

    public static Intent newIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_login);
        initializeViews();

        AuthenticationManager.getInstance().setContextActivity(this);

        MixpanelAPI mixpanel = MixpanelAPI.getInstance(this, BuildConfig.MIXPANL_TOKEN);
        mixpanel.track(TAG, null);

        checkPermissions(this, android.Manifest.permission.RECORD_AUDIO);
    }

    private void initializeViews() {
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        mLoginAppNameTextView = (TextView) findViewById(R.id.appName_textView);
        mLoginLogoImageView = (ImageView) findViewById(R.id.logo_imageView);

    }

    private void showLoading(boolean on) {
        if (on) {
            mLoginLogoImageView.setVisibility(View.GONE);
            mLoginAppNameTextView.setVisibility(View.GONE);
            mLoginButton.setVisibility(View.GONE);
            mLoginProgressBar.setVisibility(View.VISIBLE);
        } else {
            mLoginLogoImageView.setVisibility(View.VISIBLE);
            mLoginAppNameTextView.setVisibility(View.VISIBLE);
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
        showLoading(true);
        checkO365Config();
        o365login();
    }

    private void o365login() {
        AuthenticationManager.getInstance().connect(new AuthenticationCallback<AuthenticationResult>() {
            @Override
            public void onSuccess(AuthenticationResult result) {
                //Need to get the new access token to the RESTHelper instance
                Log.i(TAG, "onConnectButtonClick onSuccess() - Successfully connected to Office 365");

                //Once we have o365 access token, setup picasso
                setupPicasso();

                UserInfo info = result.getUserInfo();
                UserData.setName(LoginActivity.this, info.getGivenName());
                UserData.setEmail(LoginActivity.this, info.getDisplayableId());

                Intent intent = ContactActivity.newIntent(LoginActivity.this);
                startActivity(intent);
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

    private void setupPicasso() {
        OkHttpClient picassoClient = new OkHttpClient();
        picassoClient.networkInterceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                String accessToken = AuthenticationManager.getInstance().getAccessToken();
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .addHeader("Content-Type", "image/jpg")
                        .build();
                return chain.proceed(newRequest);
            }
        });

        picassoClient.setCache(new Cache(getCacheDir(), PICASSO_CACHE_SIZE_100_MB));
        OkHttpDownloader downloader = new OkHttpDownloader(picassoClient);
        Picasso picasso = new Picasso.Builder(this)
                .downloader(downloader)
                .listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        if (exception.toString().contains("404 Not Found")) {
                            UserData.sAvatar404Cache.add(uri.toString());
                        }
                        Log.d(TAG, "onImageLoadFailed() called with: " + "picasso = [" + picasso + "], uri = [" + uri + "], exception = [" + exception + "]");
                    }
                })
                .build();

        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException ignored) {
            // Picasso already started
        }

        Picasso.with(this).invalidate(Constants.MY_PHOTO_URL);
        Picasso.with(this).setLoggingEnabled(false);
        Picasso.with(this).setIndicatorsEnabled(false);
    }

    private void showConnectErrorUI(String errorMessage) {
        String msg = String.format(getResources().getString(
                R.string.connect_toast_text_error), errorMessage
        );

        NotificationUtil.displayStatus(this, msg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: " + "requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");

        AuthenticationContext context = AuthenticationManager.getInstance().getAuthenticationContext();
        if (context != null) {
            context.onActivityResult(requestCode, resultCode, data);
        }
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

        if (AuthenticationManager.getInstance().isConnected()) {
            login();
        }
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
