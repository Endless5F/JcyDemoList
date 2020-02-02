package com.android.customwidget.widget.surface;

import android.content.Context;
import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
// 使用实例
public class SurfaceViewTest extends SurfaceView implements SurfaceHolder.Callback {
    /*
        SurfaceView功能简述:
        1.提供嵌入视图层次结构内部的专用绘图表面。
        2.提供一个辅助线程可以在其中渲染屏幕的表面。

        SurfaceView注意事项:
        1.所有SurfaceView和SurfaceHolder.Callback方法都将从UI线程中调用。
        2.绘图线程仅接触SurfaceHolder.Callback.surfaceCreated（）和SurfaceHolder.Callback.surfaceDestroyed（）之间的基础Surface。
    */

    private SurfaceHolder holder;
    private RenderThread renderThread; // 渲染绘制线程
    private boolean isRender;          // 控制线程

    public SurfaceViewTest(Context context) {
        super(context);
        holder = this.getHolder();
        holder.addCallback(this);
        renderThread = new RenderThread();
    }

    // 缓冲区创建
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRender = true;
        renderThread.start();
    }

    // 缓冲区内容改变（子线程渲染UI的过程）
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    // 缓冲区销毁
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRender = false;
    }

    private class RenderThread extends Thread {
        @Override
        public void run() {
            // 死循环绘制线程
            while (isRender) {
                long startTime = System.currentTimeMillis();

                Canvas canvas = null;
                try {
                    // 获取Surface中的画布
                    // 1.锁定Canvas
                    canvas = holder.lockCanvas();
                    // 2.通过Canvas绘制图形
//                    canvas.drawXX(...);

                } catch (Exception e) {
                    // 3.捕获异常,防止异常导致Canvas没有解锁
                    e.printStackTrace();
                } finally {
                    // 4.解锁Canvas,把图形更新到屏幕
                    if (canvas != null)
                        holder.unlockCanvasAndPost(canvas);
                }

                long endTime = System.currentTimeMillis();

                // 性能评定: 每秒绘制次数(帧率FPS), 动画流畅：FPS>=30
                int fps = (int) (1000/(endTime-startTime));
            }
        }
    }
}

