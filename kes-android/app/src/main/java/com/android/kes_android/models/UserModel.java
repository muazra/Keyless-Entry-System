package com.android.kes_android.models;

import java.util.List;

public class UserModel {

    private String mAdminUsername;
    private String mAdminName;
    private String mFullName;
    private String mUsername;
    private String mPassword;
    private List<PhotoModel> mPhotos;

    public String getAdminUsername() {
        return mAdminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        mAdminUsername = adminUsername;
    }

    public String getAdminName() {
        return mAdminName;
    }

    public void setAdminName(String adminName) {
        mAdminName = adminName;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public List<PhotoModel> getPhotos() {
        return mPhotos;
    }

    public void setPhotos(List<PhotoModel> photos) {
        mPhotos = photos;
    }

}
