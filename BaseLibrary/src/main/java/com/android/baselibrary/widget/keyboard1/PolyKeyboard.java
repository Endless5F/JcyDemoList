package com.android.baselibrary.widget.keyboard1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;

//specify the iconPreview as background
public class PolyKeyboard extends Keyboard {

    private KeyStyle mKeyStyle;
    private Context mContext;

    public PolyKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public PolyKeyboard(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public PolyKeyboard(Context context, int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
        this.mContext = context;
    }

    public void setKeyStyle(KeyStyle mKeyStyle) {
        this.mKeyStyle = mKeyStyle;
    }

    public KeyStyle getKeyStyle() {
        if (null == this.mKeyStyle) {
            this.mKeyStyle = new DefaultKeyStyle();
        }
        return mKeyStyle;
    }

    public interface KeyStyle {
        Drawable background(Key key);

        Drawable shiftedBackground(Key key);

        int disabledTextColor(Key key);
    }

    public class DefaultKeyStyle implements KeyStyle {

        @Override
        public Drawable background(Key key) {
            return key.iconPreview;
        }

        @Override
        public Drawable shiftedBackground(Key key) {
            return null;
        }

        @Override
        public int disabledTextColor(Key key) {
            return -1;
        }
    }
}
