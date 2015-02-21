package com.android.kes_android.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.android.kes_android.R;
import com.android.kes_android.models.DoorActivityModel;
import com.android.kes_android.models.LoggedUser;
import com.android.kes_android.network.API;
import com.loopj.android.image.SmartImageView;

public class DoorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_door);

        int position = getIntent().getIntExtra("position", 0);
        DoorActivityModel door = LoggedUser.instance().mDoor.get(position);
        String photoSimpleName = door.getPhotoSimplename();

        String photoFilePath = "/static/toggle_photos/" + photoSimpleName;
        String URL = API.BASE_URL + photoFilePath;

        SmartImageView mImage = (SmartImageView) findViewById(R.id.door_activity_image);
        mImage.setImageUrl(URL);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
