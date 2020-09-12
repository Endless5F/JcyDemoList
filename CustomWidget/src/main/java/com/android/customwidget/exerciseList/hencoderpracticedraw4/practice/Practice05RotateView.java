package com.android.customwidget.exerciseList.hencoderpracticedraw4.practice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.android.customwidget.R;

/**
 * 缩放：参数里的 sx sy 是横向和纵向的放缩倍数； px py 是放缩的轴心。
 * */
public class Practice05RotateView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;
    Point point1 = new Point(200, 200);
    Point point2 = new Point(600, 200);

    public Practice05RotateView(Context context) {
        super(context);
    }

    public Practice05RotateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice05RotateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.maps);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float bitmapCenterX1 = bitmap.getWidth() / 2 + point1.x;
        float bitmapCenterY1 = bitmap.getWidth() / 2 + point1.y;
        float bitmapCenterX2 = bitmap.getWidth() / 2 + point2.x;
        float bitmapCenterY2 = bitmap.getWidth() / 2 + point2.y;

        canvas.save();
        canvas.rotate(180,bitmapCenterX1,bitmapCenterY1);
        canvas.drawBitmap(bitmap, point1.x, point1.y, paint);
        canvas.restore();

        canvas.save();
        canvas.rotate(45,bitmapCenterX2,bitmapCenterY2);
        canvas.drawBitmap(bitmap, point2.x, point2.y, paint);
        canvas.restore();
    }
}