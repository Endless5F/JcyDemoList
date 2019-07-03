package com.android.baselibrary.base.globalConf;

import android.os.Environment;

import com.android.baselibrary.app.AppGlobal;

import java.io.File;

/**
 * Created by jcy on 2017/11/10.
 */

public class AppConf {
    public static final int DOWNLOAD_ERROR = 123;//设置下载错误返回码
    public static final int MAX_PERSISTENCE_CACHE = 50;//设置RxCache缓存最大，Mb单位
    // 默认存放文件下载的路径
    public final static String DEFAULT_SAVE_FILE_PATH = Environment
            .getExternalStorageDirectory()
            + File.separator
            + "frame";
    // 本地存放文件下载的路径
    public final static String LOCAL_SAVE_FILE_PATH = AppGlobal.getApplicationContext().getFilesDir().getAbsolutePath()
            +File.separator+ "frame"+ File.separator+"download";
    // 缓存文件保存的路径
    public final static String LOCAL_CACHE_FILE_PATH = Environment
            .getExternalStorageDirectory()
            +File.separator+ "frame"+ File.separator+"cache";
}
