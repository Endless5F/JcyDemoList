package com.android.customwidget.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.animation.AnimationUtils;

import com.android.customwidget.R;

/**
 * https://www.jianshu.com/p/e2670e5880d4
 */
public class CustomAnimationDrawable extends Drawable {

    private final String TAG = this.getClass().getSimpleName();
    private Paint mPaint;
    //动画分解成5种状态
    private static final int STATE_ORIGIN = 0;  //初始状态：背景，圆圈 叉叉
    private static final int STATE_ROTATE = 1;  //外圈旋转 叉号缩小
    private static final int STATE_MOVE_UP = 2; //圆点上移
    private static final int STATE_MOVE_DOWN = 3; //圆点下移
    private static final int STATE_FINISH = 4;  //结束时画勾勾

    public String getState(int state){
        String res = "STATE_ORIGIN";
        switch (state){
            case STATE_ORIGIN:
                res = "STATE_ORIGIN";
                break;
            case STATE_ROTATE:
                res = "STATE_ROTATE";
                break;
            case STATE_MOVE_UP:
                res = "STATE_MOVE_UP";
                break;
            case STATE_MOVE_DOWN:
                res = "STATE_MOVE_DOWN";
                break;
            case STATE_FINISH:
                res = "STATE_FINISH";
                break;
            default:
                break;
        }

        return res;
    }

    private static final int DURATION_ROTATE = 1250;  //圆圈旋转时长
    private static final int DURATION_CLEARLING = 250; //叉号 缩小至0 的时长
    private static final int DURATION_UP = 250;  //  圆点上移 时长
    private static final int DURATION_DOWN = 350; // 圆点下移 时长
    private static final int DURATION_FINISH = 200;  //短边缩放时长
    private static final int DURATION_CLEARLING_DELAY = 3000; //  返回初始状态的时长

    private static final float PI_DEGREE = (float) (180/(Math.PI));  //180/PI  是一弧度对应的度数 大概57.3
    private static final float DRAWABLE_WIDTH = 180.0f;//  drawable 宽度
    private static final float ROTATE_DEGREE_TOTAL = -1080.0f;//  总共旋转的度数，3圈  6π

    private static final float PAINT_WIDTH = 4f;  //画笔的宽度
    private static final float PAINT_WIDTH_OTHER = 1.0f;  //其他画笔宽度
    private static final float CROSS_LENGTH = 62.0f; // x  的长度
    private static final float CROSS_DEGREE = 45.0f / PI_DEGREE;  // π/4 三角函数计算用 sin(π/4) = cos(π/4) = 0.707105
    private static final float UP_DISTANCE = 24.0f;  //往上移动的距离
    private static final float DOWN_DISTANCE = 20.0f;  // 往下移动的距离

    private static final float FORK_LEFT_LEN = 33.0f;  //对号 左边长
    private static final float FORK_RIGHT_LEN = 43.0f;//对号 右边长

    private static final float FORK_LEFT_DEGREE = 40.0f;  //对号 左边弧度
    private static final float FORK_RIGHT_DEGREE = 50.0f;//对号 右边弧度

    private static final float CIRCLE_RADIUS = 3.0f;  //圆点半径

    private int mAnimState = STATE_ORIGIN;  //初始状态


    private Context mContext;
    private int mWidth,mHeight;
    private Paint mLinePaint;
    Bitmap mBgBitmap,mCircleBitmap;

    private float mViewScale;
    private float mCenterX ,mCenterY; //中心点 的坐标
    private float mPaintWidth;
    private float mPaintOtherWidth;
    private float mCrossLength;
    private float mForkLeftLen;
    private float mForkRightLen;
    private float mPointRadius;
    private float mPointUpLen;
    private float mPointDownLen;

    private float mCleaningScale;
    private float mRotateDegreeScale;

    private TimeInterpolator fast_out_slow_in;
    private TimeInterpolator fast_out_linear_in;

    private Matrix mRotateMatrix = new Matrix();
    private AnimatorSet mAnimatorSet;

    public CustomAnimationDrawable(Context context, int width, int height) {
        super();
        init(context,width,height);
    }

    private void init(Context context,int width,int height){
        this.mWidth = width;
        this.mHeight = height;
        mPaint = new Paint();
        mLinePaint = new Paint();

        Bitmap bgBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.drawable_bg);
        Bitmap circleBitmap = BitmapFactory.decodeResource(context.getResources(),R.mipmap.drawable_cricle);

