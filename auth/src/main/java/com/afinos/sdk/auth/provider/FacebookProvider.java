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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.afinos.sdk.auth.AuthCallback;
import com.afinos.sdk.auth.AuthResponse;
import com.afinos.sdk.auth.User;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FacebookProvider implements Provider, FacebookCallback<LoginResult> {
    private static final String TAG = "FacebookProvider";
    public static final String SCOPES = "scopes";
    private static final String EMAIL = "email";
    private static final String PUBLIC_PROFILE = "public_profile";

    private static CallbackManager sCallbackManager;

    private final List<String> mScopes;
    // DO NOT USE DIRECTLY: see onSuccess(String, LoginResult) and onFailure(Bundle) below
    private AuthCallback<AuthResponse> mCallbackObject;

    public FacebookProvider(HashMap<String, Object> params) {
        String[] scopes = (String[]) params.get(SCOPES);
        if (scopes == null) {
            mScopes = new ArrayList<>();
        } else {
            mScopes = Arrays.asList(scopes);
        }
//        FacebookSdk.setWebDialogTheme(theme);
    }

    public static AuthCredential createAuthCredential(AuthResponse response) {
        if (!response.getProviderType().equals(FacebookAuthProvider.PROVIDER_ID)) {
            return null;
        }
        return FacebookAuthProvider.getCredential(response.getIdpToken());
    }

    @Override
    public void startLogin(Activity activity) {
        sCallbackManager = CallbackManager.Factory.create();
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.registerCallback(sCallbackManager, this);

        List<String> permissionsList = new ArrayList<>(mScopes);

        // Ensure we have email and public_profile scopes
        if (!permissionsList.contains(EMAIL)) {
            permissionsList.add(EMAIL);
        }

        if (!permissionsList.contains(PUBLIC_PROFILE)) {
            permissionsList.add(PUBLIC_PROFILE);
        }

        // Log in with permissions
        loginManager.logInWithReadPermissions(activity, permissionsList);
    }

    @Override
    public void setAuthenticationCallback(AuthCallback<AuthResponse> callback) {
        mCallbackObject = callback;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (sCallbackManager != null) {
            sCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSuccess(final LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        FacebookRequestError requestError = response.getError();
                        if (requestError != null) {
                            Log.e(TAG,
                                    "Received Facebook error: " + requestError.getErrorMessage());
                            onFailure();
                            return;
                        }
                        if (object == null) {
                            Log.w(TAG, "Received null response from Facebook GraphRequest");
                            onFailure();
                        } else {
                            String email = null;
                            String name = null;
                            String lastName = null;
                            String firstName = null;
                            String gender = null;
                            Uri photoUri = null;

                            try {
                                email = object.getString("email");
                            } catch (JSONException e) {
                                Log.e(TAG, "Failure retrieving Facebook email", e);
                            }
                            try {
                                name = object.getString("name");
                            } catch (JSONException ignored) {}
                            try {
                                lastName = object.getString("last_name");
                            } catch (JSONException ignored) {}
                            try {
                                firstName = object.getString("first_name");
                            } catch (JSONException ignored) {}
                            try {
                                gender = object.getString("gender");
                            } catch (JSONException ignored) {}
                            try {
                                photoUri = Uri.parse(object.getJSONObject("picture")
                                        .getJSONObject("data")
                                        .getString("url"));
                            } catch (JSONException ignored) {}
                            onSuccess(loginResult,
                                    email,
                                    name,
                                    lastName,
                                    firstName,
                                    gender,
                                    photoUri);
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onCancel() {
        onFailure();
    }

    @Override
    public void onError(FacebookException error) {
        Log.e(TAG, "Error logging in with Facebook. " + error.getMessage());
        onFailure();
    }

    private void onSuccess(LoginResult loginResult,
                           @Nullable String email,
                           String name,
                           String lastName,
                           String firstName,
                           String gender,
                           Uri photoUri) {
        gcCallbackManager();
        mCallbackObject.onSuccess(new AuthResponse.Builder(
                new User.Builder(FacebookAuthProvider.PROVIDER_ID, email)
                        .setName(name)
                        .setLastName(lastName)
                        .setFirstName(firstName)
                        .setGender(gender)
                        .setPhotoUri(photoUri)
                        .build())
                .setToken(loginResult.getAccessToken().getToken())
                .build());
    }

    private void onFailure() {
        gcCallbackManager();
        mCallbackObject.onFailure();
    }

    private void gcCallbackManager() {
        sCallbackManager = null;
    }
}
