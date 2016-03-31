package com.wiadvance.sipdemo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.squareup.picasso.Picasso;
import com.wiadvance.sipdemo.model.Contact;
import com.wiadvance.sipdemo.office365.Constants;

public class ContactHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "ContactHolder";

    private final TextView mNameTextView;
    private final ImageView mPhoneImageview;
    private final ImageView mAvatar;
    private final Context mContext;
    private final SwipeLayout mRootItemSwipeLayoutView;
//    private final ImageView mOutLineImageView;
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
//        mOutLineImageView = (ImageView) itemView.findViewById(R.id.outline_image_view);
        mBottomWrapperView = itemView.findViewById(R.id.bottom_wrapper);
    }

    public void bindViewHolder(final Contact contact) {

        mRootItemSwipeLayoutView.setShowMode(SwipeLayout.ShowMode.PullOut);
        mRootItemSwipeLayoutView.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {
                Log.d(TAG, "onStartOpen() called with: " + "layout = [" + layout + "]");
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                Log.d(TAG, "onOpen() called with: " + "layout = [" + layout + "]");
                isSwiping = false;
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
                Log.d(TAG, "onStartClose() called with: " + "layout = [" + layout + "]");

            }

            @Override
            public void onClose(SwipeLayout layout) {
                Log.d(TAG, "onClose() called with: " + "layout = [" + layout + "]");
                isSwiping = false;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                Log.d(TAG, "onUpdate() called with: " + "layout = [" + layout + "], leftOffset = [" + leftOffset + "], topOffset = [" + topOffset + "]");
                isSwiping = true;
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                Log.d(TAG, "onHandRelease() called with: " + "layout = [" + layout + "], xvel = [" + xvel + "], yvel = [" + yvel + "]");
            }
        });

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