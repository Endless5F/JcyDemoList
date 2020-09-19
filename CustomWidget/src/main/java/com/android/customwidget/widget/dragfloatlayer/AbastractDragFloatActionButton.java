package com.android.customwidget.widget.dragfloatlayer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.android.customwidget.util.ScreenUtils;

public abstract class AbastractDragFloatActionButton extends RelativeLayout {
    private int parentHeight;//悬浮的父布局高度
    private int parentWidth;

    public AbastractDragFloatActionButton(Context context) {
        this(context, null, 0);
    }

    public AbastractDragFloatActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public abstract int getLayoutId();

    public abstract void renderView(View view);

    public AbastractDragFloatActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(getLayoutId(), this);
        renderView(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        View view = getChildAt(0);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
    }

    private int lastX;
    private int lastY;

    private boolean isDrag;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int rawX = (int) event.getRawX();
        int rawY = (int) event.getRawY();
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true); // 默认是点击事件
                isDrag = false; // 默认是非拖动而是点击事件
                getParent().requestDisallowInterceptTouchEvent(true);// 父布局不要拦截子布局的监听
                lastX = rawX;
                lastY = rawY;
                ViewGroup parent;
                if (getParent() != null) {
                    parent = (ViewGroup) getParent();
                    parentHeight = parent.getHeight();
                    parentWidth = parent.getWidth();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                isDrag = (parentHeight > 0 && parentWidth > 0);// 只有父布局存在你才可以拖动
                if (!isDrag) break;

                int dx = rawX - lastX;
                int dy = rawY - lastY;
                // 这里修复一些华为手机无法触发点击事件
                int distance = (int) Math.sqrt(dx * dx + dy * dy);
                isDrag = distance > 0; // 只有位移大于0说明拖动了
                if (!isDrag) break;

                float x = getX() + dx;
                float y = getY() + dy;
                // 检测是否到达边缘 左上右下
                x = x < 0 ? 0 : x > parentWidth - getWidth() ? parentWidth - getWidth() : x;
                y = y < 0 ? 0 : y > parentHeight - getHeight() ? parentHeight - getHeight() : y;
                setX(x);
                setY(y);
                lastX = rawX;
                lastY = rawY;
                break;
            case MotionEvent.ACTION_UP:
                // 如果是拖动状态下即非点击按压事件
                setPressed(isDrag);
                moveHide(rawX, rawY);
                break;
        }

        // 如果不是拖拽，那么就不消费这个事件，以免影响点击事件的处理
        // 拖拽事件要自己消费
        return isDrag || super.onTouchEvent(event);
    }


    private void moveHide(int rawX, int rawY) {
        if (rawX >= parentWidth / 2) {
            // 靠右吸附
            animate().setInterpolator(new DecelerateInterpolator())
                    .setDuration(500)
                    .xBy(parentWidth - getWidth() - getX() - ScreenUtils.dp2px(15))
                    .start();
        } else {
            // 靠左吸附
            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(),
                    ScreenUtils.dp2px(15));
            oa.setInterpolator(new DecelerateInterpolator());
            oa.setDuration(500);
            oa.start();

        }
        if (rawY >= parentHeight - ScreenUtils.dp2px(50)) { // 这里的50是距顶部的距离加上控件一半的高
            animate().setInterpolator(new DecelerateInterpolator())
                    .setDuration(500)
                    .yBy(parentHeight - getHeight() - getY() - ScreenUtils.dp2px(20))
                    .start();
        } else if (rawY <= ScreenUtils.dp2px(112)) {// 这里的112是距底部的距离加上控件一半的高
            ObjectAnimator oa = ObjectAnimator.ofFloat(this, "y", getY(),
                    ScreenUtils.dp2px(82));
            oa.setInterpolator(new DecelerateInterpolator());
            oa.setDuration(500);
            oa.start();
        }
    }

}
