package com.android.kes_android.models;

public class DeviceModel {

    private String mDeviceID;
    private String mAvailable;
    private String mAdmin;
    private String mStatus;
    private String mBattery;

    public DeviceModel() {}

    public String getDeviceID() {
        return mDeviceID;
    }

    public void setDeviceID(String deviceID) {
        mDeviceID = deviceID;
    }

    public String getAvailable() {
        return mAvailable;
    }

    public void setAvailable(String available) {
        mAvailable = available;
    }

    public String getAdmin() {
        return mAdmin;
    }

    public void setAdmin(String admin) {
        mAdmin = admin;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public String getBattery() {
        return mBattery;
    }

    public void setBattery(String battery) {
        mBattery = battery;
    }

}
