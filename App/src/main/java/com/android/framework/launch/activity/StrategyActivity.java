package com.android.framework.launch.activity;

import android.os.Bundle;
import android.view.View;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.bean.ResultBean;
import com.android.baselibrary.strategy.httpProcessor.HttpHelper;
import com.android.baselibrary.strategy.httpProcessor.callBack.HttpCallback;
import com.android.baselibrary.util.ToastUtil;
import com.android.framework.R;

import java.util.WeakHashMap;

/**
 * 策略模式，一行可换一个框架思想
 */
public class StrategyActivity extends BaseToolbarCompatActivity {
    String url = "http://v.juhe.cn/weather/index";
    WeakHashMap<String, Object> params = new WeakHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_architecture);
        setMiddleTitle("网络抽象架构框架");

        params.put("cityname", "长沙");
        params.put("key", "123456");
    }

    public void click(View view) {
        HttpHelper.obtain().post(this, url, params, new HttpCallback<ResultBean>() {
            @Override
            public void onSuccess(ResultBean resultBean) {
                ToastUtil.showShortToast(resultBean.getReason());
            }

            @Override
            public void onFailure(String e) {
                super.onFailure(e);
                ToastUtil.showShortToast(e);
            }
        });
    }

}
