package com.android.kes_android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends ListActivity {

    private TextView mBannerTextView;
    private Button mAddNewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mBannerTextView = (TextView) findViewById(R.id.banner);
        mBannerTextView.setText("Currently Added Users");

        mAddNewButton = (Button) findViewById(R.id.add_new);
        mAddNewButton.setText("Add New User");

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                profileDialog().show();
            }
        });
    }

    private void refreshUsers(){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APILinks.USERS_URL, new TextHttpResponseHandler() {
            ProgressDialog mProgressDialog;
            boolean usersexist = false;
            List<String> mUsers = new ArrayList<String>();
            SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);

            @Override
            public void onStart(){
                mProgressDialog = new ProgressDialog(getApplicationContext(), ProgressDialog.THEME_HOLO_DARK);
                mProgressDialog.setTitle("Loading");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                try{
                    JSONArray jsonArray = new JSONArray(s);
                    for(int x = 0; x < jsonArray.length(); x++){
                        JSONObject object = jsonArray.getJSONObject(x);
                        if(object.get("parent_username").equals(mPrefs.getString("admin_username", null))){
                            usersexist = true;
                            mUsers.add(object.get("name").toString());
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("users", s);
                            editor.apply();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(){
                mProgressDialog.dismiss();
                if(usersexist){
                    ModelListAdapter adapter = new ModelListAdapter(getApplicationContext(), mUsers);
                    setListAdapter(adapter);
                }
                else{
                    Toast.makeText(getApplicationContext(), "No users found",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

        });
    }

    private AlertDialog.Builder profileDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_profile, null));

        builder.setTitle("USER DETAILS");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        return builder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        getActionBar().setTitle("Users");
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
            case R.id.action_refresh:
                refreshUsers();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
