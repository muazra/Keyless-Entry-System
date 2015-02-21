package com.android.kes_android.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.kes_android.R;
import com.android.kes_android.models.AdminModel;
import com.android.kes_android.models.DeviceModel;
import com.android.kes_android.models.DoorActivityModel;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.models.LoggedUserType;
import com.android.kes_android.models.PhotoModel;
import com.android.kes_android.models.UserModel;
import com.android.kes_android.network.API;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends Activity {
    private static final String LOG = LoginActivity.class.getSimpleName();
    private Context mContext = this;

    private TextView mUsername;
    private TextView mPassword;
    private ProgressBar mLoginProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences mPrefs = getSharedPreferences("KES", 0);
        if (mPrefs.getBoolean("Logged", false)) {
            LoggedUser.instance().mType = LoggedUserType.ADMIN;
            if(mPrefs.getString("Type", "ADMIN").equals("USER"))
                LoggedUser.instance().mType = LoggedUserType.USER;

            verifyLogin(mPrefs.getString("username", "username"), mPrefs.getString("password", "password"));
        }

        mUsername = (TextView) findViewById(R.id.login_username);
        mPassword = (TextView) findViewById(R.id.login_password);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);

        Button mSignInAdmin = (Button) findViewById(R.id.login_sign_in_admin);
        Button mSignInUser = (Button) findViewById(R.id.login_sign_in_user);

        mSignInAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                LoggedUser.instance().mType = LoggedUserType.ADMIN;
                verifyLogin(username, password);
            }
        });

        mSignInUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                LoggedUser.instance().mType = LoggedUserType.USER;
                verifyLogin(username, password);
            }
        });

    }

    public void verifyLogin(final String username, final String password) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", username);
        params.put("password", password);

        String LOGIN_URL = API.LOGIN_ADMIN;
        if (LoggedUser.instance().mType == LoggedUserType.USER) {
            LOGIN_URL = API.LOGIN_USER;
        }

        client.post(LOGIN_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart(){
                mLoginProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Login Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Login Credentials Invalid", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //profile
                        JSONObject profile = response.getJSONObject("profile");
                        gatherProfile(profile);

                        //device
                        JSONObject device = response.getJSONObject("device");
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.setDeviceID(device.getString("device_id"));
                        deviceModel.setAvailable(device.getString("available"));
                        deviceModel.setAdmin(device.getString("admin"));
                        deviceModel.setStatus(device.getString("status"));
                        deviceModel.setBattery(device.getString("battery"));
                        LoggedUser.instance().mDevice = deviceModel;

                        //photos
                        JSONArray photosArray = response.getJSONArray("photos");
                        List<PhotoModel> photoModelList = new ArrayList<>();
                        for(int i = 0; i < photosArray.length(); i++){
                            JSONObject photoObj = photosArray.getJSONObject(i);
                            PhotoModel photoModel = new PhotoModel();

                            photoModel.setProfileType(photoObj.getString("profile_type"));
                            photoModel.setProfileName(photoObj.getString("profile_name"));
                            photoModel.setPhotoFilepath(photoObj.getString("photo_filepath"));
                            photoModel.setPhotoSimplename(photoObj.getString("photo_simplename"));
                            photoModelList.add(photoModel);
                        }
                        LoggedUser.instance().mPhotos = photoModelList;

                        //door activities
                        JSONArray doorActivitiesArray = response.getJSONArray("door");
                        List<DoorActivityModel> doorActivityModelList = new ArrayList<>();
                        for(int i = 0; i < doorActivitiesArray.length(); i++){
                            JSONObject doorActivityObj = doorActivitiesArray.getJSONObject(i);
                            DoorActivityModel doorActivityModel = new DoorActivityModel();

                            doorActivityModel.setAdmin(doorActivityObj.getString("admin"));
                            doorActivityModel.setProfileType(doorActivityObj.getString("profile_type"));
                            doorActivityModel.setUsername(doorActivityObj.getString("username"));
                            doorActivityModel.setPhotoSimplename(doorActivityObj.getString("photo_simplename"));
                            doorActivityModel.setGranted(doorActivityObj.getString("granted"));
                            doorActivityModel.setID(doorActivityObj.getString("_id"));

                            doorActivityModelList.add(doorActivityModel);
                        }
                        LoggedUser.instance().mDoor = doorActivityModelList;

                        SharedPreferences mPrefs = getSharedPreferences("KES", 0);
                        SharedPreferences.Editor editor = mPrefs.edit();
                        editor.putBoolean("Logged", true);
                        editor.putString("Type", LoggedUser.instance().mType.toString());
                        editor.putString("username", username);
                        editor.putString("password", password);
                        editor.apply();

                        Intent intent = new Intent(mContext, DashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(){
                mLoginProgressBar.setVisibility(View.INVISIBLE);
            }

        });
    }

    private void gatherProfile(JSONObject profile){
        switch(LoggedUser.instance().mType) {
            case ADMIN:
                try {
                    LoggedUser.instance().mAdmin = new AdminModel();
                    LoggedUser.instance().mAdmin.setUsername(profile.getString("username"));
                    LoggedUser.instance().mAdmin.setPassword(profile.getString("password"));
                    LoggedUser.instance().mAdmin.setFullName(profile.getString("full_name"));
                    LoggedUser.instance().mUsers = new ArrayList<>();
                    LoggedUser.instance().mGuests = new ArrayList<>();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case USER:
                try {
                    LoggedUser.instance().mUser = new UserModel();
                    LoggedUser.instance().mUser.setAdminUsername(profile.getString("admin_username"));
                    LoggedUser.instance().mUser.setAdminName(profile.getString("admin_name"));
                    LoggedUser.instance().mUser.setFullName(profile.getString("full_name"));
                    LoggedUser.instance().mUser.setUsername(profile.getString("username"));
                    LoggedUser.instance().mUser.setPassword(profile.getString("password"));
                    LoggedUser.instance().mGuests = new ArrayList<>();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null)
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Quit if back is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
