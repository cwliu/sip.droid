package com.wiadvance.sipdemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class DrawerItemAdapter extends BaseAdapter {

    private final List<DrawerItem> mItems;
    private final Context mContext;

    public DrawerItemAdapter(Context context, List<DrawerItem> items) {
        mItems = items;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position == 0) {
            View rootView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_drawer_header, parent, false);

            TextView name = (TextView) rootView.findViewById(R.id.drawer_nameTextView);
            name.setText(UserPreference.getName(mContext));
            return rootView;

        } else {
            View rootView = LayoutInflater.from(mContext).inflate(
                    R.layout.list_item_drawer_item, parent, false);

            TextView item = (TextView) rootView.findViewById(R.id.drawer_item_text);
            ImageView icon = (ImageView) rootView.findViewById(R.id.drawer_item_icon);

            item.setText(mItems.get(position).getName());
            icon.setImageResource(mItems.get(position).getIcon());
            return rootView;
        }
    }
}
