package com.android.kes_android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;

import java.io.ByteArrayOutputStream;

public class DashActivity extends ListActivity {

    private Button mToggle;
    private Context mContext = this;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        mToggle = (Button) findViewById(R.id.dash_toggle);
        mToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap resized = Bitmap.createScaledBitmap(imageBitmap,imageBitmap.getWidth(),
                    imageBitmap.getHeight(), true);

            toggleDialog(resized).show();
        }
    }

    private AlertDialog.Builder toggleDialog(final Bitmap imageBitmap){

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_DARK);
        LayoutInflater inflater = getLayoutInflater();
        View customDialog = inflater.inflate(R.layout.dialog_toggle, null);

        ImageView togglePhoto = (ImageView) customDialog.findViewById(R.id.dialog_toggle_image);
        togglePhoto.setImageBitmap(imageBitmap);

        builder.setView(customDialog);
        builder.setTitle("CONFIRM IMAGE");

        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.NO_WRAP|Base64.URL_SAFE);
                sendtoserver(encoded);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        return builder;
    }

    private void sendtoserver(String encodedstring){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APILinks.TOGGLE_URL + encodedstring, new TextHttpResponseHandler() {
            ProgressDialog mProgressDialog;

            @Override
            public void onStart(){
                mProgressDialog = new ProgressDialog(mContext, ProgressDialog.THEME_HOLO_DARK);
                mProgressDialog.setTitle("Sending");
                mProgressDialog.setMessage("Please wait..");
                mProgressDialog.show();
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                mProgressDialog.dismiss();
                if(s.equals("success")) {
                    Toast.makeText(mContext, "Door successfully toggled",
                            Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(mContext, "Door toggle request denied",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFinish(){

            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dash_menu, menu);
        getActionBar().setTitle("Dashboard");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);
        switch(item.getItemId()){
            case R.id.action_home:
                intent = new Intent(this, DashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            case R.id.action_users:
                if(mPrefs.getString("user", null) == null) {
                    intent = new Intent(this, UsersListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Permission not allowed - See Admin",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_guests:
                if(mPrefs.getString("user", null) == null) {
                    intent = new Intent(this, GuestsListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "Permission not allowed - See Admin",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        // Quit if back is pressed
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
