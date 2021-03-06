package com.afinos.skd.auth;

import android.app.Activity;

/**
 * Result codes returned when using {@link AuthUI.SignInIntentBuilder#build()} with {@code
 * startActivityForResult}.
 *
 * @deprecated Check for {@link Activity#RESULT_OK} and {@link Activity#RESULT_CANCELED} instead.
 */
@Deprecated
public final class ResultCodes {
    /**
     * Sign in succeeded
     **/
    public static final int OK = Activity.RESULT_OK;

    /**
     * Sign in canceled by user
     **/
    public static final int CANCELED = Activity.RESULT_CANCELED;

    private ResultCodes() {
        // no instance
    }
}
