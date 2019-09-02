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

### 1. retrofit的成员属性serviceMethodCacheServiceMethod
```
    /**
     * ConcurrentHashMap：一个线程安全的、支持高效并发的HashMap;
     *  Key 是 Method，Value 是 ServiceMethod。
     *  Method：就是上面接口中定义的getResult，这个方法中有很多注解：@GET、@Path、@Query等
     *  ServiceMethod：是将Method方法中所有的注解，解析后的对象就是ServiceMethod
     * serviceMethodCache：主要是用于缓存ServiceMethod，比如说缓存ServiceMethod中一些网络请求的相关配置、网络请求的方法、数据转换器和网络请求适配器等等
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
            // Api < 24 直接返回false
            if (Build.VERSION.SDK_INT < 24) {
                return false;
            }
            /**
             * method.isDefault() 判断该方法是否为默认方法
             * 默认方法是什么？
             *  Java 8 新增了接口的默认方法。
             *  简单说，默认方法就是接口可以有实现方法，而且不需要实现类去实现其方法。
             *  我们只需在方法名前面加个 default 关键字即可实现默认方法。
             * 因此我们网络请求接口中一般定义的都不是默认方法
             */
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
                    : Collections.<Converter.Factory>emptyList(); // 返回空集合
        }
    
        // 默认转换器工厂大小
        @Override
        int defaultConverterFactoriesSize() {
            return Build.VERSION.SDK_INT >= 24 ? 1 : 0;
        }
        
        // 主线程执行器，内部使用Handler
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
get 方法会去调用findPlatform方法，通过Class.forName反射查找"android.os.Build"或者"java.util.Optional"并返回不同平台的platform对象，我们分析 Android 平台，所以会返回一个Android()对象。我们在Android对象中看到几个方法：

* defaultCallbackExecutor：默认回调执行器，其return 一个 MainThreadExecutor对象，可以看出MainThreadExecutor里面使用了Handler，因此我们最终切换线程使用的还是Handler。
* defaultCallAdapterFactories：默认的网络请求适配器，用于发送网络请求，并将回调函数切换回主线程，从源码中可以看出若api>= 24,即支持函数式编程，则会多添加一个适配器CompletableFutureCallAdapterFactory
* defaultConverterFactories：默认转换器工厂，若api> 24,即支持函数式编程，则会多添加转换器工厂OptionalConverterFactory
* isDefaultMethod：判断接口中的方法是否为默认方法，一般都不是默认方法，只要该方法不被default 关键字标识并且带有方法体，则该方法就不是默认方法，因此一般返回false

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

        // 首先添加内置(默认)转换器工厂，后添加开发者自己add的，若此时Api>24,则最后还会添加默认的转换器工厂
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
        // 验证服务接口
        Utils.validateServiceInterface(service);
        // 熟不熟悉，惊不惊喜，我们在【retrofit的成员属性】中说过，validateEagerly为是否立即解析网络请求接口中的方法
        if (validateEagerly) {
            // 从名字可以看出，“热切地验证方法”，因此就是立即解析请求接口“ApiService”中的方法
            eagerlyValidateMethods(service);
        }
        
        // retrofit的精华之一：动态代理模式
        return (T) Proxy.newProxyInstance(service.getClassLoader()
            , new Class<?>[]{service},new InvocationHandler() {
                    // 获取到当前的平台
                    private final Platform platform = Platform.get();
                    private final Object[] emptyArgs = new Object[0];

                    @Override
                    public @Nullable
                    Object invoke(Object proxy, Method method,@Nullable Object[] args) throws Throwable {
                        // 如果方法是来自Object的方法，则遵循正常调用
                        if (method.getDeclaringClass() == Object.class) {
                            return method.invoke(this, args);
                        }
                        // 如果该方法是默认方法，则直接调用默认方法
                        if (platform.isDefaultMethod(method)) {
                            return platform.invokeDefaultMethod(method, service, proxy, args);
                        }
                        // 加载网络请求接口方法，并执行该方法
                        return loadServiceMethod(method).invoke(args != null ? args : emptyArgs);
                    }
                });
    }

    private void eagerlyValidateMethods(Class<?> service) {
        // 获取当前平台对象
        Platform platform = Platform.get();
        // 通过接口字节码class类获取所有 “声明的方法”
        for (Method method : service.getDeclaredMethods()) {
            // 我们上面【retrofit的Builder】中
            // 讲解到一般网络请求接口中都不是默认方法，并且也不会是静态方法，因此此if判断为 true
            if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers())) {
                // 加载解析请求接口中的方法，该方法后续详细讲解
                loadServiceMethod(method);
            }
        }
    }
```
此方法就是Retrofit设计的精髓之一，采用了外观模式和动态代理模式，并且创建了API接口的实例。此方法涉及的逻辑有点多，先来总结一下：

