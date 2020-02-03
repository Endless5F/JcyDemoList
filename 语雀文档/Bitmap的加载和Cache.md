# Bitmap的加载和Cache

## Bitmap的高效加载
Bitmap在Android中指的是一张图片，想要加载的话BitmapFactory的decodeFile，decodeResource，decodeStream，decodeByteArray分别对应的从文件系统，资源，输入流，以及字节数组加载一个Bitmap对象，其中文件和资源，其实是间接调用了输入流的方法，这四类方法最终实在android的底层实现的，对应的几个native方法。

** 如何高效的加载Bitmap呢？** 其实核心思想也很简单，那就是采用BitmapFactory.Options来加载所需尺寸的图片，这里假设通过ImageView来显示图片，很多时候ImageView并没有图片的原始尺寸那么大，这个时候把整个图片加载进来后再设给ImageView，这显然是没有必要的，BitmapFactory.Options就可以按照一定的采用率来压缩图片，然后显示，这样降低内存占用可以一定程度避免OOM

通过采样率即有效的加载图片，那么到底如何回去采样率呢？

1. 将BitmapFactory.Options的inJustDecodeBounds参数设置为true并且加载图片
1. 从BitmapFactory.Options中取出图片的原始宽高
1. 根据采用率的规则结合目标的View所需计算采用率
1. 将BitmapFactory.Options的inJustDecodeBounds参数设置为false然后重新加载图片

经过上面的4个步骤，加载出的图片就是最终缩放的图片，当然也有可能不需要缩放，这么说一下inJustDecodeBounds这个参数，为true的时候，BitmapFactory只会解析图片的原始宽高，并不会去真正的加载图片，所以这是轻量级的操作，另外需要注意的是，这个时候BitmapFactory获取到的图片宽高和图片位置以及程序运行的设备有关，比如同一张图放在不同的drawable目录下或者程序运行在不同屏幕密度的设备上，这都可能导致BitmapFactory获取到不同的结果，之所以会出现这个现象，这和Activity的资源家在机制有关。
上面4个流程，用代码实现：
```
// 参数二：resId资源id，参数三：reqWidth期望的宽度，参数四：reqHeight期望的高度
public static Bitmap decodeSampleBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}

public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    int width = options.outWidth;
    int height = options.outHeight;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        int halfHeight = height / 2;
        int halfWidth = width / 2;
        while (halfHeight / inSampleSize >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }
    }
    return inSampleSize;
}
```
使用示例：
```
iv.setImageBitmap(decodeSampleBitmapFromResource(getResources(),R.drawable.ic_launcher_background,100,100));
```
## Android中的缓存策略
日常使用的算法就是LRU了，LRU是近期最少使用的算法，他的核心思想是当缓存满时，会优先淘汰那些近期最少使用的缓存对象，采用LRU算法的缓存有两种，LruCache和DiskLruCache,二者可以完美结合。
### LruCache
LruCache是android3.1所提供的一个缓存类，通过v4兼容包，可以兼容到早期的Android版本。

LruCache是一个泛型类，他内部采用了LinkedHashMap以强引用的方式存储外界的缓存对象，然后提供get和set的操作。

- 强引用：直接的对象引用
- 软引用：当一个对象只有软引用存在时，系统内存不足时此对象会被gc回收
- 弱引用：当一个对象只有弱引用存在时，此对象会随时被gc回收。

