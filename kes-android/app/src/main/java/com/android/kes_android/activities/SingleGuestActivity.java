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
import com.android.kes_android.models.GuestModel;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.models.PhotoModel;
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

public class SingleGuestActivity extends ListActivity {
    private Context mContext = this;
    private GuestModel mGuest;
    private int mPosition;

    private TextView mName;
    private TextView mAdmin;
    private TextView mType;

    private static final int PICK_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singleguest);

        mPosition = getIntent().getIntExtra("position", 0);

        mName = (TextView) findViewById(R.id.singleguest_name);
        mAdmin = (TextView) findViewById(R.id.singleguest_admin);
        mType = (TextView) findViewById(R.id.singleguest_profile_type);

        Button mAddPhoto = (Button) findViewById(R.id.singleguest_add_image_button);
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

        Button mRemoveGuest = (Button) findViewById(R.id.singleguest_remove_guest_button);
        mRemoveGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteGuest();
            }
        });

        populateViews();
    }

    private void deleteGuest() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("full_name", mGuest.getFullName());

        client.post(API.DELETE_GUEST, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Delete Guest");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Deleting Guest Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Deleting Guest Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        LoggedUser.instance().mGuests.remove(mPosition);
                        Intent intent = new Intent(mContext, GuestsListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        Toast.makeText(mContext, "Deleting Guest Complete", Toast.LENGTH_SHORT).show();
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
        mGuest = LoggedUser.instance().mGuests.get(mPosition);

        mName.setText(mGuest.getFullName());
        mAdmin.setText(mGuest.getAdminUsername());
        mType.setText("Guest");

        PhotoListAdapter adapter = new PhotoListAdapter(getApplicationContext(), mGuest.getPhotos());
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

        params.put("profile_type", "guest");
        params.put("profile_name", mGuest.getFullName());

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
                        refreshGuest();
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

    private void refreshGuest() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("guest", mGuest.getFullName());

        client.post(API.GET_GUEST, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Refreshing Guest");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Guest Refresh Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Guest Refresh Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //profile
                        JSONObject profile = response.getJSONObject("profile");
                        GuestModel guestModel = new GuestModel();
                        guestModel.setAdminUsername(profile.getString("admin_username"));
                        guestModel.setAdminName(profile.getString("admin_name"));
                        guestModel.setFullName(profile.getString("full_name"));

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
                        guestModel.setPhotos(photoModelList);
                        LoggedUser.instance().mGuests.add(mPosition, guestModel);

                        populateViews();
                        Toast.makeText(mContext, "Guest Refresh Complete", Toast.LENGTH_SHORT).show();
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
                refreshGuest();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
