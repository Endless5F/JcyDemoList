package com.android.baselibrary;

import android.annotation.SuppressLint;
import androidx.lifecycle.Lifecycle;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.OrientationEventListener;
import android.widget.Button;

import com.android.baselibrary.util.ScreenUtils;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class RxJavaActivity extends AppCompatActivity {

    private String DEBUG_TAG = "RxJavaActivity";
    private Button button;
    private OrientationEventListener mOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_java);
        button = findViewById(R.id.button);

        mOrientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                Log.e(DEBUG_TAG, "Orientation changed to " + orientation);
                Log.e(DEBUG_TAG, "Orientation changed  width to " + ScreenUtils.getScreenWidth());
                Log.e(DEBUG_TAG, "Orientation changed height to " + ScreenUtils.getScreenHeight());
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            Log.i(DEBUG_TAG, "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.i(DEBUG_TAG, "Cannot detect orientation");
            mOrientationListener.disable();
        }

        makeRxJava();
    }

    @SuppressLint("CheckResult")
    private void makeRxJava() {
        // 问：有一些异常无法映射成一个结果，在RxJava使用过程中无法捕获？
        // 答：则可以做一个全局的异常捕获，并且日志上报，但是此异常若为很验证的异常，则抛出
        RxJavaPlugins.setErrorHandler(e -> {
            // OnErrorNotImplementedException此异常里真实的异常为e.getCause()，若不提取，则会包含大量无用信息
            report(e instanceof OnErrorNotImplementedException ? e.getCause() : e);
            // 致命的错误抛出
            Exceptions.throwIfFatal(e);
        });
        String[] strings = new String[]{"111", "222", "333", "444", "555"};
        Observable.fromArray(strings)
                .map(Integer::getInteger)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
//                .compose(bindUntilEvent(ActivityEvent.DESTROY)) // RxLifecycle：防止内存泄漏，自动取消处理。需要继承自 RxActivity 或 RxFragment 等
                .onErrorReturn(t -> throwableToResponse())
                // AutoDispose：防止内存泄漏，自动取消处理。使用AutoDispose时需要当前Activity实现LifecycleOwner接口，而AppCompatActivity是默认实现了该接口
//                .as(RxLifecycleUtils.bindLifecycle(this)) // as1 规范性，封装AutoDispose工具类
//                .as(AutoDispose.autoDisposable(ViewScopeProvider.from(button))) // as2 监听View状态自动取消订阅，即根据button按钮与Window分离时onDetachedFromWindow取消处理
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_DESTROY))) // as3 根据声明周期取消处理
                .subscribe(this::onNextResponse);
    }

    private void report(Throwable throwable) {
        // 日志上报处理
    }

    private Integer throwableToResponse() {
        // 返回异常数字，自定义
        return -1000;
    }

    private void onNextResponse(Integer integer) {

    }

}
