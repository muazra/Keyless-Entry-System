package com.android.kes_android.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.kes_android.R;
import com.android.kes_android.adapters.UsersListAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends ListActivity {
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        Button mAddUser = (Button) findViewById(R.id.add_new_user_button);
        mAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewUserActivity.class);
                startActivity(intent);
            }
        });

        UsersListAdapter adapter = new UsersListAdapter(getApplicationContext(), LoggedUser.instance().mUsers);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l ,View v, int position, long id) {
        Intent intent = new Intent(mContext, SingleUserActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void refreshUsers() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("admin", LoggedUser.instance().mDevice.getAdmin());

        client.post(API.GET_USERS, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Updating Users");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(mContext, "Updating Users Failed", Toast.LENGTH_SHORT).show();
                Log.d("MUAZ", "error response" + response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if(!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Updating Users Failed", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        //users
                        JSONArray usersArray = response.getJSONArray("users");
                        List<UserModel> userModelList = new ArrayList<>();

                        for(int i = 0; i < usersArray.length(); i++){
                            JSONObject userObj = usersArray.getJSONObject(i);
                            UserModel userModel = new UserModel();

                            userModel.setAdminUsername(userObj.getString("admin_username"));
                            userModel.setAdminName(userObj.getString("admin_name"));
                            userModel.setFullName(userObj.getString("full_name"));
                            userModel.setUsername(userObj.getString("username"));
                            userModel.setPassword(userObj.getString("password"));
                            userModel.setPhotos(new ArrayList<PhotoModel>());

                            userModelList.add(userModel);
                        }

                        //photos
                        JSONArray photosArray = response.getJSONArray("photos");

                        for(int i = 0; i < photosArray.length(); i++) {
                            JSONObject photoObj = photosArray.getJSONObject(i);
                            PhotoModel photoModel = new PhotoModel();

                            photoModel.setProfileType(photoObj.getString("profile_type"));
                            photoModel.setProfileName(photoObj.getString("profile_name"));
                            photoModel.setPhotoFilepath(photoObj.getString("photo_filepath"));
                            photoModel.setPhotoSimplename(photoObj.getString("photo_simplename"));

                            for (int j = 0; j < userModelList.size(); j++) {
                                if (userModelList.get(j).getUsername().equals(photoModel.getProfileName())) {
                                    List<PhotoModel> userModelPhotos = userModelList.get(j).getPhotos();
                                    userModelPhotos.add(photoModel);

                                    userModelList.get(j).setPhotos(userModelPhotos);
                                    break;
                                }
                            }
                        }

                        LoggedUser.instance().mUsers = userModelList;

                        UsersListAdapter adapter = new UsersListAdapter(getApplicationContext(), LoggedUser.instance().mUsers);
                        setListAdapter(adapter);

                        Toast.makeText(mContext, "Updating Users Complete", Toast.LENGTH_SHORT).show();
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
                refreshUsers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
