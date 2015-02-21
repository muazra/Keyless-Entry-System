package com.android.kes_android.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.kes_android.R;
import com.android.kes_android.adapters.PhotoListAdapter;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.models.LoggedUserType;
import com.android.kes_android.models.PhotoModel;
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

public class SettingsActivity extends ListActivity {
    private Context mContext = this;

    private TextView mName;
    private TextView mUsername;
    private TextView mDeviceID;
    private TextView mType;

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mName = (TextView) findViewById(R.id.settings_name);
        mUsername = (TextView) findViewById(R.id.settings_username);
        mDeviceID = (TextView) findViewById(R.id.settings_deviceID);
        mType = (TextView) findViewById(R.id.settings_profile_type);

        Button mAddPhotoGallery = (Button) findViewById(R.id.settings_add_image_gallery);
        mAddPhotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PICK_IMAGE);
            }
        });

        Button mAddPhotoCamera = (Button) findViewById(R.id.settings_add_image_camera);
        mAddPhotoCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        populateViews();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri originalUri = data.getData();
            String id = originalUri.getLastPathSegment().split(":")[1];
            final String[] imageColumns = {MediaStore.Images.Media.DATA };
            final String imageOrderBy = null;

            Uri uri = getUri();
            String selectedImagePath = "path";

            Cursor imageCursor = managedQuery(uri, imageColumns,
                    MediaStore.Images.Media._ID + "="+id, null, imageOrderBy);

            if (imageCursor.moveToFirst()) {
                selectedImagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            Log.d("MUAZ", "image path = " + selectedImagePath ); // use selectedImagePath
            addPhoto(new File(selectedImagePath));
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            addPhoto(new File(mCurrentPhotoPath));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    private void addPhoto(File photo){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        if (LoggedUser.instance().mType == LoggedUserType.ADMIN) {
            params.put("profile_type", "admin");
            params.put("profile_name", LoggedUser.instance().mAdmin.getUsername());
        }
        else {
            params.put("profile_type", "user");
            params.put("profile_name", LoggedUser.instance().mUser.getUsername());
        }

        try {
            params.put("photo", photo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.post(API.ADD_PHOTO, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Adding Photo");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Adding Photo Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Settings Refresh Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        refreshSettings();
                        Toast.makeText(mContext, "Adding Photo Complete", Toast.LENGTH_SHORT).show();
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

    private void populateViews() {
        switch(LoggedUser.instance().mType) {
            case ADMIN:
                mName.setText(LoggedUser.instance().mAdmin.getFullName());
                mUsername.setText(LoggedUser.instance().mAdmin.getUsername());
                mDeviceID.setText(LoggedUser.instance().mDevice.getDeviceID());
                mType.setText(LoggedUser.instance().mType.toString().toLowerCase());
                break;

            case USER:
                mName.setText(LoggedUser.instance().mUser.getFullName());
                mUsername.setText(LoggedUser.instance().mUser.getUsername());
                mDeviceID.setText(LoggedUser.instance().mDevice.getDeviceID());
                mType.setText(LoggedUser.instance().mType.toString().toLowerCase());
                break;

            default:
                break;
        }
        PhotoListAdapter adapter = new PhotoListAdapter(getApplicationContext(),
                LoggedUser.instance().mPhotos);
        setListAdapter(adapter);
    }

    private void refreshSettings() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        String URL = "";
        if(LoggedUser.instance().mType == LoggedUserType.ADMIN) {
            URL = API.GET_ADMIN;
            params.put("admin", LoggedUser.instance().mAdmin.getUsername());
        }
        else {
            URL = API.GET_USER;
            params.put("user", LoggedUser.instance().mUser.getUsername());
        }

        client.post(URL, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Refreshing Settings");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Settings Refresh Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Settings Refresh Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
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

                        //profile
                        JSONObject profile = response.getJSONObject("profile");
                        gatherProfile(profile);

                        Toast.makeText(mContext, "Settings Refresh Complete", Toast.LENGTH_SHORT).show();
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

    private void gatherProfile(JSONObject profile){
        switch(LoggedUser.instance().mType) {
            case ADMIN:
                try {
                    LoggedUser.instance().mAdmin.setUsername(profile.getString("username"));
                    LoggedUser.instance().mAdmin.setPassword(profile.getString("password"));
                    LoggedUser.instance().mAdmin.setFullName(profile.getString("full_name"));

                    mName.setText(LoggedUser.instance().mAdmin.getFullName());
                    mUsername.setText(LoggedUser.instance().mAdmin.getUsername());
                    mDeviceID.setText(LoggedUser.instance().mDevice.getDeviceID());
                    mType.setText(LoggedUser.instance().mType.toString().toLowerCase());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case USER:
                try {
                    LoggedUser.instance().mUser.setAdminUsername(profile.getString("admin_username"));
                    LoggedUser.instance().mUser.setAdminName(profile.getString("admin_name"));
                    LoggedUser.instance().mUser.setFullName(profile.getString("full_name"));
                    LoggedUser.instance().mUser.setUsername(profile.getString("username"));
                    LoggedUser.instance().mUser.setPassword(profile.getString("password"));

                    mName.setText(LoggedUser.instance().mUser.getFullName());
                    mUsername.setText(LoggedUser.instance().mUser.getUsername());
                    mDeviceID.setText(LoggedUser.instance().mDevice.getDeviceID());
                    mType.setText(LoggedUser.instance().mType.toString().toLowerCase());

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;
        }

        PhotoListAdapter adapter = new PhotoListAdapter(getApplicationContext(),
                LoggedUser.instance().mPhotos);
        setListAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()){
            case R.id.action_home:
                intent = new Intent(this, DashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;

            case R.id.action_refresh:
                refreshSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
