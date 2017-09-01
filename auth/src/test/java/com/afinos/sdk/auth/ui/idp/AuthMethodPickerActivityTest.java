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

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.afinos.sdk.auth.Auth;
import com.afinos.sdk.auth.BuildConfig;
import com.afinos.sdk.auth.R;
import com.afinos.sdk.auth.testhelpers.AuthHelperShadow;
import com.afinos.sdk.auth.testhelpers.AutoCompleteTask;
import com.afinos.sdk.auth.testhelpers.FacebookProviderShadow;
import com.afinos.sdk.auth.testhelpers.FakeAuthResult;
import com.afinos.sdk.auth.testhelpers.GoogleProviderShadow;
import com.afinos.sdk.auth.testhelpers.LoginManagerShadow;
import com.afinos.sdk.auth.testhelpers.TestConstants;
import com.afinos.sdk.auth.testhelpers.TestHelper;
import com.afinos.sdk.auth.ui.email.RegisterEmailActivity;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.Arrays;
import java.util.List;

import static com.afinos.sdk.auth.testhelpers.TestHelper.verifySmartLockSave;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class,
        shadows = {
                GoogleProviderShadow.class,
                FacebookProviderShadow.class,
                LoginManagerShadow.class
        }, sdk = 25)
public class AuthMethodPickerActivityTest {
    @Test
    public void testAllProvidersArePopulated() {
        List<String> providers = Arrays.asList(
                Auth.FACEBOOK_PROVIDER,
                Auth.GOOGLE_PROVIDER,
                Auth.TWITTER_PROVIDER,
                Auth.EMAIL_PROVIDER,
                Auth.PHONE_VERIFICATION_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        assertEquals(providers.size(),
                     ((LinearLayout) authMethodPickerActivity.findViewById(R.id.btn_holder))
                             .getChildCount());
        Button emailButton = authMethodPickerActivity.findViewById(R.id.email_button);
        assertEquals(View.VISIBLE, emailButton.getVisibility());
    }

    @Test
    public void testEmailLoginFlow() {
        List<String> providers = Arrays.asList(Auth.EMAIL_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        Button emailButton = authMethodPickerActivity.findViewById(R.id.email_button);
        emailButton.performClick();
        ShadowActivity.IntentForResult nextIntent =
                Shadows.shadowOf(authMethodPickerActivity).getNextStartedActivityForResult();

        assertEquals(
                RegisterEmailActivity.class.getName(),
                nextIntent.intent.getComponent().getClassName());
    }

    @Test
    public void testPhoneLoginFlow() {
        List<String> providers = Arrays.asList(Auth.PHONE_VERIFICATION_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        Button phoneButton = authMethodPickerActivity.findViewById(R.id.phone_button);
        phoneButton.performClick();
        ShadowActivity.IntentForResult nextIntent =
                Shadows.shadowOf(authMethodPickerActivity).getNextStartedActivityForResult();

        assertEquals(
                PhoneVerificationActivity.class.getName(),
                nextIntent.intent.getComponent().getClassName());
    }

    @Test
    @Config(shadows = {AuthHelperShadow.class, AuthHelperShadow.class})
    public void testFacebookLoginFlow() {
        // initialize mocks
        reset(AuthHelperShadow.getSaveSmartLockInstance(null));

        when(AuthHelperShadow.getCurrentUser().getProviders())
                .thenReturn(Arrays.asList(FacebookAuthProvider.PROVIDER_ID));
        when(AuthHelperShadow.getFirebaseAuth().signInWithCredential((AuthCredential) any()))
                .thenReturn(new AutoCompleteTask<>(FakeAuthResult.INSTANCE, true, null));

        List<String> providers = Arrays.asList(Auth.FACEBOOK_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        Button facebookButton = authMethodPickerActivity.findViewById(R.id.facebook_button);
        assertNotNull(facebookButton);
        facebookButton.performClick();

        verifySmartLockSave(Auth.FACEBOOK_PROVIDER, TestConstants.EMAIL, null);
    }

    @Test
    @Config(shadows = {GoogleProviderShadow.class, AuthHelperShadow.class, AuthHelperShadow.class})
    public void testGoogleLoginFlow() {
        // initialize mocks
        reset(AuthHelperShadow.getSaveSmartLockInstance(null));

        List<String> providers = Arrays.asList(Auth.GOOGLE_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        when(AuthHelperShadow.getCurrentUser().getProviders())
                .thenReturn(Arrays.asList(GoogleAuthProvider.PROVIDER_ID));

        when(AuthHelperShadow.getFirebaseAuth().signInWithCredential((AuthCredential) any()))
                .thenReturn(new AutoCompleteTask<>(FakeAuthResult.INSTANCE, true, null));

        Button googleButton = authMethodPickerActivity.findViewById(R.id.google_button);

        assertNotNull(googleButton);
        googleButton.performClick();

        verifySmartLockSave(Auth.GOOGLE_PROVIDER, TestConstants.EMAIL, null);
    }

    @Test
    @Config(shadows = {AuthHelperShadow.class})
    public void testTwitterLoginFlowStarts() {
        List<String> providers = Arrays.asList(Auth.TWITTER_PROVIDER);

        AuthMethodPickerActivity authMethodPickerActivity = createActivity(providers);

        when(AuthHelperShadow.getCurrentUser().getProviders())
                .thenReturn(Arrays.asList(TwitterAuthProvider.PROVIDER_ID));

        when(AuthHelperShadow.getFirebaseAuth().signInWithCredential(any(AuthCredential.class)))
                .thenReturn(new AutoCompleteTask<>(FakeAuthResult.INSTANCE, true, null));
        Button twitterButton =
                authMethodPickerActivity.findViewById(R.id.twitter_button);

        assertNotNull(twitterButton);
        twitterButton.performClick();
        ShadowActivity.IntentForResult nextIntent =
                Shadows.shadowOf(authMethodPickerActivity).getNextStartedActivityForResult();

        assertTrue(nextIntent.intent.getComponent().getClassName().contains("com.twitter.sdk"));
    }

    private AuthMethodPickerActivity createActivity(List<String> providers) {
        Intent startIntent = AuthMethodPickerActivity.createIntent(
                RuntimeEnvironment.application,
                TestHelper.getFlowParameters(providers));

        return Robolectric
                .buildActivity(AuthMethodPickerActivity.class, startIntent)
                .create()
                .visible()
                .get();
    }
}