        mBgBitmap = Bitmap.createScaledBitmap(bgBitmap,width,height,true);
        mCircleBitmap = Bitmap.createScaledBitmap(circleBitmap,width,height,true);

        mViewScale = width / DRAWABLE_WIDTH;
        Log.i(TAG, "init: mViewScale= " + mViewScale);

        if(mCircleBitmap != circleBitmap){
            circleBitmap.recycle();
        }
        if(mBgBitmap != bgBitmap){
            bgBitmap.recycle();
        }

        mCenterX = width / 2;
        mCenterY = height / 2 ;
        mPaintWidth = PAINT_WIDTH * mViewScale;
        mPaintOtherWidth = PAINT_WIDTH_OTHER * mViewScale;

        mCrossLength = CROSS_LENGTH * mViewScale;
        mForkLeftLen = FORK_LEFT_LEN *mViewScale;
        mForkRightLen = FORK_RIGHT_LEN * mViewScale;
        mPointRadius = CIRCLE_RADIUS * mViewScale;
        mPointUpLen = UP_DISTANCE * mViewScale;
        mPointDownLen = DOWN_DISTANCE * mViewScale;

        mCleaningScale = 1.0f;
        mRotateDegreeScale = 0.0f;

        fast_out_slow_in = AnimationUtils.loadInterpolator(context,android.R.interpolator.fast_out_slow_in);
        fast_out_linear_in = AnimationUtils.loadInterpolator(context,android.R.interpolator.fast_out_linear_in);

    }

    @Override
    public void draw(@NonNull Canvas canvas) {

        float x1,y1,x2,y2,x3,y3,x4,y4;
        float length; // 叉的长度
        float sin45 = (float) Math.sin(CROSS_DEGREE);
        float sin40 = (float) Math.sin(FORK_LEFT_DEGREE);
        float cos40 = (float) Math.cos(FORK_LEFT_DEGREE);

        float sin50 = (float) Math.sin(FORK_RIGHT_DEGREE);
        float cos50 = (float) Math.cos(FORK_RIGHT_DEGREE);

        //设置画笔
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mPaintWidth);


        //绘制背景
        canvas.drawBitmap(mBgBitmap,0,0,null);
        Log.e(TAG, "画背景 " );
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.BLUE);
        mLinePaint.setStrokeWidth(4);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        //画辅助虚线
        mLinePaint.setPathEffect(new DashPathEffect(new float[]{20,10},0));