LruCache的使用：
```
//获取系统分配给每个应用程序的最大内存
int maxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);
int cacheSize=maxMemory/8;
private LruCache<String, Bitmap> mMemoryCache;
//给LruCache分配1/8
mMemoryCache = new LruCache<String, Bitmap>(mCacheSize){
    //重写该方法，来测量Bitmap的大小
    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight()/1024;
    }
};

// 把Bitmap对象加入到缓存中
public void addBitmapToMemory(String key, Bitmap bitmap) {
    if (getBitmapFromMemCache(key) == null) {
        lruCache.put(key, bitmap);
    }
}

// 从缓存中得到Bitmap对象
public Bitmap getBitmapFromMemCache(String key) {
    Log.i(TAG, "lrucache size: " + lruCache.size());
    return lruCache.get(key);
}

// 从缓存中删除指定的Bitmap
public void removeBitmapFromMemory(String key) {
    lruCache.remove(key);
}
```
### DiskLruCache
DiskLruCache用于实现存储设备缓存，即磁盘缓存，他通过缓存对象写入文件系统从而实现缓存的效果。
#### DiskLruCache的创建
DiskLruCache并不能通过构造方法来创建，他提供了open方法才可以：
```
public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize)
```

- 参数一：表示磁盘缓存在文件系统中的存储路径，缓存路径可以选择SD卡上的缓存目录，具体是指sdcard/Android/data/package_name/cache目录。
- 参数二：表示应用的版本号，一般设为1即可，当版本号发生改变时DiskLruCache会清空之前所有的缓存文件，这个特性作用不大，很多情况下即使版本号更改了还是有效。
- 参数三：表示单个节点所对应的数据个数，一般设为1即可。
- 参数四：表示缓存的总大小，比如50MB，当换粗大小超过一个设定值的时候，他会清楚一些缓存来保证总大小不小于这个值。

open使用示例：
```
long DISK_CACHE_SIZE = 1024 * 1024 *50;
File diskCacheDir = getDiskCacheDir(this,"bitmap");
if(!diskCacheDir.exists()){
    diskCacheDir.mkdirs();
}
try {
    mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
} catch (IOException e) {
    e.printStackTrace();
}
```

