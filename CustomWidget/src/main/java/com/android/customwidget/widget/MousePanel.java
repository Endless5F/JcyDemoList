package com.android.customwidget.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.android.customwidget.R;
import com.android.customwidget.widget.mouseEvent.EnhanceMotionEvent;
import com.android.customwidget.widget.mouseEvent.OnMouseListener;


/**
 * the mouse panel move if move after less than 0.15 seconds since mouse panel
 * touched drag if move after more than 0.5 seconds since mouse panel touched
 * scrollVertically move in the vertically Bar ScrollHorizontally move in the
 * Horizontally Bar
 */

public class MousePanel extends FrameLayout {

    int MODE_NONE = -1;
    int MODE_ZOOM = 0;
    int MODE_VERTICALLY = 1;
    int MODE_HORIZONTALLY = 2;
    int MODE_COMMON = 3;
    int operMode = MODE_NONE;
    private Context mContext;
    private OnMouseListener listener;
    private long width, height;
    private float p0_x_old = 0, p0_x_new = 0, p0_y_old = 0, p0_y_new = 0, p1_x_old = 0, p1_x_new = 0, p1_y_old = 0, p1_y_new = 0;
    private float x0, x1, x2, y0, y1, y2;
    private long time_old = 0, time_new = 0;
    private int multi_points = 2;
    private boolean dragMode = false;
    private boolean moveMode = false;
    private float[] horizontally = {1.0f, 6.0f}, vertically = {1.0f, 6.0f};
    private float swamp = 4;
    private int clickStartTime = 150;
    private int dragStartTime = 500;
    boolean areaLockFlag = true;
    private boolean debugMode = false;

    public final static String TAG = "MOUSE PANEL";

	/* end add */

