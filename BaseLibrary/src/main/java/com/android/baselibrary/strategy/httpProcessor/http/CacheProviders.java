package com.android.baselibrary.strategy.httpProcessor.http;

import com.android.baselibrary.bean.AppInfo;
import com.android.baselibrary.strategy.httpProcessor.http.common.HttpResult;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.rx_cache2.DynamicKey;
import io.rx_cache2.Encrypt;
import io.rx_cache2.EncryptKey;
import io.rx_cache2.EvictDynamicKey;
import io.rx_cache2.LifeCache;

/**
 * Created by jcy on 2017/7/10.
 * desc:
 */
@EncryptKey("FrameKey-123456")
public interface CacheProviders {

    /**
     * LifeCache设置缓存过期时间. 如果没有设置@LifeCache ,
     *      数据将被永久缓存理除非你使用了 EvictProvider,EvictDynamicKey or EvictDynamicKeyGroup .
     * @param appInfo 这是个Observable类型的对象，简单来说，这就是你将要缓存的数据对象。
     * @param version 驱逐与一个特定的键使用EvictDynamicKey相关的数据。比如分页，排序或筛选要求，
     *                 DynamicKey类型，顾名思义，就是一个动态的key，我们以它作为tag，将数据存储到对应名字的File中
     * @param evictDynamicKey 可以明确地清理指定的数据 DynamicKey.
     *              可以明确地清理指定的数据 ，很简单，如果我们该参数传入为true，
     *              那么RxCache就会驱逐对应的缓存数据直接进行网络的新一次请求（即使缓存没有过期）。
     *              如果传入为false，说明不驱逐缓存数据，如果缓存数据没有过期，那么就不请求网络，
     *                        直接读取缓存数据返回。
     * @return 可以看到，该接口方法中，返回值为Observable,泛型为AppInfo，
     *              这个Observable的对象AppInfo和参数中传进来的Observable的对象AppInfo有什么区别呢？
     *              很简单，返回值Observable中的数据为经过缓存处理的数据。
     * @EncryptKey @Encrypt RxCache只会给本地缓存进行加密操作,并不会给内存缓存进行加密,
     *              给本地数据加密使用的是Java自带的CipherInputStream,解密使用的是CipherOutputStream
     */
    @Encrypt
    @LifeCache(duration = 1,timeUnit = TimeUnit.MINUTES)
    Observable<HttpResult<AppInfo>> checkVersion(
            Observable<HttpResult<AppInfo>> appInfo, DynamicKey version, EvictDynamicKey evictDynamicKey);

}
