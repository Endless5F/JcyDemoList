package com.android.baselibrary.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.baselibrary.R;
import com.android.baselibrary.lambdaInterface.Functional1;
import com.android.baselibrary.lambdaInterface.Functional2;

import java.util.Objects;

/**
 * 建造者模式 Builder
 * */
public class CustomDialog extends Dialog implements View.OnClickListener {

    private Activity mContext;
    private String mTitle;
    private int mTitleColor;
    private int mTitleTextSize;
    private String mContent;
    private int mContentColor;
    private int mContentTextSize;
    private String mEditHint;
    private int mEditTextColor;
    private int mEditTextSize;
    private String mNegativeButtonText;
    private int mNegativeButtonColor;
    private int mNegativeButtonTextSize;
    private String mPositiveButtonText;
    private int mPositiveButtonColor;
    private int mPositiveButtonTextSize;
    private Functional1<View> negOnClickListener;
    private Functional1<View> posOnClickListener;

    public CustomDialog(@NonNull Activity context,
                        String mTitle,
                        int mTitleColor,
                        int mTitleTextSize,
                        String mContent,
                        int mContentColor,
                        int mContentTextSize,
                        String mEditHint,
                        int mEditTextColor,
                        int mEditTextSize,
                        String mNegativeButtonText,
                        int mNegativeButtonColor,
                        int mNegativeButtonTextSize,
                        String mPositiveButtonText,
                        int mPositiveButtonColor,
                        int mPositiveButtonTextSize,
                        Functional1<View> negOnClickListener,
                        Functional1<View> posOnClickListener) {
        super(context, R.style.dialog_custom);
        this.mContext = context;
        this.mTitle = mTitle;
        this.mTitleColor = mTitleColor;
        this.mTitleTextSize = mTitleTextSize;
        this.mContent = mContent;
        this.mContentColor = mContentColor;
        this.mContentTextSize = mContentTextSize;
        this.mEditHint = mEditHint;
        this.mEditTextColor = mEditTextColor;
        this.mEditTextSize = mEditTextSize;
        this.mNegativeButtonText = mNegativeButtonText;
        this.mNegativeButtonColor = mNegativeButtonColor;
        this.mNegativeButtonTextSize = mNegativeButtonTextSize;
        this.mPositiveButtonText = mPositiveButtonText;
        this.mPositiveButtonColor = mPositiveButtonColor;
        this.mPositiveButtonTextSize = mPositiveButtonTextSize;
        this.negOnClickListener = negOnClickListener;
        this.posOnClickListener = posOnClickListener;
    }

    public static Builder builder(Activity context) {
        return new Builder(context);
    }

    public static class Builder {
        private Activity mContext;
        private String mTitle = "提示";
        private int mTitleColor = R.color.colorPrimary;
        private int mTitleTextSize = 18;
        private String mContent = "";
        private int mContentColor = R.color.colorAccent;
        private int mContentTextSize = 14;
        private String mEditHint = "";
        private int mEditTextColor = R.color.colorAccent;
        private int mEditTextSize = 14;
        private String mNegativeButtonText = "取消";
        private int mNegativeButtonColor = R.color.baseLoadingDialogMessageColor;
        private int mNegativeButtonTextSize = 14;
        private String mPositiveButtonText = "确定";
        private int mPositiveButtonColor = R.color.comm_main_color;
        private int mPositiveButtonTextSize = 14;
        private Functional1<View> negOnClickListener;
        private Functional1<View> posOnClickListener;

        Builder(Activity context) {
            mContext = context;
        }

        public Builder setmTitle(String mTitle) {
            this.mTitle = mTitle;
            return this;
        }

        public Builder setmTitleColor(int mTitleColor) {
            this.mTitleColor = mTitleColor;
            return this;
        }

        public Builder setmTitleTextSize(int mTitleTextSize) {
            this.mTitleTextSize = mTitleTextSize;
            return this;
        }

        public Builder setmContent(String mContent) {
            this.mContent = mContent;
            return this;
        }

        public Builder setmContentColor(int mContentColor) {
            this.mContentColor = mContentColor;
            return this;
        }

        public Builder setmContentTextSize(int mContentTextSize) {
            this.mContentTextSize = mContentTextSize;
            return this;
        }

        public Builder setmEditHint(String mEditHint) {
            this.mEditHint = mEditHint;
            return this;
        }

        public Builder setmEditTextColor(int mEditTextColor) {
            this.mEditTextColor = mEditTextColor;
            return this;
        }

