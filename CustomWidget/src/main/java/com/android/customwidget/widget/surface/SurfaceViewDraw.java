package com.android.customwidget.widget.surface;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
// 自定义绘制
public class SurfaceViewDraw extends SurfaceView implements SurfaceHolder.Callback,Runnable{
    private static final String TAG = "SurfaceViewDraw";
    private SurfaceHolder mHolder;
    private boolean surfaceAvailable;

    private Canvas mCanvas;
    private Paint mPaint;
    private Path mPath;

    private Thread mDrawThread;

    public SurfaceViewDraw(Context context) {
        super(context);
        initView();
    }

    public SurfaceViewDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mHolder = getHolder();
        mHolder.addCallback(this);

        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    private void draw() {
        try {
            //锁定画布并返回画布对象
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(Color.WHITE);
            mCanvas.drawPath(mPath, mPaint);
        } catch (Exception e) {
        } finally {
            //当画布内容不为空时才提交显示
            if (mCanvas != null)
                mHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 在主线程中接收用户的触摸事件，记录下来。在子线程中，每隔100毫秒执行一次draw()方法，根据主线程中的触摸绘制图像
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "onTouchEvent - down");
                mPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "onTouchEvent - move");
                mPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(TAG, "onTouchEvent - up");
                break;
        }
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceAvailable = true;

        // surface创建完成后，就启动子线程，循环进行绘制操作。
        if(null == mDrawThread){
            mDrawThread = new Thread(this);
        }
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceAvailable = false;
    }

    @Override
    public void run() {
        // 每隔100毫秒，执行一次draw()方法，即刷新频率为100毫秒
        while (surfaceAvailable) {
            draw();

            // 严格一点的话应该把绘制时间算进去，这里主要演示用法，就不搞那么复杂了
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}