package com.android.customwidget.exerciseList.hencoderpracticedraw1.practice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Practice2DrawCircleView extends View {

    private Paint paint1;
    private Paint paint2;
    private Paint paint3;
    private Paint paint4;

    public Practice2DrawCircleView(Context context) {
        super(context);
        init();
    }

    public Practice2DrawCircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Practice2DrawCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint1 = new Paint();
        paint1.setAntiAlias(true);
        paint1.setColor(Color.BLACK);
        paint1.setStyle(Paint.Style.FILL);
        paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setColor(Color.BLACK);
        paint2.setStyle(Paint.Style.STROKE);
        paint3 = new Paint();
        paint3.setAntiAlias(true);
        paint3.setColor(Color.BLUE);
        paint3.setStyle(Paint.Style.FILL);
        paint4 = new Paint();
        paint4.setAntiAlias(true);
        paint4.setColor(Color.BLACK);
        paint4.setStyle(Paint.Style.STROKE);
        paint4.setStrokeWidth(20f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        练习内容：使用 canvas.drawCircle() 方法画圆
//        一共四个圆：1.实心圆 2.空心圆 3.蓝色实心圆 4.线宽为 20 的空心圆
        canvas.drawCircle(120,120,50,paint1);
        canvas.drawCircle(240,120,50,paint2);
        canvas.drawCircle(120,240,50,paint3);
        canvas.drawCircle(240,240,50,paint4);
    }
}