        public Builder setmEditTextSize(int mEditTextSize) {
            this.mEditTextSize = mEditTextSize;
            return this;
        }

        public Builder setmNegativeButtonText(String mNegativeButtonText) {
            this.mNegativeButtonText = mNegativeButtonText;
            return this;
        }

        public Builder setmNegativeButtonColor(int mNegativeButtonColor) {
            this.mNegativeButtonColor = mNegativeButtonColor;
            return this;
        }

        public Builder setmNegativeButtonTextSize(int mNegativeButtonTextSize) {
            this.mNegativeButtonTextSize = mNegativeButtonTextSize;
            return this;
        }

        public Builder setmPositiveButtonText(String mPositiveButtonText) {
            this.mPositiveButtonText = mPositiveButtonText;
            return this;
        }

        public Builder setmPositiveButtonColor(int mPositiveButtonColor) {
            this.mPositiveButtonColor = mPositiveButtonColor;
            return this;
        }

        public Builder setmPositiveButtonTextSize(int mPositiveButtonTextSize) {
            this.mPositiveButtonTextSize = mPositiveButtonTextSize;
            return this;
        }

        public Builder setNegOnClickListener(Functional1<View> negativeCallback) {
            this.negOnClickListener = negativeCallback;
            return this;
        }

        public Builder setPosOnClickListener(Functional1<View> positiveCallback) {
            this.posOnClickListener = positiveCallback;
            return this;
        }

        public CustomDialog build() {
            return new CustomDialog(mContext, mTitle, mTitleColor, mTitleTextSize
                    , mContent, mContentColor, mContentTextSize, mEditHint, mEditTextColor, mEditTextSize
                    , mNegativeButtonText, mNegativeButtonColor, mNegativeButtonTextSize
                    , mPositiveButtonText, mPositiveButtonColor, mPositiveButtonTextSize, negOnClickListener, posOnClickListener);
        }
    }

    @Override
    public void show() {
        if (mContext != null && mContext.isFinishing()) {
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_custom, null);
        setContentView(view);

        AppCompatTextView dTitle = view.findViewById(R.id.title);
        AppCompatTextView dContent = view.findViewById(R.id.content);
        AppCompatTextView dPositive = view.findViewById(R.id.positive);
        AppCompatTextView dNegative = view.findViewById(R.id.negative);
        AppCompatEditText dEditContent = view.findViewById(R.id.editContent);

        dTitle.setText(mTitle);
        dTitle.setTextSize(mTitleTextSize);
        dTitle.setTextColor(Color.parseColor(mContext.getString(mTitleColor)));
        dContent.setText(mContent);
        dContent.setTextSize(mContentTextSize);
        dContent.setTextColor(Color.parseColor(mContext.getString(mContentColor)));
        dPositive.setText(mPositiveButtonText);
        dPositive.setTextSize(mPositiveButtonTextSize);
        dPositive.setTextColor(Color.parseColor(mContext.getString(mPositiveButtonColor)));
        dNegative.setText(mNegativeButtonText);
        dNegative.setTextSize(mNegativeButtonTextSize);
        dNegative.setTextColor(Color.parseColor(mContext.getString(mNegativeButtonColor)));
        if (mEditHint.isEmpty()) {
            dEditContent.setVisibility(View.GONE);
        } else {
            dEditContent.setVisibility(View.VISIBLE);
        }
        dEditContent.setTextSize(mEditTextSize);
        dEditContent.setTextColor(Color.parseColor(mContext.getString(mEditTextColor)));
        dEditContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        if (posOnClickListener != null){
            dPositive.setOnClickListener(this);
        }
        if (negOnClickListener != null){
            dNegative.setOnClickListener(this);
        }
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        super.show();

        Window dialogWindow = getWindow();
        Objects.requireNonNull(dialogWindow).setGravity(Gravity.CENTER);
        WindowManager.LayoutParams lp = Objects.requireNonNull(dialogWindow).getAttributes();
        DisplayMetrics d = mContext.getResources().getDisplayMetrics(); // 获取屏幕宽、高用
        lp.width = (int) (d.widthPixels * 0.8); // 高度设置为屏幕的0.6
        dialogWindow.setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.positive){
            dismiss();
            posOnClickListener.function(v);
        }else if (id == R.id.negative){
            dismiss();
            negOnClickListener.function(v);
        }else {}
    }
}
