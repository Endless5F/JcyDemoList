主流开源框架源码深入了解第3篇——Glide源码分析。(源码以4.9.0版为准)

## 前言
本文从以下几个方向进行分析：

1. glide的基本用法
2. glide完整的加载流程
3. glide的Target、Transformation

## 一、Glide的基本用法
1. 添加Gradle依赖：

    > api 'com.github.bumptech.glide:glide:4.9.0'             
    // glide默认加载网络图片使用的是HttpURLconnection   
    // 若替换成OkHttp则添加上 'com.github.bumptech.glide:okhttp3-integration:4.9.0'
    api 'com.github.bumptech.glide:okhttp3-integration:4.9.0'          
    annotationProcessor 'com.github.bumptech.glide:compiler:4.9.0'
2. Glide的使用
```
// 基本使用：
    Glide.with(context)
        .load(iconUrl)
        .apply(getOptions())
        .into(imageView);
        
    // 设置选项
    private RequestOptions getOptions() {
        return new RequestOptions()
                .dontAnimate()
                .placeholder(R.drawable.ic_no_picture)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .error(R.drawable.ic_no_picture);
    }
    
// "注：若想实现自定义GlideModule，有两种方式："
    1. 在AndroidManifest.xml Application标签下添加
        <meta-data
            // 自定义GlideModule路径
            android:name="com.android.baselibrary.manager.glide.MyAppGlideModule"
            android:value="GlideModule"/> // value为固定值 GlideModule
    2. 在自定义GlideModule实现类上添加注解@GlideModule

// 自定义GlideModule示例
@GlideModule
public class MyAppGlideModule  extends AppGlideModule {
    @Override
    public boolean isManifestParsingEnabled() {
        return false; // 告知Glide不需要再去 在AndroidManifest中查找是否有自定义GlideModule的实现类
    }
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        // 设置glide加载图片格式 PREFER_RGB_565（glide4.0开始默认图片加载格式为PREFER_ARGB_8888）
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
```
## 二、Glide完整的加载流程

