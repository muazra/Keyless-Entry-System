package com.android.kes_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.kes_android.R;
import com.android.kes_android.models.PhotoModel;
import com.android.kes_android.network.API;
import com.loopj.android.image.SmartImageView;

import java.util.List;

public class PhotoListAdapter extends ArrayAdapter<PhotoModel> {
    private final List<PhotoModel> mPhotoModels;
    private final Context mContext;

    public PhotoListAdapter(Context context, List<PhotoModel> models){
        super(context, R.layout.photo_row, models);
        mContext = context;
        mPhotoModels = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View modelRow = inflater.inflate(R.layout.photo_row, parent, false);

        PhotoModel model = mPhotoModels.get(position);

        SmartImageView image = (SmartImageView) modelRow.findViewById(R.id.photo_row_image);
        image.setImageUrl(API.BASE_URL + "/" + model.getPhotoFilepath());

        return modelRow;
    }
}
