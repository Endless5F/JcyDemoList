package com.android.baselibrary.app;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.WeakHashMap;

import okhttp3.Interceptor;

/**
 * 全局配置操作类
 * */
public class Configurator {
    private static final WeakHashMap<String, Object> APP_CONFIGS = new WeakHashMap<>();
    private static final Handler HANDLER = new Handler();
    private static final ArrayList<Interceptor> INTERCEPTORS = new ArrayList<>();

    private Configurator() {
        APP_CONFIGS.put(ConfigKeys.CONFIG_READY.name(), false);
        APP_CONFIGS.put(ConfigKeys.HANDLER.name(), HANDLER);
    }

    public final WeakHashMap<String, Object> getLatteConfigs() {
        return APP_CONFIGS;
    }

    private static class Holder {
        private static final Configurator INSTANCE = new Configurator();
    }

    /**
     * 线程安全的单例模式
     */
    public static Configurator getInstance() {
        return Holder.INSTANCE;
    }

    public final void configure() {
        APP_CONFIGS.put(ConfigKeys.CONFIG_READY.name(), true);
        Logger.addLogAdapter(new AndroidLogAdapter());
    }

    public final Configurator withApiHost(String host) {
        APP_CONFIGS.put(ConfigKeys.API_HOST.name(), host);
        return this;
    }

    public final Configurator withLoaderDelayed(long delayed) {
        APP_CONFIGS.put(ConfigKeys.LOADER_DELAYED.name(), delayed);
        return this;
    }

    public final Configurator withInterceptor(Interceptor interceptor) {
        INTERCEPTORS.add(interceptor);
        APP_CONFIGS.put(ConfigKeys.INTERCEPTOR.name(), INTERCEPTORS);
        return this;
    }

    public final Configurator withInterceptors(ArrayList<Interceptor> interceptors) {
        INTERCEPTORS.addAll(interceptors);
        APP_CONFIGS.put(ConfigKeys.INTERCEPTOR.name(), INTERCEPTORS);
        return this;
    }

    public final Configurator withWeChatAppId(String appId) {
        APP_CONFIGS.put(ConfigKeys.WE_CHAT_APP_ID.name(), appId);
        return this;
    }

    public final Configurator withWeChatAppSecret(String appSecret) {
        APP_CONFIGS.put(ConfigKeys.WE_CHAT_APP_SECRET.name(), appSecret);
        return this;
    }

    public final Configurator withActivity(Activity activity) {
        APP_CONFIGS.put(ConfigKeys.ACTIVITY.name(), activity);
        return this;
    }

    public Configurator withJavascriptInterface(@NonNull String name) {
        APP_CONFIGS.put(ConfigKeys.JAVASCRIPT_INTERFACE.name(), name);
        return this;
    }

//    public Configurator withWebEvent(@NonNull String name, @NonNull Event event) {
//        final EventManager manager = EventManager.getInstance();
//        manager.addEvent(name, event);
//        return this;
//    }

    private void checkConfiguration() {
        final boolean isReady = (boolean) APP_CONFIGS.get(ConfigKeys.CONFIG_READY.name());
        if (!isReady) {
            throw new RuntimeException("配置并没有准备好");
        }
    }

    @SuppressWarnings("unchecked")
    final <T> T getConfiguration(Enum<ConfigKeys> key){
        checkConfiguration();
        final Object value = APP_CONFIGS.get(key.name());
        if (value == null) {
            throw new NullPointerException(key.toString() + " IS NULL");
        }
        return (T) APP_CONFIGS.get(key.name());
    }
}