![](https://user-gold-cdn.xitu.io/2019/9/8/16d106e2527ce484?w=515&h=754&f=png&s=174275)
此部分Glide完整的加载流程我们将根据glide的基本用法的顺序来进行讲解：
1. Glide.with(context)
2. load(T) (即：RequestManager.load(T))
3. apply(options) (即：RequestManager.apply(options))
4. into(T) (即：RequestBuilder.into(T))
5. Engine.load（启动加载）
6. 数据源的处理以及显示

### 1. Glide.with(context)

* with定义：Glide类中的静态方法，具有多个重载方法
* with作用：

    1. 获取RequestManager对象
    2. 根据传入with()方法的参数 将Glide图片加载的生命周期与Activity/Fragment的生命周期进行绑定，从而实现自动执行请求，暂停操作
* 此部分涉及到的几个重要的类：

    1. RequestManager：glide请求管理器，绑定Activity/Fragment生命周期(对请求进行加载、暂停、恢复、清除操作)，实现了LifecycleListener接口
    2. RequestManagerRetriever：创建RequestManager，并将RequestManager和自定义Fragment(例如：SupportRequestManagerFragment)绑定，从而生命周期的管理回调
    3. SupportRequestManagerFragment/RequestManagerFragment：glide根据传入的参数，创建的添加到当前Activity中的Fragment，已便达到对生命周期的监听。
* 源码：
```
public class Glide implements ComponentCallbacks2 {
    
    ......
    
    @NonNull
    public static RequestManager with(@NonNull Context context) {
        return getRetriever(context).get(context);
    }

    @NonNull
    public static RequestManager with(@NonNull Activity activity) {
        return getRetriever(activity).get(activity);
    }

    @NonNull
    public static RequestManager with(@NonNull FragmentActivity activity) {
        return getRetriever(activity).get(activity);
    }

    @NonNull
    public static RequestManager with(@NonNull Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @Deprecated // 此方法已废弃
    @NonNull 
    public static RequestManager with(@NonNull android.app.Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    @NonNull
    public static RequestManager with(@NonNull View view) {
        return getRetriever(view.getContext()).get(view);
    }
    
    ......
}
```
从源码中可以看出，虽然with方法传入的参数不同，最终调用的却都是：getRetriever(view.getContext()).get(view)，我们先看getRetriever(view.getContext())实现：
```
// Glide类：
    @NonNull
    private static RequestManagerRetriever getRetriever(@Nullable Context context) {
        // context不可为空
        Preconditions.checkNotNull(
                context,
                "You cannot start a load on a not yet attached View or a Fragment where " +
                        "getActivity() "
                        + "returns null (which usually occurs when getActivity() is called before" +
                        " the Fragment "
                        + "is attached or after the Fragment is destroyed).");
        // 构建glide，并获取RequestManagerRetriever对象
        return Glide.get(context).getRequestManagerRetriever();
    }
```
此方法先判断context是否为空，空则抛出异常。然后继续调用Glide.get(context).getRequestManagerRetriever()，我们依旧先看Glide.get(context)：
```
// Glide类：
    @NonNull
    public static Glide get(@NonNull Context context) {
        // 单例模式获取glide实例
        if (glide == null) {
            synchronized (Glide.class) {
                if (glide == null) {
                    checkAndInitializeGlide(context);
                }
            }
        }

        return glide;
    }
    
    private static void checkAndInitializeGlide(@NonNull Context context) {
        // 若正在初始化，则抛出异常，防止重复初始化
        if (isInitializing) {
            throw new IllegalStateException("You cannot call Glide.get() in registerComponents(),"
                    + " use the provided Glide instance instead");
        }
        // 正在初始化
        isInitializing = true;
        // 初始化glide
        initializeGlide(context);
        // 初始化完成
        isInitializing = false;
    }
    
    private static void initializeGlide(@NonNull Context context) {
        // 初始化GlideBuilder构造器
        initializeGlide(context, new GlideBuilder());
    }

    @SuppressWarnings("deprecation")
    private static void initializeGlide(@NonNull Context context, @NonNull GlideBuilder builder) {
        // 获取应用全局上下文
        Context applicationContext = context.getApplicationContext();
        // 获取注解生成的自定义GlideModule(即：【Glide的基本用法】中自定义GlideModule方法2)
        GeneratedAppGlideModule annotationGeneratedModule = getAnnotationGeneratedGlideModules();
        List<com.bumptech.glide.module.GlideModule> manifestModules = Collections.emptyList();
        // 注解方式自定义GlideModule为空；或者不为空时，其函数isManifestParsingEnabled()返回true
        // (一般情况注解方式自定义GlideModule时，isManifestParsingEnabled方法应返回false，防止去继续加载AndroidManifest.xml中的声明，提高性能)
        if (annotationGeneratedModule == null || annotationGeneratedModule.isManifestParsingEnabled()) {
            // 获取应用AndroidManifest.xml中声明的自定义GlideModule(即：【Glide的基本用法】中自定义GlideModule方法1)
            manifestModules = new ManifestParser(applicationContext).parse();
        }
        // 如果有注解生成的Module就对比一下清单文件获取的GlideModule，删除相同的GlideModule
        if (annotationGeneratedModule != null
                && !annotationGeneratedModule.getExcludedModuleClasses().isEmpty()) {
            Set<Class<?>> excludedModuleClasses =
                    annotationGeneratedModule.getExcludedModuleClasses();
            Iterator<com.bumptech.glide.module.GlideModule> iterator = manifestModules.iterator();
            while (iterator.hasNext()) {
                com.bumptech.glide.module.GlideModule current = iterator.next();
                if (!excludedModuleClasses.contains(current.getClass())) {
                    continue;
                }

                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "AppGlideModule excludes manifest GlideModule: " + current);
                }
                // 删除相同的GlideModule
                iterator.remove();
            }
        }
        // 打印出从清单文件中获取到的有效GlideModule
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            for (com.bumptech.glide.module.GlideModule glideModule : manifestModules) {
                Log.d(TAG, "Discovered GlideModule from manifest: " + glideModule.getClass());
            }
        }

        // 根据自定义的GlideModule获取 RequestManagerFactory，若自定义GlideModule为空，则返回null
        RequestManagerRetriever.RequestManagerFactory factory =
                annotationGeneratedModule != null
                        ? annotationGeneratedModule.getRequestManagerFactory() : null;
        // 设置factory
        builder.setRequestManagerFactory(factory);
        // 遍历循环所有清单文件中获取到的有效GlideModule，将所有GlideModule中设置的属性，添加至builder中
        for (com.bumptech.glide.module.GlideModule module : manifestModules) {
            module.applyOptions(applicationContext, builder);
        }
        if (annotationGeneratedModule != null) {
            // 将注解方式创建的GlideModule中自定义的属性，添加到builder中
            annotationGeneratedModule.applyOptions(applicationContext, builder);
        }
        // 构建glide
        Glide glide = builder.build(applicationContext);
        /**
         * 调用自定义GlideModule的registerComponents，并传入当前的Glide实例来让使用者注册自己的组件，
         * 其实在Glide实例化的过程中已经注册了默认的组件，如果用户定义了相同的组件，那么就会替换之前的。
         *
         * 注册组件的目的就是告诉Glide，当我们调用load(xxxx)方法时，应该用什么方式来获取这个xxxx所指向的资源。
         * 因此，我们可以看到register的第一个参数就是我们load(xxxx)的类型，第二个参数是对应的输入流，而第三个参数就是定义获取资源的方式。
         * */
        for (com.bumptech.glide.module.GlideModule module : manifestModules) {
            module.registerComponents(applicationContext, glide, glide.registry);
        }
        // 同上
        if (annotationGeneratedModule != null) {
            annotationGeneratedModule.registerComponents(applicationContext, glide, glide.registry);
        }

        /**
         * 向Application 中注册一个组件的回调, 用于检测系统 Config 改变和内存占用量低的信号
         * Glide实现ComponentCallback2接口，关于ComponentCallback2：
         *  是一个细粒度的内存回收管理回调。
         *  Application、Activity、Service、ContentProvider、Fragment实现了ComponentCallback2接口
         *  开发者应该实现onTrimMemory(int)方法，细粒度release 内存，参数可以体现不同程度的内存可用情况
         *  响应onTrimMemory回调：开发者的app会直接受益，有利于用户体验，系统更有可能让app存活的更持久。
         *  不响应onTrimMemory回调：系统更有可能kill 进程
         * 关于ComponentCallback（ComponentCallbacks2 继承自 ComponentCallbacks）：
         *  当组件在运行时，如果设备配置信息改变，系统就会回调onConfigurationChanged()。
         *  当整个系统内存不足时调用onLowMemory ()，正在运行的进程应该调整内存的占用。
         *
         * */
        applicationContext.registerComponentCallbacks(glide);
        Glide.glide = glide;
    }

    @Nullable
    @SuppressWarnings({"unchecked", "deprecation", "TryWithIdenticalCatches"})
    private static GeneratedAppGlideModule getAnnotationGeneratedGlideModules() {
        GeneratedAppGlideModule result = null;
        try {
            // 反射获取GeneratedAppGlideModuleImpl对象，
            // 此类为通过注解方式实现自定义GlideModule，在javac编译程序时通过注解处理器(Annotation Processor)动态生成
            Class<GeneratedAppGlideModule> clazz =
                    (Class<GeneratedAppGlideModule>)
                            Class.forName("com.bumptech.glide.GeneratedAppGlideModuleImpl");
            result = clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            ...
        }
        return result;
    }
```
总结一下此部分的源码：

1. 单例模式获取glide实例
2. 若glide为空，则初始化glide，并初始化GlideBuilder对象
3. 获取自定义GlideModule
4. 若存在自定义GlideModule，将关于glide自定义配置添加进GlideBuilder对象中
5. 构建glide实例
6. 向Application中注册一个组件的回调, 用于检测系统 Config 改变和内存占用量低的信号

#### GlideBuilder构建
我们接着看一下Glide的构建builder.build(applicationContext)：
```
public final class GlideBuilder {
    /**
     * 图片变换处理的Map集合
     * 图片变化处理就是指圆角图片之类的处理
     */
    private final Map<Class<?>, TransitionOptions<?, ?>> defaultTransitionOptions =
            new ArrayMap<>();
    // 图片加载引擎，负责启动（发送网络请求获取图片）并管理缓存资源活动
    private Engine engine;
    // Lru策略的图片池,内存不足时自动清空
    private BitmapPool bitmapPool;
    // Lru策略的数组池,用于读写IO,内存不足时自动清空
    private ArrayPool arrayPool;
    // Lru策略的内存缓存池,内存不足时自动清空
    private MemoryCache memoryCache;
    // 线程池,用于查找内存缓存,最大线程数为4,具体取决于cpu
    private GlideExecutor sourceExecutor;
    // 线程池,用于查找本地磁盘缓存,最大线程数为4,具体取决于cpu
    private GlideExecutor diskCacheExecutor;
    /**
     * 用于创建本地磁盘缓存对象的工厂
     * 默认本地磁盘缓存size为250M
     */
    private DiskCache.Factory diskCacheFactory;
    // 内存计算器，它通过获取手机硬件常量和手机的屏幕密度、宽度和长度来计算出最适合当前情况的内存缓存的大小
    private MemorySizeCalculator memorySizeCalculator;
    // 用于生产网络状态监听事件的工厂
    private ConnectivityMonitorFactory connectivityMonitorFactory;
    // 设置Glide的log打印等级
    private int logLevel = Log.INFO;
    // Glide默认的配置选项
    private RequestOptions defaultRequestOptions = new RequestOptions();
    // 用于创建RequestManager对象的工厂
    @Nullable
    private RequestManagerFactory requestManagerFactory;
    // 线程池,用于加载gif图片,默认线程数为1~2,最大数取决于cpu的核数
    private GlideExecutor animationExecutor;
    /**
     * 是否将保留图片资源数据不给回收,默认为false
     * 需要注意的是,设为true将会导致更大的内存消耗,增加crash的几率
     */
    private boolean isActiveResourceRetentionAllowed;
    @Nullable
    private List<RequestListener<Object>> defaultRequestListeners;
    private boolean isLoggingRequestOriginsEnabled;

    ......

    @NonNull
    Glide build(@NonNull Context context) {
        // -----------------初始化成员属性 start-----------------
        if (sourceExecutor == null) {
            sourceExecutor = GlideExecutor.newSourceExecutor();
        }

        if (diskCacheExecutor == null) {
            diskCacheExecutor = GlideExecutor.newDiskCacheExecutor();
        }

        if (animationExecutor == null) {
            animationExecutor = GlideExecutor.newAnimationExecutor();
        }

        if (memorySizeCalculator == null) {
            memorySizeCalculator = new MemorySizeCalculator.Builder(context).build();
        }

        if (connectivityMonitorFactory == null) {
            connectivityMonitorFactory = new DefaultConnectivityMonitorFactory();
        }

        if (bitmapPool == null) {
            int size = memorySizeCalculator.getBitmapPoolSize();
            if (size > 0) {
                bitmapPool = new LruBitmapPool(size);
            } else {
                bitmapPool = new BitmapPoolAdapter();
            }
        }

        if (arrayPool == null) {
            arrayPool = new LruArrayPool(memorySizeCalculator.getArrayPoolSizeInBytes());
        }

        if (memoryCache == null) {
            memoryCache = new LruResourceCache(memorySizeCalculator.getMemoryCacheSize());
        }

        if (diskCacheFactory == null) {
            diskCacheFactory = new InternalCacheDiskCacheFactory(context);
        }

        if (engine == null) {
            engine =
                    new Engine(
                            memoryCache,
                            diskCacheFactory,
                            diskCacheExecutor,
                            sourceExecutor,
                            GlideExecutor.newUnlimitedSourceExecutor(),
                            GlideExecutor.newAnimationExecutor(),
                            isActiveResourceRetentionAllowed);
        }

        if (defaultRequestListeners == null) {
            defaultRequestListeners = Collections.emptyList();
        } else {
            defaultRequestListeners = Collections.unmodifiableList(defaultRequestListeners);
        }

        // -----------------初始化成员属性 end-----------------

        // 初始化requestManagerRetriever对象
        RequestManagerRetriever requestManagerRetriever =
                new RequestManagerRetriever(requestManagerFactory);
        // 构建Glide
        return new Glide(
                context,
                engine,
                memoryCache,
                bitmapPool,
                arrayPool,
                requestManagerRetriever,
                connectivityMonitorFactory,
                logLevel,
                defaultRequestOptions.lock(),
                defaultTransitionOptions,
                defaultRequestListeners,
                isLoggingRequestOriginsEnabled);
    }
}

// RequestManagerRetriever类：
    public RequestManagerRetriever(@Nullable RequestManagerFactory factory) {
        // 若factory为null，则设置默认工厂
        this.factory = factory != null ? factory : DEFAULT_FACTORY;
        handler = new Handler(Looper.getMainLooper(), this /* Callback */);
    }

    private static final RequestManagerFactory DEFAULT_FACTORY = new RequestManagerFactory() {
        @NonNull
        @Override
        public RequestManager build(@NonNull Glide glide, @NonNull Lifecycle lifecycle,
                                    @NonNull RequestManagerTreeNode requestManagerTreeNode,
                                    @NonNull Context context) {
            // 初始化RequestManager
            return new RequestManager(glide, lifecycle, requestManagerTreeNode, context);
        }
    };
```
很明显Glide构建过程初始化了很多属性：
* sourceExecutor：获取图片请求线程池GlideExecutor
* diskCacheExecutor：从硬盘缓存加载图片线程池GlideExecutor
* animationExecutor：执行动画的线程池GlideExecutor
* memorySizeCalculator：从名字理解，是内存计算器
* memoryCache：内存缓存策略，LruResourceCache
* diskCacheFactory：硬盘缓存工厂
* engine：图片加载引擎
* ...
* requestManagerRetriever：创建RequestManager，并将RequestManager和自定义Fragment(例如：SupportRequestManagerFragment)绑定，从而生命周期的管理回调。并且初始化requestManagerRetriever对象时传入的factory若为空，则使用默认的DEFAULT_FACTORY来构建RequestManager。

#### Glide的构造方法
我们再来看一看Glide的构造方法中做了些什么：
```
// Glide类：
    Glide(
            @NonNull Context context,
            @NonNull Engine engine,
            @NonNull MemoryCache memoryCache,
            @NonNull BitmapPool bitmapPool,
            @NonNull ArrayPool arrayPool,
            @NonNull RequestManagerRetriever requestManagerRetriever,
            @NonNull ConnectivityMonitorFactory connectivityMonitorFactory,
            int logLevel,
            @NonNull RequestOptions defaultRequestOptions,
            @NonNull Map<Class<?>, TransitionOptions<?, ?>> defaultTransitionOptions,
            @NonNull List<RequestListener<Object>> defaultRequestListeners,
            boolean isLoggingRequestOriginsEnabled) {
        // 将 Builder 中的线程池, 缓存池等保存
        this.engine = engine;
        this.bitmapPool = bitmapPool;
        this.arrayPool = arrayPool;
        this.memoryCache = memoryCache;
        this.requestManagerRetriever = requestManagerRetriever;
        this.connectivityMonitorFactory = connectivityMonitorFactory;
        // 解码格式对象
        DecodeFormat decodeFormat =
                defaultRequestOptions.getOptions().get(Downsampler.DECODE_FORMAT);
        bitmapPreFiller = new BitmapPreFiller(memoryCache, bitmapPool, decodeFormat);

        final Resources resources = context.getResources();

        registry = new Registry();
        registry.register(new DefaultImageHeaderParser());
        // 现在我们只将这个解析器用于HEIF图像，仅在OMR1 +上支持。 
        // 如果我们需要这个用于其他文件类型，我们应该考虑删除此限制。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            registry.register(new ExifInterfaceImageHeaderParser());
        }

        List<ImageHeaderParser> imageHeaderParsers = registry.getImageHeaderParsers();
        Downsampler downsampler =
                new Downsampler(
                        imageHeaderParsers,
                        resources.getDisplayMetrics(),
                        bitmapPool,
                        arrayPool);
        // 解码类:将InputStream中解码成GIF
        ByteBufferGifDecoder byteBufferGifDecoder =
                new ByteBufferGifDecoder(context, imageHeaderParsers, bitmapPool, arrayPool);
        // 解码类:将Video中解码成bitmap
        ResourceDecoder<ParcelFileDescriptor, Bitmap> parcelFileDescriptorVideoDecoder =
                VideoDecoder.parcel(bitmapPool);
        // 解码类:将ByteBuffer解码成bitmap
        ByteBufferBitmapDecoder byteBufferBitmapDecoder = new ByteBufferBitmapDecoder(downsampler);
        // 解码类:将InputStreams解码成bitmap
        StreamBitmapDecoder streamBitmapDecoder = new StreamBitmapDecoder(downsampler, arrayPool);
        // 解码类:通过Uri解码成Drawable
        ResourceDrawableDecoder resourceDrawableDecoder =
                new ResourceDrawableDecoder(context);
        // 解码类:将资源文件转换成InputStream
        ResourceLoader.StreamFactory resourceLoaderStreamFactory =
                new ResourceLoader.StreamFactory(resources);
        // 将资源文件转换成URI
        ResourceLoader.UriFactory resourceLoaderUriFactory =
                new ResourceLoader.UriFactory(resources);
        // 将res中资源文件转换成ParcelFileDescriptor
        ResourceLoader.FileDescriptorFactory resourceLoaderFileDescriptorFactory =
                new ResourceLoader.FileDescriptorFactory(resources);
        // 将Asset中资源文件转换成ParcelFileDescriptor
        ResourceLoader.AssetFileDescriptorFactory resourceLoaderAssetFileDescriptorFactory =
                new ResourceLoader.AssetFileDescriptorFactory(resources);
        // Bitmap解码类
        BitmapEncoder bitmapEncoder = new BitmapEncoder(arrayPool);
        // bitmap转btye[]类
        BitmapBytesTranscoder bitmapBytesTranscoder = new BitmapBytesTranscoder();
        // GifDrawable转btye[]类
        GifDrawableBytesTranscoder gifDrawableBytesTranscoder = new GifDrawableBytesTranscoder();

        ContentResolver contentResolver = context.getContentResolver();
        // 使用 registry 注册 Glide 需要的 Encoder 与 Decoder
        registry
                .append(ByteBuffer.class, new ByteBufferEncoder())
                .append(InputStream.class, new StreamEncoder(arrayPool))
                // 添加转换成Bitmap相应解码类
                .append(Registry.BUCKET_BITMAP, ByteBuffer.class, Bitmap.class,
                        byteBufferBitmapDecoder)
                .append(Registry.BUCKET_BITMAP, InputStream.class, Bitmap.class,
                        streamBitmapDecoder)
                .append(
                        Registry.BUCKET_BITMAP,
                        ParcelFileDescriptor.class,
                        Bitmap.class,
                        parcelFileDescriptorVideoDecoder)
                .append(
                        Registry.BUCKET_BITMAP,
                        AssetFileDescriptor.class,
                        Bitmap.class,
                        VideoDecoder.asset(bitmapPool))
                .append(Bitmap.class, Bitmap.class, UnitModelLoader.Factory.<Bitmap>getInstance())
                .append(
                        Registry.BUCKET_BITMAP, Bitmap.class, Bitmap.class, new UnitBitmapDecoder())
                .append(Bitmap.class, bitmapEncoder)
                // 添加转换成BitmapDrawables相应解码类
                .append(
                        Registry.BUCKET_BITMAP_DRAWABLE,
                        ByteBuffer.class,
                        BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, byteBufferBitmapDecoder))
                .append(
                        Registry.BUCKET_BITMAP_DRAWABLE,
                        InputStream.class,
                        BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, streamBitmapDecoder))
                .append(
                        Registry.BUCKET_BITMAP_DRAWABLE,
                        ParcelFileDescriptor.class,
                        BitmapDrawable.class,
                        new BitmapDrawableDecoder<>(resources, parcelFileDescriptorVideoDecoder))
                .append(BitmapDrawable.class, new BitmapDrawableEncoder(bitmapPool, bitmapEncoder))
                // 添加转换成GIFs相应解码类
                .append(
                        Registry.BUCKET_GIF,
                        InputStream.class,
                        GifDrawable.class,
                        new StreamGifDecoder(imageHeaderParsers, byteBufferGifDecoder, arrayPool))
                .append(Registry.BUCKET_GIF, ByteBuffer.class, GifDrawable.class,
                        byteBufferGifDecoder)
                .append(GifDrawable.class, new GifDrawableEncoder())
                /* GIF Frames */
                // Compilation with Gradle requires the type to be specified for UnitModelLoader
                // 添加将GIFs解码成Bitmap
                .append(
                        GifDecoder.class, GifDecoder.class,
                        UnitModelLoader.Factory.<GifDecoder>getInstance())
                .append(
                        Registry.BUCKET_BITMAP,
                        GifDecoder.class,
                        Bitmap.class,
                        new GifFrameResourceDecoder(bitmapPool))
                // 添加转换成Drawables相应解码类
                .append(Uri.class, Drawable.class, resourceDrawableDecoder)
                .append(
                        Uri.class, Bitmap.class,
                        new ResourceBitmapDecoder(resourceDrawableDecoder, bitmapPool))
                // 添加文件处理类
                .register(new ByteBufferRewinder.Factory())
                .append(File.class, ByteBuffer.class, new ByteBufferFileLoader.Factory())
                .append(File.class, InputStream.class, new FileLoader.StreamFactory())
                .append(File.class, File.class, new FileDecoder())
                .append(File.class, ParcelFileDescriptor.class,
                        new FileLoader.FileDescriptorFactory())
                // Compilation with Gradle requires the type to be specified for UnitModelLoader
                // here.
                .append(File.class, File.class, UnitModelLoader.Factory.<File>getInstance())
                // 添加转换类(将任意复杂的数据模型转化为一个具体的数据类型,然后通过DataFetcher处理得到相应的可用资源)
                .register(new InputStreamRewinder.Factory(arrayPool))
                // 通过资源文件转化成InputStream
                .append(int.class, InputStream.class, resourceLoaderStreamFactory)
                // 通过资源文件转化成ParcelFileDescriptor
                .append(
                        int.class,
                        ParcelFileDescriptor.class,
                        resourceLoaderFileDescriptorFactory)
                // 通过资源文件转化成Uri
                .append(Integer.class, InputStream.class, resourceLoaderStreamFactory)
                .append(
                        Integer.class,
                        ParcelFileDescriptor.class,
                        resourceLoaderFileDescriptorFactory)
                .append(Integer.class, Uri.class, resourceLoaderUriFactory)
                .append(
                        int.class,
                        AssetFileDescriptor.class,
                        resourceLoaderAssetFileDescriptorFactory)
                .append(
                        Integer.class,
                        AssetFileDescriptor.class,
                        resourceLoaderAssetFileDescriptorFactory)
                .append(int.class, Uri.class, resourceLoaderUriFactory)
                // 通过字符串转化成InputStream
                .append(String.class, InputStream.class, new DataUrlLoader.StreamFactory<String>())
                // 通过Uri转化成InputStream
                .append(Uri.class, InputStream.class, new DataUrlLoader.StreamFactory<Uri>())
                // 通过String转化成InputStream
                .append(String.class, InputStream.class, new StringLoader.StreamFactory())
                // 通过String转化成ParcelFileDescriptor
                .append(String.class, ParcelFileDescriptor.class,
                        new StringLoader.FileDescriptorFactory())
                // 通过String转化成AssetFileDescriptor
                .append(
                        String.class, AssetFileDescriptor.class, new StringLoader.AssetFileDescriptorFactory())
                // 通过网络Uri转化成InputStream
                .append(Uri.class, InputStream.class, new HttpUriLoader.Factory())
                // 通过资产目录Uri转化成InputStream
                .append(Uri.class, InputStream.class,
                        new AssetUriLoader.StreamFactory(context.getAssets()))
                // 通过Uri转化成ParcelFileDescriptor
                .append(
                        Uri.class,
                        ParcelFileDescriptor.class,
                        new AssetUriLoader.FileDescriptorFactory(context.getAssets()))
                // 通过image Uri转化成InputStream
                .append(Uri.class, InputStream.class, new MediaStoreImageThumbLoader.Factory(context))
                // 通过video Uri转化成InputStream
                .append(Uri.class, InputStream.class, new MediaStoreVideoThumbLoader.Factory(context))
                // 通过Uri转化成InputStream
                .append(
                        Uri.class,
                        InputStream.class,
                        new UriLoader.StreamFactory(contentResolver))
                // 通过Uri转化成ParcelFileDescriptor
                .append(
                        Uri.class,
                        ParcelFileDescriptor.class,
                        new UriLoader.FileDescriptorFactory(contentResolver))
                // 通过Uri转化成AssetFileDescriptor
                .append(
                        Uri.class,
                        AssetFileDescriptor.class,
                        new UriLoader.AssetFileDescriptorFactory(contentResolver))
                // 通过http/https Uris转化成InputStream
                .append(Uri.class, InputStream.class, new UrlUriLoader.StreamFactory())
                // 通过  java.net.URL转化成InputStream
                .append(URL.class, InputStream.class, new UrlLoader.StreamFactory())
                // 通过多媒体文件uri转化成文件
                .append(Uri.class, File.class, new MediaStoreFileLoader.Factory(context))
                // 通过http/https url转化成InputStream
                .append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory())
                // 通过数组转化成ByteBuffer
                .append(byte[].class, ByteBuffer.class, new ByteArrayLoader.ByteBufferFactory())
                // 通过数组转化成InputStream
                .append(byte[].class, InputStream.class, new ByteArrayLoader.StreamFactory())
                .append(Uri.class, Uri.class, UnitModelLoader.Factory.<Uri>getInstance())
                .append(Drawable.class, Drawable.class,
                        UnitModelLoader.Factory.<Drawable>getInstance())
                .append(Drawable.class, Drawable.class, new UnitDrawableDecoder())
                //注册转码类
                //bitmap转码成BitmapDrawable
                .register(
                        Bitmap.class,
                        BitmapDrawable.class,
                        new BitmapDrawableTranscoder(resources))
                // bitmap转码成byte[]
                .register(Bitmap.class, byte[].class, bitmapBytesTranscoder)
                // Drawable转码成byte[]
                .register(
                        Drawable.class,
                        byte[].class,
                        new DrawableBytesTranscoder(
                                bitmapPool, bitmapBytesTranscoder, gifDrawableBytesTranscoder))
                // GifDrawable转码成byte[]
                .register(GifDrawable.class, byte[].class, gifDrawableBytesTranscoder);
        // 初始化ImageViewTargetFactory，负责生产正确的Target，into时用到
        ImageViewTargetFactory imageViewTargetFactory = new ImageViewTargetFactory();
        // 构建一个 Glide 的上下文
        glideContext =
                new GlideContext(
                        context,
                        arrayPool,
                        registry,
                        imageViewTargetFactory,
                        defaultRequestOptions,
                        defaultTransitionOptions,
                        defaultRequestListeners,
                        engine,
                        isLoggingRequestOriginsEnabled,
                        logLevel);
    }
```
可以看出Glide的初始化很是复杂，我们来总结一下：

1. 将 GlideBuilder 中的数据保存到成员属性中
2. 构建一个 registry, 注册了众多的编解码器
3. 初始化了ImageViewTargetFactory，用于后面into时正确的生产Target(例如：BitmapImageViewTarget、DrawableImageViewTarget或者抛出异常)
4. 初始化glideContext, 描述其数据资源的上下文

先简单介绍两个类：

1. GlideContext：glide的全局上下文，为glide整个流程提供相应的组件(包括注册器、加载引擎engine、imageViewTargetFactory目标工厂等)
2. DataFetcher(Glide的初始化中registry.append最后一个参数的父类)：：Fetcher的意思是抓取，所以该类可以称为数据抓取器。

    作用：根据不同的数据来源（本地，网络，Asset等）以及读取方式（Stream，ByteBuffer等）来提取并解码数据资源

    其实现类如下：
     * AssetPathFetcher：加载Asset数据
     * HttpUrlFetcher：加载网络数据
     * LocalUriFetcher：加载本地数据
     * 其他实现类...

终于Glide的初始化分析结束，我们重新回到Glide.get(context).getRequestManagerRetriever()中的getRequestManagerRetriever()部分：
```
// Glide类：
    @NonNull
  public RequestManagerRetriever getRequestManagerRetriever() {
    return requestManagerRetriever;
  }
```
实际上getRequestManagerRetriever()就是返回我们在构建Glide过程中(GlideBuilder.build()中)初始化的requestManagerRetriever。因此我们最终回到getRetriever(context).get(context)中的get(context)部分：
```
// RequestManagerRetriever类：
    // with参数为Context调用此方法
    @NonNull
    public RequestManager get(@NonNull Context context) {
        if (context == null) {
            throw new IllegalArgumentException("You cannot start a load on a null Context");
        } else if (Util.isOnMainThread() && !(context instanceof Application)) {
            // 如果在主线程并且context不是Application类型，则通过判断context真实类型调用相应方法
            if (context instanceof FragmentActivity) {
                return get((FragmentActivity) context);
            } else if (context instanceof Activity) {
                return get((Activity) context);
            } else if (context instanceof ContextWrapper) {
                // 获取基础上下文递归调用本方法
                return get(((ContextWrapper) context).getBaseContext());
            }
        }
        // 若不在主线程或者context为ApplicationContext类型则调用此方法
        return getApplicationManager(context);
    }

    // with参数类型为 FragmentActivity调用此方法
    @NonNull
    public RequestManager get(@NonNull FragmentActivity activity) {
        // 后台线程，按with参数类型为applicationContext类型处理
        if (Util.isOnBackgroundThread()) {
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            FragmentManager fm = activity.getSupportFragmentManager();
            return supportFragmentGet(
                    activity, fm, /*parentHint=*/ null, isActivityVisible(activity));
        }
    }

    // with参数类型为Fragment调用此方法
    @NonNull
    public RequestManager get(@NonNull Fragment fragment) {
        Preconditions.checkNotNull(fragment.getActivity(),
                "You cannot start a load on a fragment before it is attached or after it is " +"destroyed");
        // 后台线程，按with参数类型为applicationContext类型处理
        if (Util.isOnBackgroundThread()) {
            return get(fragment.getActivity().getApplicationContext());
        } else {
            FragmentManager fm = fragment.getChildFragmentManager();
            return supportFragmentGet(fragment.getActivity(), fm, fragment, fragment.isVisible());
        }
    }

    // with参数类型为Activity调用此方法
    @SuppressWarnings("deprecation")
    @NonNull
    public RequestManager get(@NonNull Activity activity) {
        // 后台线程，按with参数类型为applicationContext类型处理
        if (Util.isOnBackgroundThread()) {
            return get(activity.getApplicationContext());
        } else {
            assertNotDestroyed(activity);
            android.app.FragmentManager fm = activity.getFragmentManager();
            return fragmentGet(
                    activity, fm, /*parentHint=*/ null, isActivityVisible(activity));
        }
    }

    // with参数类型为View调用此方法
    @SuppressWarnings("deprecation")
    @NonNull
    public RequestManager get(@NonNull View view) {
        // 后台线程，按with参数类型为applicationContext类型处理
        if (Util.isOnBackgroundThread()) {
            return get(view.getContext().getApplicationContext());
        }

        Preconditions.checkNotNull(view);
        Preconditions.checkNotNull(view.getContext(),
                "Unable to obtain a request manager for a view without a Context");
        // 通过view的上下文获取当前view处于的Activity
        Activity activity = findActivity(view.getContext());
        // 视图可能位于其他位置，例如服务，此时无activity，则视为context为.applicationContext类型
        if (activity == null) {
            return get(view.getContext().getApplicationContext());
        }

        // 查找是否有v4包下的Fragment（通过android.R.id.content）
        if (activity instanceof FragmentActivity) {
            Fragment fragment = findSupportFragment(view, (FragmentActivity) activity);
            return fragment != null ? get(fragment) : get(activity);
        }

        // 查找是否有app(标准)包下的Fragment（通过android.R.id.content）
        android.app.Fragment fragment = findFragment(view, activity);
        if (fragment == null) {
            return get(activity);
        }
        // 当作with参数类型为Fragment处理
        return get(fragment);
    }

    // with参数类型为applicationContext类型调用此方法
    @NonNull
    private RequestManager getApplicationManager(@NonNull Context context) {
        // 若不在主线程或者context为ApplicationContext类型
        if (applicationManager == null) {
            synchronized (this) {
                if (applicationManager == null) {
                    // 构建application级别的manager，并传入ApplicationLifecycle
                    // 即此时context为applicationContext类型，此时就意味着glide加载周期和application一致
                    // application启动后即可加载图片，知道application结束才会去清除缓存等，中间即便内存不够用或者activity/fragment不可见等glide也不会清除数据
                    Glide glide = Glide.get(context.getApplicationContext());
                    applicationManager =
                            factory.build(
                                    glide,
                                    new ApplicationLifecycle(),
                                    new EmptyRequestManagerTreeNode(),
                                    context.getApplicationContext());
                }
            }
        }

        return applicationManager;
    }
```
get(context)方法有很多重载方法，分别根据传入的参数不同有不同的处理，由于若当前不在主线程或者context为ApplicationContext类型时glide的周期回调基本没有什么意义，因此不具体看getApplicationManager方法，而其余方法中最终调用的就是两个方法：

    fragmentGet(fragment.getActivity(), fm, fragment, fragment.isVisible())
    supportFragmentGet(activity, fm, /*parentHint=*/ null, isActivityVisible(activity))

两个方法一个返回app包下的标准Fragment，一个返回v4包下的Fragment，两个方法很相似，我们就选择supportFragmentGet方法看一下具体实现：
```
// RequestManagerRetriever类：
    @NonNull
    private RequestManager supportFragmentGet(
            @NonNull Context context,
            @NonNull FragmentManager fm,
            @Nullable Fragment parentHint,
            boolean isParentVisible) {
        // 初始化自定义SupportRequestManagerFragment
        SupportRequestManagerFragment current =
                getSupportRequestManagerFragment(fm, parentHint, isParentVisible);
        // 获取存入的requestManager，第一次为空
        RequestManager requestManager = current.getRequestManager();
        if (requestManager == null) {
            // TODO(b/27524013): Factor out this Glide.get() call.
            Glide glide = Glide.get(context);
            // 构建requestManager，并将当前Fragment的lifecycle(ActivityFragmentLifecycle)传入
            requestManager =
                    factory.build(
                            glide, current.getGlideLifecycle(),
                            current.getRequestManagerTreeNode(), context);
            // 将构建的requestManager设置到current中，用于除第一次以后直接获取
            current.setRequestManager(requestManager);
        }
        return requestManager;
    }
    
    @NonNull
    private SupportRequestManagerFragment getSupportRequestManagerFragment(
            @NonNull final FragmentManager fm, @Nullable Fragment parentHint,
            boolean isParentVisible) {
        // 通过Tag查找是否存在Fragment，防止重复添加Fragment
        SupportRequestManagerFragment current =
                (SupportRequestManagerFragment) fm.findFragmentByTag(FRAGMENT_TAG);
        if (current == null) {
            // 从 pendingRequestManagerFragments 缓存中获取一个
            current = pendingSupportRequestManagerFragments.get(fm);
            if (current == null) {
                // 创建SupportRequestManagerFragment
                current = new SupportRequestManagerFragment();
                current.setParentFragmentHint(parentHint);
                if (isParentVisible) {
                    current.getGlideLifecycle().onStart();
                }
                // 将当前fragment添加进pendingSupportRequestManagerFragments缓存
                pendingSupportRequestManagerFragments.put(fm, current);
                // 将Fragment设置TAG并添加进当前activity
                fm.beginTransaction().add(current, FRAGMENT_TAG).commitAllowingStateLoss();
                // 发送消息,清空刚存进去的Fragment（走到此步代表Fragment已添加进去，通过Tag判断即可）
                handler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }
        return current;
    }
    
    @Override
    public boolean handleMessage(Message msg) {
        boolean handled = true;
        Object removed = null;
        Object key = null;
        switch (msg.what) {
            case ID_REMOVE_SUPPORT_FRAGMENT_MANAGER:
                FragmentManager supportFm = (FragmentManager) msg.obj;
                key = supportFm;
                // 移除Fragment
                removed = pendingSupportRequestManagerFragments.remove(supportFm);
                break;
            default:
                handled = false;
                break;
        }
        
        return handled;
    }

// SupportRequestManagerFragment类：
    public SupportRequestManagerFragment() {
        初始化ActivityFragmentLifecycle
        this(new ActivityFragmentLifecycle());
    }

    @VisibleForTesting
    @SuppressLint("ValidFragment")
    public SupportRequestManagerFragment(@NonNull ActivityFragmentLifecycle lifecycle) {
        // 赋值到成员属性lifecycle中
        this.lifecycle = lifecycle;
    }
    
    @NonNull // 将ActivityFragmentLifecycle返回
    ActivityFragmentLifecycle getGlideLifecycle() {
        return lifecycle;
    }
```
现在我们看完了getRetriever(context).get(context)中的get(context)部分的源码，主要是在 Activity 页面中添加一个RequestManagerFragment/SupportRequestManagerFragment 实例, 以便用于监听 Activity 的生命周期, 然后给这个 Fragment 注入一个 RequestManager。并且为了防止同一个Activity多次重复创建Fragment，glide不仅使用了Tag来标记，而且还使用了 pendingRequestManagerFragments 进行缓存，以保证不会重复创建Fragment，而且一旦 Fragment 顺利生成后再又把它在 map 里面清空。

(o゜▽゜)o☆[BINGO!]现在我们已经看完此部分的所有源码，让我们继续吧！！！

### 2. load(T) (即：RequestManager.load(T))
* load定义：Glide.with(T) 返回了RequestManager对象，所以接下来调用的是RequestManager.load(T)用来确定加载源（URL字符串、图片本地路径等），由于源有多种，因此load有多个重载方法。
* load作用：预先创建好对图片进行一系列操作（加载、编解码、转码）的对象，并全部封装到 RequestBuilder<Drawable>对象中。
* RequestBuilder：一个通用类，可以处理常规资源类型的设置选项和启动负载。用于启动下载并将下载的资源转换为指定的图片资源对象,然后将图片资源对象装置在指定的控件上面，比方说外层传入一个Drawable类型,就把下载的原始资源转换为Drawable的图片资源对象。
   
   目前可以使用的原始资源转换类型有以下：
    * 1.Drawable(默认类型)及其子类
    * 2.Bitmap
    * 3.GifDrawable
    * 4.File(未知类型)
    * @param TransCodeType 需要解析变成的图片资源类型,默认是Drawable类型
* load源码
```
// RequestManager类：
    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<Drawable> load(@Nullable Uri uri) {
        return asDrawable().load(uri);
    }

    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<Drawable> load(@Nullable File file) {
        return asDrawable().load(file);
    }

    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<Drawable> load(@Nullable byte[] model) {
        return asDrawable().load(model);
    }

    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<Drawable> load(@Nullable Object model) {
        return asDrawable().load(model);
    }
    
    ...
    
    // 默认加载Drawable
    @NonNull
    @CheckResult
    public RequestBuilder<Drawable> asDrawable() {
        // 资源类型为Drawable.class
        return as(Drawable.class);
    }
    // 指定加载Bitmap
    public RequestBuilder<Bitmap> asBitmap() {
        return as(Bitmap.class).apply(DECODE_TYPE_BITMAP);
    }
    // 指定加载Gif
    public RequestBuilder<GifDrawable> asGif() {
        return as(GifDrawable.class).apply(DECODE_TYPE_GIF);
    }

    @NonNull
    @CheckResult
    public <ResourceType> RequestBuilder<ResourceType> as(
            @NonNull Class<ResourceType> resourceClass) {
        return new RequestBuilder<>(glide, this, resourceClass, context);
    }
```
从多个load重载方法中可以看出，都是先调用asDrawable()方法获取一个RequestBuilder<Drawable>请求的构造器。从这里我们就知道了 Glide 默认将下载的图片资源转为Drawable资源对象，如果对资源转换有其他指定的需求的话，我们可以在 laod 方法之前使用 asBitmap()或者 asGif() ，而且他们的内部也是一样的代码，只是传入的Class不一样而已。之后调用load方法并传入参数。
```
// RequestBuilder类：
    @NonNull
    @Override
    @CheckResult
    public RequestBuilder<TranscodeType> load(@Nullable String string) {
        return loadGeneric(string);
    }

    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<TranscodeType> load(@Nullable Uri uri) {
        return loadGeneric(uri);
    }

    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<TranscodeType> load(@Nullable File file) {
        return loadGeneric(file);
    }

    @NonNull
    @CheckResult
    @SuppressWarnings("unchecked")
    @Override
    public RequestBuilder<TranscodeType> load(@Nullable Object model) {
        return loadGeneric(model);
    }

    @NonNull
    private RequestBuilder<TranscodeType> loadGeneric(@Nullable Object model) {
        this.model = model;
        isModelSet = true;
        return this;
    }
```
由于load参数有多种类型，因此RequestBuilder的load方法也是有多个重载的，其最后调用的都是loadGeneric方法，此方法主要数据来源model保存到RequestBuilder本身的成员model里；并且将isModelSet设置为true，表示model已经设置过啦；最后返回对象本身this，以待后面的调用。

### 3. apply(options) (即：RequestManager.apply(options))
* apply定义：设置选项参数
* apply作用：设置选项参数，比如缓存策略、图片显示(占位图、错误图、scaletype等)、有无动画等
* 源码
```
// RequestBuilder类：
    @NonNull
    @CheckResult
    @Override
    public RequestBuilder<TranscodeType> apply(@NonNull BaseRequestOptions<?> requestOptions) {
        Preconditions.checkNotNull(requestOptions);
        return super.apply(requestOptions);
    }
    
// BaseRequestOptions类：
    
    private static final int UNSET = -1;
    private static final int SIZE_MULTIPLIER = 1 << 1;
    private static final int DISK_CACHE_STRATEGY = 1 << 2;
    private static final int PRIORITY = 1 << 3;
    private static final int ERROR_PLACEHOLDER = 1 << 4;
    private static final int ERROR_ID = 1 << 5;
    private static final int PLACEHOLDER = 1 << 6;
    private static final int PLACEHOLDER_ID = 1 << 7;
    private static final int IS_CACHEABLE = 1 << 8;
    private static final int OVERRIDE = 1 << 9;
    private static final int SIGNATURE = 1 << 10;
    private static final int TRANSFORMATION = 1 << 11;
    private static final int RESOURCE_CLASS = 1 << 12;
    private static final int FALLBACK = 1 << 13;
    private static final int FALLBACK_ID = 1 << 14;
    private static final int THEME = 1 << 15;
    private static final int TRANSFORMATION_ALLOWED = 1 << 16;
    private static final int TRANSFORMATION_REQUIRED = 1 << 17;
    private static final int USE_UNLIMITED_SOURCE_GENERATORS_POOL = 1 << 18;
    private static final int ONLY_RETRIEVE_FROM_CACHE = 1 << 19;
    private static final int USE_ANIMATION_POOL = 1 << 20;
    
    private static boolean isSet(int fields, int flag) {
        return (fields & flag) != 0;
    }

    @NonNull
    @CheckResult
    public T apply(@NonNull BaseRequestOptions<?> o) {
        if (isAutoCloneEnabled) {
            return clone().apply(o);
        }
        BaseRequestOptions<?> other = o;

        if (isSet(other.fields, SIZE_MULTIPLIER)) {
            sizeMultiplier = other.sizeMultiplier;
        }
        if (isSet(other.fields, USE_UNLIMITED_SOURCE_GENERATORS_POOL)) {
            useUnlimitedSourceGeneratorsPool = other.useUnlimitedSourceGeneratorsPool;
        }
        if (isSet(other.fields, USE_ANIMATION_POOL)) {
            useAnimationPool = other.useAnimationPool;
        }
        if (isSet(other.fields, DISK_CACHE_STRATEGY)) {
            diskCacheStrategy = other.diskCacheStrategy;
        }
        if (isSet(other.fields, PRIORITY)) {
            priority = other.priority;
        }
        if (isSet(other.fields, ERROR_PLACEHOLDER)) {
            errorPlaceholder = other.errorPlaceholder;
            errorId = 0;
            fields &= ~ERROR_ID;
        }
        if (isSet(other.fields, ERROR_ID)) {
            errorId = other.errorId;
            errorPlaceholder = null;
            fields &= ~ERROR_PLACEHOLDER;
        }
        if (isSet(other.fields, PLACEHOLDER)) {
            placeholderDrawable = other.placeholderDrawable;
            placeholderId = 0;
            fields &= ~PLACEHOLDER_ID;
        }
        if (isSet(other.fields, PLACEHOLDER_ID)) {
            placeholderId = other.placeholderId;
            placeholderDrawable = null;
            fields &= ~PLACEHOLDER;
        }
        if (isSet(other.fields, IS_CACHEABLE)) {
            isCacheable = other.isCacheable;
        }
        if (isSet(other.fields, OVERRIDE)) {
            overrideWidth = other.overrideWidth;
            overrideHeight = other.overrideHeight;
        }
        if (isSet(other.fields, SIGNATURE)) {
            signature = other.signature;
        }
        if (isSet(other.fields, RESOURCE_CLASS)) {
            resourceClass = other.resourceClass;
        }
        if (isSet(other.fields, FALLBACK)) {
            fallbackDrawable = other.fallbackDrawable;
            fallbackId = 0;
            fields &= ~FALLBACK_ID;
        }
        if (isSet(other.fields, FALLBACK_ID)) {
            fallbackId = other.fallbackId;
            fallbackDrawable = null;
            fields &= ~FALLBACK;
        }
        if (isSet(other.fields, THEME)) {
            theme = other.theme;
        }
        if (isSet(other.fields, TRANSFORMATION_ALLOWED)) {
            isTransformationAllowed = other.isTransformationAllowed;
        }
        if (isSet(other.fields, TRANSFORMATION_REQUIRED)) {
            isTransformationRequired = other.isTransformationRequired;
        }
        if (isSet(other.fields, TRANSFORMATION)) {
            transformations.putAll(other.transformations);
            isScaleOnlyOrNoTransform = other.isScaleOnlyOrNoTransform;
        }
        if (isSet(other.fields, ONLY_RETRIEVE_FROM_CACHE)) {
            onlyRetrieveFromCache = other.onlyRetrieveFromCache;
        }

        // Applying options with dontTransform() is expected to clear our transformations.
        if (!isTransformationAllowed) {
            transformations.clear();
            fields &= ~TRANSFORMATION;
            isTransformationRequired = false;
            fields &= ~TRANSFORMATION_REQUIRED;
            isScaleOnlyOrNoTransform = true;
        }

        fields |= other.fields;
        // 将所有配置添加到Options对象中，此对象是直接在成员属性中初始化
        // 此对象内部维护一个CachedHashCodeArrayMap(ArrayMap的子类)类型的ArrayMap来存储选项配置
        options.putAll(other.options);

        return selfOrThrowIfLocked();
    }
```
RequestManager.apply(options)最后会调用到父类BaseRequestOptions的apply方法中。注意到BaseRequestOptions有一个int类型成员变量fields，apply方法一直在调用isSet()方法做判断，isSet()逻辑是如果传入的这个RequestOptions对象other设置了xxx属性，就替换掉现有的属性，如果特殊情况再清楚掉其他标志位，最后会将所有配置添加到Options对象中。我们来简单说一声fields，它是用来标志各个属性是否被赋值，我们就拿常用的placeholder()方法来分析，placeholder()有两个重载方法：
```
    public RequestOptions placeholder(@Nullable Drawable drawable) {
        if (isAutoCloneEnabled) {
            return clone().placeholder(drawable);
        }

        this.placeholderDrawable = drawable;
        fields |= PLACEHOLDER;//给代表drawable的placeholder标志位至1

        placeholderId = 0;
        fields &= ~PLACEHOLDER_ID;//给代表id的placeholder标志位至0

        return selfOrThrowIfLocked();
    }

    public RequestOptions placeholder(@DrawableRes int resourceId) {
        if (isAutoCloneEnabled) {
            return clone().placeholder(resourceId);
        }

        this.placeholderId = resourceId;
        fields |= PLACEHOLDER_ID;//给代表id的placeholder标志位至1

        placeholderDrawable = null;
        fields &= ~PLACEHOLDER;//给代表drawable的placeholder标志位至0

        return selfOrThrowIfLocked();
    }
```
placeholder(drawable)和placeholder(resourceId)这两个方法不能同时对placeholder这一实物产生效果，所以会有fields |= PLACEHOLDER和fields &= ~PLACEHOLDER_ID这样的代码，

系统定义了21个标志位，通过每个标志位代表RequestOptions对应属性的赋值与否，巧妙使用位运算，用一个int类型表示了21个bool逻辑(其实一个int最多能标识32个逻辑)。

### 4. into(T) (即：RequestBuilder.into(T))
* into定义：正常加载设置显示图片最后一步
* into作用：构建网络请求对象 并 执行 该网络请求
* 源码
```
// RequestBuilder类：
    @NonNull
    public <Y extends Target<TranscodeType>> Y into(@NonNull Y target) {
        // 注意：传入Executors.mainThreadExecutor() 主线程调度器
        return into(target, /*targetListener=*/ null, Executors.mainThreadExecutor());
    }

    @NonNull
    public ViewTarget<ImageView, TranscodeType> into(@NonNull ImageView view) {
        Util.assertMainThread();
        Preconditions.checkNotNull(view);
        BaseRequestOptions<?> requestOptions = this;
        if (!requestOptions.isTransformationSet()
                && requestOptions.isTransformationAllowed()
                && view.getScaleType() != null) {
            // 根据View的scaleType，克隆一个RequestOptions，然后配置一个View的scaleType缩放选项
            switch (view.getScaleType()) {
                case CENTER_CROP:
                    requestOptions = requestOptions.clone().optionalCenterCrop();
                    break;
                case CENTER_INSIDE:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
                case FIT_CENTER:
                case FIT_START:
                case FIT_END:
                    requestOptions = requestOptions.clone().optionalFitCenter();
                    break;
                case FIT_XY:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
                case CENTER:
                case MATRIX:
                default:
                    // Do nothing.
            }
        }
        // 调用4个参数的into方法
        return into(
                // 调用 GlideContext.buildImageViewTarget 构建一个 ViewTarget
                glideContext.buildImageViewTarget(view, transcodeClass),
                /*targetListener=*/ null,
                requestOptions,
                // 注意：传入Executors.mainThreadExecutor() 主线程调度器
                Executors.mainThreadExecutor());
    }

    @NonNull
    @Synthetic
    <Y extends Target<TranscodeType>> Y into(
            @NonNull Y target,
            @Nullable RequestListener<TranscodeType> targetListener,
            Executor callbackExecutor) {
        return into(target, targetListener, /*options=*/ this, callbackExecutor);
    }

    private <Y extends Target<TranscodeType>> Y into(
            @NonNull Y target,
            @Nullable RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> options,
            Executor callbackExecutor) {
        ......
    }
```
into(target)方法也有多个重载，不过我们仔细看，其实into只有两个方法是提供给开发者使用的一个是into(@NonNull Y target)一个是into(@NonNull ImageView view)，而我们既可以通过直接设置ImageView方法显示图片，也可以通过设置自定义Target来实现资源回调的监听，可在回调中设置图片显示。

此处我们先引入一个概念Glide的Transformation变换(即：into(@NonNull ImageView view)中提到的配置View的scaleType缩放选项)，而此概念放到后面【Glide之Transformation转换】再进行分析。继续我们上面的into方法，在这几个into的重载方法中，会发现其实最后调用的还是最后一个4个参数的into方法：
```
// GlideContext类：

    private final ImageViewTargetFactory imageViewTargetFactory;

    public GlideContext(
            ...,
            @NonNull ImageViewTargetFactory imageViewTargetFactory,
            ...) {
        ...
        this.imageViewTargetFactory = imageViewTargetFactory;
        ...
    }
    
    @NonNull
    public <X> ViewTarget<ImageView, X> buildImageViewTarget(
        @NonNull ImageView imageView, @NonNull Class<X> transcodeClass) {
        // 调用工厂类来创建一个 imageView 的 ViewTarget
        return imageViewTargetFactory.buildTarget(imageView, transcodeClass);
    }
    
public class ImageViewTargetFactory {
    @NonNull
    @SuppressWarnings("unchecked")
    public <Z> ViewTarget<ImageView, Z> buildTarget(@NonNull ImageView view,@NonNull Class<Z> clazz) {
        // 根据目标编码的类型来创建不同的 ViewTarget 对象
        // 因为我们没有调用asBitmap, 因此这里为 ViewTarget为DrawableImageViewTarget
        if (Bitmap.class.equals(clazz)) {
            return (ViewTarget<ImageView, Z>) new BitmapImageViewTarget(view);
        } else if (Drawable.class.isAssignableFrom(clazz)) {
            return (ViewTarget<ImageView, Z>) new DrawableImageViewTarget(view);
        } else {
            throw new IllegalArgumentException(
                    "Unhandled class: " + clazz + ", try .as*(Class).transcode" + "(ResourceTranscoder)");
        }
    }
}

// RequestBuilder类：
    private <Y extends Target<TranscodeType>> Y into(
            @NonNull Y target,
            @Nullable RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> options,
            Executor callbackExecutor) {
        Preconditions.checkNotNull(target);
        if (!isModelSet) {
            throw new IllegalArgumentException("You must call #load() before calling #into()");
        }
        // 调用 buildRequest 构建了一个 Glide 请求
        Request request = buildRequest(target, targetListener, options, callbackExecutor);
        // 获取当前ImageView对应的请求，初次为null
        Request previous = target.getRequest();
        // 若当前ImageView对应的请求已经设置(request == previous)
        if (request.isEquivalentTo(previous)
                && !isSkipMemoryCacheWithCompletePreviousRequest(options, previous)) {
            request.recycle();
            // 如果上一次请求已完成，则再次开始将确保重新传递结果，触发RequestListeners和Targets。
            // 如果请求失败，则再次开始重新启动请求，再给它一次完成的机会。
            // 如果请求已经正在运行，我们可以让它继续运行而不会中断。
            if (!Preconditions.checkNotNull(previous).isRunning()) {
                // 使用先前的请求而不是新的请求来允许优化，
                // 例如跳过设置占位符，跟踪和取消跟踪目标，以及获取视图尺寸。
                previous.begin();
            }
            return target;
        }
        // 清除先Target之前的所有任务并释放资源
        requestManager.clear(target);
        // 给目标View设置请求(保存)
        target.setRequest(request);
        // 调用RequestManager.track 方法执行请求
        requestManager.track(target, request);

        return target;
    }
```
可以看到into(imageView)方法中使用GlideContext通过工厂类创建了ImageView的ViewTarget, 它描述的是图像处理结束之后, 最终要作用到的 View目标，关于ViewTarget我们放到后面【Glide之Target】部分分析。
构建好了ViewTarge, 就走到了最后一个into中，可以看到调用了 buildRequest 构建了一个 Glide 的请求, 其构建过程也非常有意思, 最终最调用 SingleRequest.obtain 构建一个 Request 的实例对象, 之后便是调用 RequestManager.track 将其分发并执行。

我们接着看requestManager.track(target, request)的实现：
```
// RequestManager类：
    synchronized void track(@NonNull Target<?> target, @NonNull Request request) {
        // targetTracker,在成员属性时即初始化完成
        // 作用：保存当前为RequestManager激活的Target集，并转发生命周期事件。
        targetTracker.track(target);
        // requestTracker，在RequestManager初始化时完成的初始化
        // 作用：用于跟踪，取消和重新启动正在进行，已完成和失败的请求的类。 此类不是线程安全的，必须在主线程上访问。
        requestTracker.runRequest(request);
    }
// RequestTracker类：
    public void runRequest(@NonNull Request request) {
        requests.add(request);
        if (!isPaused) {
            request.begin();
        } else {
            request.clear();
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Paused, delaying request");
            }
            pendingRequests.add(request);
        }
    }
```
可的看到，track方法是先将target添加到targetTracker类的targets集合中，然后运行request的begin方法。我们直接点request.begin方法去跟踪源码，会发现Request只是一个接口，所以我们要找到它的实现类。
#### Request的构建
我们来看一下其Request的buildRequest构建请求的过程：
```
// RequestBuilder类：
    private Request buildRequest(
            Target<TranscodeType> target,
            @Nullable RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> requestOptions,
            Executor callbackExecutor) {
        // 构建请求 递归方法
        return buildRequestRecursive(
                target,
                targetListener,
                /*parentCoordinator=*/ null,
                transitionOptions,
                requestOptions.getPriority(),
                requestOptions.getOverrideWidth(),
                requestOptions.getOverrideHeight(),
                requestOptions,
                callbackExecutor);
    }

    private Request buildRequestRecursive(
            Target<TranscodeType> target,
            @Nullable RequestListener<TranscodeType> targetListener,
            @Nullable RequestCoordinator parentCoordinator,
            TransitionOptions<?, ? super TranscodeType> transitionOptions,
            Priority priority,
            int overrideWidth,
            int overrideHeight,
            BaseRequestOptions<?> requestOptions,
            Executor callbackExecutor) {

        // 如有必要，首先构建ErrorRequestCoordinator，以便我们可以更新parentCoordinator。
        ErrorRequestCoordinator errorRequestCoordinator = null;
        if (errorBuilder != null) {
            errorRequestCoordinator = new ErrorRequestCoordinator(parentCoordinator);
            parentCoordinator = errorRequestCoordinator;
        }
        // 主要要求
        Request mainRequest =
                // 构建缩略图请求 递归方法
                buildThumbnailRequestRecursive(
                        target,
                        targetListener,
                        parentCoordinator,
                        transitionOptions,
                        priority,
                        overrideWidth,
                        overrideHeight,
                        requestOptions,
                        callbackExecutor);

        if (errorRequestCoordinator == null) {
            return mainRequest;
        }

        // 构建错误请求
        int errorOverrideWidth = errorBuilder.getOverrideWidth();
        int errorOverrideHeight = errorBuilder.getOverrideHeight();
        if (Util.isValidDimensions(overrideWidth, overrideHeight)
                && !errorBuilder.isValidOverride()) {
            errorOverrideWidth = requestOptions.getOverrideWidth();
            errorOverrideHeight = requestOptions.getOverrideHeight();
        }

        Request errorRequest =
                errorBuilder.buildRequestRecursive(
                        target,
                        targetListener,
                        errorRequestCoordinator,
                        errorBuilder.transitionOptions,
                        errorBuilder.getPriority(),
                        errorOverrideWidth,
                        errorOverrideHeight,
                        errorBuilder,
                        callbackExecutor);
        errorRequestCoordinator.setRequests(mainRequest, errorRequest);
        return errorRequestCoordinator;
    }

    private Request buildThumbnailRequestRecursive(
            Target<TranscodeType> target,
            RequestListener<TranscodeType> targetListener,
            @Nullable RequestCoordinator parentCoordinator,
            TransitionOptions<?, ? super TranscodeType> transitionOptions,
            Priority priority,
            int overrideWidth,
            int overrideHeight,
            BaseRequestOptions<?> requestOptions,
            Executor callbackExecutor) {
        // thumbnailBuilder默认为null，除非手动调用
        //  RequestBuilder类的thumbnail(@Nullable RequestBuilder<TranscodeType> thumbnailRequest)方法。
        if (thumbnailBuilder != null) {
            // 递归案例：包含可能的递归缩略图请求构建器。
            if (isThumbnailBuilt) {
                throw new IllegalStateException("You cannot use a request as both the main " +
                        "request and a "
                        + "thumbnail, consider using clone() on the request(s) passed to " +
                        "thumbnail()");
            }

            TransitionOptions<?, ? super TranscodeType> thumbTransitionOptions =
                    thumbnailBuilder.transitionOptions;

            // 默认情况下，将我们的转换应用于缩略图请求，但避免覆盖可能已明确应用于缩略图请求的自定义选项
            if (thumbnailBuilder.isDefaultTransitionOptionsSet) {
                thumbTransitionOptions = transitionOptions;
            }
            // 缩略图有线
            Priority thumbPriority = thumbnailBuilder.isPrioritySet()
                    ? thumbnailBuilder.getPriority() : getThumbnailPriority(priority);
            // 缩略图宽高
            int thumbOverrideWidth = thumbnailBuilder.getOverrideWidth();
            int thumbOverrideHeight = thumbnailBuilder.getOverrideHeight();
            // 宽高检验
            if (Util.isValidDimensions(overrideWidth, overrideHeight)
                    && !thumbnailBuilder.isValidOverride()) {
                thumbOverrideWidth = requestOptions.getOverrideWidth();
                thumbOverrideHeight = requestOptions.getOverrideHeight();
            }
            // 缩略图请求协议器，同时协调原图与缩略图的request
            ThumbnailRequestCoordinator coordinator =
                    new ThumbnailRequestCoordinator(parentCoordinator);
            // 原图请求
            Request fullRequest =
                    obtainRequest(
                            target,
                            targetListener,
                            requestOptions,
                            coordinator,
                            transitionOptions,
                            priority,
                            overrideWidth,
                            overrideHeight,
                            callbackExecutor);
            isThumbnailBuilt = true;
            //递归生成缩略图请求
            //调用buildRequestRecursive，还是会调到buildThumbnailRequestRecursive，这是个递归方法
            Request thumbRequest =
                    thumbnailBuilder.buildRequestRecursive(
                            target,
                            targetListener,
                            coordinator,
                            thumbTransitionOptions,
                            thumbPriority,
                            thumbOverrideWidth,
                            thumbOverrideHeight,
                            thumbnailBuilder,
                            callbackExecutor);
            isThumbnailBuilt = false;
            // 把这两个request包装到ThumbnailRequestCoordinator中
            coordinator.setRequests(fullRequest, thumbRequest);
            return coordinator;
        // thumbSizeMultiplier默认为null，否则需要调用RequestBuilder类的thumbnail(float sizeMultiplier)方法
        } else if (thumbSizeMultiplier != null) {
            // 根据指定的缩放系数加载缩略图
            ThumbnailRequestCoordinator coordinator =
                    new ThumbnailRequestCoordinator(parentCoordinator);
            Request fullRequest =
                    obtainRequest(
                            target,
                            targetListener,
                            requestOptions,
                            coordinator,
                            transitionOptions,
                            priority,
                            overrideWidth,
                            overrideHeight,
                            callbackExecutor);
            BaseRequestOptions<?> thumbnailOptions =
                    requestOptions.clone().sizeMultiplier(thumbSizeMultiplier);

            Request thumbnailRequest =
                    obtainRequest(
                            target,
                            targetListener,
                            thumbnailOptions,
                            coordinator,
                            transitionOptions,
                            getThumbnailPriority(priority),
                            overrideWidth,
                            overrideHeight,
                            callbackExecutor);

            coordinator.setRequests(fullRequest, thumbnailRequest);
            return coordinator;
        } else {
            // 只加载原图
            return obtainRequest(
                    target,
                    targetListener,
                    requestOptions,
                    parentCoordinator,
                    transitionOptions,
                    priority,
                    overrideWidth,
                    overrideHeight,
                    callbackExecutor);
        }
    }

    private Request obtainRequest(
            Target<TranscodeType> target,
            RequestListener<TranscodeType> targetListener,
            BaseRequestOptions<?> requestOptions,
            RequestCoordinator requestCoordinator,
            TransitionOptions<?, ? super TranscodeType> transitionOptions,
            Priority priority,
            int overrideWidth,
            int overrideHeight,
            Executor callbackExecutor) {
        return SingleRequest.obtain(
                context,
                glideContext,
                model, // 对应load(url)，比如一个图片地址
                transcodeClass,
                requestOptions,
                overrideWidth, // 宽
                overrideHeight, // 高
                priority,
                target,
                targetListener,
                requestListeners,
                requestCoordinator,
                glideContext.getEngine(), // 全局加载引擎
                transitionOptions.getTransitionFactory(),
                callbackExecutor);
    }
```
从源码中来看，若我们不调用RequestBuilder类的thumbnail()方法，最后构建的请求就是SingleRequest，那么看看它的begin方法：
```
// SingleRequest类：
    @Override
    public synchronized void begin() {
        assertNotCallingCallbacks();
        stateVerifier.throwIfRecycled();
        startTime = LogTime.getLogTime();
        // 如果图片的来源没有设置，加载失败
        if (model == null) {
            if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
                width = overrideWidth;
                height = overrideHeight;
            }
            int logLevel = getFallbackDrawable() == null ? Log.WARN : Log.DEBUG;
            onLoadFailed(new GlideException("Received null model"), logLevel);
            return;
        }

        if (status == Status.RUNNING) {
            throw new IllegalArgumentException("Cannot restart a running request");
        }

        // 如果我们在完成后重新启动（通常通过notifyDataSetChanged 在相同的Target或View中启动相同的请求），
        // 我们可以简单地使用上次检索的资源和大小并且跳过获取新的大小，开始新加载等。
        // 这意味着想要重新启动加载的用户因为他们期望视图大小已经改变而需要明确清除视图或目标在开始新加载之前。
        if (status == Status.COMPLETE) {
            onResourceReady(resource, DataSource.MEMORY_CACHE);
            return;
        }

        // 重新启动既不完整也不运行的请求可以被视为新的请求并且可以从头开始重新运行。

        status = Status.WAITING_FOR_SIZE;
        // 如果Target的宽高已经获取并合法，就开始下一步
        if (Util.isValidDimensions(overrideWidth, overrideHeight)) {
            onSizeReady(overrideWidth, overrideHeight);
        } else {
            // 手动获取宽高，注意：参数传的是当前对象this，首次会走这里
            target.getSize(this);
        }

        if ((status == Status.RUNNING || status == Status.WAITING_FOR_SIZE)
                && canNotifyStatusChanged()) {
            // 设置占位图
            target.onLoadStarted(getPlaceholderDrawable());
        }
        if (IS_VERBOSE_LOGGABLE) {
            logV("finished run method in " + LogTime.getElapsedMillis(startTime));
        }
    }

// ViewTarget类：
    @CallSuper
    @Override
    public void getSize(@NonNull SizeReadyCallback cb) {
        sizeDeterminer.getSize(cb);
    }
// ViewTarget.SizeDeterminer类：
    void getSize(@NonNull SizeReadyCallback cb) {
        int currentWidth = getTargetWidth();
        int currentHeight = getTargetHeight();
        // 视图状态和大小有效
        if (isViewStateAndSizeValid(currentWidth, currentHeight)) {
            // 又调用回了SingleRequest中的onSizeReady方法
            cb.onSizeReady(currentWidth, currentHeight);
            return;
        }

        // 我们希望按照添加的顺序通知回调，我们只希望一次添加一个或多个回调，因此List是一个合理的选择。
        // 若在上一步获取的宽高或者状态无效，则先将cb保存在list集合中，以备后续测量好有效宽高后再调用onSizeReady方法
        if (!cbs.contains(cb)) {
            cbs.add(cb);
        }
        if (layoutListener == null) {
            // 通过ViewTreeObserver监听View绘制完成再获取大小，防止获取为0
            ViewTreeObserver observer = view.getViewTreeObserver();
            layoutListener = new SizeDeterminerLayoutListener(this);
            // 添加预绘制侦听器，此侦听器，主要是监听View在界面上绘制完成
            // 然后调用其自身的onPreDraw()方法--》sizeDeterminer.checkCurrentDimens()
            // --》notifyCbs(currentWidth, currentHeight) 来通知cbs集合按顺序执行所有的onSizeReady方法
            observer.addOnPreDrawListener(layoutListener);
        }
    }
```
SingleRequest实现了SizeReadyCallback接口用来回调当前View大小有效的测量完成。好，我们来总结一下begin方法中的实现：
1. 判断图片来源是否为null，为null则加载失败
2. 判断保存在Request中的Target的宽高是否合法，合法则走onSizeReady方法
3. 若2中的不合法，则去ViewTarget中获取宽高
4. 若ViewTraget中get到的宽高有效并且视图状态有效，则直接调用SingleRequest中的onSizeReady方法
5. 若4中get的无效获取视图不合法，则通过ViewTreeObserver监听View绘制完成，并最后执行SingleRequest中的onSizeReady方法

通过begin方法的总结，可以明确的知道，接下来走到了SingleRequest中的onSizeReady方法：
```
// SingleRequest类：
    @Override
    public synchronized void onSizeReady(int width, int height) {
        stateVerifier.throwIfRecycled();
        if (IS_VERBOSE_LOGGABLE) {
            logV("Got onSizeReady in " + LogTime.getElapsedMillis(startTime));
        }
        if (status != Status.WAITING_FOR_SIZE) {
            return;
        }
        status = Status.RUNNING;
        // 计算缩略图的尺寸
        float sizeMultiplier = requestOptions.getSizeMultiplier();
        this.width = maybeApplySizeMultiplier(width, sizeMultiplier);
        this.height = maybeApplySizeMultiplier(height, sizeMultiplier);

        if (IS_VERBOSE_LOGGABLE) {
            logV("finished setup for calling load in " + LogTime.getElapsedMillis(startTime));
        }
        // 真正的加载流程
        loadStatus =
                engine.load(
                        glideContext,
                        model,
                        requestOptions.getSignature(),
                        this.width,
                        this.height,
                        requestOptions.getResourceClass(),
                        transcodeClass,
                        priority,
                        requestOptions.getDiskCacheStrategy(),
                        requestOptions.getTransformations(),
                        requestOptions.isTransformationRequired(),
                        requestOptions.isScaleOnlyOrNoTransform(),
                        requestOptions.getOptions(),
                        requestOptions.isMemoryCacheable(),
                        requestOptions.getUseUnlimitedSourceGeneratorsPool(),
                        requestOptions.getUseAnimationPool(),
                        requestOptions.getOnlyRetrieveFromCache(),
                        this,
                        callbackExecutor);

        ...
    }
```
现在终于来到真正的加载流程啦！！！

### 5. Engine.load（启动加载）
```
    public synchronized <R> LoadStatus load(
            GlideContext glideContext,
            Object model,
            Key signature,
            int width,
            int height,
            Class<?> resourceClass,
            Class<R> transcodeClass,
            Priority priority,
            DiskCacheStrategy diskCacheStrategy,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            boolean isScaleOnlyOrNoTransform,
            Options options,
            boolean isMemoryCacheable,
            boolean useUnlimitedSourceExecutorPool,
            boolean useAnimationPool,
            boolean onlyRetrieveFromCache,
            ResourceCallback cb,
            Executor callbackExecutor) {
        long startTime = VERBOSE_IS_LOGGABLE ? LogTime.getLogTime() : 0;
        // 根据传入的参数, 构建这个请求的 引擎密钥
        EngineKey key = keyFactory.buildKey(model, signature, width, height, transformations,
                resourceClass, transcodeClass, options);
        // 从 内存ActiveResources(内部维护一个HashMap)缓存中查找这个 key 的引擎资源，尝试复用
        EngineResource<?> active = loadFromActiveResources(key, isMemoryCacheable);
        if (active != null) {
            // 若引擎资源 存在, 无需继续请求则直接回调 onResourceReady 处理后续操作
            cb.onResourceReady(active, DataSource.MEMORY_CACHE);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from active resources", startTime, key);
            }
            return null;
        }
        // 尝试从 LruResourceCache 中找寻这个资源，尝试复用，若存在则先缓存到内存ActiveResources中一份
        EngineResource<?> cached = loadFromCache(key, isMemoryCacheable);
        if (cached != null) {
            // 若引擎资源 存在, 无需继续请求则直接回调 onResourceReady 处理后续操作
            cb.onResourceReady(cached, DataSource.MEMORY_CACHE);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from cache", startTime, key);
            }
            return null;
        }
        // 从任务缓存中查找 key 对应的任务
        EngineJob<?> current = jobs.get(key, onlyRetrieveFromCache);
        if (current != null) {
            // 走到这里说明这个任务已经正在执行了, 无需再次构建执行
            current.addCallback(cb, callbackExecutor);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Added to existing load", startTime, key);
            }
            // 返回加载状态即可
            return new LoadStatus(cb, current);
        }

        // 走到这里, 说明是一个新的任务，则构建一个新的引擎任务
        EngineJob<R> engineJob =
                engineJobFactory.build(
                        key,
                        isMemoryCacheable,
                        useUnlimitedSourceExecutorPool,
                        useAnimationPool,
                        onlyRetrieveFromCache);
        // 构建解码任务
        DecodeJob<R> decodeJob =
                decodeJobFactory.build(
                        glideContext,
                        model,
                        key,
                        signature,
                        width,
                        height,
                        resourceClass,
                        transcodeClass,
                        priority,
                        diskCacheStrategy,
                        transformations,
                        isTransformationRequired,
                        isScaleOnlyOrNoTransform,
                        onlyRetrieveFromCache,
                        options,
                        engineJob);
        // 添加到任务缓存中
        jobs.put(key, engineJob);
        // 添加到回调以及将callbackExecutor(into时初始化的主线程调度器)
        engineJob.addCallback(cb, callbackExecutor);
        // 执行任务
        engineJob.start(decodeJob);

        if (VERBOSE_IS_LOGGABLE) {
            logWithTimeAndKey("Started new load", startTime, key);
        }
        return new LoadStatus(cb, engineJob);
    }
```
总结一下Engine.load中都干了些什么？：
1. 构建这个请求的 key
2. 从缓存中查找 key 对应的资源, 若存在直接调用onResourceReady表示资源已准备好
    * 从ActiveResources缓存中查找
    * 从LruResourceCache缓存中查找，若存在，则先缓存到ActiveResources中，然后返回
3. 从任务缓存中查找 key 对应的任务

    若存在则说明无需再次获取资源
4. 若3中不存在缓存任务，则构建新的任务
    * 构建引擎任务 EngineJob
    * 引擎的任务为解码任务 DecodeJob
    * 将任务添加到缓存, 防止多次构建
    * 添加回调以及传入主线程调度器
    * 执行任务
    
先了解几个概念：

    * 活动资源Active Resources 正在显示的资源
    * 内存缓存Memory cache 显示过的资源
    * 资源类型Resources 被解码、转换后的资源
    * 数据来源Data 源文件（未处理过的资源）
    
接下来继续看任务的执行engineJob.start(decodeJob)：
```
    // EngineJob类：
    public synchronized void start(DecodeJob<R> decodeJob) {
        this.decodeJob = decodeJob;
        //这里根据缓存策略，决定使用哪一个Executor，默认情况返回DiskCacheExecutor
        //共有三种执行器，diskcacheExecutor,sourceExecutor,sourceUnlimitedExecutor
        GlideExecutor executor = decodeJob.willDecodeFromCache()
                ? diskCacheExecutor
                : getActiveSourceExecutor();
        // 执行任务
        executor.execute(decodeJob);
    }


    // DecodeJob类：
    @SuppressWarnings("PMD.AvoidRethrowingException")
    @Override
    public void run() {
        GlideTrace.beginSectionFormat("DecodeJob#run(model=%s)", model);
        DataFetcher<?> localFetcher = currentFetcher;
        try {
            // 如果已经取消，则通知加载失败
            if (isCancelled) {
                notifyFailed();
                return;
            }
            // 调用了 runWrapped
            runWrapped();
        } catch (CallbackException e) {
            ......
        }

    }

    private void runWrapped() {
        switch (runReason) {
            // 默认状态为INITISLIZE
            case INITIALIZE:
                // 获取尝试在哪里解码数据的枚举类
                stage = getNextStage(Stage.INITIALIZE);
                // 获取当前任务的DataFetcherGenerator(数据提取器生成器)
                currentGenerator = getNextGenerator();
                // 执行任务
                runGenerators();
                break;
            case SWITCH_TO_SOURCE_SERVICE:
                runGenerators();
                break;
            case DECODE_DATA:
                decodeFromRetrievedData();
                break;
            default:
                throw new IllegalStateException("Unrecognized run reason: " + runReason);
        }
    }

    // 获取任务执行阶段：初始化，读取转换后的缓存，读取原文件（未处理）缓存，远程图片加载，结束状态
    private Stage getNextStage(Stage current) {
        switch (current) {
            // 初始状态
            case INITIALIZE:
                // 若我们配置的缓存策略允许从 资源缓存 中读数据, 则返回 Stage.RESOURCE_CACHE
                return diskCacheStrategy.decodeCachedResource()
                        ? Stage.RESOURCE_CACHE : getNextStage(Stage.RESOURCE_CACHE);
            // 读取转换后的缓存
            case RESOURCE_CACHE:
                // 若我们配置的缓存策略允许从 源数据缓存中读数据, 则返回 Stage.DATA_CACHE
                return diskCacheStrategy.decodeCachedData()
                        ? Stage.DATA_CACHE : getNextStage(Stage.DATA_CACHE);
            // 读取原文件缓存
            case DATA_CACHE:
                // 如果用户选择仅从缓存中检索资源，则从源跳过加载。
                return onlyRetrieveFromCache ? Stage.FINISHED : Stage.SOURCE;
            case SOURCE: // 远程图片加载
            case FINISHED: // 结束状态
                return Stage.FINISHED;
            default:
                throw new IllegalArgumentException("Unrecognized stage: " + current);
        }
    }

    private DataFetcherGenerator getNextGenerator() {
        // 根据 尝试在哪里解码数据的枚举类 当前的类型获取 数据提取器生成器
        switch (stage) {
            case RESOURCE_CACHE:
                // 资源缓存生成器，从换后的缓存读取文件
                return new ResourceCacheGenerator(decodeHelper, this);
            case DATA_CACHE:
                // 数据缓存生成器，从原文件缓存加载
                return new DataCacheGenerator(decodeHelper, this);
            case SOURCE:
                // 源生成器，没有缓存，远程图片资源加载器（比如网络，本地文件）
                return new SourceGenerator(decodeHelper, this);
            case FINISHED:
                return null;
            default:
                throw new IllegalStateException("Unrecognized stage: " + stage);
        }
    }


    private void runGenerators() {
        currentThread = Thread.currentThread();
        startFetchTime = LogTime.getLogTime();
        boolean isStarted = false;
        // 这里Generator.startNext方法中就是加载过程，如果成功就返回true并跳出循环，否则切换Generator继续执行
        while (!isCancelled && currentGenerator != null
                && !(isStarted = currentGenerator.startNext())) {
            // 重新获取执行器
            stage = getNextStage(stage);
            currentGenerator = getNextGenerator();
            // 如果任务执行到去远程加载，且切换任务执行环境
            if (stage == Stage.SOURCE) {
                reschedule();
                return;
            }
        }
        // 此次请求失败，继续
        if ((stage == Stage.FINISHED || isCancelled) && !isStarted) {
            notifyFailed();
        }
    }

    @Override
    public void reschedule() {
        // 更改执行目标为：SOURCE服务。当然也只有在stage == Stage.SOURCE的情况下会被调用。
        runReason = RunReason.SWITCH_TO_SOURCE_SERVICE;
        callback.reschedule(this); // 这里callback正是EngineJob。
    }

//EngineJob类：代码跟进EngineJob类中
    @Override
    public void reschedule(DecodeJob<?> job) {
        // 可以看到，这里获取的SourceExecutor来执行decodeJob。
        //也就巧妙地将此decodeJob任务从cacheExecutor切换到了SourceExecutor，这样分工协作更加高效。
        getActiveSourceExecutor().execute(job);
    }
```
可以看到，这里几个方法构成了Glide的解码流程：尝试从转换过的本地资源加载图片；尝试从没有处理过的原始资源中加载图片；尝试远程加载图片。通过状态的切换来寻找下一个加载器，直到加载到这张图，返回成功，如果找不到，返回失败。

再总结一下：

* 执行Engine的start方法，默认情况下会获取到diskCacheExecutor执行器来执行decodeJob任务；
* decodeJob这个runnable的run方法就会被调用；
* 因为RunReason为INITIALIZE，接着获取stage，默认返回Stage.RESOURCE_CACHE
* 这时通过getNextGenerator就返回了ResourceCacheGenerator加载器
* 接着调用ResourceCacheGenerator的startNext方法，从转换后的缓存中读取已缓存的资源
* 如果资源获取成功，就结束任务并回调结果，反之，切换到DataCacheGenerator，同样的，如果还没命中就切换到SourceGenerator加载器（比如初次加载，没有任何缓存，就会走到这）。
* 切换到SourceGenerator环境，等它结束后，结束任务，回调结果，流程结束。

现在代码已经走到了EngineJob类中的reschedule中，并执行了任务。

#### 加载网络图片——SourceGenerator
```
// SourceGenerator类：
    /**
     * SourceGenerator
     * DataFetcher的简介：Fetcher的意思是抓取，所以该类可以称为数据抓取器
     *  作用：根据不同的数据来源（本地，网络，Asset等）以及读取方式（Stream，ByteBuffer等）来提取并解码数据资源
     *  其实现类如下：
     *      AssetPathFetcher：加载Asset数据
     *      HttpUrlFetcher：加载网络数据
     *      LocalUriFetcher：加载本地数据
     *      其他实现类...
     */
    @Override
    public boolean startNext() {
        //1.判断是否有缓存，如果有，直接加载缓存（这里第一次加载，所以dataToCache为null）
        if (dataToCache != null) {
            Object data = dataToCache;
            dataToCache = null;
            cacheData(data);
        }

        if (sourceCacheGenerator != null && sourceCacheGenerator.startNext()) {
            return true;
        }
        sourceCacheGenerator = null;

        loadData = null;
        boolean started = false;
        //是否有更多的ModelLoader
        while (!started && hasNextModelLoader()) {
            // 从 DecodeHelper 的数据加载集合中, 获取一个数据加载器
            loadData = helper.getLoadData().get(loadDataListIndex++);
            if (loadData != null
                    && (helper.getDiskCacheStrategy().isDataCacheable(loadData.fetcher.getDataSource())
                    || helper.hasLoadPath(loadData.fetcher.getDataClass()))) {
                started = true;
                // 选择合适的LoadData，并使用fetcher来抓取数据
                loadData.fetcher.loadData(helper.getPriority(), this);
            }
        }
        return started;
    }
```
看到这里是否有些蒙，ModelLoader是什么？

现在我们来回想一下，我们上面Glide初始化时，是否有一段类似于这样的代码(很长很长的)：
```
    Glide(...) {
        ...
        registry
        ...
        .append(String.class, InputStream.class, new StringLoader.StreamFactory())

        .append(Uri.class, InputStream.class, new HttpUriLoader.Factory())
        ....
        .append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory())
        ...
        ImageViewTargetFactory imageViewTargetFactory = new ImageViewTargetFactory();
        glideContext =
                new GlideContext(
                        context,
                        arrayPool,
                        registry,
                        imageViewTargetFactory,
                        defaultRequestOptions,
                        defaultTransitionOptions,
                        defaultRequestListeners,
                        engine,
                        isLoggingRequestOriginsEnabled,
                        logLevel);
    }
```
而这段代码中append的DataFetcher，都是很请求网络相关的。我们再来看一下registry这个注册类：
```
 public class Registry {
    //各种功能类注册器。加载、转换、解码、加密等。
    private final ModelLoaderRegistry modelLoaderRegistry;
    private final EncoderRegistry encoderRegistry;
    private final ResourceDecoderRegistry decoderRegistry;
    private final ResourceEncoderRegistry resourceEncoderRegistry;
    private final DataRewinderRegistry dataRewinderRegistry;
    private final TranscoderRegistry transcoderRegistry;
    private final ImageHeaderParserRegistry imageHeaderParserRegistry;
  
    ...

    //modelLoader注册
    public <Model, Data> Registry append(Class<Model> modelClass, Class<Data> dataClass,
                                         ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.append(modelClass, dataClass, factory);
        return this;
    }
  
    ...
}

    //继续跟进代码,ModelLoaderRegistry类中
    public synchronized <Model, Data> void append(Class<Model> modelClass
        , Class<Data> dataClass,ModelLoaderFactory<Model, Data> factory) {
        multiModelLoaderFactory.append(modelClass, dataClass, factory);
        cache.clear();
    }

    //最后进入MultiModelLoaderFactory类中的add方法
    private <Model, Data> void add(Class<Model> modelClass, Class<Data> dataClass,
                                   ModelLoaderFactory<Model, Data> factory, boolean append) {
        Entry<Model, Data> entry = new Entry<>(modelClass, dataClass, factory);
        //entries是一个list。所以，到这里就知道注册的LoaderFactory被缓存到了列表中，以便后面取用。
        entries.add(append ? entries.size() : 0, entry);
    }
}
```
通过以上代码，知道了ModelLoaderFactory在Glide初始化时注册到了一个列表中，以备后面使用。在分析DecodeJob的代码里时，我们使用SourceGenerator加载远程图片，并分析到了loadData.fetcher.loadData(helper.getPriority(), this);是真正加载数据的地方。而获取loadData的地方为loadData = helper.getLoadData().get(loadDataListIndex++); 那helper是哪来的呢？helper实际上是DecodeJob的成员属性，并且直接在声明时直接初始化啦，那么我们接着看helper.getLoadData()的实现：
```
//DecodeHelper  
    List<LoadData<?>> getLoadData() {
        if (!isLoadDataSet) {
            isLoadDataSet = true;
            loadData.clear();
            //根据model类型，通过glideContext中的registry注册器获取ModelLoader列表
            List<ModelLoader<Object, ?>> modelLoaders = glideContext.getRegistry().getModelLoaders(model);
            //noinspection ForLoopReplaceableByForEach to improve perf
            for (int i = 0, size = modelLoaders.size(); i < size; i++) {
                ModelLoader<Object, ?> modelLoader = modelLoaders.get(i);
                // 构建LoadData
                LoadData<?> current =
                        modelLoader.buildLoadData(model, width, height, options);
                // 若current不为空，则添加到loadData集合中
                if (current != null) {
                    loadData.add(current);
                }
            }
        }
        return loadData;
    }
```
先说两个概念：
* ModelLoader：是通过ModelLoaderRegistry进行管理 ModelLoader需要接受两个泛型类型 <Model,Data> ，ModelLoader本身是一个工厂接口，主要工作是将复杂数据模型转通过DataFetcher转换成需要的Data， LoadData 是ModelLoader的内部类，是对DataFetcher和Key的封装实体。
* LoadData：来源于 Registry 注册器，而注册器是在Glide初始化时构建的。作用：获取合适的数据抓取器(DataFetcher)来获取数据。

可以看出getLoadData()返回一个LoadData的集合。而跟网络有关的数据抓取器：
```
.append(String.class, InputStream.class, new StringLoader.StreamFactory()
.append(Uri.class, InputStream.class, new HttpUriLoader.Factory())
.append(GlideUrl.class, InputStream.class, new HttpGlideUrlLoader.Factory())
```
很明显我们基本传入的都是String类型的url，因此我们真实使用的是StringLoader：
```
public class StringLoader<Data> implements ModelLoader<String, Data> {
    private final ModelLoader<Uri, Data> uriLoader;

    // Public API.
    @SuppressWarnings("WeakerAccess")
    public StringLoader(ModelLoader<Uri, Data> uriLoader) {
        this.uriLoader = uriLoader;
    }

    @Override
    public LoadData<Data> buildLoadData(@NonNull String model, int width, int height,
                                        @NonNull Options options) {
        Uri uri = parseUri(model);
        if (uri == null || !uriLoader.handles(uri)) {
            return null;
        }
        return uriLoader.buildLoadData(uri, width, height, options);
    }

    ...

    /**
     * Factory for loading {@link InputStream}s from Strings.
     */
    public static class StreamFactory implements ModelLoaderFactory<String, InputStream> {
        @NonNull
        @Override
        public ModelLoader<String, InputStream> build(
                @NonNull MultiModelLoaderFactory multiFactory) {
            //关键在这儿
            return new StringLoader<>(multiFactory.build(Uri.class, InputStream.class));
        }

        @Override
        public void teardown() {
            // Do nothing.
        }
    }
}
```
明显的能看出StringLoader.StreamFactory创建了StringLoader(实现ModelLoader接口)，而在getLoadData()获取列表时，调用的ModelLoader的buildLoadData方法来构建LoadData，而StringLoader的buildLoadData方法将通过Uri.class和InputStream.class创建一个ModelLoader给StringLoader，所以StringLoader的加载功能转移了。而且根据注册关系知道转移到了HttpUriLoader中。而HttpUriLoader中也类似于StringLoader一样，将加载功能转移到了HttpGlideUrlLoader中，而HttpGlideUrlLoader.buildLoadData对应的LoadData的Fetcher就是HttpUrlFetcher。
```
// HttpUrlFetcher类：
    /**
     * HttpUrlFetcher的简介：网络数据抓取器，通俗的来讲就是去服务器上下载图片，支持地址重定向（最多5次）
     */
    @Override
    public void loadData(@NonNull Priority priority,
                         @NonNull DataCallback<? super InputStream> callback) {
        long startTime = LogTime.getLogTime();
        try {
            // 使用重定向加载数据，注意此处返回的是InputStream数据流
            InputStream result = loadDataWithRedirects(glideUrl.toURL(), 0, null,
                    glideUrl.getHeaders());
            // 加载完成，回调数据准备完毕
            callback.onDataReady(result);
        } catch (IOException e) {
            ...
        }
    }


    private InputStream loadDataWithRedirects(URL url, int redirects
        , URL lastUrl,Map<String,String> headers) throws IOException {
        //重定向次数过多
        if (redirects >= MAXIMUM_REDIRECTS) {
            throw new HttpException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
        } else {
            //通过URL的equals方法来比较会导致网络IO开销，一般会有问题
            //可以参考 http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
            try {
                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
                    throw new HttpException("In re-direct loop");

                }
            } catch (URISyntaxException e) {
                // Do nothing, this is best effort.
            }
        }

        //下面开始，终于看到了可爱的HttpUrlConnection下载图片
        urlConnection = connectionFactory.build(url);
        for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
            urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
        }
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setReadTimeout(timeout);
        urlConnection.setUseCaches(false);
        urlConnection.setDoInput(true);

        urlConnection.setInstanceFollowRedirects(false);
        urlConnection.connect();
        stream = urlConnection.getInputStream();
        if (isCancelled) {
            return null;
        }
        final int statusCode = urlConnection.getResponseCode();
        if (isHttpOk(statusCode)) {
            return getStreamForSuccessfulRequest(urlConnection);
        } else if (isHttpRedirect(statusCode)) {
            String redirectUrlString = urlConnection.getHeaderField("Location");
            if (TextUtils.isEmpty(redirectUrlString)) {
                throw new HttpException("Received empty or null redirect url");
            }
            URL redirectUrl = new URL(url, redirectUrlString);
            // Closing the stream specifically is required to avoid leaking ResponseBodys in 
            // addition
            // to disconnecting the url connection below. See #2352.
            cleanup();
            return loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
        } else if (statusCode == INVALID_STATUS_CODE) {
            throw new HttpException(statusCode);
        } else {
            throw new HttpException(urlConnection.getResponseMessage(), statusCode);
        }
    }
```
看到这，终于找到了网络通讯的代码，就是通过HttpUrlConnection来获取数据流InputStream并返回。当然也可以自定义使用OkHttp。

### 6. 数据源的处理以及显示
我们从HttpUrlFetcher类中的loadData方法中可以看到，当数据加载完成后会调用callback.onDataReady(result)，那callback是谁呢？我们是从SourceGenerator中开始加载的网络数据，那callback理所应当是SourceGenerator，因此我们看一下SourceGenerator的onDataReady(result)方法：
```
    @Override
    public void onDataReady(Object data) {
        DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
        // 若数据不为空，或者 如果此请求应缓存原始未修改数据，则返回true。
        if (data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource())) {
            // 如果这段数据是缓存
            dataToCache = data;
            // 重新执行此次加载
            cb.reschedule();
        } else {
            // 不是缓存，则走这一步
            cb.onDataFetcherReady(loadData.sourceKey, data, loadData.fetcher,
                    loadData.fetcher.getDataSource(), originalKey);
        }
    }
```
这里的cb是谁呢？我们是从DecodeJob初始化的SourceGenerator，因此cb就是我们的DecodeJob啦，由于我们走的不是缓存而是网络，因此我们看一下DecodeJob的onDataFetcherReady方法：
```
// DecodeJob类：
    @Override
    public void onDataFetcherReady(Key sourceKey, Object data, DataFetcher<?> fetcher,
                                   DataSource dataSource, Key attemptedKey) {
        this.currentSourceKey = sourceKey; // 保存数据源的 key
        this.currentData = data; // 保存数据源实体
        this.currentFetcher = fetcher; // 保存数据源的获取器
        this.currentDataSource = dataSource; // 数据来源: url 为 REMOTE 类型的枚举, 表示从远程获取
        this.currentAttemptingKey = attemptedKey;
        if (Thread.currentThread() != currentThread) {
            // 若请求时线程和解析时线程不一致
            runReason = RunReason.DECODE_DATA;
            // 重新执行此次加载
            callback.reschedule(this);
        } else {
            GlideTrace.beginSection("DecodeJob.decodeFromRetrievedData");
            try {
                // 调用 decodeFromRetrievedData 解析获取的数据
                decodeFromRetrievedData();
            } finally {
                GlideTrace.endSection();
            }
        }
    }
    
    private void decodeFromRetrievedData() {
        ...
        Resource<R> resource = null;
        try {
            // 调用decodeFromData从数据中解码出资源resource
            resource = decodeFromData(currentFetcher, currentData, currentDataSource);
        } catch (GlideException e) {
            e.setLoggingDetails(currentAttemptingKey, currentDataSource);
            throwables.add(e);
        }
        
        if (resource != null) {
            // 通知编码完成
            notifyEncodeAndRelease(resource, currentDataSource);
        } else {
            // resource为null，则重新加载
            runGenerators();
        }
    }
    
    private <Data> Resource<R> decodeFromData(DataFetcher<?> fetcher, Data data,
                                              DataSource dataSource) throws GlideException {
        try {
            if (data == null) {
                return null;
            }
            long startTime = LogTime.getLogTime();
            // 从Fetcher中解码
            Resource<R> result = decodeFromFetcher(data, dataSource);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                logWithTimeAndKey("Decoded result " + result, startTime);
            }
            return result;
        } finally {
            // 关闭流stream和连接urlConnection。
            fetcher.cleanup();
        }
    }

    @SuppressWarnings("unchecked")
    private <Data> Resource<R> decodeFromFetcher(Data data, DataSource dataSource)
            throws GlideException {
        // 获取当前数据类的解析器 LoadPath
        LoadPath<Data, ?, R> path = decodeHelper.getLoadPath((Class<Data>) data.getClass());
        // 通过解析器来解析来解析数据
        return runLoadPath(data, dataSource, path);
    }

    private <Data, ResourceType> Resource<R> runLoadPath(Data data, DataSource dataSource,
                                                         LoadPath<Data, ResourceType, R> path) throws GlideException {
        // 通过硬件配置获取选项
        Options options = getOptionsWithHardwareConfig(dataSource);
        // 根据数据类型获取一个数据重造器, 获取的数据为 InputStream, 因此它是一个 InputStreamRewinder 的实例
        "注：DataRewinder作用为处理将包装的数据流重置到原始位置并返回。"
        DataRewinder<Data> rewinder = glideContext.getRegistry().getRewinder(data);
        try {
            // // 将解析资源的任务转移到了 LoadPath.load 方法中
            return path.load(
                    rewinder, options, width, height, new DecodeCallback<ResourceType>(dataSource));
        } finally {
            rewinder.cleanup();
        }
    }
```
这段代码中可以看到方法由onDataFetcherReady--> decodeFromRetrievedData--> decodeFromData--> decodeFromFetcher--> runLoadPath--> path.load进入LoadPath.load 方法中去进行数据解析。而这个过程中构建了一个LoadPath, 然后创建了一个InputStreamRewinder 类型的DataRewinder。

我们先来看一下LoadPath怎么来的？
```
    <Data> LoadPath<Data, ?, Transcode> getLoadPath(Class<Data> dataClass) {
        return glideContext.getRegistry().getLoadPath(dataClass, resourceClass, transcodeClass);
    }

    @Nullable
    public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> getLoadPath(
            @NonNull Class<Data> dataClass, @NonNull Class<TResource> resourceClass,
            @NonNull Class<Transcode> transcodeClass) {
        LoadPath<Data, TResource, Transcode> result =
                loadPathCache.get(dataClass, resourceClass, transcodeClass);
        if (loadPathCache.isEmptyLoadPath(result)) {
            return null;
        } else if (result == null) { // 如果为空
            // 获取DecodePath
            "注：DecodePath类似于LoadData 来源于 Registry 注册器，解析类"
            List<DecodePath<Data, TResource, Transcode>> decodePaths =
                    getDecodePaths(dataClass, resourceClass, transcodeClass);
            if (decodePaths.isEmpty()) {
                result = null;
            } else {
                // 先创建一个LoadPaht
                result =
                        new LoadPath<>(
                                dataClass, resourceClass, transcodeClass, decodePaths,
                                throwableListPool);
            }
            // put进缓存
            loadPathCache.put(dataClass, resourceClass, transcodeClass, result);
        }
        return result;
    }
```
先从缓存中取LoadPath，如果为null，先获取一个DecodePath集合，我们创建Glide的时候在registry中append的各种解析方式，getDecodePaths就是根据我们传入的参数拿到对应的解析类。在然后创建出LoadPath，传入刚创建的DecodePath，并放入到缓存中。 

接下来我们进入LoadPath.load：
```
// LoadPath类：
    public Resource<Transcode> load(DataRewinder<Data> rewinder, @NonNull Options options,
            int width, int height, DecodePath.DecodeCallback<ResourceType> decodeCallback) throws GlideException {
        List<Throwable> throwables = Preconditions.checkNotNull(listPool.acquire());
        try {
            return loadWithExceptionList(rewinder, options, width, height, decodeCallback,
                    throwables);
        } finally {
            listPool.release(throwables);
        }
    }

    private Resource<Transcode> loadWithExceptionList(DataRewinder<Data> rewinder,
            @NonNull Options options,int width, int height,DecodePath.DecodeCallback<ResourceType> decodeCallback, List<Throwable> exceptions) throws GlideException {
        Resource<Transcode> result = null;
        // 尝试遍历所有decodePath来解码
        for (int i = 0, size = decodePaths.size(); i < size; i++) {
            DecodePath<Data, ResourceType, Transcode> path = decodePaths.get(i);
            try {
                // 调用path.decode进行解码
                result = path.decode(rewinder, width, height, options, decodeCallback);
            } catch (GlideException e) {
                exceptions.add(e);
            }
            if (result != null) {
                break;
            }
        }

        if (result == null) {
            throw new GlideException(failureMessage, new ArrayList<>(exceptions));
        }
        // 解码完成则跳出循环
        return result;
    }

// DecodePath类：
    public Resource<Transcode> decode(DataRewinder<DataType> rewinder, int width
        , int height,@NonNull Options options,DecodeCallback<ResourceType> callback) throws GlideException {
        // 调用 decodeResource 将源数据(InputStream)解析成中间资源(Bitmap)
        Resource<ResourceType> decoded = decodeResource(rewinder, width, height, options);
        // 调用 DecodeCallback.onResourceDecoded 处理中间资源(处理成Drawable)
        Resource<ResourceType> transformed = callback.onResourceDecoded(decoded);
        // 调用 ResourceTranscoder.transcode 将中间资源转为目标资源(对应的Target)
        return transcoder.transcode(transformed, options);
    }
    
    @NonNull
    private Resource<ResourceType> decodeResource(DataRewinder<DataType> rewinder
        , int width,int height, @NonNull Options options) throws GlideException {
        List<Throwable> exceptions = Preconditions.checkNotNull(listPool.acquire());
        try {
            // 调用decodeResourceWithList
            return decodeResourceWithList(rewinder, width, height, options, exceptions);
        } finally {
            listPool.release(exceptions);
        }
    }

    @NonNull
    private Resource<ResourceType> decodeResourceWithList(DataRewinder<DataType> rewinder
        ,int width,int height, @NonNull Options options,List<Throwable> exceptions) throws GlideException {
        Resource<ResourceType> result = null;
        // 遍历decoder集合，获取到ResourceDecoder解码器（包括BitmapDrawableDecoder，GifFrameResourceDecoder
        // ，FileDecoder等），然后通过rewinder.rewindAndGet()获取到InputStream数据流，然后调用decoder.decode方法
        for (int i = 0, size = decoders.size(); i < size; i++) {
            ResourceDecoder<DataType, ResourceType> decoder = decoders.get(i);
            try {
                DataType data = rewinder.rewindAndGet();
                if (decoder.handles(data, options)) {
                    data = rewinder.rewindAndGet();
                    // 调用 ResourceDecoder.decode 解析源数据InputStream
                    result = decoder.decode(data, width, height, options);
                }
            } catch (IOException | RuntimeException | OutOfMemoryError e) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Failed to decode data for " + decoder, e);
                }
                exceptions.add(e);
            }

            if (result != null) {
                break;
            }
        }

        if (result == null) {
            throw new GlideException(failureMessage, new ArrayList<>(exceptions));
        }
        return result;
    }
```
通过上面代码了解到，数据解析的任务最后是通过DecodePath来执行的, 它内部有三个操作：
* 调用 decodeResource 将源数据InputStream解析成资源(Bitmap)
* 调用 DecodeCallback.onResourceDecoded 处理资源(CenterCrop、FitCenter等)
* 调用 ResourceTranscoder.transcode 将资源转为目标资源(BitmapDrawable)

我们分别来看一下这三个操作：

由于本次流程的源数据为 InputStream 因此它的解析器为 StreamBitmapDecoder：
```
@Override
    public Resource<Bitmap> decode(@NonNull InputStream source, int width, int height,
                                   @NonNull Options options)
            throws IOException {

        final RecyclableBufferedInputStream bufferedStream;
        final boolean ownsBufferedStream;
        if (source instanceof RecyclableBufferedInputStream) {
            bufferedStream = (RecyclableBufferedInputStream) source;
            ownsBufferedStream = false;
        } else {
            bufferedStream = new RecyclableBufferedInputStream(source, byteArrayPool);
            ownsBufferedStream = true;
        }

        // 用于检索读取时抛出的异常。
        ExceptionCatchingInputStream exceptionStream =
                ExceptionCatchingInputStream.obtain(bufferedStream);

        // 用于读取数据。 确保我们可以在读取图像标题后始终重置，这样即使标头解码失败或溢出，我们的读取缓冲区，仍然可以尝试解码整个图像。
        MarkEnforcingInputStream invalidatingStream = new MarkEnforcingInputStream(exceptionStream);
        UntrustedCallbacks callbacks = new UntrustedCallbacks(bufferedStream, exceptionStream);
        try {
            // 主要看这里：根据请求配置的数据, 对数据流进行采样压缩, 获取到一个 Resource<Bitmap>
            return downsampler.decode(invalidatingStream, width, height, options, callbacks);
        } finally {
            exceptionStream.release();
            if (ownsBufferedStream) {
                bufferedStream.release();
            }
        }
    }
```
可以看到它内部通过 Downsampler.decode 方法对数据流进行采样压缩(采样策略就是我们在构建 Request时传入的), 来获取这个流的 Bitmap。

我们看看获取到了 Resource 之后, 如何处理这个资源：

从源码中可以看出资源处理调用的callback.onResourceDecoded(decoded)处理资源，而这个callback实际上就是我们的DecodeJob.DecodeCallback这个回调类：
```
// DecodeJob.DecodeCallback类：
    @NonNull
    @Override
    public Resource<Z> onResourceDecoded(@NonNull Resource<Z> decoded) {
        // 调用了外部类的 onResourceDecoded 方法
        return DecodeJob.this.onResourceDecoded(dataSource, decoded);
    }

// DecodeJob类：
    @Synthetic
    @NonNull
    <Z> Resource<Z> onResourceDecoded(DataSource dataSource,
                                      @NonNull Resource<Z> decoded) {
        // 获取数据资源的类型
        @SuppressWarnings("unchecked")
        Class<Z> resourceSubClass = (Class<Z>) decoded.get().getClass();
        Transformation<Z> appliedTransformation = null;
        Resource<Z> transformed = decoded;
        // 若非从资源磁盘缓存中获取的数据源, 则对资源进行 transformation 操作
        if (dataSource != DataSource.RESOURCE_DISK_CACHE) {
            // 根据类型获取转换器(例如：CenterCrop、FitCenter等)
            appliedTransformation = decodeHelper.getTransformation(resourceSubClass);
            transformed = appliedTransformation.transform(glideContext, decoded, width, height);
        }
        // TODO: Make this the responsibility of the Transformation.
        if (!decoded.equals(transformed)) {
            decoded.recycle();
        }
        // 构建数据编码的策略
        final EncodeStrategy encodeStrategy;
        final ResourceEncoder<Z> encoder;
        if (decodeHelper.isResourceEncoderAvailable(transformed)) {
            encoder = decodeHelper.getResultEncoder(transformed);
            encodeStrategy = encoder.getEncodeStrategy(options);
        } else {
            encoder = null;
            encodeStrategy = EncodeStrategy.NONE;
        }
        // 根据编码策略, 构建缓存的 key
        Resource<Z> result = transformed;
        boolean isFromAlternateCacheKey = !decodeHelper.isSourceKey(currentSourceKey);
        if (diskCacheStrategy.isResourceCacheable(isFromAlternateCacheKey, dataSource,
                encodeStrategy)) {
            if (encoder == null) {
                throw new Registry.NoResultEncoderAvailableException(transformed.get().getClass());
            }
            final Key key;
            switch (encodeStrategy) {
                case SOURCE:
                    // 源数据的 key
                    key = new DataCacheKey(currentSourceKey, signature);
                    break;
                case TRANSFORMED:
                    key = new ResourceCacheKey(
                                    decodeHelper.getArrayPool(),
                                    currentSourceKey,
                                    signature,
                                    width,
                                    height,
                                    appliedTransformation,
                                    resourceSubClass,
                                    options);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown strategy: " + encodeStrategy);
            }
            // 初始化编码管理者, 用于提交内存缓存
            LockedResource<Z> lockedResult = LockedResource.obtain(transformed);
            deferredEncodeManager.init(key, encoder, lockedResult);
            result = lockedResult;
        }
        // 返回 transform 之后的 bitmap
        return result;
    }
```
onResourceDecoded方法中，其主要的逻辑是根据我们设置的参数进行变化，也就是说，如果我们使用了 centerCrop 等参数，那么这里将会对其进行处理。这里的 Transformation 是一个接口，它的一系列的实现都是对应于 scaleType 等参数的，比如CenterCrop、FitCenter、CenterInside等都是实现了Transformation接口的缩放转换器。

由于我们的目标格式为Drawable, 因此它的转换器为 BitmapDrawableTranscoder，而ResourceTranscoder.transcode这三步实际上使用 BitmapDrawableTranscoder 的 transcode() 方法返回 Resouces<BitmapDrawable>。

这样，当这三个方法也执行完毕，我们的岔路口方法就分析完了。然后就是不断向上 return 进行返回。所以，我们又回到了 DecodeJob 的 decodeFromRetrievedData() 方法如下：
```
    private void decodeFromRetrievedData() {
        ...
        Resource<R> resource = null;
        try {
            // 调用decodeFromData从数据中解码出资源resource
            resource = decodeFromData(currentFetcher, currentData, currentDataSource);
        } catch (GlideException e) {
            e.setLoggingDetails(currentAttemptingKey, currentDataSource);
            throwables.add(e);
        }
        
        if (resource != null) {
            // 通知编码完成
            notifyEncodeAndRelease(resource, currentDataSource);
        } else {
            // resource为null，则重新加载
            runGenerators();
        }
    }
```
此时decodeFromData方法完全走完，接下来就是notifyEncodeAndRelease：
```
    private void notifyEncodeAndRelease(Resource<R> resource, DataSource dataSource) {
        ......
        // 通知已准备好
        notifyComplete(result, dataSource);
        ......
        try {
            // 2. 将数据缓存到磁盘
            if (deferredEncodeManager.hasResourceToEncode()) {
                deferredEncodeManager.encode(diskCacheProvider, options);
            }
        } finally {
        ...
        }
    }

    private Callback<R> callback;

    private void notifyComplete(Resource<R> resource, DataSource dataSource) {
        ......
        // 从DecodeJob的构建中, 我们知道这个Callback是EngineJob
        callback.onResourceReady(resource, dataSource);
    }
```
此方法分两步：1.通知资源已准备好 2. 将数据缓存到磁盘。我们主要看一下notifyComplete方法中的callback.onResourceReady：
```
// EngineJob类：
    @Override
    public void onResourceReady(Resource<R> resource, DataSource dataSource) {
        synchronized (this) {
            this.resource = resource;
            this.dataSource = dataSource;
        }
        notifyCallbacksOfResult();
    }

    void notifyCallbacksOfResult() {
        ResourceCallbacksAndExecutors copy;
        Key localKey;
        EngineResource<?> localResource;
        synchronized (this) {
            ......
            engineResource = engineResourceFactory.build(resource, isCacheable);
            // 在下面我们的回调持续时间保持资源，所以我们不会在通知中间回收它是否由其中一个回调同步释放。
            // 在锁定下获取它，以便在我们调用回调之前，在下一个锁定部分下面执行的任何新添加的回调都无法回收资源。
            hasResource = true;
            copy = cbs.copy();
            incrementPendingCallbacks(copy.size() + 1);

            localKey = key;
            localResource = engineResource;
        }
        // 1. 通知上层 Engine 任务完成了
        listener.onEngineJobComplete(this, localKey, localResource);
        // 2. 回调给 ImageViewTarget 展示资源
        for (final ResourceCallbackAndExecutor entry : copy) {
            entry.executor.execute(new CallResourceReady(entry.cb));
        }
    }

// Engine类：
    @Override
    public synchronized void onEngineJobComplete(
            EngineJob<?> engineJob, Key key, EngineResource<?> resource) {
        if (resource != null) {
            resource.setResourceListener(key, this);
            // 将加载好的资源添加到内存缓存
            if (resource.isCacheable()) {
                activeResources.activate(key, resource);
            }
        }

        jobs.removeIfCurrent(key, engineJob);
    }

private class CallResourceReady implements Runnable {

    private final ResourceCallback cb;

    CallResourceReady(ResourceCallback cb) {
        this.cb = cb;
    }

    @Override
    public void run() {
        synchronized (EngineJob.this) {
            if (cbs.contains(cb)) {
                engineResource.acquire();
                // 呼叫资源就绪回拨
                callCallbackOnResourceReady(cb);
                removeCallback(cb);
            }
            decrementPendingCallbacks();
        }
    }
}

// EngineJob类：
    @Synthetic
    synchronized void callCallbackOnResourceReady(ResourceCallback cb) {
        try {
            // 回调SingleRequest中的onResourceReady方法
            cb.onResourceReady(engineResource, dataSource);
        } catch (Throwable t) {
            throw new CallbackException(t);
        }
    }
// SingleRequest类：
    @Override
    public synchronized void onResourceReady(Resource<?> resource, DataSource dataSource) {
        ......
        // 资源就绪
        onResourceReady((Resource<R>) resource, (R) received, dataSource);
    }

    private synchronized void onResourceReady(Resource<R> resource, R result, DataSource dataSource) {
        ...

        isCallingCallbacks = true;
        try {
            boolean anyListenerHandledUpdatingTarget = false;
            if (requestListeners != null) {
                for (RequestListener<R> listener : requestListeners) {
                    anyListenerHandledUpdatingTarget |=
                            listener.onResourceReady(result, model, target, dataSource, isFirstResource);
                }
            }
            anyListenerHandledUpdatingTarget |=
                    targetListener != null
                            && targetListener.onResourceReady(result, model, target, dataSource, isFirstResource);

            if (!anyListenerHandledUpdatingTarget) {
                Transition<? super R> animation =
                        animationFactory.build(dataSource, isFirstResource);
                // 注意这一步
                target.onResourceReady(result, animation);
            }
        } finally {
            isCallingCallbacks = false;
        }

        notifyLoadSuccess();
    }
```
这段代码有点多有点乱，我一步一步说：
1. EngineJob.onResourceReady方法调用了本类中的notifyCallbacksOfResult继续通知回调和结果
2. notifyCallbacksOfResult方法中做了两件事：1. 通知上层 Engine 任务完成了 2. 回调给 ImageViewTarget 展示资源。
3. 第2步中的2.1 主要调用Engine类的onEngineJobComplete方法讲加载好的资源添加到内存缓存中，此时磁盘和内存缓存都添加完毕
4. 第2步中的2.2 回调ImageViewTarget 展示资源，则是先循环遍历执行EngineJob类中的CallResourceReady(Runnable类型)的run方法
5. 第4步的CallResourceReady(Runnable类型)的run方法中继续执行callCallbackOnResourceReady方法，从名字“呼叫资源就绪回拨”就可以看出，该显示资源啦。
6. 接第5步，EngineJob类的callCallbackOnResourceReady方法中，回调了SingleRequest类的onResourceReady方法
7. SingleRequest类的onResourceReady方法中又调用了其重载方法onResourceReady
8. 最终终于在第7步的重载方法onResourceReady中看到了target.onResourceReady(result, animation)这一步。
9. 第8步中的target实际上就是ImageViewTarget。具体ImageViewTarget调用onResourceReady显示资源可参考下一节【Glide之Target】。

至此，终于Glide一次完整的请求显示已经分析完成，不过这不过是Glide的最基础的流程，而Glide 支持Gif, 视频加载，二进制，Assets等操作, 可想而知其内部的处理了多少逻辑代码,如此复杂的流程, 嵌套了如此之多的回调，想想都很可怕，最后的解析资源部分大部分属于借鉴，主要其内部实现实在是复杂。

## Glide之Target
* 什么是Target？
* Target都有哪些实现类？
* Target作用是什么？

在说上面三个问题之前，我们先来思考一下Glide对ImageView都有哪些行为？
    
    1. 获取ImageView的大小
    2. 根据生命周期调度ImageView是否重新加载、取消加载图片
    3. ImageView的动画效果
    4. ImageView加载过程中显示的默认图片、加载失败时显示的图片、加载成功时显示的是Bitmap还是Drawable以及动效效果
根据设计模式里的单一职责原则，Request只处理图片加载的逻辑；而Target则是处理ImageView相关的相关行为的接口，这就回答了第一个问题。

我们来看下Target的主要相关的类图关系：

![](https://user-gold-cdn.xitu.io/2019/9/8/16d0f0e0f9aeb26c?w=572&h=793&f=jpeg&s=96424)
第二个问题也在类图中啦。不过我们来简单说一下这几个主要类的功能：

* BaseTarget：它是一个抽象类，值定义了一个Request成员变量，用于保存与之关联的图片加载请求Request，方便在ImageView绑定到window时去加载图片或者从window卸载时取消图片加载。
* ViewTarget：主要做两件事：a、获取ImageView的大小 b、监听与Window的绑定关系
```
a、获取一个View的大小无非是getWidth()、layoutParam.width，而当View还没绘制时是拿不到大小的，
    那么此时通过Activity的onWindowFocusChanged或者ViewTreeObserver来监听View的绘制完成时期
    再调用getWidth就可以拿到大小了。Glide的获取是不太可能通过onWindowFocusChanged的了，
    剩下就只剩下ViewTreeObserver了，因此Glide就是通过ViewTreeObserver来获取的。

// ViewTarget类：
    public void getSize(@NonNull SizeReadyCallback cb) {
        //调用成员遍历sizeDeterminer的getSize()方法
        sizeDeterminer.getSize(cb);
    }

// ViewTarget.SizeDeterminer类：
    void getSize(@NonNull SizeReadyCallback cb) {
        //获取当前view的宽度
        int currentWidth = getTargetWidth();
        //获取当前view的高度
        int currentHeight = getTargetHeight();
        if (isViewStateAndSizeValid(currentWidth, currentHeight)) {
            //如果View的大小大于0
            //回调告知view的大小
            cb.onSizeReady(currentWidth, currentHeight);
            return;
        }

        if (!cbs.contains(cb)) {
            //添加大小观察者
            cbs.add(cb);
        }
        if (layoutListener == null) {
            //获取ViwTreeObserver
            ViewTreeObserver observer = view.getViewTreeObserver();
            layoutListener = new SizeDeterminerLayoutListener(this);
            //监听View的preDraw行为
            observer.addOnPreDrawListener(layoutListener);
        }
    }

    //获取宽度
    private int getTargetWidth() {
        int horizontalPadding = view.getPaddingLeft() + view.getPaddingRight();
        LayoutParams layoutParams = view.getLayoutParams();
        int layoutParamSize = layoutParams != null ? layoutParams.width : PENDING_SIZE;
        return getTargetDimen(view.getWidth(), layoutParamSize, horizontalPadding);
    }

    //判断宽高是否大于0
    private boolean isViewStateAndSizeValid(int width, int height) {
        return isDimensionValid(width) && isDimensionValid(height);
    }

    //判断指定的大小是否大于0或者==Integer.MAX_VALUE
    private boolean isDimensionValid(int size) {
        return size > 0 || size == SIZE_ORIGINAL;
    }

// ViewTarget.SizeDeterminerLayoutListener类：
    public boolean onPreDraw() {
        SizeDeterminer sizeDeterminer = sizeDeterminerRef.get();
        if (sizeDeterminer != null) {
            sizeDeterminer.checkCurrentDimens();
        }
        return true;
    }
// ViewTarget.SizeDeterminer类：
    void checkCurrentDimens() {
        if (cbs.isEmpty()) {
            return;
        }
        //获取宽度
        int currentWidth = getTargetWidth();
        //获取高度
        int currentHeight = getTargetHeight();
        if (!isViewStateAndSizeValid(currentWidth, currentHeight)) {
            //如果宽高小于0，表示view尚未测量好
            return;
        }
        //回调通知监听则view的大小已经测量好
        notifyCbs(currentWidth, currentHeight);
        //移除监听者
        clearCallbacksAndListener();
    }

  a 总结：先判断当前View的大小是否大于0，如果大于0就直接回调onSizeReady告知View大小已知；
    否则通过ViewTreeObserver监听View的onPreDraw行为来获取View的大小并告知监听者view的大小已经测量好。
    
b、监听与Window的绑定关系，通过监听view与window的绑定关系，进而调度图片加载发起加载请求或者取消加载请求。
    // ViewTarget类：
    public final ViewTarget<T, Z> clearOnDetach() {
        if (attachStateListener != null) {
            return this;
        }
        //创建绑定状态监听者
        attachStateListener = new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                //绑定到window
                resumeMyRequest();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                //从window解绑
                pauseMyRequest();
            }
        };
        maybeAddAttachStateListener();
        return this;
    }

    //设置view绑定window状态的监听者
    private void maybeAddAttachStateListener() {
        if (attachStateListener == null || isAttachStateListenerAdded) {
            return;
        }
        //添加绑定状态监听者
        view.addOnAttachStateChangeListener(attachStateListener);
        isAttachStateListenerAdded = true;
    }

    @Synthetic
    void resumeMyRequest() {
        //绑定window时
        //获取图片加载对象request
        Request request = getRequest();
        if (request != null && request.isCleared()) {
            //开始请求加载
            request.begin();
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Synthetic
    void pauseMyRequest() {
        //从window解绑时
        //获取图片加载对象request
        Request request = getRequest();
        if (request != null) {
            isClearedByUs = true;
            //取消图片加载
            request.clear();
            isClearedByUs = false;
        }
    }
```
* ImageViewTarget：
```
    // a、设置加载中的显示图片，将原来的图片资源设置为空，在设置placeHolder为加载中显示的图片
    public void onLoadStarted(@Nullable Drawable placeholder){
        super.onLoadStarted(placeholder);
        setResourceInternal(null);
        setDrawable(placeholder);
    }

    public void setDrawable(Drawable drawable){
        view.setImageDrawable(drawable);
    }
    
    // b、设置加载失败的显示图片，将原来的图片资源设置为空，在设置errorDrawable为加载失败显示的图片
    public void onLoadFailed(@Nullable Drawable errorDrawable) {
        super.onLoadFailed(errorDrawable);
        setResourceInternal(null);
        setDrawable(errorDrawable);
    }
    
    // c、图片加载成功处理，在图片加载成功回调时，如果没有动效调用setResource设置加载成功的图片资源，
    // 而setResource是抽象方法，其实现是在DrawableImageViewTarget和BitmapImageVIewTarget来实现的；如果有动效则使用maybeUpdateAnimatable实现动效的逻辑。
    public void onResourceReady(@NonNull Z resource, @Nullable Transition<? super Z> transition) {
        if (transition == null || !transition.transition(resource, this)) {
            //没有动画
            //调用setResourceInternal设置加载成功的图片
            setResourceInternal(resource);
        } else {
            //有动画，调用maybeUpdateAnimatable实现动效
            maybeUpdateAnimatable(resource);
        }
    }

    private void setResourceInternal(@Nullable Z resource) {
        //调用setResource设置图片资源
        setResource(resource);
        //执行动效
        maybeUpdateAnimatable(resource);
    }

    protected abstract void setResource(@Nullable Z resource);
    
    // d、动画的实现，先判断图片资源是否是Animatable的实现类，是的话就转换为Animatable，并调用start开始动效。
    private void maybeUpdateAnimatable(@Nullable Z resource) {
        if (resource instanceof Animatable) {
            animatable = (Animatable) resource;
            animatable.start();
        } else {
            animatable = null;
        }
    }
```
* BitmapImageViewTarget：就是以Bitmap的形式设置图片的资源，在分析ImageViewTarget的时候就明确指出设置图片资源是在子类的setResource来实现。
```
    protected void setResource(Bitmap resource) {
        view.setImageBitmap(resource);
    }
```
* DrawableImageViewTarget：就是以Drawable的形式设置图片的资源，同理BitmapImageViewTarget
```
    protected void setResource(@Nullable Drawable resource) {
        view.setImageDrawable(resource);
    }
```

## Glide之Transformation转换
我们在【4. into(T) (即：RequestBuilder.into(T))】中了解到into(imageview)方法中会根据当前的ImageView的scaleType(缩放类型)配置当前View的缩放选项。我们以CenterCrop效果为例，先看一个配置缩放效果：requestOptions.clone().optionalCenterCrop();
```
// BaseRequestOptions类：
    // 此对象内部维护一个CachedHashCodeArrayMap(ArrayMap的子类)类型的ArrayMap来存储选项配置（应用于内存和磁盘缓存键）
    private Options options = new Options();
    private Map<Class<?>, Transformation<?>> transformations = new CachedHashCodeArrayMap<>();

    @NonNull
    @CheckResult
    public T optionalCenterCrop() {
        // DownsampleStrategy 描述降采样压缩的策略
        // 初始化CenterCrop对象，用来描述图像变化方式
        return optionalTransform(DownsampleStrategy.CENTER_OUTSIDE, new CenterCrop());
    }

    @SuppressWarnings({"WeakerAccess", "CheckResult"})
    @NonNull
    final T optionalTransform(@NonNull DownsampleStrategy downsampleStrategy,
                              @NonNull Transformation<Bitmap> transformation) {
        if (isAutoCloneEnabled) {
            return clone().optionalTransform(downsampleStrategy, transformation);
        }
        // 将降采样压缩策略添加到 options 中
        downsample(downsampleStrategy);
        // 将图像变化方式添加到 transformations 中
        return transform(transformation, /*isRequired=*/ false);
    }

    @NonNull
    @CheckResult
    public T downsample(@NonNull DownsampleStrategy strategy) {
        return set(DownsampleStrategy.OPTION, Preconditions.checkNotNull(strategy));
    }

    @NonNull
    @CheckResult
    public <Y> T set(@NonNull Option<Y> option, @NonNull Y value) {
        if (isAutoCloneEnabled) {
            return clone().set(option, value);
        }

        Preconditions.checkNotNull(option);
        Preconditions.checkNotNull(value);
        options.set(option, value); // 保存到options中
        return selfOrThrowIfLocked();
    }

    @NonNull
    T transform(
            @NonNull Transformation<Bitmap> transformation, boolean isRequired) {
        if (isAutoCloneEnabled) {
            return clone().transform(transformation, isRequired);
        }

        // DrawableTransformation Drawable转换器
        DrawableTransformation drawableTransformation =
                new DrawableTransformation(transformation, isRequired);
        // 调用了 transform 的重载方法, 将这个图像变化的方式作用到多种资源类型上
        transform(Bitmap.class, transformation, isRequired);
        transform(Drawable.class, drawableTransformation, isRequired);
        transform(BitmapDrawable.class, drawableTransformation.asBitmapDrawable(), isRequired);
        transform(GifDrawable.class, new GifDrawableTransformation(transformation), isRequired);
        return selfOrThrowIfLocked();
    }

    @NonNull
    <Y> T transform(
            @NonNull Class<Y> resourceClass,
            @NonNull Transformation<Y> transformation,
            boolean isRequired) {
        if (isAutoCloneEnabled) {
            return clone().transform(resourceClass, transformation, isRequired);
        }

        Preconditions.checkNotNull(resourceClass);
        Preconditions.checkNotNull(transformation);
        // 添加到了 transformations 缓存中
        transformations.put(resourceClass, transformation);
        ...
        return selfOrThrowIfLocked();
    }

```
可以看到配置缩放选项的操作除了添加了图像变化操作, 还设定了采样方式, 分别保存在 transformations 和 options 中。

这段代码出现了几个新的类：

* transformations缓存
* Transformation<?>
* DrawableTransformation
* BitmapTransformation(new CenterCrop() 继承自 BitmapTransformation)

Transformation用来干什么？
    
    平常开发中，我们时常会对网络加载的图片进行处理，
    比如Glide自带centerCrop()，fitCenter()处理，自定义圆形，圆角，模糊处理等等都是通过Transformation完成。

我们先来了解一下CenterCrop类的源码吧：
```
public class CenterCrop extends BitmapTransformation {
    private static final String ID = "com.bumptech.glide.load.resource.bitmap.CenterCrop";
    private static final byte[] ID_BYTES = ID.getBytes(CHARSET);

    // 实现图形变换，主要是这个方法
    @Override
    protected Bitmap transform(
            @NonNull BitmapPool pool, @NonNull Bitmap toTransform, int outWidth, int outHeight) {
        return TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
    }

    // 重写epquals和hashcode方法，确保对象唯一性，以和其他的图片变换做区分
    @Override
    public boolean equals(Object o) {
        return o instanceof CenterCrop;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    // 可通过内部算法 重写此方法自定义图片缓存key
    @Override
    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
        messageDigest.update(ID_BYTES);
    }
}

```
1. CenterCrop继承自BitmapTransformation的，这个是必须的。我们自定义Transformation也要继承这个类。因为整个图片变换功能都是建立在这个继承结构基础上的。
2. 图像变换最重要的就是transform()方法，这个是我们自定义Transformation的关键方法，我们的处理逻辑都要在这个方法里实现。transform()方法中有四个参数。 
    * pool，这个是Glide中的BitmapPool缓存池，用于对Bitmap对象进行重用，否则每次图片变换都重新创建Bitmap对象将会非常消耗内存。 
    * toTransform，这个是原始图片的Bitmap对象，我们就是要对它来进行图片变换。 
    * 图片变换后的宽度 
    * 图片变换后的高度

我们可以看到transform()的处理都在TransformationUtils中，那么我们看一下transform()方法的细节。
```
// TransformationUtils类：
    public static Bitmap centerCrop(@NonNull BitmapPool pool, @NonNull Bitmap inBitmap, int width, int height) {
        // 简单校验
        if (inBitmap.getWidth() == width && inBitmap.getHeight() == height) {
            return inBitmap;
        }
        // From ImageView/Bitmap.createScaledBitmap. 计算画布的缩放的比例以及偏移值
        final float scale;
        final float dx;
        final float dy;
        Matrix m = new Matrix();
        if (inBitmap.getWidth() * height > width * inBitmap.getHeight()) {
            scale = (float) height / (float) inBitmap.getHeight();
            dx = (width - inBitmap.getWidth() * scale) * 0.5f;
            dy = 0;
        } else {
            scale = (float) width / (float) inBitmap.getWidth();
            dx = 0;
            dy = (height - inBitmap.getHeight() * scale) * 0.5f;
        }

        m.setScale(scale, scale);
        m.postTranslate((int) (dx + 0.5f), (int) (dy + 0.5f));
        // 从Bitmap缓存池中尝试获取一个可重用的Bitmap对象
        Bitmap result = pool.get(width, height, getNonNullConfig(inBitmap));
        // 将原图Bitmap对象的alpha值复制到裁剪Bitmap对象上面
        TransformationUtils.setAlpha(inBitmap, result);
        // 裁剪Bitmap对象进行绘制，并将最终的结果进行返回
        applyMatrix(inBitmap, result, m);
        return result;
    }
```
对于equals，hashCode，updateDiskCacheKey不太重要，最重要的方法就是transform()。

若想要更多变换效果可以尝试自定义或者使用glide-transformations这个库，它实现了很多通用的图片变换效果，如裁剪变换、颜色变换、模糊变换等等，使得我们可以非常轻松地进行各种各样的图片变换。

**参考链接：**

https://www.jianshu.com/p/2f520af8461b

https://juejin.im/post/5ca5c7f7e51d45430235ba03

https://blog.csdn.net/f409031mn/article/details/80984650

https://blog.csdn.net/weixin_34377919/article/details/88033060

https://www.jianshu.com/p/043c3c1e127c

https://blog.csdn.net/ApkCore/article/details/92016656

https://blog.csdn.net/say_from_wen/article/details/81218948