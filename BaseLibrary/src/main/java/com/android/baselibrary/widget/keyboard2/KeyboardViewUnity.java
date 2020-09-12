package com.android.baselibrary.widget.keyboard2;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.android.baselibrary.R;
import com.android.baselibrary.util.LogUtils;

public class KeyboardViewUnity extends RelativeLayout {
    private static final String TAG = "KeyboardViewUnity";

    public enum KeyboardType {
        ENG, //english
        SYM, //symbol
        DIG  //digital
    }

    public static final int FUNCTIONAL_KEY_CAPS = -1; //switch letter case
    public static final int FUNCTIONAL_KEY_DEL = -2; //backspace
    public static final int FUNCTIONAL_KEY_ENTER = -3;
    public static final int FUNCTIONAL_KEY_NUMBER = -4; //switch to digital keyboard
    public static final int FUNCTIONAL_KEY_SYMBOL = -5; //switch to symbol keyboard
    public static final int FUNCTIONAL_KEY_LETTER = -7; //switch to lang keyboard
    public static final int FUNCTIONAL_KEY_SPACE = 32;

    private Context mContext;

    private PolyKeyboard mKbEng;
    private PolyKeyboard mKbSym;
    private PolyKeyboard mKbDig;

    private KeyboardType currentLangType;
    private PolyKeyboardView mKeyboardView;

    private OnKeyboardActionListener mListener;

    public KeyboardViewUnity(Context context) {
        this(context, null);
    }

    public KeyboardViewUnity(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyboardViewUnity(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.layout_poly_keyboard_view2, this);
        this.mKeyboardView = (PolyKeyboardView) findViewById(R.id.poly_keyboard_view);
    }

    public void init(KeyboardType type) {
        mKbDig = new PolyKeyboard(mContext, R.xml.keyboard_dig);
        mKbSym = new PolyKeyboard(mContext, R.xml.keyboard_sym);
        mKbEng = new PolyKeyboard(mContext, R.xml.keyboard_eng2);

        registerCallback();
        switchKeyboard(type);
        showKeyboard();
    }

    public void hideKeyboard() {
        setVisibility(GONE);
    }


    public void showKeyboard() {
        setVisibility(VISIBLE);
    }


    public void switchKeyboard(KeyboardType type) {
        PolyKeyboard target = null;
        switch (type) {
            case ENG:
                target = mKbEng;
                currentLangType = type;
                break;
            case DIG:
                target = mKbDig;
                break;
            case SYM:
                target = mKbSym;
                break;
            default:

                break;
        }
        mKeyboardView.setKeyboard(target);
        mKeyboardView.setEnabled(true);
        mKeyboardView.setPreviewEnabled(false);
        setKeyState();
    }

    private void registerCallback() {
        mKeyboardView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {

            }

            @Override
            public void onRelease(int primaryCode) {

            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                LogUtils.d(TAG, "onKey = " + primaryCode);
                if (handleFunctionalKey(primaryCode)) return;
                if (null != mListener) {
                    mListener.onKey(primaryCode);

                    for (Keyboard.Key key : mKeyboardView.getKeyboard().getKeys()) {
                        if (null != key.label && key.codes[0] == primaryCode) {
                            mListener.onText(key.label.toString());
                            break;
                        }
                    }
                }
                setKeyState();
            }

            @Override
            public void onText(CharSequence text) {
                LogUtils.d(TAG, "onText = " + text.toString());
            }

            @Override
            public void swipeLeft() {

            }

            @Override
            public void swipeRight() {

            }

            @Override
            public void swipeDown() {

            }

            @Override
            public void swipeUp() {

            }
        });
    }

    private boolean handleFunctionalKey(int keyCode) {
        switch (keyCode) {
            case FUNCTIONAL_KEY_CAPS:
                if (mListener != null) {
                    mListener.onText(null);
                    mListener.onKey(FUNCTIONAL_KEY_CAPS);
                }
                return true;
            case FUNCTIONAL_KEY_DEL:
                if (mListener != null) {
                    mListener.onText(null);
                    mListener.onKey(FUNCTIONAL_KEY_DEL);
                }
                setKeyState();
                return true;
            case FUNCTIONAL_KEY_SPACE:
                if (mListener != null) {
                    mListener.onText(null);
                    mListener.onKey(FUNCTIONAL_KEY_SPACE);
                }
                return true;
            case FUNCTIONAL_KEY_ENTER:
                if (mListener != null) {
                    mListener.onText(null);
                    mListener.onKey(FUNCTIONAL_KEY_ENTER);
                }
                return true;
            case FUNCTIONAL_KEY_LETTER:
                switchKeyboard(currentLangType);
                return true;
            case FUNCTIONAL_KEY_NUMBER:
                switchKeyboard(KeyboardType.DIG);
                return true;
            case FUNCTIONAL_KEY_SYMBOL:
                switchKeyboard(KeyboardType.SYM);
                return true;
            default:
                break;
        }
        return false;
    }

    private void setKeyState() {
        if (null != mListener) {
            Integer keyCode = FUNCTIONAL_KEY_ENTER; //set the 'send' key's state
            if (mListener.hasText()) {
                mKeyboardView.enableKey(keyCode);
            } else {
                mKeyboardView.disabledKey(keyCode);
            }
        }
    }

    public void setOnKeyboardActionListener(OnKeyboardActionListener listener) {
        this.mListener = listener;
        setKeyState();
    }

    public interface OnKeyboardActionListener {
        void onKey(int primarykeycode);

        //append this 'text' to Edittext's text
        void onText(CharSequence text);

        //return if Edittext.getText().length != 0
        boolean hasText();
    }
}
