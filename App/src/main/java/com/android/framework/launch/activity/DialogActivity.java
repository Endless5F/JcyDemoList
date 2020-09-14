package com.android.framework.launch.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.utils.ToastUtils;
import com.android.baselibrary.widget.CustomDialog;
import com.android.framework.R;

public class DialogActivity extends BaseToolbarCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        setMiddleTitle("Dialog工具栏");

        CustomDialog.builder(this)
                .setmTitle("提示")
                .setmContent("我是自定义Dialog啊")
                .setmNegativeButtonText("哦哦")
                .setmPositiveButtonText("好的")
                .setPosOnClickListener(v -> {
                    ToastUtils.showShortToast("哈哈哈，我是自定义Dialog啊");
                })
                .setNegOnClickListener(v -> {
                    ToastUtils.showShortToast("再见哦，我是自定义Dialog啊");
                })
                .build().show();
    }
}
