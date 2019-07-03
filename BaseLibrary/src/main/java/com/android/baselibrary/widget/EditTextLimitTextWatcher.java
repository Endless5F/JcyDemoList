package com.android.baselibrary.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

/**
 * 对输入框进行字数限制的textWatcher，支持中英文混输，超出字符数会弹出Toast
 *
 * @author Lento
 */
public class EditTextLimitTextWatcher implements TextWatcher {
    private final int mMaxLenth;

    private String mToastText;

    private Context mContext;

    private Toast mToast;

    private EditText mEditText;

    private int mCharCount;

    private OnTextChanged onTextChanged;

    /**
     * @param mContext
     * @param mEditText: 需要监视的输入框
     * @param mMaxLenth :支持输入的最大字符数（1个汉字为2个字符，1个英文字母为1个字符）
     * @param mToastText: 输入字符数超出最大值时的toast文字提示，为null时，不提示
     */
    public EditTextLimitTextWatcher(Context mContext, EditText mEditText, int mMaxLenth,
                                    String mToastText) {
        this.mContext = mContext;
        this.mMaxLenth = mMaxLenth;
        this.mToastText = mToastText;
        this.mEditText = mEditText;
    }

    /**
     * 避免多次重复弹出toast
     *
     * @param text
     */
    private void showToast(String text) {
        if (mToast == null) {
            mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mCharCount = before + count;
        if (mCharCount > mMaxLenth) {
            mEditText.setSelection(mEditText.length());
        }
        try {
            mCharCount = mEditText.getText().toString().getBytes("GBK").length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        onTextChanged.textChange(s);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mCharCount > mMaxLenth) {
            CharSequence subSequence = null;
            for (int i = 0; i < s.length(); i++) {
                subSequence = s.subSequence(0, i);
                try {
                    if (subSequence.toString().getBytes("GBK").length == mCharCount) {
                        mEditText.setText(subSequence.toString());
                        break;
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            if (!TextUtils.isEmpty(mToastText)) {
                //showToast(mToastText);
            }
            String androidVersion = android.os.Build.VERSION.RELEASE;
            if (androidVersion.charAt(0) >= '4') {
                mEditText.setText(subSequence.toString());
            }
        }
    }

    public interface OnTextChanged{
        void textChange(CharSequence s);
    }

    public void setOnAfterTextChanged(OnTextChanged onTextChanged){
        this.onTextChanged = onTextChanged;
    }
}
