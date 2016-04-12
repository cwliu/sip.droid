package com.wiadvance.sip.office365;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationResult;

public class OfficeAuthManager {
    private final AuthenticationCallback<AuthenticationResult> mAuthenticationCallback;

    /**
     * @param authenticationCallback The callback to notify when the processing is finished.
     */
    public OfficeAuthManager(AuthenticationCallback<AuthenticationResult> authenticationCallback) {
        mAuthenticationCallback = authenticationCallback;
    }

    public AuthenticationCallback<AuthenticationResult> getAuthenticationCallback() {
        return mAuthenticationCallback;
    }
}