//        canvas.drawLine(0,mCenterY,mCenterX*2,mCenterY,mLinePaint);
//        canvas.drawLine(mCenterX,0,mCenterX,mCenterY*2,mLinePaint);

        switch (mAnimState){
            case STATE_ORIGIN: //初始状态，绘制圆圈 绘制叉
                length = mCrossLength * sin45 /2.0f;
                x1 = mCenterX - length;
                y1 = mCenterY - length;
                x2 = mCenterX + length;
                y2 = mCenterY + length;
                x3 = mCenterX + length;
                y3 = mCenterY - length;
                x4 = mCenterX - length;
                y4 = mCenterY + length;
                drawPath(canvas,x1,y1,x2,y2,x3,y3,x4,y4,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);
                Log.e(TAG, "STATE_ORIGIN length=" + length);
                break;
            case STATE_ROTATE: //画圆圈旋转，画叉缩小
                float degree = ROTATE_DEGREE_TOTAL * mRotateDegreeScale;
                mRotateMatrix.setRotate(degree,mCenterX,mCenterY);
                canvas.drawBitmap(mCircleBitmap,mRotateMatrix,null);
                length = mCleaningScale * mCrossLength *sin45 / 2.0f;
                x1 = mCenterX - length;
                y1 = mCenterY - length;
                x2 = mCenterX + length;
                y2 = mCenterY + length;
                x3 = mCenterX + length;
                y3 = mCenterY - length;
                x4 = mCenterX - length;
                y4 = mCenterY + length;
                drawPath(canvas,x1,y1,x2,y2,x3,y3,x4,y4,mPaint);
                Log.e(TAG, "STATE_ROTATE length=" + length + "----degree==" + degree);
                break;
            case STATE_MOVE_UP: //根据centerX centerY  - mPointUpLen *mScale 绘制圆点
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(mPaintOtherWidth);
                float upLen = mPointUpLen * mScale;
                canvas.drawCircle(mCenterX,mCenterY - upLen,mPointRadius,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);
                break;
            case STATE_MOVE_DOWN:
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStrokeWidth(mPaintOtherWidth);
                float downLen = (mPointDownLen + mPointUpLen) * mScale;
                canvas.drawCircle(mCenterX,mCenterY - mPointUpLen + downLen,mPointRadius,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);

                break;
            case STATE_FINISH: //画勾勾
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(mPaintWidth);

                x1 = mCenterX - Math.abs(mScale * mForkLeftLen * cos40);
                y1 = mCenterY + mPointDownLen - Math.abs(mScale * mForkLeftLen *sin40);
                x2 = mCenterX;
                y2 = mCenterY + mPointDownLen;
                x3 = mCenterX;
                y3 = mCenterY + mPointDownLen;
                x4 = mCenterX + Math.abs(mScale * mForkRightLen * cos40);
                y4 = mCenterY + mPointDownLen - Math.abs(mScale * mForkRightLen * sin40);
                drawPath(canvas,x1,y1,x2,y2,x3,y3,x4,y4,mPaint);
                canvas.drawBitmap(mCircleBitmap,0,0,null);
                //画辅助线
//                canvas.drawLine(x4,0,x4,mCenterY*2,mLinePaint);
//                canvas.drawLine(0,y4,mCenterX*2,y4,mLinePaint);

                break;
            default:break;
        }
    }

    public void drawPath(Canvas canvas,float x1,float y1,float x2,float y2,float x3,float y3,float x4,float y4,Paint paint){
        Path path = new Path();
        path.moveTo(x1,y1);
        path.lineTo(x2,y2);
        path.moveTo(x3,y3);
        path.lineTo(x4,y4);
        canvas.drawPath(path,paint);
    }


    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    private float mScale;

    private ValueAnimator createAnimator(final int drawType, long duration, TimeInterpolator interpolator){
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f,1.0f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mAnimState = drawType;
                mScale = value;
                invalidateSelf();
            }
        });
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        return animator;
    }

    public void start(){
        ValueAnimator circleAnimator;
        ValueAnimator cleaningAnimator;
        stop();

        circleAnimator = ValueAnimator.ofFloat(0.0f,1.0f);
        circleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {


                float value = (float) animation.getAnimatedValue();
                Log.e(TAG,"onAnimationUpdate value=" + value);

                mAnimState = STATE_ROTATE;
                mRotateDegreeScale = value;
                mCleaningScale = 1.0f;
                invalidateSelf();
            }
        });

        circleAnimator.setDuration(DURATION_ROTATE);
        circleAnimator.setInterpolator(fast_out_slow_in);

        cleaningAnimator = ValueAnimator.ofFloat(1.0f,0.0f);
        cleaningAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                Log.e(TAG,"cleaningAnimator value=" + value);
                mAnimState = STATE_ROTATE;
                mCleaningScale = value;
                invalidateSelf();
            }
        });
        cleaningAnimator.setDuration(DURATION_ROTATE);
//        cleaningAnimator.setStartDelay(DURATION_CLEARLING_DELAY);
        cleaningAnimator.setInterpolator(fast_out_linear_in);

        AnimatorSet beginAnimSet = new AnimatorSet();
        beginAnimSet.playTogether(circleAnimator,cleaningAnimator);

        ValueAnimator pointUpAnim = createAnimator(STATE_MOVE_UP,DURATION_UP,fast_out_slow_in);
        ValueAnimator pointDownAnim = createAnimator(STATE_MOVE_DOWN,DURATION_DOWN,fast_out_slow_in);
        ValueAnimator finishAnim = createAnimator(STATE_FINISH,DURATION_FINISH,fast_out_slow_in);

        ValueAnimator delayAnim = ValueAnimator.ofInt(0,0);
        delayAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimState = STATE_ORIGIN;
                invalidateSelf();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        delayAnim.setDuration(DURATION_CLEARLING_DELAY);
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playSequentially(beginAnimSet,pointUpAnim,pointDownAnim,finishAnim,delayAnim);
        mAnimatorSet.start();

    }

    public boolean isRunning(){
        if( null != mAnimatorSet ){
            return  mAnimatorSet.isRunning();
        }else {
            return false;
        }
    }

    public void stop(){
        if(null != mAnimatorSet){
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
        mAnimState = STATE_ORIGIN;
        invalidateSelf();
    }

}
