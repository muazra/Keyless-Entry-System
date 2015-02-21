package com.android.kes_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ModelListAdapter extends ArrayAdapter<String> {
    private final List<String> mModelList;
    private final Context mContext;

    public ModelListAdapter(Context context, List<String> models){
        super(context, android.R.layout.simple_list_item_1, models);
        mContext = context;
        mModelList = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View modelRow = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        String model = mModelList.get(position);

        TextView modelNameTextView = (TextView) modelRow.findViewById(android.R.id.text1);
        modelNameTextView.setText(model);

        return modelRow;
    }
}
