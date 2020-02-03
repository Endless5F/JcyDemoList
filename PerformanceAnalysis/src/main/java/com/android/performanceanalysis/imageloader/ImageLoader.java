package com.android.performanceanalysis.imageloader;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.performanceanalysis.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用：
 * ImageLoader.build(mContext).bindBitmap(url, iv, 100, 100);
 */
public class ImageLoader {

    public static class LoadResult {

        ImageView imageView;
        String url;
        Bitmap bitmap;

        public LoadResult(ImageView imageView, String url, Bitmap bitmap) {
            this.imageView = imageView;
            this.url = url;
            this.bitmap = bitmap;
        }

    }

    private static final int MESSAGE_POST_RESULT = 1;
    private static final int TAG_KEY_URI = R.id.imageloader_url;
    private static final String TAG = "ImageLoader>>";
    private final Context mContext;
    private LruCache<String, Bitmap> mLruCache;
    private DiskLruCache mDiskLruCache;
    private int maxDiskSize = 30 * 1204 * 1024;
    private static final int DISK_CACHE_INDEX = 0;
    private static final int IO_BUFFER_SIZE = 1 * 1024 * 1024;
    private boolean mIsDiskLruCacheCreated = false;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10l;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, TAG + mCount.getAndIncrement());
        }
    };
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(), sThreadFactory
    );
    private Handler mMainHanlder = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            LoadResult result = (LoadResult) msg.obj;
            ImageView imageView = result.imageView;
            String url = result.url;
            Bitmap bitmap = result.bitmap;
            String tag = (String) imageView.getTag(TAG_KEY_URI);
            if (tag.equals(url)) {
                imageView.setImageBitmap(bitmap);
            }else {
                Log.d(TAG, "handleMessage: set image bitmap,but imageview changeed,ignored.");
            }

        }
    };

    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }

    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        init();
    }

    private void init() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
        File cacheDir = getDiskCacheDir(mContext, "bitmap");;
        if (getUsableSpace(cacheDir) > maxDiskSize) {
            try {
                mDiskLruCache = com.jakewharton.disklrucache.DiskLruCache.open
                        (cacheDir, 1, 1, maxDiskSize);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addBitmap2MemoryCache(String key, Bitmap bitmap) {
        if (loadBitmap4MemoryCache(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    private Bitmap loadBitmap4MemoryCache(String key) {
        return mLruCache.get(key);
    }

    public Bitmap loadBitmap4Url(String urlstr) {
        HttpURLConnection connec = null;
        BufferedInputStream in = null;
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlstr);
            connec = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(
                    connec.getInputStream(), IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connec != null) {
                connec.disconnect();
            }
            close(in);
        }
        return bitmap;
    }

    private Bitmap loadBitmap4Http(String url, int reqWidth, int reqHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        String key = hashKey4Url(url);
        try {
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                if (downloadUrl2Stream(url, editor.newOutputStream(DISK_CACHE_INDEX))) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loadBitmap4DiskCache(url, reqWidth, reqHeight);
    }

    private Bitmap loadBitmap4DiskCache(String url, int reqWidth, int reqHeight) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("Load bitmap on UI Thread.");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        try {
            String key = hashKey4Url(url);
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
            if (snapshot != null) {
                FileInputStream inputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
                FileDescriptor fileDescriptor = inputStream.getFD();
                bitmap = decodeSampledBitmap4FileDescriptor(fileDescriptor, reqWidth, reqHeight);
                if (bitmap != null) {
                    addBitmap2MemoryCache(key, bitmap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private boolean downloadUrl2Stream(String urlstr, OutputStream outputStream) {
        HttpURLConnection connec = null;
        BufferedInputStream in = null;
        BufferedOutputStream out = null;
        try {
            URL url = new URL(urlstr);
            connec = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(
                    connec.getInputStream(), IO_BUFFER_SIZE);
            out = new BufferedOutputStream(
                    outputStream, IO_BUFFER_SIZE);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connec != null) {
                connec.disconnect();
            }
            close(out);
            close(in);
        }
        return false;
    }

    public static String hashKey4Url(String url) {
        String cacheKey = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytes2HexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private static String bytes2HexString(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            String hex = Integer.toHexString(0xFF & digest[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static Bitmap decodeSampledBitmap4FileDescriptor(FileDescriptor fileDescriptor
            , int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor);
    }

    public static Bitmap decodeSampledBitmap4Resource(Resources res, int resID,
                                                      int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resID);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resID, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        int result = 1;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        if (outWidth > reqWidth || outHeight > reqHeight) {
            int halWidth = outWidth / 2;
            int halHeight = outHeight / 2;
            while (halWidth / result >= reqWidth && halHeight / result >= reqHeight) {
                result *= 2;
            }
        }
        return result;
    }

    public long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
    }

    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = loadBitmap4MemoryCache(hashKey4Url(url));
        if (bitmap != null) {
            Log.d(TAG, "loadBitmap: from MemCache.");
            return bitmap;
        }
        bitmap = loadBitmap4DiskCache(url, reqWidth, reqHeight);
        if (bitmap != null) {
            Log.d(TAG, "loadBitmap: from DiskCache.");
            return bitmap;
        }
        bitmap = loadBitmap4Http(url, reqWidth, reqHeight);
        Log.d(TAG, "loadBitmap: from Http.url-->" + url);
        if (bitmap == null && !mIsDiskLruCacheCreated) {
            bitmap = loadBitmap4Url(url);
        }
        return bitmap;
    }

    public void bindBitmap(final String url, final ImageView iv, final int reqWidth, final int reqHeight) {
        iv.setTag(TAG_KEY_URI, url);
        final Bitmap bitmap = loadBitmap4MemoryCache(hashKey4Url(url));
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);
                if (bitmap != null) {
                    LoadResult result = new LoadResult(iv, url, bitmap);
                    mMainHanlder.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(runnable);
    }

    public File getDiskCacheDir(Context context, String uniqueName) {
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 关闭流
     *
     * @param closeables Closeable
     */
    @SuppressWarnings("WeakerAccess")
    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0)
            return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}