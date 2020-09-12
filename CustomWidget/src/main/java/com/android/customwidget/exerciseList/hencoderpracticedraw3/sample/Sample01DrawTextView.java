package com.android.customwidget.exerciseList.hencoderpracticedraw3.sample;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Sample01DrawTextView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String text = "Hello HenCoder";

    public Sample01DrawTextView(Context context) {
        super(context);
    }

    public Sample01DrawTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Sample01DrawTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        paint.setTextSize(60);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(text, 50, 100, paint);
    }
}
