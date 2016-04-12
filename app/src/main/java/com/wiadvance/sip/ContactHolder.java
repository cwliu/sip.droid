package com.wiadvance.sip;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.squareup.picasso.Picasso;
import com.wiadvance.sip.model.Contact;

public class ContactHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ContactHolder";

    private final TextView mNameTextView;
    private final ImageView mPhoneImageview;
    private final ImageView mAvatar;
    private final Context mContext;
    private final SwipeLayout mRootItemSwipeLayoutView;
    private boolean isButtonDisplayed;
    private final View mBottomWrapperView;

    private boolean isSwiping = false;

    public ContactHolder(Context context, View itemView) {
        super(itemView);

        mContext = context;
        mRootItemSwipeLayoutView = (SwipeLayout) itemView.findViewById(R.id.swipe_layout);
        mNameTextView = (TextView) itemView.findViewById(R.id.contact_name_text_view);
        mPhoneImageview = (ImageView) itemView.findViewById(R.id.phone_icon_image_view);
        mAvatar = (ImageView) itemView.findViewById(R.id.list_item_avatar);
        mBottomWrapperView = itemView.findViewById(R.id.bottom_wrapper);
    }

    public void bindViewHolder(final Contact contact) {

        mNameTextView.setText(contact.getName());

        Picasso.with(mContext).load(contact.getPhotoUri())
                .placeholder(R.drawable.avatar_120dp).into(mAvatar);
        mPhoneImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call(contact);
            }
        });


        mRootItemSwipeLayoutView.setLeftSwipeEnabled(false);
        mRootItemSwipeLayoutView.setRightSwipeEnabled(false);
        mRootItemSwipeLayoutView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isSwiping){
                    call(contact);
                }
                return true;
            }
        });

        mRootItemSwipeLayoutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonsRelativeLayout();
            }
        });

        mBottomWrapperView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButtonsRelativeLayout();
            }
        });
    }

    private void toggleButtonsRelativeLayout() {
        if(isButtonDisplayed){
            mRootItemSwipeLayoutView.close(true);
            isButtonDisplayed = false;
        }else{
            mBottomWrapperView.setVisibility(View.VISIBLE);
            mRootItemSwipeLayoutView.open(true);
            isButtonDisplayed = true;
        }
    }

    private void call(Contact contact) {
        Intent intent = MakeCallActivity.newIntent(mContext, contact);
        mContext.startActivity(intent);
    }
}