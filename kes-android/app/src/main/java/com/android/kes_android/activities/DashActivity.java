package com.android.kes_android.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.kes_android.R;
import com.android.kes_android.adapters.DoorActivityListAdapter;
import com.android.kes_android.models.DeviceModel;
import com.android.kes_android.models.DoorActivityModel;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.models.LoggedUserType;
import com.android.kes_android.network.API;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DashActivity extends ListActivity {
    private static final String LOG = DashActivity.class.getSimpleName();
    private Context mContext = this;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String mCurrentPhotoPath;

    private TextView mDeviceStatus;
    private TextView mDeviceBattery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        mDeviceStatus = (TextView) findViewById(R.id.dash_door_status);
        mDeviceBattery = (TextView) findViewById(R.id.dash_door_battery);

        Button mToggle = (Button) findViewById(R.id.dash_toggle_button);
        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        Button mLock = (Button) findViewById(R.id.dash_lock_door);
        mLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockDoor();
            }
        });

        mDeviceStatus.setText(LoggedUser.instance().mDevice.getStatus());
        mDeviceBattery.setText(LoggedUser.instance().mDevice.getBattery());

        DoorActivityListAdapter adapter = new DoorActivityListAdapter(getApplicationContext(),
                LoggedUser.instance().mDoor);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l ,View v, int position, long id) {
        Intent intent = new Intent(mContext, DoorActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void lockDoor() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("device_id", LoggedUser.instance().mDevice.getDeviceID());
        params.add("status", "locked");

        client.post(API.TOGGLE_DOOR_STATUS, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Locking Device");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(mContext, "Locking Device Failed", Toast.LENGTH_SHORT).show();
                Log.d("MUAZ", "error response" + response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                updateDash();
            }

            @Override
            public void onFinish(){
                mProgressDialog.hide();
            }
        });
    }

    private void updateDash() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("device_id", LoggedUser.instance().mDevice.getDeviceID());
        params.add("admin", LoggedUser.instance().mDevice.getAdmin());

        client.post(API.DASH_INFO, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Updating Dash");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(mContext, "Dash Update Failed", Toast.LENGTH_SHORT).show();
                Log.d("MUAZ", "error response" + response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Dash Update Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //device
                        JSONObject device = response.getJSONObject("device");
                        DeviceModel deviceModel = new DeviceModel();
                        deviceModel.setDeviceID(device.getString("device_id"));
                        deviceModel.setAvailable(device.getString("available"));
                        deviceModel.setAdmin(device.getString("admin"));
                        deviceModel.setStatus(device.getString("status"));
                        deviceModel.setBattery(device.getString("battery"));
                        LoggedUser.instance().mDevice = deviceModel;

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

                        mDeviceStatus.setText(LoggedUser.instance().mDevice.getStatus());
                        mDeviceBattery.setText(LoggedUser.instance().mDevice.getBattery());

                        DoorActivityListAdapter adapter = new DoorActivityListAdapter(getApplicationContext(),
                                LoggedUser.instance().mDoor);
                        setListAdapter(adapter);

                        Toast.makeText(mContext, "Dash Update Complete", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(){
                mProgressDialog.hide();
            }
        });

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            toggleDoor(new File(mCurrentPhotoPath));
        }
    }

    private void toggleDoor(File photo){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        if (LoggedUser.instance().mType == LoggedUserType.ADMIN) {
            params.put("admin", LoggedUser.instance().mAdmin.getUsername());
            params.put("profile_type", "admin");
            params.put("profile_name", LoggedUser.instance().mAdmin.getUsername());
            params.put("device_id", LoggedUser.instance().mDevice.getDeviceID());
        }
        else {
            params.put("admin", LoggedUser.instance().mUser.getAdminUsername());
            params.put("profile_type", "user");
            params.put("profile_name", LoggedUser.instance().mUser.getUsername());
            params.put("device_id", LoggedUser.instance().mDevice.getDeviceID());
        }

        try {
            params.put("toggle_photo", photo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.post(API.TOGGLE_DOOR, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Sending Photo");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Toggling Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(mContext, "Toggling Complete", Toast.LENGTH_SHORT).show();
                updateDash();
            }

            @Override
            public void onFinish(){
                mProgressDialog.hide();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){

            case R.id.action_refresh:
                updateDash();
                return true;

            case R.id.action_users:
                intent = new Intent(this, UsersListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;

            case R.id.action_guests:
                intent = new Intent(this, GuestsListActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;

            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;

            case R.id.action_logout:
                SharedPreferences mPrefs = getSharedPreferences("KES", 0);
                SharedPreferences.Editor editor = mPrefs.edit();
                editor.putBoolean("Logged", false);
                editor.commit();

                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dash_menu, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
