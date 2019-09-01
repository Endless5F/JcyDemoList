上一篇我们分析了主流的开源框架的源码的第一篇OkHttp3，现在我们来分析一下本系列第二篇——Retrofit2（源码以2.6.1版为准）。

## 基本了解

### 1. 概念了解：

Retrofit2是基于OkHttp的一个Restful Api请求工具，它是一个类型安全的http客户端请求工具(Type-safe HTTP client for Android and Java )，上一篇文章 <a href="https://juejin.im/post/5d5aa9a5e51d4561f64a0808">[主流开源框架之OkHttp3深入了解]</a> 我们已经了解OkHttp的机制和源码，这里我们就来说说Retrofit2了，从功能上来说，Retrofit有点类似Volley，但是使用方式上相对而言Retrofit会更简单，我们来看一张图：
![](https://user-gold-cdn.xitu.io/2019/9/1/16cec5caf2e94189?w=381&h=459&f=png&s=100382)

我们的应用程序(即：“Application Layer”层) 通过Retrofti Layer层来封装我们的请求参数、header头部和url等配置信息，然后交给OkHttp Layer层完成后续的请求操作。等服务端Server层将数据返回给OkHttp Layer层后又会将原始结果返回到Retrofit Layer层，最后由Retrofit Layer层返回给我们的应用层。
而Retrofit2已经内置了OkHttp，这样Retrofit就可以专注于接口的封装操作，而让OkHttp专注于网络请求的高效操作，两者分工合作更能提高效率。

总结：

1. App应用程序通过Retrofit请求网络，实际上是使用Retrofit层封装请求参数，之后交由OkHttp执行后续的请求操作
2. 服务端返回数据后，OkHttp将原始数据交给Retrofit层，Retrofit会根据开发者的需要对结果进行解析，然后返回解析后的数据

### 2. 基本使用：
```
public interface ApiService {
    @GET("api")
    Call<ResultBean> getResult(@Query("param") String param);
}

Retrofit RETROFIT_CLIENT = new Retrofit.Builder()
    // 设置baseUrl，紧急baseUrl结尾一定要带“/”，否则会有异常的哦
    .baseUrl(BASE_URL)
    .client(OkHttpHolder.OK_HTTP_CLIENT)
    // 直接返回String类型需引入：ScalarsConverterFactory.create()
    // 添加响应数据转换器工厂，这里是Gson将获得响应结果转换成Gson对象
    .addConverterFactory(GsonConverterFactory.create())
    // 添加网络请求适配器工厂，这里是RxJava2将Call转换成Rxjava中的Observable
//    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
    .build();

// 创建接口实例
ApiService service = RETROFIT_CLIENT.create(ApiService.class);
// 调用接口中方法获取Call对象，此Call并非OkHttp中的Call，
// 而是Retrofit对OkHttp中的Call进行封装的，因此使用此Call的同步/异步请求实际上就是使用OkHttp中的Call
Call<ResultBean> call = service.getResult("param");

// 发送同步请求
call.execute();
// 发送异步请求
call.enqueue(new Callback<ResultBean>() {
     @Override
     public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
        // 主线程
        String responseStr = response.body().toString();
        Log.e("result", responseStr);
    }

    @Override
    public void onFailure(Call<ResultBean> call, Throwable t) {
        // 主线程
        Log.e("result", t.getMessage());
    }
});
```
从使用上来看，Retrofit2还是挺简单的，不过要注意这两个配置：

    // 添加响应数据转换器工厂，这里是Gson将获得响应结果转换成Gson对象
    .addConverterFactory(GsonConverterFactory.create())
    // 添加网络请求适配器工厂，这里是RxJava2将Call转换成Rxjava中的Observable
    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

这里的ConverterFactory表示数据转换器工厂，就是将接收到的响应结果数据转换成想要的类型，例如一般使用Gson来解析服务器返回的数据，这里就添加了一个GsonConverterFactory。CallAdapterFactory表示的是网络请求适配器的工厂，它对应的是接口请求方法的返回类型，是将默认的ExecutorCallbackCall转换成适配其它不同平台所需要的类型。例如这里添加了RxJava的适配器，那么接口中就可以写成以下这样，返回一个RxJava中的Observable类型。
```
 @GET("api")
 Observable<ResultBean> getResult(@Query("param") String param);
```
### Retrofit网络通讯步骤：

1. 创建retrofit实例
2. 定义一个网络请求接口并为接口中的方法添加注释(基本使用中已完成)
3. 通过动态代理生成请求对象
4. 通过 网络请求适配器 将 网络请求对象 进行平台适配
5. 通过 网络请求执行器 发送网络请求
6. 通过 数据转换器 解析数据
7. 通过 回调执行器 切换线程
8. 开发者在主线程处理返回数据
## 创建Retrofit

### 1. retrofit的成员属性
```
    /**
     * ConcurrentHashMap：一个线程安全的、支持高效并发的HashMap;
     *  Key 是 Method，Value 是 ServiceMethod。
     *  Method：就是上面接口中定义的getResult，这个方法中有很多注解：@GET、@Path、@Query等
     *  ServiceMethod：是将Method方法中所有的注解，解析后的对象就是ServiceMethod
     * serviceMethodCache：主要是用于缓存，比如说缓存一些网络请求的相关配置、网络请求的方法、数据转换器和网络请求适配器等等
     */
    private final Map<Method, ServiceMethod<?, ?>> serviceMethodCache = new ConcurrentHashMap<>();
    // 请求网络的OkHttp工厂，用于生产OkHttpClient，Retrofit2中默认使用OkHttp
    final okhttp3.Call.Factory callFactory;
    // 网络请求url的基地址
    final HttpUrl baseUrl;
    // 数据转换器工厂集合
    final List<Converter.Factory> converterFactories;
    // 网络请求适配器工厂集合
    final List<CallAdapter.Factory> callAdapterFactories;
    // Executor，这是个网络请求回调执行者，就是用来切换线程的
    final @Nullable Executor callbackExecutor;
    // 一个标志位，是否立即解析网络请求接口中的方法(即ApiService中的方法)
    final boolean validateEagerly;
```
### 2. retrofit的Builder
```
// Retrofit.Builder类：
    public static final class Builder {
        // retrofit适配的平台：Android、Java8
        private final Platform platform;
        // 一下成员和第一部分【retrofit的成员属性】中讲解的一样
        private @Nullable
        okhttp3.Call.Factory callFactory;
        private HttpUrl baseUrl;
        private final List<Converter.Factory> converterFactories = new ArrayList<>();
        private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
        private @Nullable
        Executor callbackExecutor;
        private boolean validateEagerly;

        Builder(Platform platform) {
            this.platform = platform;
        }

        public Builder() {
            this(Platform.get());
        }
        
        ......
        
    }
```
从Builder的成员中我们看到了一个陌生的属性 Platform，这个 Platform 很重要，是获取当前需要适配的平台。我们通过 Builder 的构造函数可以知道，调用了 Platform.get()方法，然后赋值给自己的 platform 变量。
```
class Platform {
    private static final Platform PLATFORM = findPlatform();

    static Platform get() {
        return PLATFORM;
    }

    private static Platform findPlatform() {
        try {
            Class.forName("android.os.Build");
            if (Build.VERSION.SDK_INT != 0) {
                return new Android();
            }
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName("java.util.Optional");
            return new Java8();
        } catch (ClassNotFoundException ignored) {
        }
        return new Platform();
    }

    ......

    static class Android extends Platform {
        @IgnoreJRERequirement // Guarded by API check.
        @Override
        boolean isDefaultMethod(Method method) {
            if (Build.VERSION.SDK_INT < 24) {
                return false;
            }
            return method.isDefault();
        }
    
        // 默认回调执行程序，用来切换线程
        @Override
        public Executor defaultCallbackExecutor() {
            return new MainThreadExecutor();
        }
    
        // 默认的网络请求适配器
        @Override
        List<? extends CallAdapter.Factory> defaultCallAdapterFactories(@Nullable Executor callbackExecutor) {
            if (callbackExecutor == null) throw new AssertionError();
            DefaultCallAdapterFactory executorFactory = new DefaultCallAdapterFactory(callbackExecutor);
            // 根据SDK大小返回适配器集合
            return Build.VERSION.SDK_INT >= 24
                    // 仅在CompletableFuture(异步函数式编程)可用时添加（即：Java 8+ / Android API 24+）
                    ? asList(CompletableFutureCallAdapterFactory.INSTANCE, executorFactory)
                    : singletonList(executorFactory); // 返回仅包含指定对象的不可变列表。 返回的列表是可序列化的。
        }
    
        // 默认调用适配器工厂大小
        @Override
        int defaultCallAdapterFactoriesSize() {
            return Build.VERSION.SDK_INT >= 24 ? 2 : 1;
        }
    
        // 默认转换器工厂
        @Override
        List<? extends Converter.Factory> defaultConverterFactories() {
            return Build.VERSION.SDK_INT >= 24
                    // 仅在Optional(Java8为了解决null值判断问题)可用时添加（即：Java 8+ / Android API 24+）。
                    ? singletonList(OptionalConverterFactory.INSTANCE)
                    : Collections.<Converter.Factory>emptyList();
        }
    
        // 默认转换器工厂大小
        @Override
        int defaultConverterFactoriesSize() {
            return Build.VERSION.SDK_INT >= 24 ? 1 : 0;
        }
    
        static class MainThreadExecutor implements Executor {
            private final Handler handler = new Handler(Looper.getMainLooper());
    
            @Override
            public void execute(Runnable r) {
                handler.post(r);
            }
        }
    }
}
```
get 方法会去调用 findPlatform 方法，通过Class.forName反射查找"android.os.Build"或者"java.util.Optional"并返回不同平台的platform对象，我们分析 Android 平台，所以会返回一个Android()对象。我们在Android对象中看到几个方法：

* defaultCallbackExecutor：默认回调执行器，其return 一个 MainThreadExecutor对象，可以看出MainThreadExecutor里面使用了Handler，因此我们最终切换线程使用的还是Handler。
* defaultCallAdapterFactories：默认的网络请求适配器，用于发送网络请求，并将回调函数切换回主线程，从源码中可以看出若api>= 24,即支持函数式编程，则会多添加一个适配器CompletableFutureCallAdapterFactory
* defaultConverterFactories：默认转换器工厂，若api>= 24,即支持函数式编程，则会多添加转换器工厂OptionalConverterFactory

接下来我们看一下build()方法：
```
// Retrofit.Builder类：
    public static final class Builder {
        
        ......
        
        public Retrofit build() {
        // baseUrl不可空
        if (baseUrl == null) {
            throw new IllegalStateException("Base URL required.");
        }

        // 若请求网络工厂为空，会默认配置为 OkHttpClient
        okhttp3.Call.Factory callFactory = this.callFactory;
        if (callFactory == null) {
            callFactory = new OkHttpClient();
        }

        // 若没配置回调执行器，则默认配置为Platform 的defaultCallbackExecutor，
        // 上面我们之前分析过defaultCallbackExecutor，它所返回的就是 MainThreadExecutor
        Executor callbackExecutor = this.callbackExecutor;
        if (callbackExecutor == null) {
            callbackExecutor = platform.defaultCallbackExecutor();
        }

        // 添加默认的网络请求适配器，先添加开发者自己add的，后添加平台默认的
        List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);
        callAdapterFactories.addAll(platform.defaultCallAdapterFactories(callbackExecutor));
        
        List<Converter.Factory> converterFactories = new ArrayList<>(
                1 + this.converterFactories.size() + platform.defaultConverterFactoriesSize());

        // 首先添加内置(默认)转换器工厂，后添加开发者自己add的，若此时Api>=24,则最后还会添加默认的转换器工厂
        converterFactories.add(new BuiltInConverters());
        converterFactories.addAll(this.converterFactories);
        converterFactories.addAll(platform.defaultConverterFactories());

        return new Retrofit(callFactory, baseUrl, unmodifiableList(converterFactories),
                unmodifiableList(callAdapterFactories), callbackExecutor, validateEagerly);
    }
    }
```
### baseUrl异常问题
我们多说一句，上面基本使用时我们提到过baseUrl结尾要为“/”，否则会抛异常，那么我们就来看一下baseUrl的构建：
```
// Retrofit.Builder类：
    public Builder baseUrl(String baseUrl) {
      checkNotNull(baseUrl, "baseUrl == null");
      return baseUrl(HttpUrl.get(baseUrl));
    }
    
    public Builder baseUrl(HttpUrl baseUrl) {
        checkNotNull(baseUrl, "baseUrl == null");
        List<String> pathSegments = baseUrl.pathSegments();
        if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
            throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
        }
        this.baseUrl = baseUrl;
        return this;
    }
```
baseUrl方法实现非常简单就是将baseUrl设置到Retrofit.Builder中，参数类型为String的方法将传入的字符串封装成了一个HttpUrl对象，调用对应重载方法，重载方法中调用pathSegments方法将url分割，然后判断url是否是以斜杠结尾，不是则抛出异常。

至此，我们的Retrofit就构建完成了。

## Retrofit.create(通过动态代理生成请求对象)
```
// Retrofit类：
    public <T> T create(final Class<T> service) {
        Utils.validateServiceInterface(service);
        if (validateEagerly) {
            eagerlyValidateMethods(service);
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service},
                new InvocationHandler() {
                    private final Platform platform = Platform.get();
                    private final Object[] emptyArgs = new Object[0];

                    @Override
                    public @Nullable
                    Object invoke(Object proxy, Method method,
                                  @Nullable Object[] args) throws Throwable {
                        // If the method is a method from Object then defer to normal invocation.
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }
                        return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
                    }
                });
    }

    private void eagerlyValidateMethods(Class<?> service) {
        Platform platform = Platform.get();
        for (Method method : service.getDeclaredMethods()) {
            if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers())) {
                loadServiceMethod(method);
            }
        }
    }

    ServiceMethod<?> loadServiceMethod(Method method) {
        ServiceMethod<?> result = serviceMethodCache.get(method);
        if (result != null) return result;

        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                result = ServiceMethod.parseAnnotations(this, method);
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }
```


## Retrofit中的工厂设计模式
### 1. Converter

### 2. CallAdapter

