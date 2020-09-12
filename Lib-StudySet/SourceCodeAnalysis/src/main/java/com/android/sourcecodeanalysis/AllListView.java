package com.android.sourcecodeanalysis;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class AllListView extends ListView {
    public AllListView(Context context) {
        super(context);
    }

    public AllListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AllListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AllListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