1. 首先判断validateEagerly标识，默认为false，若为true，则先进入eagerlyValidateMethods方法，此方法循环遍历了作为参数传入的网络请求接口类中所有声明的方法，并且判断每个方法既不是默认方法也不是静态方法就调用loadServiceMethod方法去加载此次遍历的方法。loadServiceMethod是一个用来解析API接口中的方法的方法，具体点来说就是上面示例中ApiService中的每个方法会被解析成一个ServiceMethod对象并进行缓存。
2. 无论validateEagerly是true还是false，都会走到return (T) Proxy.newProxyInstance这一步。这里使用了动态代理返回请求接口类的代理对象，invoke方法中先判断方法是否是Object类中方法，是就不做修改按照原样执行。再调用platform.isDefaultMethod判断是否是默认方法，是就调用platform.invokeDefaultMethod，该方法中抛出异常。最后这一行：

        return loadServiceMethod(method).invoke(args != null ? args : emptyArgs)
    
    也是正常解析接口方法走到的。这里还是先调用loadServiceMethod方法，然后执行其返回值ServiceMethod的方法。

现在我们接着上面说的继续看loadServiceMethod方法：
```
// Retrofit类：
    ServiceMethod<?> loadServiceMethod(Method method) {
        // 我们在【retrofit的成员属性】中说过serviceMethodCache，主要用于缓存ServiceMethod，
        // 而ServiceMethod，是将Method方法中所有的注解，解析后的对象就是ServiceMethod
        ServiceMethod<?> result = serviceMethodCache.get(method);
        // 从缓存中查找ServiceMethod，有则直接返回
        if (result != null) return result;
        // 若缓存中没有ServiceMethod，则解析请求接口类中的方法创建ServiceMethod，并缓存
        synchronized (serviceMethodCache) {
            result = serviceMethodCache.get(method);
            if (result == null) {
                // 解析网络请求接口方法(method)上开发者添加的注释
                result = ServiceMethod.parseAnnotations(this, method);
                // 以method为key，缓存ServiceMethod
                serviceMethodCache.put(method, result);
            }
        }
        return result;
    }
```
loadServiceMethod方法中首先在serviceMethodCache缓存中查找这个方法，如果不为空直接返回。找不到进入同步代码块，同步代码块中再次判断缓存中是否存在该方法，不存在调用ServiceMethod.parseAnnotations方法解析方法注解获得解析结果，并将结果ServiceMethod加入缓存。此方法是双重锁定检查(DCL单例模式)。我们接着看ServiceMethod的parseAnnotations这个解析注解的方法：
```
// ServiceMethod类：
    static <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method) {
        // 解析注解获得一个RequestFactory
        RequestFactory requestFactory = RequestFactory.parseAnnotations(retrofit, method);
        // 获取方法返回类型
        Type returnType = method.getGenericReturnType();
        // 返回类型中有变量或者通配符抛出异常
        if (Utils.hasUnresolvableType(returnType)) {
            throw methodError(method,
                    "Method return type must not include a type variable or wildcard: %s",
                    returnType);
        }
        // 返回类型为空抛出异常
        if (returnType == void.class) {
            throw methodError(method, "Service methods cannot return void.");
        }

        return HttpServiceMethod.parseAnnotations(retrofit, method, requestFactory);
    }
    
// RequestFactory类：
    static RequestFactory parseAnnotations(Retrofit retrofit, Method method) {
        return new Builder(retrofit, method).build();
    }
    
    private final Method method; // 网络请求接口中的方法
    private final HttpUrl baseUrl; // 网络请求基地址
    final String httpMethod; // http请求方法类型，比如：GET、POST等
    private final @Nullable String relativeUrl; // 网络请求的相对地址，和baseUrl组合即为绝对地址
    private final @Nullable Headers headers; // 请求头数据
    private final @Nullable MediaType contentType; // 请求内容类型
    private final boolean hasBody; // 是否有请求体
    private final boolean isFormEncoded; // 表单编码标识
    private final boolean isMultipart; // 多文件上传等的标识
    private final ParameterHandler<?>[] parameterHandlers; // 接口方法参数处理程序
    final boolean isKotlinSuspendFunction; // 是否为kotlin标识
    
    Builder(Retrofit retrofit, Method method) {
        this.retrofit = retrofit;
        this.method = method;
        // 三个数组的初始化
        // 1. 初始化请求接口方法上的注解，对应使用例子中的@GET
        this.methodAnnotations = method.getAnnotations();
        // 2. 初始化请求接口方法参数类型，对应使用例子中的方法参数param的类型String
        this.parameterTypes = method.getGenericParameterTypes();
        // 3. 初始化请求接口方法参数上的注解，对应使用例子中的方法参数param上的@Query
        this.parameterAnnotationsArray = method.getParameterAnnotations();
    }
```
我们先来看一下这一部分代码：

