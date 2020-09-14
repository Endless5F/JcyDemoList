package com.android.framework.launch.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.Fragment;
import androidx.core.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.baselibrary.utils.LogUtils;
import com.android.baselibrary.widget.keyboard1.KeyboardViewUnity;
import com.android.framework.R;

public class InputFragmentCustomized extends Fragment
        implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = "InputFragmentCustomzied";
    @SuppressLint("StaticFieldLeak")
    private static InputFragmentCustomized sf;

    private String beforText = "";
    public EditText mEditText = null;
    private ImageView panalBg;
    private LinearLayout panalTips;
    private ImageView panalSymbolFinger = null;
    private ImageView panalSymbolGesture = null;
    private GestureDetectorCompat mDetector = null;
    private boolean isLongStarted = false;
    private String KEYCODE_SPACE = "SPACE";
    private String KEYCODE_ENTER = "ENTER";
    private String KEYCODE_SHIFT = "SHIFT";
    private String KEYCODE_BACKSPACE = "BACKSPACE";

    private KeyboardViewUnity customizedKeyboard;

    public static InputFragmentCustomized getInstance() {
        return new InputFragmentCustomized();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fm_input_customized, null);
        initView(v);
        return v;
    }

    private void initView(View v) {
        initGestureUI(v);
        initInputView(v);
    }

    private void initGestureUI(View v) {
        panalTips = (LinearLayout) v.findViewById(R.id.remote_center_panal_tips);
        panalSymbolGesture = ((ImageView) v.findViewById(R.id.remote_center_panal_symbol_gesture));
        initAnimation();
        panalSymbolFinger = ((ImageView) v.findViewById(R.id.remote_center_panal_symbol_finger));
        panalSymbolFinger.setImageResource(R.drawable.ic_dot_remote);
        panalSymbolFinger.setVisibility(View.INVISIBLE);
        mDetector = new GestureDetectorCompat(getContext(), this);
        initGestureUIBg(v);
    }

    private Animation animation;

    private void initAnimation() {
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.alpha_disappear);
        animation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                panalSymbolGesture.setImageDrawable(null);
            }

            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initGestureUIBg(View v) {
        panalBg = ((ImageView) v.findViewById(R.id.remote_center_panal_bg));
        panalBg.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                switch (arg1.getAction()) {
                    case MotionEvent.ACTION_UP:
                        panalSymbolFinger.setVisibility(View.INVISIBLE);
                        mDetector.onTouchEvent(arg1);
                        if (isLongStarted = true) {
                            Log.d("liuyuanyuan", "ontouch onLongEnd");
                            onLongEnd();
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL:
                        panalSymbolFinger.setVisibility(View.INVISIBLE);
                        mDetector.onTouchEvent(arg1);
                        if (isLongStarted = true) {
                            Log.d("liuyuanyuan", "ontouch onLongEnd");
                            onLongEnd();
                        }
                        return true;
                    default:
                        break;
                }
                mDetector.onTouchEvent(arg1);
                return true;
            }
        });
    }

    private void initInputView(View v) {
        customizedKeyboard = (KeyboardViewUnity) v.findViewById(R.id.customized_keyboard);

        KeyboardViewUnity.KeyboardType defaultType = KeyboardViewUnity.KeyboardType.ENG;//TODO
        // get default keyboard type
        customizedKeyboard.init(defaultType);

        mEditText = ((EditText) v.findViewById(R.id.remote_input_et));
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                showSoftInputFromWindow();
                InputMethodManager imm =
                        (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                return true;
            }
        });
        initSoftInput();
    }

    private void initSoftInput() {
        mEditText.setFocusable(true);
        mEditText.setFocusableInTouchMode(true);
        mEditText.requestFocus();

        customizedKeyboard.setOnKeyboardActionListener(new KeyboardViewUnity.OnKeyboardActionListener() {
            @Override
            public void onKey(int primarykeycode) {
                switch (primarykeycode) {
                    case KeyboardViewUnity.FUNCTIONAL_KEY_DEL:
                        String text = mEditText.getText().toString();
                        if (text.length() >= 1) {
                            mEditText.setText(text.substring(0, text.length() - 1));
                        }
                        break;
                    case KeyboardViewUnity.FUNCTIONAL_KEY_ENTER:
//                            sendContent();
                        break;

                }
                mEditText.setSelection(mEditText.getText().toString().length());
            }

            @Override
            public void onText(CharSequence text) {
                if (text.toString().toLowerCase().equals("space")) text = " ";
                if (text.toString().toLowerCase().equals("send")) text = "";
                if (text.toString().toLowerCase().equals("shift")) {
                    sendContent(KEYCODE_SHIFT);
                    return;
                }
                mEditText.getText().append(text);
            }

            @Override
            public boolean hasText() {
                return mEditText.getText().toString().trim().length() > 0;
            }
        });
    }

    public void sendContent(String keyCode) {
        if (!TextUtils.isEmpty(keyCode)) {
            String key = conversionSpecialChar(keyCode);
            // todo 发送键值
        }
    }

    private String conversionSpecialChar(String keyCode) {
        LogUtils.d("InputFragmentCustomized", "keyCode : " + keyCode);
        if ("!".equals(keyCode)) {
            return "SHIFT_1";
        } else if ("@".equals(keyCode)) {
            return "SHIFT_2";
        } else if ("#".equals(keyCode)) {
            return "SHIFT_3";
        } else if ("$".equals(keyCode)) {
            return "SHIFT_4";
        } else if ("%".equals(keyCode)) {
            return "SHIFT_5";
        } else if ("^".equals(keyCode)) {
            return "SHIFT_6";
        } else if ("&".equals(keyCode)) {
            return "SHIFT_7";
        } else if ("*".equals(keyCode)) {
            return "SHIFT_8";
        } else if ("(".equals(keyCode)) {
            return "SHIFT_9";
        } else if (")".equals(keyCode)) {
            return "SHIFT_0";
        } else if ("_".equals(keyCode)) {
            return "SHIFT_MINUS";
        } else if ("+".equals(keyCode)) {
            return "SHIFT_EQUALS";
        } else if ("{".equals(keyCode)) {
            return "SHIFT_LEFT_BRACKET";
        } else if ("}".equals(keyCode)) {
            return "SHIFT_RIGHT_BRACKET";
        } else if (":".equals(keyCode)) {
            return "SHIFT_SEMICOLON";
        } else if ("\"".equals(keyCode)) {
            return "SHIFT_APOSTROPHE";
        } else if ("~".equals(keyCode)) {
            return "SHIFT_GRAVE";
        } else if ("|".equals(keyCode)) {
            return "SHIFT_BACKSLASH";
        } else if ("<".equals(keyCode)) {
            return "SHIFT_COMMA";
        } else if (">".equals(keyCode)) {
            return "SHIFT_PERIOD";
        } else if ("?".equals(keyCode)) {
            return "SHIFT_SLASH";
        } else if ("-".equals(keyCode)) {
            return "MINUS";
        } else if ("=".equals(keyCode)) {
            return "EQUALS";
        } else if ("[".equals(keyCode)) {
            return "LEFT_BRACKET";
        } else if ("]".equals(keyCode)) {
            return "RIGHT_BRACKET";
        } else if (";".equals(keyCode)) {
            return "SEMICOLON";
        } else if ("'".equals(keyCode)) {
            return "APOSTROPHE";
        } else if ("`".equals(keyCode)) {
            return "GRAVE";
        } else if ("\\".equals(keyCode)) {
            return "BACKSLASH";
        } else if (",".equals(keyCode)) {
            return "COMMA";
        } else if (".".equals(keyCode)) {
            return "PERIOD";
        } else if ("/".equals(keyCode)) {
            return "SLASH";
//        }else if ("".equals(keyCode)){
//            return "";
        } else {
            return null;
        }
    }

    private void resetSelection() {
        Message msg = this.mmHandler.obtainMessage();
        msg.sendToTarget();
    }

    @SuppressLint("HandlerLeak")
    private Handler mmHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (null != mEditText) {
                mEditText.setSelection(mEditText.getText().toString().length());
            }
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            hideSoftInputFromWindow();
        } else {
            showSoftInputFromWindow();
        }
    }

    private void showSoftInputFromWindow() {
    }

    public void hideSoftInputFromWindow() {

    }

    private void onLongEnd() {
        LogUtils.d("liuyuanyuan", "onLongEnd");
        isLongStarted = false;
    }

    private float mRawY = 0.0F;
    private float mRawX = 0.0F;
    private float maxX = 0.0F;
    private float maxY = 0.0F;
    private float longStartX = 0.0F;
    private float longStartY = 0.0F;
    private float mImageWidth = 0.0F;
    private float mImageHeight = 0.0F;

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // 可发送ok键
        return true;
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        mRawX = arg0.getX();
        mRawY = arg0.getY();

        moveViewWithFinger(panalSymbolFinger);
        panalSymbolFinger.setVisibility(View.VISIBLE);
        panalTips.setVisibility(View.INVISIBLE);
        mImageWidth = panalSymbolFinger.getWidth();
        mImageHeight = panalSymbolFinger.getHeight();
        maxX = (panalBg.getWidth() - mImageWidth / 2.0F);
        maxY = (panalBg.getHeight() - mImageHeight / 2.0F);

        return false;
    }

    private void moveViewWithFinger(View view) {
        float rawX = mRawX;
        float rawY = mRawY;
        RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams) view.getLayoutParams());
        if ((0.0F != maxX) && (rawX >= maxX)) {
            rawX = maxX;
        } else if (rawX < mImageWidth / 2.0F) {
            rawX = mImageWidth / 2.0F;
        }
        if ((0.0F != maxY) && (rawY >= maxY)) {
            rawY = maxY;
        } else if (rawY < mImageHeight / 2.0F) {
            rawY = mImageHeight / 2.0F;
        }
        params.leftMargin = ((int) (rawX - mImageWidth / 2.0F));
        params.topMargin = ((int) (rawY - mImageHeight / 2.0F));
        view.setLayoutParams(params);
    }

    private int mDirectionFlag = 0;
    private int mScrolltag = 0;
    private Boolean mIsScroolEvent = Boolean.FALSE;

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        int x_start = (int) arg0.getX();
        int x_end = (int) arg1.getX();
        int y_start = (int) arg0.getY();
        int y_end = (int) arg1.getY();
        mScrolltag = 0;
        String keyCode = "";
        if (Math.abs(x_end - x_start) > Math.abs(y_end - y_start)) {
            if (x_start - x_end > 0) {
                Log.d(TAG, "onFling");
                if (!mIsScroolEvent) {
                    // todo 发送左移键
                    startAnimation(R.drawable.ic_arrow_left_remote);
                    mDirectionFlag = 1;
                    onLongStart(x_end, y_end);
                } else {
                    mIsScroolEvent = Boolean.FALSE;
                }
            } else {
                Log.d(TAG, "onFling");
                if (!mIsScroolEvent) {
                    // todo 发送右移键
                    startAnimation(R.drawable.ic_arrow_right_remote);
                    mDirectionFlag = 2;
                    onLongStart(x_end, y_end);
                } else {
                    mIsScroolEvent = Boolean.FALSE;
                }
            }
        } else if (Math.abs(x_end - x_start) < Math.abs(y_end - y_start)) {
            if (y_start - y_end > 1) {
                if (!mIsScroolEvent) {
                    // todo 发送抬起
                    startAnimation(R.drawable.ic_arrow_up_remote);
                    mDirectionFlag = 3;
                    onLongStart(x_end, y_end);
                } else {
                    mIsScroolEvent = Boolean.FALSE;
                }
            } else {
                Log.d(TAG, "onFling");
                if (!mIsScroolEvent) {
                    // todo 发送按下
                    startAnimation(R.drawable.ic_arrow_down_remote);
                    mDirectionFlag = 4;
                    onLongStart(x_end, y_end);
                } else {
                    mIsScroolEvent = Boolean.FALSE;
                }
            }
        }
        return false;
    }

    private void startAnimation(int drawable) {
        panalTips.setVisibility(View.INVISIBLE);
        panalSymbolGesture.setImageResource(drawable);
        panalSymbolGesture.startAnimation(animation);
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        Log.v(TAG, "onLongPress");
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        String keyCode = "";
        mRawX = arg1.getX();
        mRawY = arg1.getY();
        moveViewWithFinger(panalSymbolFinger);
        if (isLongStarted) {
            isMove(longStartX, longStartY, mRawX, mRawY);
        }
        mScrolltag += 1;
        if (Math.abs(arg2) > Math.abs(arg3)) {
            if (arg2 > 1.0F) {
                if (mScrolltag % 20 == 0) {
                    // 左
                    startAnimation(R.drawable.ic_arrow_left_remote);
                    mIsScroolEvent = Boolean.TRUE;
                    mDirectionFlag = 1;
                    onLongStart(mRawX, mRawY);
                }
            } else if (mScrolltag % 20 == 0) {
                // 右
                startAnimation(R.drawable.ic_arrow_right_remote);
                mIsScroolEvent = Boolean.TRUE;
                mDirectionFlag = 2;
                onLongStart(mRawX, mRawY);
            }
        } else if (Math.abs(arg2) < Math.abs(arg3)) {
            if (arg3 > 1.0F) {
                if (mScrolltag % 20 == 0) {
                    // 上
                    startAnimation(R.drawable.ic_arrow_up_remote);
                    mIsScroolEvent = Boolean.TRUE;
                    mDirectionFlag = 3;
                    onLongStart(mRawX, mRawY);
                }
            } else if (mScrolltag % 20 == 0) {
                // 下
                startAnimation(R.drawable.ic_arrow_down_remote);
                mIsScroolEvent = Boolean.TRUE;
                mDirectionFlag = 4;
                onLongStart(mRawX, mRawY);
            }
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        Log.v(TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        Log.v(TAG, "onSingleTapUp");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg0) {
        Log.v(TAG, "onDoubleTap");
        // 返回键
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        Log.v(TAG, "onDoubleTapEvent");
        return true;
    }

    private void onLongStart(float x, float y) {
        Log.d("liuyuanyuan", "onLongStart");
        isLongStarted = true;
        longStartX = x;
        longStartY = y;
        String keyCode = "";
        switch (mDirectionFlag) {
            case 1:
                // 左
                break;
            case 2:
                // 右
                break;
            case 3:
                // 上
                break;
            case 4:
                // 下
                break;
            default:
                break;
        }
    }

    private void isMove(float x, float y, float xEND, float yEND) {
        int direction = 0;
        x -= xEND;
        y -= yEND;
        if ((Math.abs(x - xEND) > 10.0F) || (Math.abs(y - yEND) > 10.0F)) {
            if (Math.abs(x) > Math.abs(y)) {
                if (x > 0.0F) {
                    Log.d("liuyuanyuan", "isMove LEFT");
                    direction = 1;
                } else {
                    Log.d("liuyuanyuan", "isMove RIGHT");
                    direction = 2;
                }
            } else if (y > 0.0F) {
                Log.d("liuyuanyuan", "isMove UP");
                direction = 3;
            } else {
                Log.d("liuyuanyuan", "isMove DOWN");
                direction = 4;
            }
        }
    }
}
