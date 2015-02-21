package com.android.kes_android.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.kes_android.R;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.network.API;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.File;

public class NewUserActivity extends Activity {
    private Context mContext = this;

    private static final int PICK_IMAGE = 1;
    private String mSelectedImagePath = "";

    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newuser);

        final TextView mName = (TextView) findViewById(R.id.newuser_username);
        final TextView mUsername = (TextView) findViewById(R.id.newuser_username);
        final TextView mPassword = (TextView) findViewById(R.id.newuser_password);

        mImage = (ImageView) findViewById(R.id.newuser_image);
        Button mImageButton = (Button) findViewById(R.id.newuser_addphoto_button);
        Button mSubmit = (Button) findViewById(R.id.newuser_submit);

        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Picture"), PICK_IMAGE);
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = mName.getText().toString();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || mSelectedImagePath.isEmpty()) {
                    Toast.makeText(mContext, "Please complete all fields", Toast.LENGTH_SHORT).show();
                } else {
                    File f = new File(mSelectedImagePath);
                    createNewUser(fullName, username, password, f);
                }
            }
        });
    }

    private void createNewUser(String fullName, String username, String password, File photo) {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        params.put("username", username);
        params.put("password", password);
        params.put("full_name", fullName);
        params.put("admin_username", LoggedUser.instance().mDevice.getAdmin());

        try {
            params.put("user_photo", photo);
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.post(API.ADD_USER, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Creating User");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(mContext, "Creating User Failed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Creating User Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Intent intent = new Intent(mContext, UsersListActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        startActivity(intent);

                        Toast.makeText(mContext, "Creating User Complete", Toast.LENGTH_SHORT).show();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri originalUri = data.getData();
            mImage.setImageURI(originalUri);

            String id = originalUri.getLastPathSegment().split(":")[1];
            final String[] imageColumns = {MediaStore.Images.Media.DATA };
            final String imageOrderBy = null;

            Uri uri = getUri();
            mSelectedImagePath = "path";

            Cursor imageCursor = managedQuery(uri, imageColumns,
                    MediaStore.Images.Media._ID + "="+id, null, imageOrderBy);

            if (imageCursor.moveToFirst()) {
                mSelectedImagePath = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            Log.d("MUAZ", "image path = " + mSelectedImagePath); // use selectedImagePath
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private Uri getUri() {
        String state = Environment.getExternalStorageState();
        if(!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;

        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

}
