package com.android.customwidget.widget.mouseEvent;


public interface OnMouseListener {

    int TYPE_NONE = -1;

    int TYPE_MOUSE_CUESOR = 0;

    int TYPE_MOUSE_CLICK = 2;

    int TYPE_SCROLL_VERTICALLY_UP = 3;

    int TYPE_SCROLL_VERTICALLY_DOWN = 4;

    int TYPE_SCROLL_HORIZONTALLY_LEFT = 5;

    int TYPE_SCROLL_HORIZONTALLY_RIGHT = 6;

    int TYPE_ZOOM_OUT = 7;

    int TYPE_ZOOM_IN = 8;

    int TYPE_DRAG = 9;

    void onEvent(int type, EnhanceMotionEvent enhanceEvent);
}
