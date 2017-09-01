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

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v7.app.AppCompatActivity;

import com.afinos.sdk.auth.provider.EmailProvider;
import com.afinos.sdk.auth.provider.FacebookProvider;
import com.afinos.sdk.auth.provider.GoogleProvider;
import com.afinos.sdk.auth.provider.PhoneProvider;
import com.afinos.sdk.auth.provider.Provider;
import com.afinos.sdk.auth.provider.TwitterProvider;
import com.afinos.sdk.auth.util.GoogleSignInHelper;
import com.afinos.sdk.auth.util.signincontainer.SmartLockBase;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;


public class Auth {
    @StringDef({
                       EmailAuthProvider.PROVIDER_ID, EMAIL_PROVIDER,
                       PhoneAuthProvider.PROVIDER_ID, PHONE_VERIFICATION_PROVIDER,
                       GoogleAuthProvider.PROVIDER_ID, GOOGLE_PROVIDER,
                       FacebookAuthProvider.PROVIDER_ID, FACEBOOK_PROVIDER,
                       TwitterAuthProvider.PROVIDER_ID, TWITTER_PROVIDER
               })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SupportedProvider {}

    public static final String EMAIL_PROVIDER = EmailAuthProvider.PROVIDER_ID;

    public static final String GOOGLE_PROVIDER = GoogleAuthProvider.PROVIDER_ID;

    public static final String FACEBOOK_PROVIDER = FacebookAuthProvider.PROVIDER_ID;

    public static final String TWITTER_PROVIDER = TwitterAuthProvider.PROVIDER_ID;

    public static final String PHONE_VERIFICATION_PROVIDER = PhoneAuthProvider.PROVIDER_ID;

    public static final int NO_LOGO = -1;

    /**
     * The set of authentication providers supported in Firebase Auth UI.
     */
    public static final Set<String> SUPPORTED_PROVIDERS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    EMAIL_PROVIDER,
                    GOOGLE_PROVIDER,
                    FACEBOOK_PROVIDER,
                    TWITTER_PROVIDER,
                    PHONE_VERIFICATION_PROVIDER
            )));

    private static final IdentityHashMap<FirebaseApp, Auth> INSTANCES = new IdentityHashMap<>();

    private final FirebaseApp mApp;
    private final FirebaseAuth mAuth;

    private Auth(FirebaseApp app) {
        mApp = app;
        mAuth = FirebaseAuth.getInstance(mApp);
    }

    /**
     * Retrieves the {@link Auth} instance associated with the default app, as returned by {@code
     * FirebaseApp.getInstance()}.
     *
     * @throws IllegalStateException if the default app is not initialized.
     */
    public static Auth getInstance() {
        return getInstance(FirebaseApp.getInstance());
    }

    /**
     * Retrieves the {@link Auth} instance associated the the specified app.
     */
    public static Auth getInstance(FirebaseApp app) {
        Auth authUi;
        synchronized (INSTANCES) {
            authUi = INSTANCES.get(app);
            if (authUi == null) {
                authUi = new Auth(app);
                INSTANCES.put(app, authUi);
            }
        }
        return authUi;
    }

    @SuppressWarnings("unchecked")
    public void signIn(AppCompatActivity activity,
                       String providerId,
                       HashMap<String, Object> params,
                       AuthCallback<AuthResponse> authCallback) {
        Provider provider = null;
        switch (providerId) {
            case GoogleAuthProvider.PROVIDER_ID:
                provider = new GoogleProvider(activity, params);
                break;
            case FacebookAuthProvider.PROVIDER_ID:
                provider = new FacebookProvider(params);
                break;
            case TwitterAuthProvider.PROVIDER_ID:
                provider = new TwitterProvider(activity);
                break;
            case EmailAuthProvider.PROVIDER_ID:
                provider = new EmailProvider(params);
                break;
            case PhoneAuthProvider.PROVIDER_ID:
                provider = new PhoneProvider(params);
                break;
        }
        if (provider == null) return;
        provider.setAuthenticationCallback(authCallback);
        provider.startLogin(activity);
    }

    /**
     * Signs the current user out, if one is signed in.
     *
     * @param activity the activity requesting the user be signed out
     * @return A task which, upon completion, signals that the user has been signed out ({@link
     * Task#isSuccessful()}, or that the sign-out attempt failed unexpectedly !{@link
     * Task#isSuccessful()}).
     */
    public Task<Void> signOut(@NonNull AppCompatActivity activity) {
        // Get Credentials Helper
        GoogleSignInHelper signInHelper = GoogleSignInHelper.getInstance(activity);

        // Firebase Sign out
        mAuth.signOut();

        // Disable credentials auto sign-in
        Task<Status> disableCredentialsTask = signInHelper.disableAutoSignIn();

        // Google sign out
        Task<Status> signOutTask = signInHelper.signOut();

        // Facebook sign out
        try {
            LoginManager.getInstance().logOut();
        } catch (NoClassDefFoundError e) {
            // do nothing
        }

        // Twitter sign out
        try {
            TwitterProvider.signOut(activity);
        } catch (NoClassDefFoundError e) {
            // do nothing
        }
        // Wait for all tasks to complete
        return Tasks.whenAll(disableCredentialsTask, signOutTask);
    }

    /**
     * Delete the use from FirebaseAuth and delete any associated credentials from the Credentials
     * API. Returns a {@link Task} that succeeds if the Firebase Auth user deletion succeeds and
     * fails if the Firebase Auth deletion fails. Credentials deletion failures are handled
     * silently.
     *
     * @param activity the calling {@link Activity}.
     */
    public Task<Void> delete(@NonNull AppCompatActivity activity) {
        // Initialize SmartLock helper
        GoogleSignInHelper signInHelper = GoogleSignInHelper.getInstance(activity);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            // If the current user is null, return a failed task immediately
            return Tasks.forException(new Exception("No currently signed in user."));
        }

        // Delete the Firebase user
        Task<Void> deleteUserTask = firebaseUser.delete();

        // Get all SmartLock credentials associated with the user
        List<Credential> credentials = SmartLockBase.credentialsFromFirebaseUser(firebaseUser);

        // For each Credential in the list, create a task to delete it.
        List<Task<?>> credentialTasks = new ArrayList<>();
        for (Credential credential : credentials) {
            credentialTasks.add(signInHelper.delete(credential));
        }

        // Create a combined task that will succeed when all credential delete operations
        // have completed (even if they fail).
        final Task<Void> combinedCredentialTask = Tasks.whenAll(credentialTasks);

        // Chain the Firebase Auth delete task with the combined Credentials task
        // and return.
        return deleteUserTask.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(@NonNull Task<Void> task) throws Exception {
                // Call getResult() to propagate failure by throwing an exception
                // if there was one.
                task.getResult(Exception.class);

                // Return the combined credential task
                return combinedCredentialTask;
            }
        });
    }
}
