package com.android.customwidget.exerciseList.hencoderpracticedraw3.practice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class Practice06SetStrikeThruTextView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    String text = "Hello HenCoder";

    public Practice06SetStrikeThruTextView(Context context) {
        super(context);
    }

    public Practice06SetStrikeThruTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice06SetStrikeThruTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        paint.setTextSize(60);

        // 使用 Paint.setStrikeThruText() 来设置删除线
        paint.setStrikeThruText(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawText(text, 50, 100, paint);
    }
}