1. 调用RequestFactory的parseAnnotations方法解析注解获得一个RequestFactory对象
2. 获取方法的返回类型并校验异常情况
3. 继续调用HttpServiceMethod的parseAnnotations方法

我们从上面代码中可以简单的看出第1步中，parseAnnotations方法以建造者模式创建一个RequestFactory对象，并在其Builder中初始化了三个数组，分别用来保存请求接口方法中的：

    1.方法上的注解、2.方法参数的类型、3.方法参数里的注解
我们接着看第1步中RequestFactory.Builder.build()方法：
```
// RequestFactory.Builder类：
    RequestFactory build() {
        // 1. 循环解析方法上的注解
        for (Annotation annotation : methodAnnotations) {
            parseMethodAnnotation(annotation);
        }
        // HTTP请求方法类型为空抛出异常
        if (httpMethod == null) {
            throw methodError(method, "HTTP method annotation is required (e.g., @GET, @POST, etc" +".).");
        }
        // 注解错误抛出异常，Multipart与FormUrlEncoded必须在有请求体的post请求上使用
        if (!hasBody) {
            if (isMultipart) {
                throw methodError(method,
                        "Multipart can only be specified on HTTP methods with request body (e.g.," +" @POST).");
            }
            if (isFormEncoded) {
                throw methodError(method, "FormUrlEncoded can only be specified on HTTP methods " +   "with "+ "request body (e.g., @POST).");
            }
        }
        // 获得方法参数个数
        int parameterCount = parameterAnnotationsArray.length;
        parameterHandlers = new ParameterHandler<?>[parameterCount];
        // 2. 循环遍历解析每个请求接口方法的参数
        for (int p = 0, lastParameter = parameterCount - 1; p < parameterCount; p++) {
            // 传入通过从Builder()构造函数中初始化的parameterTypes和parameterAnnotationsArray对应位置的值
            parameterHandlers[p] =
                    parseParameter(p, parameterTypes[p], parameterAnnotationsArray[p],p == lastParameter);
        }
        // 3. 判断各种使用错误情况抛出对应异常
        if (relativeUrl == null && !gotUrl) {
            throw methodError(method, "Missing either @%s URL or @Url parameter.", httpMethod);
        }
        if (!isFormEncoded && !isMultipart && !hasBody && gotBody) {
            throw methodError(method, "Non-body HTTP method cannot contain @Body.");
        }
        if (isFormEncoded && !gotField) {
            throw methodError(method, "Form-encoded method must contain at least one @Field.");
        }
        if (isMultipart && !gotPart) {
            throw methodError(method, "Multipart method must contain at least one @Part.");
        }
        // 4. 创建RequestFactory对象返回
        return new RequestFactory(this);
    }

    // 1.1 解析方法注解
    private void parseMethodAnnotation(Annotation annotation) {
        // 根据注解类型，解析方法、路径或者初始化header、isMultipart、isFormEncoded等成员属性
        if (annotation instanceof DELETE) {
            parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
        } else if (annotation instanceof GET) {
            parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
        } else if (annotation instanceof HEAD) {
            parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
        } else if (annotation instanceof PATCH) {
            parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
        } else if (annotation instanceof POST) {
            parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
        } else if (annotation instanceof PUT) {
            parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
        } else if (annotation instanceof OPTIONS) {
            parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
        } else if (annotation instanceof HTTP) {
            HTTP http = (HTTP) annotation;
            parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
        } else if (annotation instanceof retrofit2.http.Headers) {
            String[] headersToParse = ((retrofit2.http.Headers) annotation).value();
            if (headersToParse.length == 0) {
                throw methodError(method, "@Headers annotation is empty.");
            }
            headers = parseHeaders(headersToParse);
        } else if (annotation instanceof Multipart) {
            if (isFormEncoded) {
                throw methodError(method, "Only one encoding annotation is allowed.");
            }
            isMultipart = true;
        } else if (annotation instanceof FormUrlEncoded) {
            if (isMultipart) {
                throw methodError(method, "Only one encoding annotation is allowed.");
            }
            isFormEncoded = true;
        }
    }
    
    // 1.2 解析Http方法和路径
    private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
        if (this.httpMethod != null) {
            throw methodError(method, "Only one HTTP method is allowed. Found: %s and %s.",
                    this.httpMethod, httpMethod);
        }
        // 初始化httpMethod和hasBody成员属性
        this.httpMethod = httpMethod;
        this.hasBody = hasBody;

        if (value.isEmpty()) {
            return;
        }

        // 获取相对URL路径和现有查询字符串（如果存在）。
        int question = value.indexOf('?');
        if (question != -1 && question < value.length() - 1) {
            // 确保查询字符串没有任何命名参数。
            String queryParams = value.substring(question + 1);
            Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
            if (queryParamMatcher.find()) {
                throw methodError(method, "URL query string \"%s\" must not have replace block. " + "For dynamic query parameters use @Query.", queryParams);
            }
        }
        // 初始化relativeUrl和relativeUrlParamNames成员属性
        this.relativeUrl = value;
        this.relativeUrlParamNames = parsePathParameters(value);
    }
    
    // 2.1 解析请求方法中的参数
    private @Nullable ParameterHandler<?> parseParameter(
            int p, Type parameterType, @Nullable Annotation[] annotations, boolean allowContinuation) {
        ParameterHandler<?> result = null;
        if (annotations != null) {
            for (Annotation annotation : annotations) {
                // 解析参数中的注解，包括参数中的可能包含的路径、查询条件等等
                // 由于此方法代码过多，暂就不分析啦，感兴趣的小伙伴自行查看吧。
                ParameterHandler<?> annotationAction =
                        parseParameterAnnotation(p, parameterType, annotations, annotation);

                if (annotationAction == null) {
                    continue;
                }

                if (result != null) {
                    throw parameterError(method, p, "Multiple Retrofit annotations found, only one allowed.");
                }

                result = annotationAction;
            }
        }

        if (result == null) {
            if (allowContinuation) {
                try {
                    if (Utils.getRawType(parameterType) == Continuation.class) {
                        isKotlinSuspendFunction = true;
                        return null;
                    }
                } catch (NoClassDefFoundError ignored) {
                }
            }
            throw parameterError(method, p, "No Retrofit annotation found.");
        }

        return result;
    }
```
build方法中做了四件事：

