package com.android.baselibrary.strategy.httpProcessor.http;

import android.util.Log;

import com.android.baselibrary.base.globalConf.AppConf;
import com.android.baselibrary.util.file.FileUtil;

import java.io.File;
import java.io.IOException;

import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;

/**
 * Created by jcy on 2018/3/10.
 * desc:缓存工具类
 */

public class CacheManager {

    private static CacheProviders cacheProvidersService;

    public synchronized static CacheProviders getCache() {
        File file = new File(AppConf.LOCAL_CACHE_FILE_PATH);
        FileUtil.createFileDir(file);
        if (cacheProvidersService == null && file.exists()) {
            cacheProvidersService = new RxCache.Builder()
                    .setMaxMBPersistenceCache(AppConf.MAX_PERSISTENCE_CACHE)
                    .useExpiredDataIfLoaderNotAvailable(true)//允许RxCache在这种情况下提供被驱逐的数据
                    .persistence(file, new GsonSpeaker())
                    .using(CacheProviders.class);
        }
        return cacheProvidersService;
    }
    //只能获取未加密的文件内容
    public synchronized static void getCacheData(String key) {
        File file = new File(AppConf.LOCAL_CACHE_FILE_PATH);
        File[] files = file.listFiles();
        if (file.exists()&&files!=null){
            for (File fil : files) {
                if (fil.getName().contains(key)){
                    try {
                        String fileContent = FileUtil.getFileContent(fil.getAbsolutePath());
                        Log.e("fileContent",fileContent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }
}
