package com.android.customwidget.widget;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Scroller;

public class CustomScrollerView extends LinearLayout {

    private static final String TAG = "Scroller";

    private Scroller mScroller;

    public CustomScrollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    public CustomScrollerView(Context context, @Nullable AttributeSet attrs, Scroller mScroller) {
        super(context, attrs);
        this.mScroller = mScroller;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.e("CustomView", "父布局 dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.e("CustomView", "父布局 onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("CustomView", "父布局 onTouchEvent");
        return super.onTouchEvent(event);
    }

    //调用此方法滚动到目标位置
//    diffX = mStartX - mScroller.getCurrX();
//    diffY = mStartY - mScroller.getCurrY();
    public void smoothScrollTo(int destX,int destY) {
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int dx = destX - scrollX;
        int dy = destY - scrollY;
        Log.e("smoothScrollTo", getScrollX() + "");
        smoothScrollBy(dx, dy);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(getScrollX(), getScrollY(), dx, dy);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }
}
