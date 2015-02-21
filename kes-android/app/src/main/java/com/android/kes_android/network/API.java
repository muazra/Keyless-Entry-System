package com.android.kes_android.network;

public class API {

    public static final String API_KEY = "/api/KES_MJN";
    public static final String BASE_URL = "http://3dc6913b.ngrok.com";

    public static final String LOGIN_ADMIN = BASE_URL + API_KEY + "/admin/login";
    public static final String LOGIN_USER = BASE_URL + API_KEY + "/user/login";
    public static final String NEW_ADMIN = BASE_URL + API_KEY + "/newadmin";

    public static final String GET_USERS = BASE_URL + API_KEY + "/users";
    public static final String GET_GUESTS = BASE_URL + API_KEY + "/guests";

    public static final String GET_ADMIN = BASE_URL + API_KEY + "/getadmin";
    public static final String GET_USER = BASE_URL + API_KEY + "/getuser";
    public static final String GET_GUEST = BASE_URL + API_KEY + "/getguest";

    public static final String ADD_USER = BASE_URL + API_KEY + "/user/add";
    public static final String ADD_GUEST = BASE_URL + API_KEY + "/guest/add";

    public static final String DELETE_USER = BASE_URL + API_KEY + "/user/delete";
    public static final String DELETE_GUEST = BASE_URL + API_KEY + "/guest/delete";

    public static final String ADD_PHOTO = BASE_URL + API_KEY + "/photo/add";
    public static final String REMOVE_PHOTO = BASE_URL + API_KEY + "/photo/remove";

    public static final String TOGGLE_DOOR = BASE_URL + API_KEY + "/toggledoor";
    public static final String TOGGLE_DOOR_STATUS = BASE_URL + API_KEY + "/toggledoorstatus";

    public static final String GET_DOOR_ACTIVITY = BASE_URL + API_KEY + "/dooractivities";

    public static final String DASH_INFO = BASE_URL + API_KEY + "/dashinfo";

    public static boolean isSuccess(String result){
        return result.equals("success");
    }

}
