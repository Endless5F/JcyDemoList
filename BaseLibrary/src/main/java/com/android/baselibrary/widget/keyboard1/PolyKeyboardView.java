package com.android.baselibrary.widget.keyboard1;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.baselibrary.util.LogUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

//override the KeyboardView to customize key background
//use #iconPreview in xmL file to specify background
public class PolyKeyboardView extends KeyboardView {

    private static final String TAG = "PolyKeyboardView";

    private Drawable rKeyBackground;
    private int rLabelTextSize;
    private int rKeyTextSize;
    private int rKeyTextColor;
    private float rShadowRadius;
    private int rShadowColor;
    private Set<Integer> disabledKeys = new TreeSet<>();

    private Rect rClipRegion;
    private Keyboard.Key rInvalidatedKey;

    public PolyKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public PolyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PolyKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        rKeyBackground = (Drawable) getFieldValue(this, "mKeyBackground");
        rLabelTextSize = (int) getFieldValue(this, "mLabelTextSize");
        rKeyTextSize = (int) getFieldValue(this, "mKeyTextSize");
        rKeyTextColor = (int) getFieldValue(this, "mKeyTextColor");
        rShadowColor = (int) getFieldValue(this, "mShadowColor");
        rShadowRadius = (float) getFieldValue(this, "mShadowRadius");
    }

    @Override
    public void onDraw(Canvas canvas) {
        //仅对PolyKeyboard重绘
        if (null == getKeyboard() || !(getKeyboard() instanceof PolyKeyboard)) {
            super.onDraw(canvas);
            return;
        }
        rClipRegion = (Rect) getFieldValue(this, "mClipRegion");
        rInvalidatedKey = (Keyboard.Key) getFieldValue(this, "mInvalidatedKey");
        super.onDraw(canvas);
        onRefreshKey(canvas);
    }

    /**
     * @param canvas
     */
    private void onRefreshKey(Canvas canvas) {
        final Paint paint = (Paint) getFieldValue(this, "mPaint");
        final Rect padding = (Rect) getFieldValue(this, "mPadding");

        paint.setColor(rKeyTextColor);
        final int kbdPaddingLeft = getPaddingLeft();
        final int kbdPaddingTop = getPaddingTop();
        Drawable keyBackground = null;

        final Rect clipRegion = rClipRegion;
        final Keyboard.Key invalidKey = rInvalidatedKey;
        boolean drawSingleKey = false;
        if (invalidKey != null && canvas.getClipBounds(clipRegion)) {
            // Is clipRegion completely contained within the invalidated key?
            if (invalidKey.x + kbdPaddingLeft - 1 <= clipRegion.left &&
                    invalidKey.y + kbdPaddingTop - 1 <= clipRegion.top &&
                    invalidKey.x + invalidKey.width + kbdPaddingLeft + 1 >= clipRegion.right &&
                    invalidKey.y + invalidKey.height + kbdPaddingTop + 1 >= clipRegion.bottom) {
                drawSingleKey = true;
            }
        }

        PolyKeyboard.KeyStyle customKeyStyle = ((PolyKeyboard) getKeyboard()).getKeyStyle();

        List<Keyboard.Key> keys = getKeyboard().getKeys();
        final int keyCount = keys.size();
        //canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        for (int i = 0; i < keyCount; i++) {
            final Keyboard.Key key = keys.get(i);
            if (drawSingleKey && invalidKey != key) {
                continue;
            }

            //get shifted first
            if (getKeyboard().isShifted()) {
                keyBackground = customKeyStyle.shiftedBackground(key);
            }
            //get special next
            if (null == keyBackground) {
                keyBackground = customKeyStyle.background(key);
            }
            //get common last
            if (null == keyBackground) {
                LogUtils.d(TAG, "curr key = " + key.codes[0]);
                keyBackground = rKeyBackground;
            }

            int[] drawableState = key.getCurrentDrawableState();
            keyBackground.setState(drawableState);

            CharSequence keyLabel = key.label;
            // Switch the character to uppercase if shift is pressed
            String label = keyLabel == null ? null : adjustCase(keyLabel).toString();

            final Rect bounds = keyBackground.getBounds();
            if (key.width != bounds.right ||
                    key.height != bounds.bottom) {
                keyBackground.setBounds(0, 0, key.width, key.height);
            }
            canvas.translate(key.x + kbdPaddingLeft, key.y + kbdPaddingTop);
            keyBackground.draw(canvas);

            if (label != null) {
                if (label.length() > 1 && key.codes.length < 2) {
                    paint.setTextSize(rLabelTextSize);
                    paint.setTypeface(Typeface.DEFAULT);
                    //paint.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    paint.setTextSize(rKeyTextSize);
                    paint.setTypeface(Typeface.DEFAULT);
                    //paint.setTypeface(Typeface.DEFAULT);
                }

                int disabledTextColor = customKeyStyle.disabledTextColor(key);
                if (disabledTextColor < 0) {
                    disabledTextColor = rKeyTextColor;
                }
                if(disabledKeys.contains(key.codes[0])){
                    paint.setColor(disabledTextColor);
                }else {
                    paint.setColor(rKeyTextColor);
                }

                // Draw a drop shadow for the text
                paint.setShadowLayer(rShadowRadius, 0, 0, rShadowColor);
                // Draw the text
                canvas.drawText(label,
                        (key.width - padding.left - padding.right) / 2
                                + padding.left,
                        (key.height - padding.top - padding.bottom) / 2
                                + (paint.getTextSize() - paint.descent()) / 2 + padding.top,
                        paint);
                // Turn off drop shadow
                paint.setShadowLayer(0, 0, 0, 0);
            } else if (key.icon != null) {
                final int drawableX = (key.width - padding.left - padding.right
                        - key.icon.getIntrinsicWidth()) / 2 + padding.left;
                final int drawableY = (key.height - padding.top - padding.bottom
                        - key.icon.getIntrinsicHeight()) / 2 + padding.top;
                canvas.translate(drawableX, drawableY);
                key.icon.setBounds(0, 0,
                        key.icon.getIntrinsicWidth(), key.icon.getIntrinsicHeight());
                key.icon.draw(canvas);
                canvas.translate(-drawableX, -drawableY);
            }
            canvas.translate(-key.x - kbdPaddingLeft, -key.y - kbdPaddingTop);

            //reset background for draw next key finally
            keyBackground = null;
        }
        rInvalidatedKey = null;
    }

    private CharSequence adjustCase(CharSequence label) {
        if (getKeyboard().isShifted() && label != null && label.length() < 3
                && Character.isLowerCase(label.charAt(0))) {
            label = label.toString().toUpperCase();
        }
        return label;
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
        disabledKeys.add(keyCode);
        invalidate();
    }

    public void enableKey(Integer keyCode) {
        if (disabledKeys.contains(keyCode)) {
            disabledKeys.remove(keyCode);
        }
        invalidate();
    }
}



