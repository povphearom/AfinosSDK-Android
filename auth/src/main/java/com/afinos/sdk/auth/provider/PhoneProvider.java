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
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneProvider implements NonSocialProvider {
    public final static String PHONE = "phone";
    private AuthCallback<AuthResponse> mAuthCallback;

    private HashMap<String, Object> mParams;

    public PhoneProvider(HashMap<String, Object> parameters) {
        mParams = parameters;
    }

    private AuthResponse createPhoneResponse(FirebaseUser user) {
        return new AuthResponse.Builder(
                new User.Builder(EmailAuthProvider.PROVIDER_ID, user.getEmail())
                        .setName(user.getDisplayName())
                        .setPhoneNumber(user.getPhoneNumber())
                        .build()).build();
    }

    @Override
    public void startLogin(Activity activity) {
        mAuthCallback.onLoadingChange(true);
        String phone = String.valueOf(mParams.get(PHONE));
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,
                60,
                TimeUnit.SECONDS,
                activity,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential credential) {
                        FirebaseAuth.getInstance()
                                .signInWithCredential(credential)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        mAuthCallback.onSuccess(createPhoneResponse(task.getResult()
                                                .getUser()));
                                        mAuthCallback.onLoadingChange(true);
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
                    public void onVerificationFailed(FirebaseException e) {
                        mAuthCallback.onLoadingChange(false);
                    }
                });
    }

    @Override
    public void startRegister(Activity activity) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void setAuthenticationCallback(AuthCallback<AuthResponse> callback) {
        mAuthCallback = callback;
    }
}
