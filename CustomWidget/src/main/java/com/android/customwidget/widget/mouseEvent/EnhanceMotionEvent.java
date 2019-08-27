package com.android.customwidget.widget.mouseEvent;

import android.view.MotionEvent;

public class EnhanceMotionEvent {
    public MotionEvent event;
    public float speed_x;
    public float speed_y;
    public float acceleration_x;
    public float acceleration_y;

    public EnhanceMotionEvent(MotionEvent event) {
        this.event = event;
    }
}
