package com.afinos.sdk.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import com.afinos.sdk.auth.ui.ExtraConstants;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class User implements Parcelable {
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.readString(),
                    in.<Uri>readParcelable(Uri.class.getClassLoader()));
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    private final String mProviderId;
    private final String mEmail;
    private final String mPhoneNumber;
    private final String mName;
    private String mLastName;
    private String mFirstName;
    private String mGender;
    private final Uri mPhotoUri;

    private User(String providerId,
                 String email,
                 String phoneNumber,
                 String name,
                 String lastName,
                 String firstName,
                 String gender,
                 Uri photoUri) {
        mProviderId = providerId;
        mEmail = email;
        mPhoneNumber = phoneNumber;
        mName = name;
        mLastName = lastName;
        mFirstName = firstName;
        mGender = gender;
        mPhotoUri = photoUri;
    }

    public static User getUser(Intent intent) {
        return intent.getParcelableExtra(ExtraConstants.EXTRA_USER);
    }

    public static User getUser(Bundle arguments) {
        return arguments.getParcelable(ExtraConstants.EXTRA_USER);
    }

    @NonNull
    @Auth.SupportedProvider
    public String getProviderId() {
        return mProviderId;
    }

    @Nullable
    public String getEmail() {
        return mEmail;
    }

    @Nullable
    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    @Nullable
    public String getName() {
        return mName;
    }

    @Nullable
    public String getLastName() {
        return mLastName;
    }

    @Nullable
    public String getFirstName() {
        return mFirstName;
    }

    @Nullable
    public String getGender() {
        return mGender;
    }

    @Nullable
    public Uri getPhotoUri() {
        return mPhotoUri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return mProviderId.equals(user.mProviderId)
                && (mEmail == null ? user.mEmail == null : mEmail.equals(user.mEmail))
                && (mName == null ? user.mName == null : mName.equals(user.mName))
                && (mLastName == null ? user.mLastName == null : mLastName.equals(user.mLastName))
                && (mFirstName == null ? user.mFirstName == null : mFirstName.equals(user.mFirstName))
                && (mGender == null ? user.mGender == null : mGender.equals(user.mGender))
                && (mPhotoUri == null ? user.mPhotoUri == null : mPhotoUri.equals(user.mPhotoUri));
    }

    @Override
    public int hashCode() {
        int result = mProviderId.hashCode();
        result = 31 * result + (mEmail == null ? 0 : mEmail.hashCode());
        result = 31 * result + (mName == null ? 0 : mName.hashCode());
        result = 31 * result + (mLastName == null ? 0 : mLastName.hashCode());
        result = 31 * result + (mFirstName == null ? 0 : mFirstName.hashCode());
        result = 31 * result + (mGender == null ? 0 : mGender.hashCode());
        result = 31 * result + (mPhotoUri == null ? 0 : mPhotoUri.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "mProviderId='" + mProviderId + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mName='" + mName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mFirstName='" + mFirstName + '\'' +
                ", mGender='" + mGender + '\'' +
                ", mPhotoUri=" + mPhotoUri +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mProviderId);
        dest.writeString(mEmail);
        dest.writeString(mPhoneNumber);
        dest.writeString(mName);
        dest.writeParcelable(mPhotoUri, flags);
    }

    public static class Builder {
        private String mProviderId;
        private String mEmail;
        private String mPhoneNumber;
        private String mName;
        private String mLastName;
        private String mFirstName;
        private String mGender;
        private Uri mPhotoUri;

        public Builder(@Auth.SupportedProvider @NonNull String providerId,
                       @Nullable String email) {
            mProviderId = providerId;
            mEmail = email;
        }

        public Builder setPhoneNumber(String phoneNumber) {
            mPhoneNumber = phoneNumber;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setFirstName(String firstName) {
            mFirstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            mLastName = lastName;
            return this;
        }

        public Builder setGender(String gender) {
            mGender = gender;
            return this;
        }

        public Builder setPhotoUri(Uri photoUri) {
            mPhotoUri = photoUri;
            return this;
        }

        public User build() {
            return new User(mProviderId,
                    mEmail,
                    mPhoneNumber,
                    mName,
                    mLastName,
                    mFirstName,
                    mGender,
                    mPhotoUri);
        }
    }
}
