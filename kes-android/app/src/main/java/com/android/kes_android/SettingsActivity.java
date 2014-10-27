package com.android.kes_android;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

import org.json.JSONObject;

public class SettingsActivity extends Activity {

    private TextView mName;
    private TextView mUsername;
    private TextView mDeviceID;
    private TextView mType;
    private Button mUpdate;
    private SmartImageView mPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mName = (TextView) findViewById(R.id.settings_name);
        mUsername = (TextView) findViewById(R.id.settings_username);
        mDeviceID = (TextView) findViewById(R.id.settings_deviceID);
        mType = (TextView) findViewById(R.id.settings_profile_type);
        mUpdate = (Button) findViewById(R.id.settings_update_button);
        mPhoto = (SmartImageView) findViewById(R.id.settings_image);

        SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);
        try {
            JSONObject object = new JSONObject(mPrefs.getString("admin", null));
            mName.setText(object.get("name").toString());
            mUsername.setText(object.get("username").toString());
            mDeviceID.setText(object.get("deviceid").toString());
            mType.setText("Admin");
            mPhoto.setImageUrl(APILinks.PHOTOS_URL + object.get("photo").toString());
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getActionBar().setTitle("Settings");
        getActionBar().setHomeButtonEnabled(true);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
