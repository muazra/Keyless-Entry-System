package com.android.kes_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;


public class LoginActivity extends Activity {

    private TextView mUsername;
    private TextView mPassword;
    private ProgressBar mLoginProgressBar;
    private Button mSignIn;
    private Button mRegiser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (TextView) findViewById(R.id.login_username);
        mPassword = (TextView) findViewById(R.id.login_password);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);
        mSignIn = (Button) findViewById(R.id.login_sign_in);
        mRegiser = (Button) findViewById(R.id.login_register);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                verifyLoginAdmin(username, password);
            }
        });

        mRegiser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Feature will be available on next release",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void verifyLoginAdmin(final String username, final String password){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APILinks.ADMINS_URL, new TextHttpResponseHandler() {
            boolean authenticate = false;
            SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);

            @Override
            public void onStart(){
                mLoginProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                try{
                    JSONArray jsonArray = new JSONArray(s);
                    for(int x = 0; x < jsonArray.length(); x++){
                        JSONObject object = jsonArray.getJSONObject(x);
                        if(object.get("username").equals(username) &&
                                object.get("password").equals(password)){
                            authenticate = true;

                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("admin", object.toString());
                            editor.putString("admin_username", object.get("username").toString());
                            editor.apply();
                            break;
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(){
                if(authenticate) {
                    SharedPreferences.Editor editor = mPrefs.edit();
                    editor.putString("user", null);
                    editor.putString("user_username", null);
                    editor.apply();

                    mLoginProgressBar.setVisibility(View.INVISIBLE);
                    Intent i = new Intent(getApplicationContext(), DashActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(), "Welcome to KES",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    verifyLoginUser(username, password);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

        });
    }

    public void verifyLoginUser(final String username, final String password){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APILinks.USERS_URL, new TextHttpResponseHandler() {
            boolean authenticate = false;

            @Override
            public void onStart(){
            }

            @Override
            public void onSuccess(int i, Header[] headers, String s) {
                try{
                    JSONArray jsonArray = new JSONArray(s);
                    for(int x = 0; x < jsonArray.length(); x++){
                        JSONObject object = jsonArray.getJSONObject(x);
                        if(object.get("username").equals(username) &&
                                object.get("password").equals(password)){
                            authenticate = true;

                            SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("user", object.toString());
                            editor.putString("user_username", object.get("username").toString());
                            editor.apply();
                            break;
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish(){
                mLoginProgressBar.setVisibility(View.INVISIBLE);
                if(authenticate) {
                    Intent i = new Intent(getApplicationContext(), DashActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(), "Welcome to KES",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Sorry no credentials found",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, String s, Throwable throwable) {

            }

        });
    }



    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view != null)
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
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
