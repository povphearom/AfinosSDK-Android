package com.afinos.sdk.auth.provider;

import android.app.Activity;
import android.content.Intent;

import com.afinos.sdk.auth.AuthCallback;
import com.afinos.sdk.auth.AuthResponse;

public interface Provider {
    void startLogin(Activity activity);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void setAuthenticationCallback(AuthCallback<AuthResponse> callback);
}
