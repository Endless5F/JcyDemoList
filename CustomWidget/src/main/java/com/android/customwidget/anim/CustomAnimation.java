package com.android.customwidget.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * 自定义补间动画
 * Android 提供了 Animation 作为补间动画抽象基类，而且为该抽象基类提供了
 * AlphaAnimation、RotationAnimation、ScaleAnimation、TranslateAnimation
 * 四个实现类，这四个实现类只是补间动画的基本形式：透明度、旋转、缩放、位移。但是要实现复杂的动画，就需要继承 Animation。继承 Animation 类关键是要重写一个方法：
 *
 * applyTransformation(float interpolatedTime,Transformation t)
 * interploatedTime: 代表了动画的时间进行比。不管动画实际的持续时间如何，当动画播放时，该参数总是从 0 到 1。
 * Transformation t:该参数代表了补间动画在不同时刻对图形或组件的变形程度。
 *
 * 在实现自定义动画的关键就是重写 applyTransformation 方法时 根据 interpolatedTime 时间来动态地计算动画对图片或视图的变形程度。
 *
 * Transformation 代表了对图片或者视图的变形，该对象封装了一个 Matrix 对象，对它所包装了 Matrix 进行位移、倾斜、旋转等变换时，Transformation
 * 将会控制对应的图片或视图进行相应的变换。
 *
 * 为了控制图片或者 View 进行三维空间的变换，还需要借助于 Android 提供的一个 Camera，这个 Camera 并非代表手机摄像头，而是空间变换工具。作用类似于
 * Matrix，其常用方法如下：
 * getMatrix(Matrix matrix)：将 Camera 所做的变换应用到指定的 matrix 上。
 * rotateX(float deg):将组件沿 X 轴旋转。
 * rotateY(float deg):将组件沿 Y 轴旋转。
 * rotateZ(float deg):将组件沿 Z 轴旋转。
 * translate(float x,float y,float z):目标组件在三维空间里变换。
 * applyToCanvas(Canvas canvas):把 Camera 所做的变换应用到 Canvas 上。
 */
public class CustomAnimation extends Animation {
    private float centerX;
    private float centerY;
    // 定义动画的持续事件
    private int duration;
    private Camera camera = new Camera();
    public CustomAnimation(float x,float y,int duration)
    {
        this.centerX = x;
        this.centerY = y;
        this.duration = duration;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
        //设置动画的持续时间
        setDuration(duration);
        //设置动画结束后保留效果
        setFillAfter(true);
        setInterpolator(new LinearInterpolator());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        //super.applyTransformation(interpolatedTime, t);
        camera.save();
        // 根据 interpolatedTime 时间来控制X,Y,Z 上偏移
        camera.translate(100.0f - 100.f * interpolatedTime,150.0f * interpolatedTime - 150,80.0f - 80.0f * interpolatedTime);
        // 根据 interploatedTime 设置在 X 轴 和 Y 轴旋转
        camera.rotateX(360 * interpolatedTime);
        camera.rotateY(360 * interpolatedTime);
        // 获取 Transformation 参数的 Matrix 对象
        Matrix matrix = t.getMatrix();
        camera.getMatrix(matrix);
        matrix.preTranslate(-centerX,-centerY);
        matrix.postTranslate(centerX,centerY);
        camera.restore();
    }
}
