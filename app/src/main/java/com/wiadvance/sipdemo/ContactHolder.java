package com.wiadvance.sipdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.office365.Constants;

public class ContactHolder extends RecyclerView.ViewHolder {

    private final TextView mNameTextView;
    private final ImageView mPhoneImageview;
    private final ImageView mAvatar;
    private final Context mContext;
    private final View mRootItemView;
    private final ImageView mOutLineImageView;
    private boolean isButtonDisplayed;
    private final RelativeLayout mButtonRelativeLayout;

    public ContactHolder(Context context, View itemView) {
        super(itemView);

        mContext = context;
        mRootItemView = itemView;
        mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
        mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
        mAvatar = (ImageView) itemView.findViewById(R.id.list_item_avatar);
        mOutLineImageView = (ImageView) itemView.findViewById(R.id.outline_image_view);
        mButtonRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.buttons_relative_layout);
    }

    public void bindViewHolder(final Contact contact) {
        mNameTextView.setText(contact.getName());

        String photoUrl = String.format(Constants.USER_PHOTO_URL_FORMAT, contact.getEmail());
        Picasso.with(mContext).load(photoUrl)
                .placeholder(R.drawable.avatar_120dp).into(mAvatar);
        mPhoneImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call(contact);
            }
        });

        mRootItemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                call(contact);
                return true;
            }
        });

        mRootItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonsRelativeLayout();
            }
        });

        mButtonRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void toggleButtonsRelativeLayout() {
        if(isButtonDisplayed){
            mButtonRelativeLayout.setVisibility(View.GONE);
            mOutLineImageView.setVisibility(View.VISIBLE);
            isButtonDisplayed = false;
        }else{
            mButtonRelativeLayout.setVisibility(View.VISIBLE);
            mOutLineImageView.setVisibility(View.GONE);
            isButtonDisplayed = true;
        }
    }

    private void call(Contact contact) {
        Intent intent = MakeCallActivity.newIntent(mContext, contact);
        mContext.startActivity(intent);
    }
}