#### DiskLruCache的缓存添加
DiskLruCache的缓存添加的操作是通过Editor完成的，Editor表示一个缓存对象的编辑对象，这里仍然以图片缓存为例，首先需要获取图片url所对应的key，然后根据key就可以通过edit()来获取Editor对象，如果一个缓存对象正在被编辑，那么edit()会返回null，，之所以把url转换成key，是因为图片的url中很可能有特殊字符，这将影响url在Android中的直接使用，一般采用url的md5值作为key。
MD5转换url为key，代码实现：
```
private String hashKeyFormUrl(String url){
    String cacheKey;
    try {
        MessageDigest mDigest = MessageDigest.getInstance("MD5");
        mDigest.update(url.getBytes());
        cacheKey = bytesToHexString(mDigest.digest());
    } catch (NoSuchAlgorithmException e) {
        cacheKey = String.valueOf(url.hashCode());
    }
    return cacheKey;
}

private String bytesToHexString(byte[] digest) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < digest.length; i++) {
        String hex = Integer.toHexString(0xFF&digest[i]);
        if(hex.length() == 1){
            sb.append('0');
        }
        sb.append(hex);
    }
    return sb.toString();
}
```
将图片的url转为key之后，就可以获取Editor对象了，对于这个key来说，如果当前不存在其他Editor对象，那么edit()就会返回一个新的Editor对象，通过他就可以得到一个文件输入流，需要注意的是，由于前面的open设置了一个节点只能有一个数据，因此DISK_CACHE_SIZE = 0即可。
Editor使用示例：
```
int DISK_CACHE_INDEX = 0;
String key = hashKeyFormUrl(url);
try {
    DiskLruCache.Editor editor = mDiskLruCache.edit(key);
    if(editor != null){
        OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
    }
} catch (IOException e) {
    e.printStackTrace();
}
```
有了文件输入输出流，当从网络下载图片的时候，图片就可以写入文件系统了。
写入文件系统代码示例：
```
private boolean downloadUrlToStream(String urlString,OutputStream outputStream){
    int IO_BUFFER_SIZE = 0;
    HttpURLConnection urlConnection = null;
    BufferedOutputStream out = null;
    BufferedInputStream in = null;
    try {
        URL url = new URL(urlString);
        urlConnection = (HttpURLConnection) url.openConnection();
        in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
        out = new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);
        int b ;
        while ((b = in.read()) != -1){
            out.write(b);
        }
        return true;
    } catch (MalformedURLException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }finally {
        if(urlConnection != null){
            urlConnection.disconnect();
        }
    }
    return false;
}
```
经过上面的步骤，其实并没有真正的将图片写入文件系统，还必须通过Editor的commit来提交操作，这个图片下载过程发生了异常，那么还可以通过Editor的abort来回退操作。
提交或回退代码示例：
```
DiskLruCache.Editor editor = mDiskLruCache.edit(key);
if(editor != null){
    OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
    if(downloadUrlToStream(url,outputStream)){
        editor.commit();
    }else {
        editor.abort();
    }
}
```
#### DiskLruCache的缓存查找
和缓存的添加过程类似，缓存查找过程中也需要将url转换为key。然后通过DiskLruCache的get方法得到一个Snapshot对象，接着再通过Snapshot对象即可得到缓存的文件输入流，自然就可以得到Bitmap对象了，为了避免图片过程中导致的OOM问题，一般不建议直接加载原始图片，上面已经介绍了通过BitmapFactory.Options对象来加载一张缩放后的图片方式，但是这种方法对FileInputStream的缩放存在问题，原因是FileInputStaeam是一种有序的文件流，而两次decodeStream调用影响了文件流的位置属性，导致了第二次decodeStream时得道的是null。为了解决这个问题，可以通过文件流得到他所对应的文件描述符然后再通过BitmapFactory.decodeFileDescriptor方法来加载一张缩放后的图片。
缓存查找代码示例：
```
Bitmap bitmap = null;
String keys = hashKeyFormUrl(url);
try {
    DiskLruCache.Snapshot snapshot = mDiskLruCache.get(keys);
    if(snapshot != null){
        FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
        FileDescriptor fileDescriptor = fileInputStream.getFD();
        bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
        if(bitmap != null){
            addBitmapToMemoryCache(keys,bitmap);
        }
    }
} catch(Exception e) {
	  e.printStackTrace();
}
```
这上面是DiskLruCache的创建吗，缓存，和添加查找过程，读者应该对他的使用有一个大概的理解，当然他还有一些其他的方法remoce,delete之类的用于磁盘缓存的删除操作。
### ImageLoader的实现
一个优秀的ImageLoader应该具备：

- 图片的同步加载
- 图片的异步加载
- 图片压缩
- 内存缓存
- 磁盘缓存
- 网络拉取

图片的同步加载是指能够同步的方式向调用者所提供加载的图片，这个图片可能是从内存缓存中读取，也可以是磁盘的，还可以是网络拉取的，图片的异步加载是一个很有用的功能，很多时候调用者不想再单独的线程里加载图片并将图片设置给需要的ImageView，图片压缩的作用毋庸置疑了，避免OOM的有效手段

内存和磁盘缓存是ImageLoader的核心，也是ImageLoader的意义所在，通过两级缓存极大的提高了程序的效率降低了用户所造成的流量消耗，只是当着二级缓存都不可用才需要从网络拉取

除此之外，ImageLoader还需要一个特殊的情况，比如ListView中，View复用即是他的优点也是他的缺点，缺点就是在ListView中，假设一个item A正在从网站加载图片，他对应的ImageView为A，这个时候用户快速向下滑动列表，很有可能itemB复用了ImageView A,然后等一会之前的图片加载完之后，如果直接给ImageView A设置图片，由于这个时候ImageViewA被itemB所复用，但是item B要显示的图片显然不是item A刚刚下载好的图片，这个时候就会出现item B中显示了item A的图片，这就是比较常见的错误。
ImageLoader完整代码示例：

```
// DiskLruCache算法：implementation 'com.jakewharton:disklrucache:2.0.2'

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
```

