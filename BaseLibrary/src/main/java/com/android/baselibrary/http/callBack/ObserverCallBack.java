package com.android.baselibrary.http.callBack;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class ObserverCallBack<T> implements Observer<T> {
    @Override
    public void onSubscribe(Disposable d) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public abstract void onError(Throwable e);

    @Override
    public void onComplete() {

    }
}
