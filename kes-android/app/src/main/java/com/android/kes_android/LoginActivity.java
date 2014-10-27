package com.android.kes_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsername = (TextView) findViewById(R.id.login_username);
        mPassword = (TextView) findViewById(R.id.login_password);
        mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progressbar);
        mSignIn = (Button) findViewById(R.id.login_sign_in);

        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                verifyLogin(username, password);
            }
        });
    }

    public void verifyLogin(final String username, final String password){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APILinks.ADMINS_URL, new TextHttpResponseHandler() {
            boolean authenticate = false;

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

                            SharedPreferences mPrefs = getSharedPreferences("KES_DB", 0);
                            SharedPreferences.Editor editor = mPrefs.edit();
                            editor.putString("admin", object.toString());
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
                            Toast.LENGTH_LONG).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
