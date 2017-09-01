/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.afinos.sdk.auth.provider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.afinos.sdk.auth.AuthCallback;
import com.afinos.sdk.auth.AuthResponse;
import com.afinos.sdk.auth.R;
import com.afinos.sdk.auth.User;
import com.afinos.sdk.auth.util.GoogleApiHelper;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;

public class GoogleProvider implements Provider, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleProvider";

    private static final String EMAIL = "email";
    private static final String SCOPES = "scopes";

    private static final int RC_SIGN_IN = 20;

    private GoogleApiClient mGoogleApiClient;
    private FragmentActivity mActivity;
    private HashMap<String, Object> mParams;
    private AuthCallback<AuthResponse> mIdpCallback;
    private boolean mSpecificAccount;

    public GoogleProvider(AppCompatActivity activity, HashMap<String, Object> params) {
        mActivity = activity;
        mParams = params;
        String email = String.valueOf(params.get(EMAIL));
        mSpecificAccount = !TextUtils.isEmpty(email);
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .enableAutoManage(mActivity, GoogleApiHelper.getSafeAutoManageId(), this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, getSignInOptions(email))
                .build();
    }

    public static AuthCredential createAuthCredential(AuthResponse response) {
        return GoogleAuthProvider.getCredential(response.getIdpToken(), null);
    }

    private GoogleSignInOptions getSignInOptions(@Nullable String email) {
        String clientId = mActivity.getString(R.string.default_web_client_id);

        GoogleSignInOptions.Builder builder =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(clientId);

        String[] scopes = (String[]) mParams.get(SCOPES);
        // Add additional scopes
        for (String scopeString : scopes) {
            builder.requestScopes(new Scope(scopeString));
        }

        if (!TextUtils.isEmpty(email)) {
            builder.setAccountName(email);
        }

        return builder.build();
    }

    @Override
    public void setAuthenticationCallback(AuthCallback<AuthResponse> callback) {
        mIdpCallback = callback;
    }

    public void disconnect() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    private AuthResponse createSocialResponse(GoogleSignInAccount account) {
        return new AuthResponse.Builder(
                new User.Builder(GoogleAuthProvider.PROVIDER_ID, account.getEmail())
                        .setName(account.getDisplayName())
                        .setLastName(account.getFamilyName())
                        .setFirstName(account.getGivenName())
                        .setPhotoUri(account.getPhotoUrl())
                        .build())
                .setToken(account.getIdToken())
                .build();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null) {
                if (result.isSuccess()) {
                    if (mSpecificAccount) {
                        Toast.makeText(
                                mActivity,
                                mActivity.getString(
                                        R.string.fui_signed_in_with_specific_account,
                                        result.getSignInAccount().getEmail()),
                                Toast.LENGTH_SHORT).show();
                    }
                    mIdpCallback.onSuccess(createSocialResponse(result.getSignInAccount()));
                } else {
                    onError(result);
                }
            } else {
                onError("No result found in intent");
            }
        }
    }

    @Override
    public void startLogin(Activity activity) {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onError(GoogleSignInResult result) {
        Status status = result.getStatus();

        if (status.getStatusCode() == CommonStatusCodes.INVALID_ACCOUNT) {
            mGoogleApiClient.stopAutoManage(mActivity);
            mGoogleApiClient.disconnect();
            mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                    .enableAutoManage(mActivity, GoogleApiHelper.getSafeAutoManageId(), this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, getSignInOptions(null))
                    .build();
            startLogin(mActivity);
        } else {
            if (status.getStatusCode() == CommonStatusCodes.DEVELOPER_ERROR) {
                Log.w(TAG, "Developer error: this application is misconfigured. Check your SHA1 " +
                        " and package name in the Firebase console.");
                Toast.makeText(mActivity, "Developer error.", Toast.LENGTH_SHORT).show();
            }
            onError(status.getStatusCode() + " " + status.getStatusMessage());
        }
    }

    private void onError(String errorMessage) {
        Log.e(TAG, "Error logging in with Google. " + errorMessage);
        mIdpCallback.onFailure();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed:" + connectionResult);
    }
}

