package com.android.customwidget.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.android.customwidget.R;

public class ThumbUpView extends View implements View.OnClickListener {

    private Bitmap selected;
    private Bitmap unselected;
    private Bitmap shining;
    private Paint paintIcon;
    private Paint paintText;
    private Paint paintCircle;

    // 点赞数量
    private int likeNumber;
    // 图标和文字间距
    private int widthSpace;
    private int textHeight;
    // 文字的绘制是基于baseline的，而高度则是通过descent - ascent获取的
    private int textDescentAndBaselineSpace;
    // 火花和点赞图标之间的间距，此值为负
    private int shinAndThubSpace;

    private Path mClipPath = new Path();

    private float SCALE_MIN = 0.9f;
    private float SCALE_MAX = 1f;
    private float mScale = SCALE_MIN;
    private float mUnScale = SCALE_MAX;

    private int alpha;
    private int alphaStart = 64;
    private int alphaEnd = 0;
    private float radius = 24;
    private float radiusStart = 0;
    private float radiusEnd;

    // 是否是喜爱
    private boolean isLike = false;

    public ThumbUpView(Context context) {
        super(context);
        init();
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        likeNumber = 0;
        widthSpace = dip2px(5);
        shinAndThubSpace = -dip2px(8);

        paintIcon = new Paint();
        paintIcon.setStyle(Paint.Style.STROKE);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setTextSize(dip2px(14));
        paintText.setColor(getResources().getColor(R.color.comm_main_color));

        paintCircle = new Paint();
        paintCircle.setColor(Color.RED);
        paintCircle.setAntiAlias(true);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5);

