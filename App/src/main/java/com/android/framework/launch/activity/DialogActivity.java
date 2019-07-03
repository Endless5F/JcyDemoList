package com.android.framework.launch.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.bean.ExpressList;
import com.android.baselibrary.strategy.httpProcessor.HttpHelper;
import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;
import com.android.baselibrary.util.ToastUtil;
import com.android.baselibrary.widget.CustomDialog;
import com.android.framework.R;

import java.util.WeakHashMap;

public class DialogActivity extends BaseToolbarCompatActivity {

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        setMiddleTitle("Dialog工具栏");

//        CommonDialog.getInstance().showAlertDialog(this, "我是Dialog工具类3"
//                , (dialogInterface, which) -> ToastUtil.showShortToast("哈哈哈哈哈3，确认")
//                , (dialogInterface, which) -> ToastUtil.showShortToast("啦啦啦啦啦3，取消"))
//                .setAlertDialogStyle((title, content, posButton, negButton) -> {
//                    // setTextColor 颜色值不允许设置为 R.color.xxx
//                    title.setTextColor(Color.parseColor(getString(R.color
//                    .baseLoadingDialogProgressColor)));
//                    content.setTextSize(30);
//                    posButton.setTextColor(Color.parseColor(getString(R.color
//                    .baseLoadingDialogProgressColor)));
//                });
//
//        CommonDialog.getInstance().showAlertDialog(this, "我是Dialog工具类3"
//                , (dialogInterface, which) -> ToastUtil.showShortToast("哈哈哈哈哈3，确认")
//                , (dialogInterface, which) -> ToastUtil.showShortToast("啦啦啦啦啦3，取消"))
//                .setTitle(16, R.color.baseLoadingDialogProgressColor)
//                .setContent(14, R.color.baseLoadingDialogProgressColor)
//                .setPosButton(12, R.color.baseLoadingDialogProgressColor);

        CustomDialog.builder(this)
                .setmTitle("提示")
                .setmContent("我是自定义Dialog啊")
                .setmNegativeButtonText("哦哦")
                .setmPositiveButtonText("好的")
                .setPosOnClickListener(v -> {
                    ToastUtil.showShortToast("哈哈哈，我是自定义Dialog啊");
                    WeakHashMap<String, Object> hashMap = new WeakHashMap<>();
                    hashMap.put("type", "yuantong");
                    hashMap.put("postid", "11111111111");
                    HttpHelper.obtain()
                            .post(DialogActivity.this, "query", hashMap,
                                    new HttpCallback<ExpressList>() {
                                @Override
                                public void onSuccess(ExpressList user) {
                                    ToastUtil.showShortToast(user.getCom());
                                }

                                @Override
                                public void onFailure(String e) {
                                    super.onFailure(e);
                                    ToastUtil.showShortToast(e);
                                }
                            });
                })
                .setNegOnClickListener(v -> {
                    ToastUtil.showShortToast("略略略，我是自定义Dialog啊");
                    WeakHashMap<String, Object> hashMap = new WeakHashMap<>();
                    hashMap.put("type", "yuantong");
                    hashMap.put("postid", "11111111111");
                    HttpHelper.obtain()
                            .get(DialogActivity.this, "query", hashMap,
                                    new HttpCallback<ExpressList>() {
                                @Override
                                public void onSuccess(ExpressList user) {
                                    ToastUtil.showShortToast(user.getCom());
                                }

                                @Override
                                public void onFailure(String e) {
                                    super.onFailure(e);
                                    ToastUtil.showShortToast(e);
                                }
                            });
                })
                .build().show();
    }
}
