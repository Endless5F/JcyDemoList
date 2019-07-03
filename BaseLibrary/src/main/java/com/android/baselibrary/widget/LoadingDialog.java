package com.android.baselibrary.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.baselibrary.R;

/**
 * @author jcy
 * @date 2018/8/26
 * descrption:加载提示等待框
 */
public class LoadingDialog extends Dialog {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static LoadingDialog dialog;
    private Context context;
    private TextView loadingMessage;
    private ProgressBar progressBar;
    private LinearLayout loadingView;

    public LoadingDialog(Context context) {
        super(context, R.style.CompatLoadingDialog);
        this.context = context;
        setContentView(R.layout.base_dialog_loading);
        loadingMessage = findViewById(R.id.loading_message);
        progressBar =  findViewById(R.id.loading_progressbar);
        loadingView =  findViewById(R.id.loading_view);
        loadingMessage.setPadding(15, 0, 0, 0);
        loadingView.setBackgroundColor(Color.WHITE);
    }

    /**
     * 构造方法
     *
     * @param context Context
     * @return
     */
    public static LoadingDialog with(Context context) {
        if (dialog == null) {
            dialog = new LoadingDialog(context);
        }
        return dialog;
    }

    /**
     * 设置文字与ProgressBar的排列方式
     *
     * @param orientation 横竖
     */
    public LoadingDialog setOrientation(int orientation) {
        loadingView.setOrientation(orientation);
        if (orientation == HORIZONTAL) {
            loadingMessage.setPadding(15, 0, 0, 0);
        } else {
            loadingMessage.setPadding(0, 15, 0, 0);
        }
        return dialog;
    }

    /**
     * 设置ProgressBar是否可见
     *
     * @param visible 是否可见
     */
    public LoadingDialog setProgressBarVisible(boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        return dialog;
    }

    /**
     * 设置ProgressBar大小
     *
     * @param size 大小
     */
    public LoadingDialog setProgressBarSize(int size) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) progressBar.getLayoutParams();
        params.height = size;//设置当前控件布局的高度
        params.width = size;//设置当前控件布局的高度
        progressBar.setLayoutParams(params);
        return dialog;
    }

    /**
     * 设置ProgressBar样式
     *
     * @param drawable 样式
     */
    public LoadingDialog setProgressBarDrawable(Drawable drawable) {
        progressBar.setIndeterminateDrawable(drawable);
        return dialog;
    }

    /**
     * 设置ProgressBar样式
     *
     * @param drawableRes 样式drawableRes
     */
    public LoadingDialog setProgressBarDrawable(@DrawableRes int drawableRes) {
        progressBar.setIndeterminateDrawable(ContextCompat.getDrawable(this.context, drawableRes));
        return dialog;
    }

    /**
     * 设置背景颜色
     *
     * @param color Res
     */
    public LoadingDialog setBackgroundColor(@ColorInt int color) {
        loadingView.setBackgroundColor(color);
        return dialog;
    }

    /**
     * dismiss的时候将dialog置空
     */
    @Override
    public void dismiss() {
        super.dismiss();
        if (dialog != null)
            dialog = null;
    }

    /**
     * 设置是否可以取消
     *
     * @param cancel 是否可以取消
     */
    public LoadingDialog setCanceled(boolean cancel) {
        setCanceledOnTouchOutside(cancel);
        setCancelable(cancel);
        return dialog;
    }

    /**
     * 设置消息是否可见
     *
     * @param visible 是否可见
     */
    public LoadingDialog setMessageVisible(boolean visible) {
        loadingMessage.setVisibility(visible ? View.VISIBLE : View.GONE);
        return dialog;
    }

    /**
     * 设置消息
     *
     * @param message 消息
     */
    public LoadingDialog setMessage(String message) {
        if (null != message && !"".equals(message)) {
            loadingMessage.setText(message);
        }
        return this;
    }

    /**
     * 设置消息
     *
     * @param sp 消息
     */
    public LoadingDialog setMessageSP(int sp) {
        loadingMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return this;
    }

    /**
     * 设置消息字体颜色
     *
     * @param color Res
     */
    public LoadingDialog setMessageColor(@ColorInt int color) {
        loadingMessage.setTextColor(color);
        return this;
    }
}
