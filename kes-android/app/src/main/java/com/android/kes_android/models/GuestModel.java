package com.android.kes_android.models;

import java.util.List;

public class GuestModel {

    private String mAdminUsername;
    private String mAdminName;
    private String mFullName;
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

    public List<PhotoModel> getPhotos() {
        return mPhotos;
    }

    public void setPhotos(List<PhotoModel> photos) {
        mPhotos = photos;
    }

}
