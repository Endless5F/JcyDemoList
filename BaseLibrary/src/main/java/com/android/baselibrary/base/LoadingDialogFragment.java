package com.android.baselibrary.base;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.baselibrary.R;

/**
 * @author jcy
 * @date 2018/7/10
 * descrption:
 */
public class LoadingDialogFragment extends DialogFragment {

    public static final String ARG_DIALOG_CANCELABLE = "dialog_cancelable";

    public static final String POP_NAME_DIALOG = "dialog";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading_dialog, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null) {
            //布局中的背景无法生效，只能动态设置
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        //设置透明背景，此方法只是在dialog不全屏的状态下生效，但无法去除dialog的边框
/*        Window window = getDialog().getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        windowParams.dimAmount = 0.0f;
        window.setAttributes(windowParams);*/
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
