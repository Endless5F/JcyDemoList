package com.android.baselibrary.widget.keyboard2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.android.baselibrary.util.LogUtils;

import java.lang.reflect.Field;
import java.util.List;

//override the KeyboardView to customize key background
//use #iconPreview in xmL file to specify background
public class PolyKeyboardView extends KeyboardView {

    private static final String TAG = "PolyKeyboardView";

    private int rKeyTextSize;
    private int rKeyTextColor;
    private int rLabelTextSize;
    private Drawable rKeyBackground;

    public PolyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public PolyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        rKeyTextSize = (int) getFieldValue(this, "mKeyTextSize");
        rKeyTextColor = (int) getFieldValue(this, "mKeyTextColor");
        rLabelTextSize = (int) getFieldValue(this, "mLabelTextSize");
        rKeyBackground = (Drawable) getFieldValue(this, "mKeyBackground");
    }

    @Override
    public void onDraw(Canvas canvas) {
        //仅对PolyKeyboard重绘
        if (null == getKeyboard() || !(getKeyboard() instanceof PolyKeyboard)) {
            super.onDraw(canvas);
            return;
        }
        List<Keyboard.Key> keys = getKeyboard().getKeys();
        if (keys != null && keys.size() > 0) {
            for (Keyboard.Key key : keys) {
                if (key.codes[0] == -1) {
                    key.label = "";
                }
            }
        }
        super.onDraw(canvas);
        onRefreshKey(canvas);
    }

    /**
     * @param canvas
     */
    private void onRefreshKey(Canvas canvas) {
        Keyboard keyboard = getKeyboard();
        if (keyboard == null) return;
        List<Keyboard.Key> keys = keyboard.getKeys();
        if (keys != null && keys.size() > 0) {
            final TextPaint paint = new TextPaint();
            paint.setTextAlign(Paint.Align.CENTER);
            Typeface font = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);
            paint.setTypeface(font);
            paint.setAntiAlias(true);
            for (Keyboard.Key key : keys) {
//                if (key.codes[0] == -3) {
//                    Drawable dr = getContext().getResources().getDrawable(R.drawable.keyboard_blue);
//                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                    dr.draw(canvas);
//                } else {
//                    Drawable dr = getContext().getResources().getDrawable(R.drawable.keyboard_white);
//                    dr.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                    dr.draw(canvas);
//                }
                if (key.label != null) {
                    if (key.codes[0] == -4 || key.codes[0] == -5) {
                        paint.setTextSize(17 * 2);
                    } else {
                        paint.setTextSize(20 * 2);
                    }
//                    if (key.codes[0] == -4) {
//                        paint.setColor(getContext().getResources().getColor(R.color.white));
//                    } else {
//                        paint.setColor(getContext().getResources().getColor(R.color.blue_03A9F4));
//                    }
                    paint.setColor(rKeyTextColor);
                    // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
//                    paint.setTextAlign(Paint.Align.CENTER);

                    if (key.codes[0] == -1) {
                        String caps = "caps\nlock";
                        // 实现换行
                        StaticLayout staticLayout = new StaticLayout(caps, paint, key.width,
                                Layout.Alignment.ALIGN_CENTER, 1, 0, false);
                        canvas.save();
                        int widthCaps = (int) paint.measureText(caps);//测出来的宽度是"capslock"的宽度，而不是换行后的
                        Rect rect = new Rect(key.x, key.y, key.x + key.width, key.y + key.height);
                        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
                        int dx = key.x + (key.width - widthCaps / 2);
                        Log.e("long all", "caps long : " + widthCaps + "   " + key.x + "   " + key.width + "  " + dx);
                        int dy = (rect.bottom + rect.top - fontMetrics.bottom - fontMetrics.top - key.height) / 2;
                        canvas.translate(dx, dy);
                        staticLayout.draw(canvas);
                        canvas.restore();
//                        canvas.drawText("Caps", rect.centerX(), dy, paint);
                    }
                } else if (key.iconPreview != null) {
//                    final int drawableX = (key.width - padding.left - padding.right
//                            - key.iconPreview.getIntrinsicWidth()) / 2 + padding.left;
//                    final int drawableY = (key.height - padding.top - padding.bottom
//                            - key.iconPreview.getIntrinsicHeight()) / 2 + padding.top;
//                    canvas.translate(drawableX, drawableY);
//                    rKeyBackground.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
//                    rKeyBackground.draw(canvas);
                    key.iconPreview.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
                    key.iconPreview.draw(canvas);

                }
            }
        }

    }

    private Object getFieldValue(Object obj, String fieldName) {
        if (null == obj || TextUtils.isEmpty(fieldName)) {
            return null;
        }

        Class<?> clazz = obj.getClass();
        while (clazz != Object.class) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(obj);
            } catch (Exception e) {
            }
            clazz = clazz.getSuperclass();
        }
        LogUtils.d(TAG, "error reflecting field = " + fieldName);
        return null;
    }

    public void disabledKey(Integer keyCode) {
        invalidate();
    }

    public void enableKey(Integer keyCode) {
        invalidate();
    }
}



