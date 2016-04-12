package com.wiadvance.sip;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

public class WiLinearLayoutManager extends LinearLayoutManager {
    private static final float MILLISECONDS_PER_INCH = 20f;
    private final Context mContext;


    public WiLinearLayoutManager(Context context) {
        super(context);

        mContext = context;

    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
                                       RecyclerView.State state, final int position) {

        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {

            //This controls the direction in which smoothScroll looks
            //for your view
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                return WiLinearLayoutManager.this.computeScrollVectorForPosition(targetPosition);
            }

            //This returns the milliseconds it takes to
            //scroll one pixel.
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }
        };

        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }
}