1. 通过从Builder()构造函数中初始化的methodAnnotations，解析方法上的注解获得解析Http方法、路径、请求头等信息，并初始化部分成员属性。
2. 循环遍历解析每个请求接口方法的参数，通过调用parseParameter方法，并传入通过从Builder()构造函数中初始化的parameterTypes和parameterAnnotationsArray对应位置的值，来解析方法参数以及参数中的注解，最后保存在parameterHandlers数组中。
3. 判断各种使用错误情况抛出对应异常。
4. 创建并返回RequestFactory实例。

接着简单看下HttpServiceMethod的parseAnnotations方法：
```
// HttpServiceMethod类：
    static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(
            Retrofit retrofit, Method method, RequestFactory requestFactory) {
        boolean isKotlinSuspendFunction = requestFactory.isKotlinSuspendFunction;
        boolean continuationWantsResponse = false;
        boolean continuationBodyNullable = false;
        // 获得方法上的所有注解
        Annotation[] annotations = method.getAnnotations();
        // 1. 获取方法返回类型
        Type adapterType;
        if (isKotlinSuspendFunction) {
            ... // 本文不分析kotlin部分
        } else {
            // 非Kotlin，获取方法返回类型，即用于确定使用哪个网络请求适配器
            adapterType = method.getGenericReturnType();
        }
        // 2. 调用createCallAdapter创建一个CallAdapter网络请求适配器
        CallAdapter<ResponseT, ReturnT> callAdapter =
                createCallAdapter(retrofit, method, adapterType, annotations);
        // 获得CallAdapter的响应类型
        Type responseType = callAdapter.responseType();
        // 是okhttp中的Response抛出异常
        if (responseType == okhttp3.Response.class) {
            throw methodError(method, "'"
                    + getRawType(responseType).getName()
                    + "' is not a valid response body type. Did you mean ResponseBody?");
        }
        // 响应类型不包含泛型类型抛出异常
        if (responseType == Response.class) {
            throw methodError(method, "Response must include generic type (e.g., " +
                    "Response<String>)");
        }
        // TODO support Unit for Kotlin?
        if (requestFactory.httpMethod.equals("HEAD") && !Void.class.equals(responseType)) {
            throw methodError(method, "HEAD method must use Void as response type.");
        }
        // 调用createResponseConverter方法创建ResponseConverter数据转换器
        Converter<ResponseBody, ResponseT> responseConverter =
                createResponseConverter(retrofit, method, responseType);
        // 获取请求工厂
        okhttp3.Call.Factory callFactory = retrofit.callFactory;
        if (!isKotlinSuspendFunction) { // 非Kotlin
            // 3. 创建并返回CallAdapted，参数：requestFactory、callFactory、responseConverter、callAdapter
            return new CallAdapted<>(requestFactory, callFactory, responseConverter, callAdapter);
        } ...
    }

// HttpServiceMethod.CallAdapted类：
    CallAdapted(RequestFactory requestFactory, okhttp3.Call.Factory callFactory,
                Converter<ResponseBody, ResponseT> responseConverter,
                CallAdapter<ResponseT, ReturnT> callAdapter) {
        super(requestFactory, callFactory, responseConverter);
        // 初始化网络适配器
        this.callAdapter = callAdapter;
    }
```
先来梳理下HttpServiceMethod的parseAnnotations方法的流程：

