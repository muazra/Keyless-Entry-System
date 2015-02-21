package com.android.kes_android.models;

public class PhotoModel {

    private String mProfileType;
    private String mProfileName;
    private String mPhotoFilepath;
    private String mPhotoSimplename;

    public String getProfileType() { return mProfileType; }

    public void setProfileType(String profileType) {
        mProfileType = profileType;
    }

    public String getProfileName() {
        return mProfileName;
    }

    public void setProfileName(String profileName) {
        mProfileName = profileName;
    }

    public String getPhotoFilepath() {
        return mPhotoFilepath;
    }

    public void setPhotoFilepath(String photoFilepath) {
        mPhotoFilepath = photoFilepath;
    }

    public String getPhotoSimplename() {
        return mPhotoSimplename;
    }

    public void setPhotoSimplename(String photoSimplename) {
        mPhotoSimplename = photoSimplename;
    }

}
