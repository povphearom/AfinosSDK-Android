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

package com.afinos.sdk.auth.ui.idp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afinos.sdk.auth.Auth;
import com.afinos.sdk.auth.Auth.IdpConfig;
import com.afinos.sdk.auth.IdpResponse;
import com.afinos.sdk.auth.R;
import com.afinos.sdk.auth.provider.EmailProvider;
import com.afinos.sdk.auth.provider.FacebookProvider;
import com.afinos.sdk.auth.provider.GoogleProvider;
import com.afinos.sdk.auth.provider.IdpProvider;
import com.afinos.sdk.auth.provider.IdpProvider.IdpCallback;
import com.afinos.sdk.auth.provider.PhoneProvider;
import com.afinos.sdk.auth.provider.Provider;
import com.afinos.sdk.auth.provider.ProviderUtils;
import com.afinos.sdk.auth.provider.TwitterProvider;
import com.afinos.sdk.auth.ui.AppCompatBase;
import com.afinos.sdk.auth.ui.FlowParameters;
import com.afinos.sdk.auth.ui.HelperActivityBase;
import com.afinos.sdk.auth.ui.TaskFailureLogger;
import com.afinos.sdk.auth.ui.email.RegisterEmailActivity;
import com.afinos.sdk.auth.util.signincontainer.SaveSmartLock;
import com.google.firebase.auth.AuthCredential;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents the list of authentication options for this app to the user. If an identity provider
 * option is selected, a {@link CredentialSignInHandler} is launched to manage the IDP-specific
 * sign-in flow. If email authentication is chosen, the {@link RegisterEmailActivity} is started. if
 * phone authentication is chosen, the {@link com.afinos.sdk.auth.ui.phone.PhoneVerificationActivity}
 * is started.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class AuthMethodPickerActivity extends AppCompatBase implements IdpCallback {
    private static final String TAG = "AuthMethodPicker";

    private static final int RC_ACCOUNT_LINK = 3;

    private List<Provider> mProviders;
    @Nullable
    private SaveSmartLock mSaveSmartLock;

    public static Intent createIntent(Context context, FlowParameters flowParams) {
        return HelperActivityBase.createBaseIntent(
                context, AuthMethodPickerActivity.class, flowParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fui_auth_method_picker_layout);
        mSaveSmartLock = getAuthHelper().getSaveSmartLockInstance(this);

        populateIdpList(getFlowParams().providerInfo);

        int logoId = getFlowParams().logoId;
        if (logoId == Auth.NO_LOGO) {
            findViewById(R.id.logo).setVisibility(View.GONE);

            ConstraintLayout layout = findViewById(R.id.root);
            ConstraintSet constraints = new ConstraintSet();
            constraints.clone(layout);
            constraints.setHorizontalBias(R.id.container, 0.5f);
            constraints.setVerticalBias(R.id.container, 0.5f);
            constraints.applyTo(layout);
        } else {
            ImageView logo = findViewById(R.id.logo);
            logo.setImageResource(logoId);
        }
    }

    private void populateIdpList(List<IdpConfig> providers) {
        mProviders = new ArrayList<>();
        for (IdpConfig idpConfig : providers) {
            switch (idpConfig.getProviderId()) {
                case Auth.GOOGLE_PROVIDER:
                    mProviders.add(new GoogleProvider(this, idpConfig));
                    break;
                case Auth.FACEBOOK_PROVIDER:
                    mProviders.add(new FacebookProvider(
                            idpConfig, getFlowParams().themeId));
                    break;
                case Auth.TWITTER_PROVIDER:
                    mProviders.add(new TwitterProvider(this));
                    break;
                case Auth.EMAIL_PROVIDER:
                    mProviders.add(new EmailProvider(this, getFlowParams()));
                    break;
                case Auth.PHONE_VERIFICATION_PROVIDER:
                    mProviders.add(new PhoneProvider(this, getFlowParams()));
                    break;
                default:
                    Log.e(TAG, "Encountered unknown provider parcel with type: "
                            + idpConfig.getProviderId());
            }
        }

        ViewGroup btnHolder = findViewById(R.id.btn_holder);
        for (final Provider provider : mProviders) {
            View loginButton = getLayoutInflater()
                    .inflate(provider.getButtonLayout(), btnHolder, false);

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (provider instanceof IdpProvider) {
                        getDialogHolder().showLoadingDialog(R.string.fui_progress_dialog_loading);
                    }
                    provider.startLogin(AuthMethodPickerActivity.this);
                }
            });
            if (provider instanceof IdpProvider) {
                ((IdpProvider) provider).setAuthenticationCallback(this);
            }
            btnHolder.addView(loginButton);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ACCOUNT_LINK) {
            finish(resultCode, data);
        } else {
            for (Provider provider : mProviders) {
                provider.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onSuccess(IdpResponse response) {
        AuthCredential credential = ProviderUtils.getAuthCredential(response);
        getAuthHelper().getFirebaseAuth()
                .signInWithCredential(credential)
                .addOnCompleteListener(new CredentialSignInHandler(
                        this,
                        mSaveSmartLock,
                        RC_ACCOUNT_LINK,
                        response))
                .addOnFailureListener(
                        new TaskFailureLogger(TAG, "Firebase sign in with credential " +
                                credential.getProvider() + " unsuccessful. " +
                                "Visit https://console.firebase.google.com to enable it."));
    }

    @Override
    public void onFailure() {
        // stay on this screen
        getDialogHolder().dismissDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProviders != null) {
            for (Provider provider : mProviders) {
                if (provider instanceof GoogleProvider) {
                    ((GoogleProvider) provider).disconnect();
                }
            }
        }
    }
}