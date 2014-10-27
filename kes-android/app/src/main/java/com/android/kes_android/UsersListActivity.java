package com.android.kes_android;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

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

        List<String> mUsers = new ArrayList<String>();

        ModelListAdapter adapter = new ModelListAdapter(this, mUsers);
        setListAdapter(adapter);
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
                startActivity(intent);
                return true;
            case R.id.action_users:
                intent = new Intent(this, UsersListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_guests:
                intent = new Intent(this, GuestsListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
