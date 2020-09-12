package com.android.customwidget.exerciseList.hencoderpracticedraw3.practice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Practice04SetTypefaceView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String text = "Hello HenCoder";
    Typeface typeface;

    public Practice04SetTypefaceView(Context context) {
        super(context);
    }

    public Practice04SetTypefaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice04SetTypefaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        paint.setTextSize(60);

        typeface = Typeface.createFromAsset(getContext().getAssets(), "Satisfy-Regular.ttf");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 使用 Paint.setTypeface() 来设置不同的字体

        // 第一处：填入 null 来设置默认字体
        paint.setTypeface(null);
        canvas.drawText(text, 50, 100, paint);
        // 第二处：填入 Typeface.SERIF 来设置衬线字体（宋体）
        paint.setTypeface(Typeface.SERIF);
        canvas.drawText(text, 50, 200, paint);
        // 第三处：填入 typeface 对象来使用 assets 目录下的 "Satisfy-Regular.ttf" 文件
        paint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "Satisfy-Regular.ttf"));
        canvas.drawText(text, 50, 300, paint);
    }
}
