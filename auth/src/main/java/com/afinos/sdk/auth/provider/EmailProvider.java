package com.afinos.sdk.auth.provider;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.afinos.sdk.auth.AuthCallback;
import com.afinos.sdk.auth.AuthResponse;
import com.afinos.sdk.auth.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class EmailProvider implements NonSocialProvider {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private HashMap<String, Object> mParams;

    private AuthCallback<AuthResponse> mAuthCallback;

    public EmailProvider(HashMap<String, Object> params) {
        mParams = params;
    }

    private AuthResponse createEmailResponse(FirebaseUser user) {
        return new AuthResponse.Builder(
                new User.Builder(EmailAuthProvider.PROVIDER_ID, user.getEmail())
                        .setName(user.getDisplayName())
                        .setPhoneNumber(user.getPhoneNumber())
                        .build()).build();
    }

    @Override
    public void startLogin(Activity activity) {
        mAuthCallback.onLoadingChange(true);
        String username = String.valueOf(mParams.get(USERNAME));
        String password = String.valueOf(mParams.get(PASSWORD));
        AuthCredential authCredential = EmailAuthProvider.getCredential(username, password);
        FirebaseAuth.getInstance()
                .signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mAuthCallback.onSuccess(createEmailResponse(task.getResult().getUser()));
                        mAuthCallback.onLoadingChange(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mAuthCallback.onLoadingChange(false);
                        mAuthCallback.onFailure();
                    }
                });
    }

    @Override
    public void startRegister(Activity activity) {
        mAuthCallback.onLoadingChange(true);
        String username = String.valueOf(mParams.get(USERNAME));
        String password = String.valueOf(mParams.get(PASSWORD));
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mAuthCallback.onSuccess(createEmailResponse(task.getResult().getUser()));
                        mAuthCallback.onLoadingChange(false);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mAuthCallback.onLoadingChange(false);
                        mAuthCallback.onFailure();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void setAuthenticationCallback(AuthCallback<AuthResponse> callback) {
        mAuthCallback = callback;
    }
}
