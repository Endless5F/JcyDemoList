package com.android.customwidget.ext;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class ActivityExt {
    private final Activity activity;
    private final ViewGroup contentParent;

    public ActivityExt(Activity activity) {
        this.activity = activity;
        /*
         * Activity中真实的根布局为DecorView(FrameLayout子类)
         * DecorView包含一个线性布局LinearLayout，LinearLayout其分为上下两部分：titleBar和mContentParent。
         * 而mContentParent实际上就是我们在布局文件中绘制布局显示的区域。mContentParent的id即android.R.id.content
         */
        this.contentParent = activity.findViewById(android.R.id.content);
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        contentParent.addView(view, params);
    }

    public void removeContentView(View view) {
        contentParent.removeView(view);
    }
}