1. 首先调用createCallAdapter方法创建一个网络请求适配器。
2. 调用createResponseConverter方法创建了响应数据转换器
3. 接着从传入的retrofit对象中获取到网络请求工厂callFactory，最后通过以上这几个对象创建了一个CallAdapted返回。注：CallAdapted为HttpServiceMethod内部静态类，并且是HttpServiceMethod的子类，**即我们上面【retrofit的成员属性】中的serviceMethodCache缓存集合中缓存的ServiceMethod，实际是CallAdapted对象。**

下面我们还是来深入分别看一下这几步具体逻辑：

* createCallAdapter：
```
// HttpServiceMethod类：
    private static <ResponseT, ReturnT> CallAdapter<ResponseT, ReturnT> createCallAdapter(
            Retrofit retrofit, Method method, Type returnType, Annotation[] annotations) {
        try {
            // 调用retrofit.callAdapter方法
            return (CallAdapter<ResponseT, ReturnT>) retrofit.callAdapter(returnType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create call adapter for %s", returnType);
        }
    }

// Retrofit类：
    public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
        // 可以看出callAdapter就是一个包装方法，继而调用nextCallAdapter
        return nextCallAdapter(null, returnType, annotations);
    }

    public CallAdapter<?, ?> nextCallAdapter(
        @Nullable CallAdapter.Factory skipPast ,Type returnType,Annotation[] annotations) {
        checkNotNull(returnType, "returnType == null");
        checkNotNull(annotations, "annotations == null");
        // 从网络请求适配器工厂集合中查找传入的适配器的位置
        // 这里注意到nextCallAdapter方法传递的skipPast参数为null，所以这里indexOf返回-1，最终初始start为0
        int start = callAdapterFactories.indexOf(skipPast) + 1;
        // 循环遍历网络请求适配器工厂集合
        for (int i = start, count = callAdapterFactories.size(); i < count; i++) {
            // 调用Factory的get方法，根据网络请求接口方法的返回值，获取对应适配器
            CallAdapter<?, ?> adapter = callAdapterFactories.get(i).get(returnType, annotations,this);
            // 不为空就返回adapter
            if (adapter != null) {
                return adapter;
            }
        }

        ...
    }
```
从这段代码中可以看清晰的看出其调用顺序，nextCallAdapter方法中遍历网络请求适配器工厂集合，根据方法的返回类型调用工厂的get获得CallAdapter。还记得创建工厂集合时默认添加了一个DefaultCallAdapterFactory吗？Retrofit默认方法返回类型Call就对应了这个工厂。进入它的get方法查看一下：
```
// DefaultCallAdapterFactory类：
    @Override
    public @Nullable CallAdapter<?, ?> get(
            Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Call.class) {
            return null;
        }
        if (!(returnType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Call return type must be parameterized as Call<Foo> or Call<? extends Foo>");
        }
        final Type responseType = Utils.getParameterUpperBound(0, (ParameterizedType) returnType);

        final Executor executor = Utils.isAnnotationPresent(annotations, SkipCallbackExecutor.class)
                ? null
                : callbackExecutor;
        // 默认返回的网络适配器
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }
            // 注意此方法，下面会讲到的
            @Override
            public Call<Object> adapt(Call<Object> call) {
                return executor == null ? call : new ExecutorCallbackCall<>(executor, call);
            }
        };
    }
```
DefaultCallAdapterFactory的get方法中根据传入的方法返回类型判断，返回类型不是Call类型就直接返回null。类型正确会返回一个CallAdapter。

