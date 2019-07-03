package com.android.baselibrary.manager.glide;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

@GlideModule
public class MyAppGlideModule  extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false;
    }
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 设置glide加载图片格式 PREFER_RGB_565
        builder.setDefaultRequestOptions(
                new RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)
        );
        //下面3中设置都可自定义大小，以及完全自定义实现
        //内存缓冲
        MemorySizeCalculator calculator = new MemorySizeCalculator.Builder(context)
                .setMemoryCacheScreens(2)
                .setBitmapPoolScreens(3)
                .build();
        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));

        //Bitmap 池
        builder.setBitmapPool(new LruBitmapPool(calculator.getBitmapPoolSize()));

        //磁盘缓存
        int diskCacheSizeBytes = 1024 * 1024 * 100;  //100 MB
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, diskCacheSizeBytes));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {

        // registry.replace(GlideUrl.class, InputStream.class, new NetworkDisablingLoader.Factory());
    }


}