    public MousePanel(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MousePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.MousePanel);
        swamp = a.getFloat(R.styleable.MousePanel_swamp, swamp);
        clickStartTime = a.getInteger(R.styleable.MousePanel_clickStartTime, clickStartTime);
        dragStartTime = a.getInteger(R.styleable.MousePanel_dragStartTime, dragStartTime);
        areaLockFlag = a.getBoolean(R.styleable.MousePanel_areaLockFlag, areaLockFlag);
        debugMode = a.getBoolean(R.styleable.MousePanel_debugMode, debugMode);
        String strH = a.getString(R.styleable.MousePanel_horizontally);
        String strV = a.getString(R.styleable.MousePanel_vertically);
        horizontally = strToArray(strH).length == 0 ? horizontally : strToArray(strH);
        vertically = strToArray(strV).length == 0 ? vertically : strToArray(strV);
        a.recycle();
        init();
    }

    public MousePanel(Context context) {
        super(context);
    }

    /**
     * @param str transform string to array
     */
    private float[] strToArray(String str) {
        float[] array = new float[2];
        try {
            array[0] = Float.parseFloat(str.substring(0, str.indexOf("/")));
            array[1] = Float.parseFloat(str.substring(str.indexOf("/") + 1, str.length()));
        } catch (Exception e) {
            array = new float[0];
        }
        return array;
    }

    public void setOnMouseListener(OnMouseListener listener) {
        this.listener = listener;
    }

    private void init() {
        initViews();
        initData();
    }

    private void initViews() {
        this.setBackgroundColor(Color.TRANSPARENT);
    }

    private void initData() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        swamp = metrics.density * swamp;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
        initPos();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /***
     * init the location of point (x0,y0)��point(x1,y1) and point (x2,y2).
     * (x0,y0) represent The upper left corner of the panel (x1,y1) represent
     * The intersection of the VerticallyBar and HorizontallyBar (x2,y2)
     * represent The lower right corner of the panel
     */
    private void initPos() {
        x0 = 0;
        x1 = width * (horizontally[1] / (horizontally[0] + horizontally[1]));
        x2 = width;
        y0 = 0;
        y1 = height * (vertically[1] / (vertically[0] + vertically[1]));
        y2 = height;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        doProcess(event);
        return true;
    }

    private void doProcess(MotionEvent event) {
        // two point, scroll,move,click,drag
        int pointes = event.getPointerCount();
        if (pointes > multi_points) {
            return;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            p0_x_old = event.getX(0);
            p0_y_old = event.getY(0);
        }
        if (operMode == MODE_ZOOM) {
            processMultiPoint(event);
        } else if (operMode == MODE_HORIZONTALLY) {
            processScrollHorizontally(event);
        } else if (operMode == MODE_VERTICALLY) {
            processScrollVertically(event);
        } else if (operMode == MODE_COMMON) {
            processOther(event);
        } else if (pointes == multi_points) {
            operMode = MODE_ZOOM;
            processMultiPoint(event);
        } else if (isInHorizontallyBar()) {
            operMode = MODE_HORIZONTALLY;
            processScrollHorizontally(event);
        } else if (isInVerticallyBar()) {
            operMode = MODE_VERTICALLY;
            processScrollVertically(event);
        } else if (isInCursorPanel() || operMode == MODE_NONE) {// zhn || operMode == MODE_NONE
            operMode = MODE_COMMON;
            processOther(event);
        }
    }

    private int lastMultiOper = OnMouseListener.TYPE_NONE;

    /**
     * two points event
     *
     * @param event
     */
    private void processMultiPoint(MotionEvent event) {
        int type = OnMouseListener.TYPE_NONE;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                lastMultiOper = OnMouseListener.TYPE_NONE;
                p0_x_old = event.getX(0);
                p0_y_old = event.getY(0);
                p1_x_old = event.getX(1);
                p1_y_old = event.getY(1);
                break;
            case MotionEvent.ACTION_MOVE:
                // hellen MouseControlFragment.setViewPagerEvent(true);//
                // hh.Touch//hh.Touch
                p0_x_new = event.getX(0);
                p0_y_new = event.getY(0);
                p1_x_new = event.getX(1);
                p1_y_new = event.getY(1);
                if (isZoomIn()) {
                    type = OnMouseListener.TYPE_ZOOM_IN;
                    updateXY();
                } else if (isZoomOut()) {
                    type = OnMouseListener.TYPE_ZOOM_OUT;
                    updateXY();
                }
                if (type == OnMouseListener.TYPE_ZOOM_IN
                        || type == OnMouseListener.TYPE_ZOOM_OUT) {
                    lastMultiOper = type;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                // hellen MouseControlFragment.setViewPagerEvent(false);
                if (null != listener) {
                    EnhanceMotionEvent enhanceEvent = new EnhanceMotionEvent(event);
                    listener.onEvent(lastMultiOper, enhanceEvent);
                }
                operMode = MODE_NONE;
                break;
            default:
                if (null != listener) {
                    EnhanceMotionEvent enhanceEvent = new EnhanceMotionEvent(event);
                    listener.onEvent(lastMultiOper, enhanceEvent);
                }
                operMode = MODE_NONE;
                break;
        }
    }

    /**
     * Scroll Horizontally when the point is in Horizontally Bar
     *
     * @param event
     */
    private void processScrollHorizontally(MotionEvent event) {
        int type = OnMouseListener.TYPE_NONE;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                p0_x_old = event.getX(0);
                p0_y_old = event.getY(0);
                break;
            case MotionEvent.ACTION_MOVE:
                // hellen MouseControlFragment.setViewPagerEvent(true);// hh.Touch
                p0_x_new = event.getX(0);
                p0_y_new = event.getY(0);
                if (isScrollLeft()) {
                    type = OnMouseListener.TYPE_SCROLL_HORIZONTALLY_LEFT;
                    updateXY();
                } else if (isScrollRight()) {
                    type = OnMouseListener.TYPE_SCROLL_HORIZONTALLY_RIGHT;
                    updateXY();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                operMode = MODE_NONE;
                break;
            default:
                operMode = MODE_NONE;
                break;
        }
        if (null != listener) {
            EnhanceMotionEvent enhanceEvent = new EnhanceMotionEvent(event);
            listener.onEvent(type, enhanceEvent);
        }
    }

    /**
     * Scroll Vertically when the point is in Vertically Bar
     *
     * @param event
     */
    private void processScrollVertically(MotionEvent event) {
        Log.i("test", "processScrollVertically");
        int type = OnMouseListener.TYPE_NONE;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                p0_x_old = event.getX(0);
                p0_y_old = event.getY(0);
                break;
            case MotionEvent.ACTION_MOVE:

                p0_x_new = event.getX(0);
                p0_y_new = event.getY(0);
                if (isScrollUp()) {

                    type = OnMouseListener.TYPE_SCROLL_VERTICALLY_UP;
                    updateXY();
                } else if (isScrollDown()) {

                    type = OnMouseListener.TYPE_SCROLL_VERTICALLY_DOWN;
                    updateXY();
                }
                break;

            default:
                operMode = MODE_NONE;
                break;
        }
        if (null != listener) {
            EnhanceMotionEvent enhanceEvent = new EnhanceMotionEvent(event);
            listener.onEvent(type, enhanceEvent);
        }
    }

    /**
     * move or drag event
     *
     * @param event
     */
    private void processOther(MotionEvent event) {
        // Log.i("test", "processother");
        int type = OnMouseListener.TYPE_NONE;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                p0_x_old = event.getX(0);
                p0_y_old = event.getY(0);
                time_old = System.currentTimeMillis();
                dragMode = false;
                moveMode = false;
                break;
            case MotionEvent.ACTION_MOVE:
                // hellen MouseControlFragment.setViewPagerEvent(true);// hh.Touch
                p0_x_new = event.getX(0);
                p0_y_new = event.getY(0);
                if (dragMode) {
                    if (isMoved()) {
                        type = OnMouseListener.TYPE_DRAG;
                        Log.d(TAG, "drag mode");
                        updateXY();
                    }
                } else if (moveMode) {
                    if (isMoved()) {
                        type = OnMouseListener.TYPE_MOUSE_CUESOR;
                        Log.d(TAG, "move mode");
                        updateXY();
                    }
                } else {
                    time_new = System.currentTimeMillis();
                    if (isMoved() && !isDragTime()) {
                        moveMode = true;
                        Log.d(TAG, "set as move mode");
                        type = OnMouseListener.TYPE_MOUSE_CUESOR;
                        updateXY();
                    } else {
                        time_new = System.currentTimeMillis();

                        if (isDragTime()) {
                            dragMode = true;
                            Log.d(TAG, "set as drag mode");
                            type = OnMouseListener.TYPE_DRAG;
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                operMode = MODE_NONE;
                break;
            case MotionEvent.ACTION_UP:
                p0_x_new = event.getX(0);
                p0_y_new = event.getY(0);
                time_new = System.currentTimeMillis();
                if (!isMoved() && isClickTime()) {
                    type = OnMouseListener.TYPE_MOUSE_CLICK;
                }
                updateXY();
                operMode = MODE_NONE;
                break;
            default:
                operMode = MODE_NONE;
                break;
        }
        if (null != listener) {
            EnhanceMotionEvent enhanceEvent = new EnhanceMotionEvent(event);
            listener.onEvent(type, enhanceEvent);
        }
    }

    /**
     * update the location of point p0 and point p1.
     */
    private void updateXY() {
        p0_x_old = p0_x_new;
        p0_y_old = p0_y_new;
        p1_x_old = p1_x_new;
        p1_y_old = p1_y_new;
    }

    /**
     * @return if the action is click
     */
    private boolean isClickTime() {
        return time_new - time_old < clickStartTime;
    }

    /**
     * @return if the action is Drag
     */
    private boolean isDragTime() {
        return time_new - time_old > dragStartTime;
    }

    /**
     * @return if the action is Scroll up
     */
    private boolean isScrollUp() {
        return p0_y_new - p0_y_old < -swamp;
    }

    /**
     * @return if the action is Scroll Down
     */
    private boolean isScrollDown() {
        return p0_y_new - p0_y_old > swamp;
    }

    /**
     * @return if the action is Scroll Left
     */
    private boolean isScrollLeft() {
        return p0_x_new - p0_x_old < -swamp;
    }

    /**
     * @return if the action is Scroll Right
     */
    private boolean isScrollRight() {
        return p0_x_new - p0_x_old > swamp;
    }

    /**
     * @return if the action is Move
     */
    private boolean isMoved() {
        return Math.abs(p0_x_new - p0_x_old) > swamp
                || Math.abs(p0_y_new - p0_y_old) > swamp;
    }

    /**
     * @return if the action is ZoomIn
     */
    private boolean isZoomIn() {
        float oldDestance = (p0_x_old - p1_x_old) * (p0_x_old - p1_x_old)
                + (p0_y_old - p1_y_old) * (p0_y_old - p1_y_old);
        float newDestance = (p0_x_new - p1_x_new) * (p0_x_new - p1_x_new)
                + (p0_y_new - p1_y_new) * (p0_y_new - p1_y_new);
        return newDestance - oldDestance > swamp * swamp;
    }

    /**
     * @return if the action is ZoomOut
     */
    private boolean isZoomOut() {
        float oldDestance = (p0_x_old - p1_x_old) * (p0_x_old - p1_x_old)
                + (p0_y_old - p1_y_old) * (p0_y_old - p1_y_old);
        float newDestance = (p0_x_new - p1_x_new) * (p0_x_new - p1_x_new)
                + (p0_y_new - p1_y_new) * (p0_y_new - p1_y_new);
        return oldDestance - newDestance > swamp * swamp;
    }

    /**
     * @return if the Focus is in the Pannel
     */
    private boolean isInPanel() {
        return p0_x_old > x0 && p0_x_old < y2 && p0_y_old > y0 && p0_y_old < y2;
    }

    /**
     * @return if the Focus is in the VerticallyBar
     */
    private boolean isInVerticallyBar() {
        return p0_x_old > x1 && p0_x_old < x2 && p0_y_old > y0 && p0_y_old < y1;
    }

    /**
     * @return if the Focus is in the HorizontallyBar
     */
    private boolean isInHorizontallyBar() {
        return p0_x_old > x0 && p0_x_old < x1 && p0_y_old > y1 && p0_y_old < y2;
    }

    /**
     * @return if the Focus is in the Cursor Panel
     */
    private boolean isInCursorPanel() {
        return p0_x_old > x0 && p0_x_old < x1 && p0_y_old > y0 && p0_y_old < y1;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (debugMode) {
            Paint paint = new Paint();
            paint.setAlpha(127);

            paint.setColor(Color.LTGRAY);
            canvas.drawRect(x0, y0, x1, y1, paint);

            paint.setColor(Color.GREEN);
            canvas.drawRect(x0, y1, x1, y2, paint);

            paint.setColor(Color.BLUE);
            canvas.drawRect(x1, y0, x2, y2, paint);

            paint.setColor(Color.RED);
            canvas.drawRect(x1, y1, x2, y2, paint);
        }
    }
}
