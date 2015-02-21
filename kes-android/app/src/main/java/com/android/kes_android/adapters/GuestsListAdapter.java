package com.android.kes_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.kes_android.models.GuestModel;

import java.util.List;

public class GuestsListAdapter extends ArrayAdapter<GuestModel> {
    private final List<GuestModel> mGuestModels;
    private final Context mContext;

    public GuestsListAdapter(Context context, List<GuestModel> models){
        super(context, android.R.layout.simple_list_item_1, models);
        mContext = context;
        mGuestModels = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View modelRow = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        GuestModel model = mGuestModels.get(position);

        TextView userFullName = (TextView) modelRow.findViewById(android.R.id.text1);
        userFullName.setText(model.getFullName());

        return modelRow;
    }
}
