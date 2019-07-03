package com.android.baselibrary.base;

import com.trello.rxlifecycle2.LifecycleProvider;
import com.trello.rxlifecycle2.android.ActivityEvent;

/**
 * 公共Presenter类
 * Created by HDL on 2017/10/17.
 *
 * @author HDL
 */

public class BasePresenter {

    private LifecycleProvider<ActivityEvent> provider;

    public BasePresenter(LifecycleProvider<ActivityEvent> provider) {
        this.provider = provider;
    }

    public LifecycleProvider<ActivityEvent> getProvider() {
        return provider;
    }
}
