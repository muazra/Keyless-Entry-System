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
import com.android.kes_android.models.PhotoModel;
import com.android.kes_android.models.UserModel;
import com.android.kes_android.network.API;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SingleUserActivity extends ListActivity {
    private Context mContext = this;
    private UserModel mUser;
    private int mPosition;

    private TextView mName;
    private TextView mUsername;
    private TextView mAdmin;
    private TextView mType;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleuser);

        mPosition = getIntent().getIntExtra("position", 0);
        mUser = LoggedUser.instance().mUsers.get(mPosition);

        mName = (TextView) findViewById(R.id.singleuser_name);
        mUsername = (TextView) findViewById(R.id.singleuser_username);
        mAdmin = (TextView) findViewById(R.id.singleuser_admin);
        mType = (TextView) findViewById(R.id.singleuser_profile_type);

        Button mAddPhoto = (Button) findViewById(R.id.singleuser_add_image_button);
        mAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PICK_IMAGE);
            }
        });

        Button mRemoveUser = (Button) findViewById(R.id.singleuser_remove_user_button);
        mRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser();
            }
        });

        populateViews();
    }

    private void deleteUser() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", mUser.getUsername());

        client.post(API.DELETE_USER, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Delete User");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Deleting User Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Deleting User Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        LoggedUser.instance().mUsers.remove(mPosition);
                        Intent intent = new Intent(mContext, UsersListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        Toast.makeText(mContext, "Deleting User Complete", Toast.LENGTH_SHORT).show();
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

    private void populateViews(){
        mUser = LoggedUser.instance().mUsers.get(mPosition);

        mName.setText(mUser.getFullName());
        mUsername.setText(mUser.getUsername());
        mAdmin.setText(mUser.getAdminUsername());
        mType.setText("User");

        PhotoListAdapter adapter = new PhotoListAdapter(getApplicationContext(), mUser.getPhotos());
        setListAdapter(adapter);
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
            Log.d("MUAZ", "image path = " + selectedImagePath); // use selectedImagePath
            addPhoto(new File(selectedImagePath));
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

        params.put("profile_type", "user");
        params.put("profile_name", mUser.getUsername());

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
                        Toast.makeText(mContext, "Adding Photo Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        refreshUser();
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

    private void refreshUser() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("user", mUser.getUsername());

        client.post(API.GET_USER, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Refreshing User");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "User Refresh Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "User Refresh Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //profile
                        JSONObject profile = response.getJSONObject("profile");
                        UserModel userModel = new UserModel();
                        userModel.setAdminUsername(profile.getString("admin_username"));
                        userModel.setAdminName(profile.getString("admin_name"));
                        userModel.setFullName(profile.getString("full_name"));
                        userModel.setUsername(profile.getString("username"));
                        userModel.setPassword(profile.getString("password"));

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
                        userModel.setPhotos(photoModelList);
                        LoggedUser.instance().mUsers.add(mPosition, userModel);

                        populateViews();
                        Toast.makeText(mContext, "User Refresh Complete", Toast.LENGTH_SHORT).show();
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
                refreshUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
