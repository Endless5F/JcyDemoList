package com.android.baselibrary.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import com.android.baselibrary.lambdaInterface.Functional2;
import com.android.baselibrary.lambdaInterface.Functional4;
import com.android.baselibrary.util.ScreenUtil;

import java.lang.reflect.Field;
import java.util.Objects;

public class CommonDialog {

    private AlertDialog alertDialog;
    @SuppressLint("StaticFieldLeak")
    private static CommonDialog dialogUtil3;
    private Activity mContext;

    private CommonDialog(){}

    public static CommonDialog getInstance(){
        if (dialogUtil3 == null){
            dialogUtil3 = new CommonDialog();
        }
        return dialogUtil3;
    }

    /**
     * show 标题为：提示 的 标准dialog
     */
    public CommonDialog showAlertDialog(Activity context, String content, Functional2<DialogInterface, Integer> positiveCallback, Functional2<DialogInterface, Integer> negativeCallback) {
        this.mContext = context;
        AlertDialog.Builder builder = createAlertBuilder(context, -1, "提示 ", content);
        //为构造器设置确定按钮,第一个参数为按钮显示的文本信息，第二个参数为点击后的监听事件，用匿名内部类实现
        builder.setPositiveButton("确认", positiveCallback::function);
        //为构造器设置取消按钮,若点击按钮后不需要做任何操作则直接为第二个参数赋值null
        builder.setNegativeButton("取消", negativeCallback::function);
        //利用构造器创建AlertDialog的对象,实现实例化
        createAlertDialog(builder, true);
        return getInstance();
    }

    /**
     * 设置Dialog的喜好，包括颜色/大小
     */
    public void setAlertDialogStyle(Functional4<TextView,TextView,Button,Button> setDialogLike) {
        //通过反射修改title字体大小和颜色
        TextView title = null;
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            title = (TextView) mTitle.get(mAlertController);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        TextView content = (TextView) alertDialog.findViewById(android.R.id.message);
        Button btnPositive =
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative =
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        setDialogLike.function(title,content,btnPositive,btnNegative);
    }

    /**
     * 设置标题 size 是 dp 值
     */
    public CommonDialog setTitle(int size, int color) {
        //通过反射修改title字体大小和颜色
        TextView title = null;
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(alertDialog);
            Field mTitle = mAlertController.getClass().getDeclaredField("mTitleView");
            mTitle.setAccessible(true);
            title = (TextView) mTitle.get(mAlertController);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (title != null){
            title.setTextSize(ScreenUtil.dp2px(size));
            // setTextColor 颜色值不允许设置为 R.color.xxx
            if (mContext != null && !mContext.isFinishing()){
                title.setTextColor(Color.parseColor(mContext.getString(color)));
            }
        }
        return getInstance();
    }

    /**
     * 设置内容 size 是 dp 值
     */
    public CommonDialog setContent(int size, int color) {
        TextView content = (TextView) alertDialog.findViewById(android.R.id.message);
        if (content != null){
            content.setTextSize(ScreenUtil.dp2px(size));
            // setTextColor 颜色值不允许设置为 R.color.xxx
            if (mContext != null && !mContext.isFinishing()){
                content.setTextColor(Color.parseColor(mContext.getString(color)));
            }
        }
        return getInstance();
    }

    /**
     * 设置内容 size 是 dp 值
     */
    public CommonDialog setPosButton(int size, int color) {
        Button btnPositive =
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (btnPositive != null){
            btnPositive.setTextSize(ScreenUtil.dp2px(size));
            // setTextColor 颜色值不允许设置为 R.color.xxx
            if (mContext != null && !mContext.isFinishing()){
                btnPositive.setTextColor(Color.parseColor(mContext.getString(color)));
            }
        }
        return getInstance();
    }

    /**
     * 设置内容 size 是 dp 值
     */
    public CommonDialog setNegButton(int size, int color) {
        Button btnNegative =
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (btnNegative != null){
            btnNegative.setTextSize(ScreenUtil.dp2px(size));
            // setTextColor 颜色值不允许设置为 R.color.xxx
            if (mContext != null && !mContext.isFinishing()){
                btnNegative.setTextColor(Color.parseColor(mContext.getString(color)));
            }
        }
        return getInstance();
    }

    /**
     * style dialog的样式
     */
    private AlertDialog.Builder createAlertBuilder(Activity context, int style, String title, String content) {
        //创建AlertDialog的构造器的对象
        AlertDialog.Builder builder;
        if (style == -1) {
            builder = new AlertDialog.Builder(context);
        } else {
            builder = new AlertDialog.Builder(context, style);
        }
        //设置构造器标题
        builder.setTitle(title);
        //构造器内容,为对话框设置文本项(之后还有列表项的例子)
        builder.setMessage(content);
        return builder;
    }

    /**
     * isShowing 是否立马展示
     */
    private AlertDialog createAlertDialog(AlertDialog.Builder builder, boolean isShowing) {
        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(alertDialog.getWindow()).setGravity(Gravity.CENTER);
        if (isShowing) {
            alertDialog.show();
        }
        return alertDialog;
    }

    public void dismissDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