        // 获取文字高度
//        textHeight = (int) (paintText.descent() - paintText.ascent());
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        Paint.FontMetricsInt fm = paintText.getFontMetricsInt();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        float leading = fontMetrics.leading;
        textHeight = (int) (descent - ascent);
        textDescentAndBaselineSpace = (int) (descent - leading);
        selected = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_selected);
        unselected = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_unselected);
        shining = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_selected_shining);

        setOnClickListener(this);
    }

    /**
     * 大家都说继承自View的自定义控件需要重写onMeasure方法，为什么？
     *  其实如果我们不重写onMeasure方法，则父布局就不知道你到底多大，
     *  就会将其剩余的所有空间都给你，此时如果还需要别的控件添加进父布局，
     *  则会出现没有空间显示该多余出的控件，因此我们需要自己测量我们到底有多大
     *
     * 此处实际上需要根据widthMeasureSpec和heightMeasureSpec中的mode去分别设置宽高
     * 不过自定义View的测量可以根据自己的期望来设置
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = getMeasureWidth();
        int measureHeight = getMeasureHeight();
        int i = resolveSize(measureWidth, widthMeasureSpec);
        int j = resolveSize(measureHeight, heightMeasureSpec);
        setMeasuredDimension(i, j);
    }

    // 根据mode和实际测量设置宽，本View未采用
    private int getWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        // 可用空间
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getMeasureWidth();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                /**
                 * 此处可以不比较大小，因为用户的需要大于一切，
                 * 那么我们会说如果我们自己测量的宽大于上面的result(specSize)怎么办？那当然是出现Bug啦，
                 * 因此若加了 Math.max(getMeasureWidth(), result) 处理则会避免由用户设置的过大而导致的Bug
                 * 不过虽然可以避免用户设置导致的Bug，但是可能需要开发此View的人依旧需要做相应的处理
                 * */
                result = Math.max(getMeasureWidth(), result);
                break;
        }
        return result;
    }

    // 根据mode和实际测量设置高，本View未采用
    private int getHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getMeasureHeight();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                // 同理getWidth方法
                result = Math.max(getMeasureHeight(), result);
                break;
        }
        return result;
    }

    // 获取测量的宽
    private int getMeasureWidth() {
        int widthResult = 0;
        // 3 * widthSpace : 图标左侧、图标与文字中间、文字右侧都设置 5dp 间距
        widthResult += selected.getWidth() + 3 * widthSpace + paintText.measureText(likeNumber + "");
        // 一定不要忘记累加padding值
        widthResult += getPaddingLeft() + getPaddingRight();
        return widthResult;
    }

    // 获取测量的高
    private int getMeasureHeight() {
        int heightResult = 0;

        // 获取点赞图标以及点赞火花图标组合后的高度
        // , shinAndThubSpace 的原因是两图标组合并非是上下并列，而是火花会靠近点赞图标
        int iconHeight = selected.getHeight() + shining.getHeight() + shinAndThubSpace;
        heightResult = Math.max(textHeight, iconHeight);
        heightResult += getPaddingTop() + getPaddingBottom();
        return heightResult;
    }

    // 周期函数--View大小发生改变时回调
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", "onSizeChanged ： width == " + w + "  height == " + h + " oldw == " + oldw + " oldh == " + oldh);
        radiusEnd = getCircleData()[2] + 3;
    }

    /**
     * 绘制的位置，一般在测量时已经确定好其位置
     * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIcon(canvas);
        drawNumber(canvas);
    }

    // 画图标
    private void drawIcon(Canvas canvas) {
        float left = widthSpace + getPaddingLeft();
        float top = shining.getHeight() + getPaddingTop() + shinAndThubSpace;
        Log.e("getMeasureWidth", "getMeasure == " + getMeasureWidth());
        if (isLike) {
            float shinLeft = left + selected.getWidth() / 2 - shining.getWidth() / 2;
            float shinTop = getPaddingTop();
            canvas.drawBitmap(shining, shinLeft, shinTop, paintIcon);

            canvas.save();
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(selected, left, top, paintIcon);
            canvas.restore();

            float[] circleData = getCircleData();
            paintCircle.setAlpha(alpha);
            canvas.drawCircle(circleData[0], circleData[1], radius, paintCircle);
        } else {
            canvas.save();
            canvas.scale(mUnScale, mUnScale);
            canvas.drawBitmap(unselected, left, top, paintIcon);
            canvas.restore();
        }
    }

    // 画数字
    private void drawNumber(Canvas canvas) {
        Log.e("getMeasureHeight", "getMeasure == " + getMeasureHeight() + "  " + textDescentAndBaselineSpace);
        float left = selected.getWidth() + 2 * widthSpace + getPaddingLeft();
        float top = shining.getHeight() + getPaddingTop() + shinAndThubSpace + selected.getHeight() / 2 + textHeight / 2 - textDescentAndBaselineSpace;
        canvas.drawText(likeNumber + "", left, top, paintText);
    }

    // 获取圆的信息-- 圆中心位置(坐标)、和半径
    private float[] getCircleData() {
        // 此圆最大要完全包裹点赞图标和火花图标，因此其圆心Y坐标要在点赞和火花图标整体的中心
        float centerX = getPaddingLeft() + widthSpace + selected.getWidth() / 2;
        float iconHeight = shining.getHeight() + selected.getHeight() + shinAndThubSpace;
        float centerY = getPaddingTop() + iconHeight / 2;
        float iconWidthMax = Math.max(shining.getWidth(), selected.getWidth());
        float radius = Math.max(iconWidthMax, iconHeight) / 2;
        return new float[]{centerX, centerY,radius};
    }

    // --------------------------------Animate Start-------------------------------------

    @Override
    public void onClick(View v) {
        if (isLike) {
            likeNumber--;
            showThumbDownAnim();
        } else {
            likeNumber++;
            showThumbUpAnim();
        }
    }

    private float getCircleRadiusAnim() {
        return radius;
    }

    // 圆半径大小动画
    public void setCircleRadiusAnim(float rudiusAnim) {
        radius = rudiusAnim;
        invalidate();
    }

    private int getCircleColorAnim() {
        return alpha;
    }

    // 透明度动画
    public void setCircleColorAnim(int alphaAnim) {
        alpha = alphaAnim;
        invalidate();
    }

    public float getUnSelectAnim() {
        return mUnScale;
    }

    // 取消点赞图标缩放动画
    public void setUnSelectAnim(float scaleSize) {
        mUnScale = scaleSize;
        invalidate();
    }

    public float getSelectAnim() {
        return mScale;
    }

    // 点赞图标缩放动画
    public void setSelectAnim(float scaleSize) {
        mScale = scaleSize;
        invalidate();
    }

    /**
     * 展示点赞动画
     * */
    public void showThumbUpAnim() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "selectAnim", SCALE_MIN, SCALE_MAX);
        animator1.setDuration(150);
        animator1.setInterpolator(new OvershootInterpolator());

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "unSelectAnim", SCALE_MAX, SCALE_MIN);
        animator2.setDuration(150);
        animator2.addListener(new ClickAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isLike = true;
            }
        });

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator animator3 = ObjectAnimator.ofInt(this, "circleColorAnim", alphaStart, alphaEnd);
        animator3.setInterpolator(new DecelerateInterpolator());

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(this, "circleRadiusAnim", radiusStart, radiusEnd);
        animator4.setDuration(150);

        AnimatorSet set = new AnimatorSet();
        set.play(animator1).with(animator3).with(animator4);
        set.play(animator1).after(animator2);
        set.start();
    }

    /**
     * 展示取消点赞动画
     * */
    public void showThumbDownAnim() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "selectAnim", SCALE_MAX, SCALE_MIN);
        animator1.setDuration(150);
        animator1.addListener(new ClickAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isLike = false;
            }
        });

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "unSelectAnim", SCALE_MIN, SCALE_MAX);
        animator2.setDuration(150);
        animator2.setInterpolator(new OvershootInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(animator2).before(animator1);
        set.start();
    }

    /**
     * 动画监听
     * */
    private abstract class ClickAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);

        }
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    // --------------------------------Animate End---------------------------------------
}
