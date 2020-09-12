package com.android.customwidget.exerciseList.hencoderpracticedraw3.practice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Practice12MeasureTextView extends View {
    Paint paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    String text1 = "三个月内你胖了";
    String text2 = "4.5";
    String text3 = "公斤";
    private float measureText1;
    private float measureText2;

    public Practice12MeasureTextView(Context context) {
        super(context);
    }

    public Practice12MeasureTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice12MeasureTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        paint1.setTextSize(60);
        paint2.setTextSize(120);
        paint2.setColor(Color.parseColor("#E91E63"));
        measureText1 = paint1.measureText(text1);
        measureText2 = paint2.measureText(text2);

        /**
         * 获取文字高度：
         *
         * 1.基准线是baseline
         * 2.ascent：字体在baseline上方被推荐的距离（一些字体制作商需要参考这个）
         * 3.descent：字体在是baseline下方被推荐的距离（一些字体制作商需要参考这个）
         * 4.top：ascent的最大值
         * 5.bottom：descent的最大值
         */
        // 方法一：
//        int textHeight = (int) (mPaint.descent()-mPaint.ascent());
        // 方法二：
//        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
//        Paint.FontMetricsInt fm=  mPaint.getFontMetricsInt();
//        float ascent = fontMetrics.ascent;
//        float descent = fontMetrics.descent;
//        float top = fontMetrics.top;
//        float bottom = fontMetrics.bottom;
//        float leading = fontMetrics.leading;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 使用 Paint.measureText 测量出文字宽度，让文字可以相邻绘制

        canvas.drawText(text1, 50, 200, paint1);
        canvas.drawText(text2, 50 + measureText1, 200, paint2);
        canvas.drawText(text3, 50 + measureText1 + measureText2, 200, paint1);
    }
}
