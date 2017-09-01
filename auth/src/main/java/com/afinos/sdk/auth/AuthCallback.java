package com.afinos.sdk.auth;

/**
 * Created by itphe on 9/1/2017.
 */

public interface AuthCallback<T> {
    void onSuccess(T response);

    void onLoadingChange(boolean loading);

    void onFailure();
}
