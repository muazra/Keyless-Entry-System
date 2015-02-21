package com.android.kes_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.kes_android.models.DoorActivityModel;

import java.util.List;

public class DoorActivityListAdapter extends ArrayAdapter<DoorActivityModel> {
    private final List<DoorActivityModel> mDoorModels;
    private final Context mContext;

    public DoorActivityListAdapter(Context context, List<DoorActivityModel> models){
        super(context, android.R.layout.simple_list_item_1, models);
        mContext = context;
        mDoorModels = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View modelRow = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        DoorActivityModel model = mDoorModels.get(position);

        TextView modelNameTextView = (TextView) modelRow.findViewById(android.R.id.text1);
        modelNameTextView.setText(model.getID());

        return modelRow;
    }
}
