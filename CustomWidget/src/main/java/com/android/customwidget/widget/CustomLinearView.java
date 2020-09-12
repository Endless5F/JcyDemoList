package com.android.customwidget.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class CustomLinearView extends LinearLayout {

    private static final String TAG = "Scroller";

    private Scroller mScroller;

    public CustomLinearView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    public CustomLinearView(Context context, @Nullable AttributeSet attrs, Scroller mScroller) {
        super(context, attrs);
        this.mScroller = mScroller;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("CustomView", "子布局 dispatchTouchEvent");
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e("CustomView", "子布局 onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("CustomView", "子布局 onTouchEvent");
        return super.onTouchEvent(event);
    }
}
