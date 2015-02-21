package com.android.kes_android.models;

import java.util.List;

public class LoggedUser {

    public LoggedUserType mType;
    public AdminModel mAdmin;
    public UserModel mUser;

    public DeviceModel mDevice;
    public List<UserModel> mUsers;
    public List<GuestModel> mGuests;
    public List<DoorActivityModel> mDoor;
    public List<PhotoModel> mPhotos;

    private LoggedUser() {}

    static LoggedUser loggedUser = null;
    public static synchronized LoggedUser instance() {
        if (loggedUser == null) loggedUser = new LoggedUser();
        return loggedUser;
    }

}
