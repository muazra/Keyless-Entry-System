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
import com.android.kes_android.adapters.GuestsListAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class GuestsListActivity extends ListActivity {
    private Context mContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guests);

        Button mAddGuest = (Button) findViewById(R.id.add_new_guest_button);
        mAddGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewGuestActivity.class);
                startActivity(intent);
            }
        });

        GuestsListAdapter adapter = new GuestsListAdapter(getApplicationContext(), LoggedUser.instance().mGuests);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l ,View v, int position, long id) {
        Intent intent = new Intent(mContext, SingleGuestActivity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    private void refreshGuests() {
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.add("admin", LoggedUser.instance().mDevice.getAdmin());

        client.post(API.GET_GUESTS, params, new JsonHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart() {
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_LIGHT);
                mProgressDialog.setTitle("Updating Guests");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable throwable) {
                Toast.makeText(mContext, "Updating Guests Failed", Toast.LENGTH_SHORT).show();
                Log.d("MUAZ", "error response" + response);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (!API.isSuccess(response.getString("result"))) {
                        Toast.makeText(mContext, "Updating Guests Failed", Toast.LENGTH_SHORT).show();
                    } else {
                        //guests
                        JSONArray guestsArray = response.getJSONArray("guests");
                        List<GuestModel> guestModelList = new ArrayList<>();

                        for (int i = 0; i < guestsArray.length(); i++) {
                            JSONObject userObj = guestsArray.getJSONObject(i);
                            GuestModel guestModel = new GuestModel();

                            guestModel.setAdminUsername(userObj.getString("admin_username"));
                            guestModel.setAdminName(userObj.getString("admin_name"));
                            guestModel.setFullName(userObj.getString("full_name"));
                            guestModel.setPhotos(new ArrayList<PhotoModel>());

                            guestModelList.add(guestModel);
                        }

                        //photos
                        JSONArray photosArray = response.getJSONArray("photos");

                        for (int i = 0; i < photosArray.length(); i++) {
                            JSONObject photoObj = photosArray.getJSONObject(i);
                            PhotoModel photoModel = new PhotoModel();

                            photoModel.setProfileType(photoObj.getString("profile_type"));
                            photoModel.setProfileName(photoObj.getString("profile_name"));
                            photoModel.setPhotoFilepath(photoObj.getString("photo_filepath"));
                            photoModel.setPhotoSimplename(photoObj.getString("photo_simplename"));

                            for (int j = 0; j < guestModelList.size(); j++) {
                                if (guestModelList.get(j).getFullName().equals(photoModel.getProfileName())) {
                                    List<PhotoModel> guestModelPhotos = guestModelList.get(j).getPhotos();
                                    guestModelPhotos.add(photoModel);

                                    guestModelList.get(j).setPhotos(guestModelPhotos);
                                    break;
                                }
                            }
                        }

                        LoggedUser.instance().mGuests = guestModelList;

                        GuestsListAdapter adapter = new GuestsListAdapter(getApplicationContext(), LoggedUser.instance().mGuests);
                        setListAdapter(adapter);

                        Toast.makeText(mContext, "Updating Guests Complete", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
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
                refreshGuests();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