* createResponseConverter：
```
// HttpServiceMethod类：
    private static <ResponseT> Converter<ResponseBody, ResponseT> createResponseConverter(
            Retrofit retrofit, Method method, Type responseType) {
        Annotation[] annotations = method.getAnnotations();
        try {
            // 调用Retorfit中的方法responseBodyConverter
            return retrofit.responseBodyConverter(responseType, annotations);
        } catch (RuntimeException e) { // Wide exception range because factories are user code.
            throw methodError(method, e, "Unable to create converter for %s", responseType);
        }
    }

// Retrofit类：
    public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
        // 同callAdapter方法为包装方法
        return nextResponseBodyConverter(null, type, annotations);
    }
    
    // 该方法逻辑和nextCallAdapter类似
    public <T> Converter<ResponseBody, T> nextResponseBodyConverter(
            @Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
        checkNotNull(type, "type == null");
        checkNotNull(annotations, "annotations == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            // 调用数据转换器工厂的responseBodyConverter方法，根据responseType类型获取数据转化器
            Converter<ResponseBody, ?> converter =
                    converterFactories.get(i).responseBodyConverter(type, annotations, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) converter;
            }
        }

        ...
    }
```
我们发现createCallAdapter和createResponseConverter，这两个方法的流程很类似，都是会调用Retrofit类中对应方法，之后再调用对应next方法遍历集合，从中获得合适的适配器和转换器。这里来看下常用的Gson转换器的responseBodyConverter方法：
```
// GsonConverterFactory类：
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations,
                                                            Retrofit retrofit) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        // 返回一个GsonResponseBodyConverter对象
        return new GsonResponseBodyConverter<>(gson, adapter);
    }
```

