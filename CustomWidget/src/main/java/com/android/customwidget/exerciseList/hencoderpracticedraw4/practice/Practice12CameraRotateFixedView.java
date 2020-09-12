package com.android.customwidget.exerciseList.hencoderpracticedraw4.practice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.android.customwidget.R;

/**
 * Camera 的三维变换有三类：旋转、平移、移动相机。
 *  Camera.rotate*() 三维旋转
 *       一共有四个方法： rotateX(deg) rotateY(deg) rotateZ(deg) rotate(x, y, z)。
 *  另外，Camera 和 Canvas 一样也需要保存和恢复状态才能正常绘制，不然在界面刷新之后绘制就会出现问题
 *
 *  如果你需要图形左右对称，需要配合上 Canvas.translate()，
 *  在三维旋转之前把绘制内容的中心点移动到原点，即旋转的轴心，然后在三维旋转后再把投影移动回来
 *
 *  注：Camera的投影点默认在Z轴上
 * */
public class Practice12CameraRotateFixedView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;
    Point point1 = new Point(200, 200);
    Point point2 = new Point(600, 200);
    Camera camera = new Camera();
    Matrix matrix = new Matrix();

    public Practice12CameraRotateFixedView(Context context) {
        super(context);
    }

    public Practice12CameraRotateFixedView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice12CameraRotateFixedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.maps);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int center1X = point1.x + bitmapWidth / 2;
        int center1Y = point1.y + bitmapHeight / 2;
        int center2X = point2.x + bitmapWidth / 2;
        int center2Y = point2.y + bitmapHeight / 2;

        // 多个canvas的几何变换操作，是反着执行的，
        // 即下面代码中会先执行translate(-center1X,-center1Y)后执行translate(center1X,center1Y)

        canvas.save();
        camera.save();
        canvas.translate(center1X,center1Y);
        camera.rotateX(30);
        camera.applyToCanvas(canvas);
        camera.restore();
        canvas.drawBitmap(bitmap, point1.x, point1.y, paint);
        canvas.translate(-center1X,-center1Y);
        canvas.restore();

        camera.save();
        matrix.reset();
        camera.rotateY(30);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-center2X, -center2Y);
        matrix.postTranslate(center2X, center2Y);
        canvas.save();
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, point2.x, point2.y, paint);
        canvas.restore();
    }
}
