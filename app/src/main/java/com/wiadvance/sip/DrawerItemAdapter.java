package com.wiadvance.sip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wiadvance.sip.office365.Constants;

import java.util.List;

public class DrawerItemAdapter extends BaseAdapter {

    private static final String TAG = "DrawerItemAdapter";

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
            name.setText(UserData.getName(mContext));
            ImageView registerOk = (ImageView) rootView.findViewById(R.id.drawer_header_register_ok);
            ImageView registerFailed = (ImageView) rootView.findViewById(R.id.drawer_header_register_fail);

            ImageView userPhotoImageView = (ImageView) rootView.findViewById(R.id.drawer_user_photo);

            int scale = Utils.getDeviceScale(mContext);
            Picasso.with(mContext).load(Constants.MY_PHOTO_URL)
                    .resize(120 * scale, 120 * scale)
                    .placeholder(R.drawable.avatar_120dp)
                    .into(userPhotoImageView);

            if (UserData.getRegistrationStatus(mContext)) {
                registerOk.setVisibility(View.VISIBLE);
                registerFailed.setVisibility(View.GONE);
            } else {
                registerOk.setVisibility(View.GONE);
                registerFailed.setVisibility(View.VISIBLE);
            }

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