终于可以回到Retrofit的create方法里了，由于之前分析可知loadServiceMethod方法最终会获得一个CallAdapted对象，这里就会接着调用它的invoke方法，而CallAdapted中没有实现invoke方法，invoke方法在其父类HttpServiceMethod中：
```
// HttpServiceMethod类：
    @Override
    final @Nullable
    ReturnT invoke(Object[] args) {
        // 创建了一个Call，实现类：OkHttpCall，注意这个Call还不是OkHttp中的Call类，它还是Retrofit包中的类
        Call<ResponseT> call = new OkHttpCall<>(requestFactory, args, callFactory,responseConverter);
        return adapt(call, args);
    }
    
// OkHttpCall类：
    final class OkHttpCall<T> implements Call<T> {
        // 保存网络请求信息的工厂对象
        private final RequestFactory requestFactory;
        private final Object[] args; // 网络请求真实参数
        private final okhttp3.Call.Factory callFactory;
        private final Converter<ResponseBody, T> responseConverter; // 数据转换器

        private volatile boolean canceled; // 判断是否取消call
        @GuardedBy("this")
        private @Nullable okhttp3.Call rawCall; // 这个rawCall才是OkHttp中的Call
        @GuardedBy("this")
        private boolean executed; // 异步请求时使用的标志位
        
        ......
    }
    
// HttpServiceMethod.CallAdapted类：
    @Override
    protected ReturnT adapt(Call<ResponseT> call, Object[] args) {
        return callAdapter.adapt(call);
    }
```
总结一下此处的逻辑：
1. invoke方法中创建了一个Call，此Call实现类为OkHttpCall(Retrofit包中的类，主要用来包装OkHttp中真实的Call，即OkHttpCall中的rawCall)。
2. 然后调用adapt方法将第1步创建的call传入
3. 调用之前【createCallAdapter】获取到的网络适配器的adapt方法，传入call，并返回相应的网络适配器adapt(适配)后的Call。比如：默认的CallAdapter返回的ExecutorCallbackCall、RxJava2CallAdapter返回的CallEnqueueObservable或者CallExecuteObservable。

我们上面【createCallAdapter】使用的是DefaultCallAdapterFactory为例，介绍的适配器的get方法，那么我们现在也使用DefaultCallAdapterFactory返回的CallAdapter为例，来介绍一下adapt方法：
```
    @Override
    public @Nullable CallAdapter<?, ?> get(
            Type returnType, Annotation[] annotations, Retrofit retrofit) {
        ...
        
        return new CallAdapter<Object, Call<?>>() {
            @Override
            public Type responseType() {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call) {
                return executor == null
                        ? call
                        : new ExecutorCallbackCall<>(executor, call);
            }
        };
    }
```
return 返回了CallAdapter,其中adapt方法返回的是一个ExecutorCallbackCall类型，它实现了Retrofit的Call接口，就是最终返回的适配后的Call类型。

总结一下：
* Call：
    1. OkHttpCall是Retrofit对OkHttp中的Call的包装
    2. 不同的网络适配器会再次将OkHttpCall进行不同的适配，并生成适合于不同适配器的Call
* CallAdapter以及CallAdapter.Factory
    1. 所有网络适配器都需要实现CallAdapter方法：
        * responseType 返回此适配器在将HTTP响应主体转换为Java 对象时使用的值类型。例如，{@code Call<User>}的响应类型是{@code User}
        * adapt 返回此适配器适合的Call（包装OkHttpCall而生成）
    2. 所有网络适配器工厂都需要继承于CallAdapter.Factory
        * get 返回网络请求接口返回值类型的调用适配器，若此工厂无法处理，则返回null。
        * getParameterUpperBound 从{@code type}中提取{@code index}处的泛型参数的上限，比如：Map<String, ? extends Runnable>} 返回 Runnable
        * getRawType 从返回值类型中提取原始类类型。比如：Observable<List<T>> 返回 List<T>
## Retrofit同步请求execute方法
## Retrofit异步请求enqueue方法
## Retrofit中的工厂设计模式
### 1. Converter

### 2. CallAdapter

