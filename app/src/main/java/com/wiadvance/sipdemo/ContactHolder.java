package com.wiadvance.sipdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.office365.Constants;

public class ContactHolder extends RecyclerView.ViewHolder {

    private final TextView mNameTextView;
    private final ImageView mPhoneImageview;
    private final ImageView mAvatar;
    private final Context mContext;

    public ContactHolder(Context context, View itemView) {
        super(itemView);

        mContext = context;
        mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
        mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
        mAvatar = (ImageView) itemView.findViewById(R.id.list_item_avatar);
    }

    public void bindViewHolder(final Contact contact) {
        mNameTextView.setText(contact.getName());

        String photoUrl = String.format(Constants.USER_PHOTO_URL_FORMAT, contact.getEmail());
        Picasso.with(mContext).load(photoUrl)
                .placeholder(R.drawable.avatar_120dp).into(mAvatar);
        mPhoneImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = MakeCallActivity.newIntent(mContext, contact);
                mContext.startActivity(intent);
            }
        });
    }
}