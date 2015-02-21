package com.android.kes_android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.kes_android.models.UserModel;

import java.util.List;

public class UsersListAdapter extends ArrayAdapter<UserModel> {
    private final List<UserModel> mUserModels;
    private final Context mContext;

    public UsersListAdapter(Context context, List<UserModel> models){
        super(context, android.R.layout.simple_list_item_1, models);
        mContext = context;
        mUserModels = models;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View modelRow = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        UserModel model = mUserModels.get(position);

        TextView userFullName = (TextView) modelRow.findViewById(android.R.id.text1);
        userFullName.setText(model.getFullName());

        return modelRow;
    }
}
