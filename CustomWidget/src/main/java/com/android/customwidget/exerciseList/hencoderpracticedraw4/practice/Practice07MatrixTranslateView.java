package com.android.customwidget.exerciseList.hencoderpracticedraw4.practice;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.android.customwidget.R;

/**
 * 使用 Matrix 来做变换
 * Matrix 做常见变换的方式：
 *
 * 创建 Matrix 对象；
 *  调用 Matrix 的 pre/postTranslate/Rotate/Scale/Skew() 方法来设置几何变换；
 *  使用 Canvas.setMatrix(matrix) 或 Canvas.concat(matrix) 来把几何变换应用到 Canvas。
 *
 * 把 Matrix 应用到 Canvas 有两个方法： Canvas.setMatrix(matrix) 和 Canvas.concat(matrix)。
 *  Canvas.setMatrix(matrix)：用 Matrix 直接替换 Canvas 当前的变换矩阵，即抛弃 Canvas 当前的变换，
 *      改用 Matrix 的变换（注：根据下面评论里以及我在微信公众号中收到的反馈，不同的系统中  setMatrix(matrix) 的行为
 *      可能不一致，所以还是尽量用 concat(matrix) 吧）；
 *  Canvas.concat(matrix)：用 Canvas 当前的变换矩阵和 Matrix 相乘，即基于 Canvas 当前的变换，叠加上 Matrix 中的变换。
 *
 *
 *  使用 Matrix 来做自定义变换 : Matrix 的自定义变换使用的是 setPolyToPoly() 方法。
 *   Matrix.setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) 用点对点映射的方式设置变换
 *      poly 就是「多」的意思。setPolyToPoly() 的作用是通过多点的映射的方式来直接设置变换。
 *      「多点映射」的意思就是把指定的点移动到给出的位置，从而发生形变。
 *      例如：(0, 0) -> (100, 100) 表示把 (0, 0) 位置的像素移动到 (100, 100) 的位置，
 *      这个是单点的映射，单点映射可以实现平移。而多点的映射，就可以让绘制内容任意地扭曲。
 *  代码示例：
 *      Matrix matrix = new Matrix();
 *      float pointsSrc = {left, top, right, top, left, bottom, right, bottom};
 *      float pointsDst = {left - 10, top + 50, right + 120, top - 90, left + 20, bottom + 30, right + 20, bottom + 60};
 *
 *      ...
 *
 *      matrix.reset();
 *      matrix.setPolyToPoly(pointsSrc, 0, pointsDst, 0, 4);
 *
 *      canvas.save();
 *      canvas.concat(matrix);
 *      canvas.drawBitmap(bitmap, x, y, paint);
 *      canvas.restore();
 */
public class Practice07MatrixTranslateView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;
    Point point1 = new Point(200, 200);
    Point point2 = new Point(600, 200);
    Matrix matrix = new Matrix();

    public Practice07MatrixTranslateView(Context context) {
        super(context);
    }

    public Practice07MatrixTranslateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice07MatrixTranslateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.maps);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        matrix.reset();
        matrix.postTranslate(-100,-100);
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, point1.x, point1.y, paint);
        canvas.restore();

        canvas.save();
        matrix.reset();
        matrix.postTranslate(200, 0);
        canvas.concat(matrix);
        canvas.drawBitmap(bitmap, point2.x, point2.y, paint);
        canvas.restore();
    }
}
