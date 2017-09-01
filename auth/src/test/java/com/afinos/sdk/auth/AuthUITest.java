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

package com.afinos.sdk.auth;

import com.afinos.sdk.auth.Auth.IdpConfig;
import com.afinos.sdk.auth.Auth.SignInIntentBuilder;
import com.afinos.sdk.auth.testhelpers.TestConstants;
import com.afinos.sdk.auth.testhelpers.TestHelper;
import com.afinos.sdk.auth.ui.ExtraConstants;
import com.google.firebase.FirebaseApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 25)
public class AuthUITest {
    private FirebaseApp mFirebaseApp;

    @Before
    public void setUp() {
        mFirebaseApp = TestHelper.initializeApp(RuntimeEnvironment.application);
    }

    @Test
    public void testCreateStartIntent_shouldHaveEmailAsDefaultProvider() {
        FlowParameters flowParameters = Auth
                .getInstance(mFirebaseApp)
                .createSignInIntentBuilder()
                .build()
                .getParcelableExtra(ExtraConstants.EXTRA_FLOW_PARAMS);
        assertEquals(1, flowParameters.providerInfo.size());
        assertEquals(Auth.EMAIL_PROVIDER, flowParameters.providerInfo.get(0).getProviderId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateStartIntent_shouldOnlyAllowOneInstanceOfAnIdp() {
        SignInIntentBuilder startIntent =
                Auth.getInstance(mFirebaseApp).createSignInIntentBuilder();
        startIntent.setAvailableProviders(
                Arrays.asList(new IdpConfig.Builder(Auth.EMAIL_PROVIDER).build(),
                              new IdpConfig.Builder(Auth.EMAIL_PROVIDER).build()));
    }

    @Test
    public void testCreatingStartIntent() {
        FlowParameters flowParameters = Auth.getInstance(mFirebaseApp).createSignInIntentBuilder()
                .setAvailableProviders(
                        Arrays.asList(new IdpConfig.Builder(Auth.EMAIL_PROVIDER).build(),
                                      new IdpConfig.Builder(Auth.GOOGLE_PROVIDER).build(),
                                      new IdpConfig.Builder(Auth.FACEBOOK_PROVIDER).build()))
                .setTosUrl(TestConstants.TOS_URL)
                .setPrivacyPolicyUrl(TestConstants.PRIVACY_URL)
                .build()
                .getParcelableExtra(ExtraConstants.EXTRA_FLOW_PARAMS);

        assertEquals(3, flowParameters.providerInfo.size());
        assertEquals(mFirebaseApp.getName(), flowParameters.appName);
        assertEquals(TestConstants.TOS_URL, flowParameters.termsOfServiceUrl);
        assertEquals(TestConstants.PRIVACY_URL, flowParameters.privacyPolicyUrl);
        assertEquals(Auth.getDefaultTheme(), flowParameters.themeId);
    }
}
