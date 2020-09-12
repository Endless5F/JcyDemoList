package com.android.baselibrary.widget.keyboard1;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.android.baselibrary.R;
import com.android.baselibrary.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class KeyboardViewUnity extends RelativeLayout {
    private static final String TAG = "KeyboardViewUnity";

    public enum KeyboardType {
        ENG, //english
        FRE, //french
        SPA, //spanish
        SYM, //symbol
        DIG  //digital
    }

    public static final int FUNCTIONAL_KEY_SHIFT = -1; //switch letter case
    public static final int FUNCTIONAL_KEY_DEL = -2; //backspace
    public static final int FUNCTIONAL_KEY_ENTER = -3;
    public static final int FUNCTIONAL_KEY_NUMBER = -4; //switch to digital keyboard
    public static final int FUNCTIONAL_KEY_SYMBOL = -5; //switch to symbol keyboard
    public static final int FUNCTIONAL_KEY_MODE_CHANGE = -6; //change language
    public static final int FUNCTIONAL_KEY_LETTER = -7; //switch to lang keyboard
    public static final int FUNCTIONAL_KEY_VOICE = -8; //?

    private Context mContext;

    private PolyKeyboard mKbEng;
    private PolyKeyboard mKbFRE;
    private PolyKeyboard mKbSpa;

    private PolyKeyboard mKbSym;
    private PolyKeyboard mKbDig;

    private KeyboardType currentType;
    private KeyboardType currentLangType;
    private PolyKeyboardView mKeyboardView;
    private boolean isUpperCase = false;

    private PolyKeyboard.KeyStyle nightmareKeyboardStyle = new PolyKeyboard.KeyStyle() {
        @Override
        public Drawable background(Keyboard.Key key) {
            return key.iconPreview;
        }

        @Override
        public Drawable shiftedBackground(Keyboard.Key key) {
            if (key.codes[0] == KeyboardViewUnity.FUNCTIONAL_KEY_SHIFT) {
                return getResources().getDrawable(R.drawable.key_bg_shift_capped);
            }
            return null;
        }

        @Override
        public int disabledTextColor(Keyboard.Key key) {
            return 0x4dffffff;
        }
    };

    private ArrayList<Integer> mCustomFunctionalKey = new ArrayList<>();

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
        LayoutInflater.from(context).inflate(R.layout.layout_poly_keyboard_view, this);
        this.mKeyboardView = (PolyKeyboardView) findViewById(R.id.poly_keyboard_view);
    }

    public void init(KeyboardType type) {
        mKbDig = new PolyKeyboard(mContext, R.xml.keyboard_dig);
        mKbSym = new PolyKeyboard(mContext, R.xml.keyboard_sym);
        mKbEng = new PolyKeyboard(mContext, R.xml.keyboard_eng);
        mKbFRE = new PolyKeyboard(mContext, R.xml.keyboard_fre);
        mKbSpa = new PolyKeyboard(mContext, R.xml.keyboard_spa);

        mKbEng.setKeyStyle(nightmareKeyboardStyle);
        mKbFRE.setKeyStyle(nightmareKeyboardStyle);
        mKbSpa.setKeyStyle(nightmareKeyboardStyle);
        mKbDig.setKeyStyle(nightmareKeyboardStyle);
        mKbSym.setKeyStyle(nightmareKeyboardStyle);

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
        isUpperCase = false;
        currentType = type;
        PolyKeyboard target = null;
        switch (type) {
            case ENG:
                target = mKbEng;
                currentLangType = type;
                break;
            case FRE:
                target = mKbFRE;
                currentLangType = type;
                break;
            case SPA:
                target = mKbSpa;
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
        isUpperCase = false;
        mKeyboardView.setShifted(isUpperCase);
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
                            mListener.onText(isUpperCase ? key.label.toString().toUpperCase() : key.label.toString().toLowerCase());
                        }
                    }

                    //TODO use the keycode-to-letter map
//                    String letter = (String) Keycode2Letter.map.get(primaryCode);
//                    mListener.onText(isUpperCase ? letter.toUpperCase() : letter.toLowerCase());
                }
                setKeyState();
            }

            @Override
            public void onText(CharSequence text) {
                LogUtils.d(TAG, "onText = " + text);
                if (null != mListener) {
                    mListener.onText(text);
                }
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

    public void preventDefaultFunctionalKey(int keyCode) {
        this.mCustomFunctionalKey.add(keyCode);
    }

    private boolean handleFunctionalKey(int keyCode) {
        if (mCustomFunctionalKey.contains(keyCode)) {
            return false;
        }
        switch (keyCode) {
            case FUNCTIONAL_KEY_DEL:

                break;
            case FUNCTIONAL_KEY_ENTER:

                break;
            case FUNCTIONAL_KEY_MODE_CHANGE:
                if (currentLangType == KeyboardType.ENG || currentLangType == KeyboardType.FRE) {
                    switchKeyboard(KeyboardType.SPA);
                } else {
                    switchKeyboard(KeyboardType.ENG);
                }
                //TODO if the eng keyboard is any different with fre, use the logic below
//                if (currentLangType == KeyboardType.ENG) {
//                    switchKeyboard(KeyboardType.FRE);
//                } else if (currentLangType == KeyboardType.FRE) {
//                    switchKeyboard(KeyboardType.SPA);
//                } else {
//                    switchKeyboard(KeyboardType.ENG);
//                }
                return true;

            case FUNCTIONAL_KEY_LETTER:
                switchKeyboard(currentLangType);
                return true;
            case FUNCTIONAL_KEY_NUMBER:
                switchKeyboard(KeyboardType.DIG);
                return true;
            case FUNCTIONAL_KEY_SHIFT:
                changeKeyboart();
                if (null != mListener) {
                    mListener.onText("shift");
                    mListener.onKey(FUNCTIONAL_KEY_SHIFT);
                }
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

    private void changeKeyboart() {
        List<Keyboard.Key> keyList = mKeyboardView.getKeyboard().getKeys();
        if (isUpperCase) {// 大写切换小写
            isUpperCase = false;
            for (Keyboard.Key key : keyList) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
            }
        } else {// 小写切换成大写
            isUpperCase = true;
            for (Keyboard.Key key : keyList) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
            }
        }
        mKeyboardView.setShifted(isUpperCase);
    }

    /**
     * 判断是否是字母
     */
    private boolean isLetter(String str) {
        String wordStr = "abcdefghijklmnopqrstuvwxyz";
        return wordStr.contains(str.toLowerCase());
    }
}
