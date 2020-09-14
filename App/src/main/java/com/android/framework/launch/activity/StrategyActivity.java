package com.android.framework.launch.activity;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.Lifecycle;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.baselibrary.bean.ShareBeanResult;
import com.android.baselibrary.http.RetrofitCreator;
import com.android.baselibrary.http.callBack.ObserverCallBack;
import com.android.baselibrary.utils.LogUtils;
import com.android.baselibrary.utils.ToastUtils;
import com.android.framework.R;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 策略模式，一行可换一个框架思想
 */
public class StrategyActivity extends BaseToolbarCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_architecture);
        setMiddleTitle("网络抽象架构框架");
    }

    public void click(View view) {
        RetrofitCreator.getRxRestService()
                .share("user/2/share_articles/1/json")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY)))
                .subscribe(new ObserverCallBack<ShareBeanResult>() {
                    @Override
                    public void onNext(ShareBeanResult result) {
                        LogUtils.d(result.toString());
                        ToastUtils.showShortToast("请求成功，数据为：" + result.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtils.i(e.toString());
                    }
                });
    }

}
