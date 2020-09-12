自上一篇自定义控件的完结，至今已经有一个月的时间，一直没有什么想写的，因此回到一开始写的初衷，看一些主流的开源框架的源码，深入的了解一下其原理，而不是只知其然，而不知其所以然。本篇是该系列第一篇——OkHttp3（源码以3.10版为准）。

## 基础
```
// 通过建造者模式构建OkHttpClient
OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .connectTimeout(60, TimeUnit.SECONDS)
    // 设置缓存 ：参数1：缓存路径（/storage/emulated/0/Android/data/xxx包名/cache） 参数2：最大缓存值(100MB)
    //.cache(new Cache(new File(getExternalCacheDir()), 100 * 1024 * 1024))
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build();
// 创建请求的Request 对象
Request request = builder
    .url(mUrl)
    .build();
// 在Okhttp中创建Call 对象，将request和Client进行绑定
Call call = OK_HTTP_CLIENT.newCall(request);
// 同步执行
Response response = call.execute();
// 异步执行
call.enqueue(new Callback() {
    @Override
    public void onFailure(Call call, IOException e) {
        LoggerUtil.d("onFailure :  "+e.getMessage());
    }

    @Override
    public void onResponse(Call call, Response response) {
        responseProcess(response);
    }
});

// 注意：
@Override public Call newCall(Request request) {
    return RealCall.newRealCall(this, request, false /* for web socket */);
}
```
总结（OkHttp请求）：

1. 创建OkHttpClient和Request对象
2. 将Request封装成Call对象
3. 调用Call的execute()/enqueue()发送同步/异步请求

**注**：

    1.在使用Builder()构建OkHttpClient时会初始化一个很重要的类Dispatcher(分发器类)
    ，其作用：会接受我们的同步或者异步的Request队列，根据不同的条件进行任务的分发。
    2.OK_HTTP_CLIENT.newCall(request)，实际上返回的是RealCall，因此同步/异步请求都是由RealCall发出的

OkHttp3同步/异步请求大体框架流程：
![](https://user-gold-cdn.xitu.io/2019/8/19/16caa4f53854f22e?w=813&h=482&f=jpeg&s=57313)

## 同步请求的源码分析
从上一节中我们能了解到同步请求执行的是execute()方法，并且都是由RealCall发出的请求
```
// RealCall类：
@Override                                                                 
public Response execute() throws IOException {                            
    synchronized (this) {                                                 
        if (executed) throw new IllegalStateException("Already Executed");
        executed = true;                                                  
    }    
    // 捕获调用堆栈跟踪(本文不是重点)
    captureCallStackTrace();                                              
    eventListener.callStart(this);                                        
    try {
        // 调用分发器入队
        client.dispatcher().executed(this); 
        // OkHttp精髓之一 通过拦截器链获得响应（具体后续单独讲解）
        Response result = getResponseWithInterceptorChain();
        if (result == null) throw new IOException("Canceled");
        return result;
    } catch (IOException e) {
        eventListener.callFailed(this, e);                                
        throw e; 
    } finally {
        // 调用分发器出队
        client.dispatcher().finished(this);
    }
}                                                                         
```
由源码分可以看出对于同步请求来说，dispatcher只是简单的入队和出队操作，其余都是通过拦截器链来处理获取响应信息。
## 异步请求的源码分析
异步调用是由RealCall类的enqueue方法发出
```
// RealCall类：
@Override
public void enqueue(Callback responseCallback) {
    synchronized (this) {
        if (executed) throw new IllegalStateException("Already Executed");
        executed = true;
    }
    captureCallStackTrace();
    eventListener.callStart(this);
    // 创建异步回调AsyncCall，并且将AsyncCall入队操作
    client.dispatcher().enqueue(new AsyncCall(responseCallback));
}

// Dispatcher类：
    private int maxRequests = 64; // 最大请求个数数
    private int maxRequestsPerHost = 5; // 每个主机的最大请求数，此请求为正在进行网络请求
    // 执行异步任务的线程池
    private @Nullable ExecutorService executorService;
    /**
    * 准备异步调用的队列
    */
    private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    /**
    * 正在运行的异步调用队列。包括尚未完成的已取消通话。
    */
    private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    /**
    * 正在运行的同步调用。包括尚未完成的已取消通话。
    */
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();

    synchronized void enqueue(AsyncCall call) {
        // 正在运行的异步队列个数 < 64 , 与共享主机的正在运行的呼叫数 < 5
        if (runningAsyncCalls.size() < maxRequests && runningCallsForHost(call) < maxRequestsPerHost) {
            // 添加到正在运行的异步队列
            runningAsyncCalls.add(call);
            // 启动线程池执行异步任务
            executorService().execute(call);
        } else {
            // 添加到准备异步调用的队列
            readyAsyncCalls.add(call);
        }
    }
```
从上面源码中可以看出来，异步请求有两个不同的队列，一个是正在运行的请求队列一个是准备异步调用的队列。两者根据正在呼叫的个数以及正在运行的异步队列的个数分别入队。而正在运行的异步队列在入队的同时通过线程池执行了其异步任务。

首先我们先来看一下其线程池的初始化：
```
// 类似于单例模式的获取方式
    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            /*
            * corePoolSize：线程池核心线程数    0
            * maximumPoolSize：线程池最大数     int 类整数的最大值是 2 的 31 次方 
            * keepAliveTime：空闲线程存活时间   60s
            * unit：时间单位                    秒
            * workQueue：线程池所使用的缓冲队列
            * threadFactory：线程池创建线程使用的工厂
            * handler：线程池对拒绝任务的处理策略
            * */
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Util.threadFactory("OkHttp Dispatcher",false));
        }
        return executorService;
    }
```
该线程池核心线程数为0，线程池最大线程为整数最大值。

问：若我们的网络请求非常多时，多达Integer.MAX_VALUE，这个线程池性能消耗是否特别大？
答：其实是不会的，因为OkHttp中的runningAsyncCalls队列最大为64，因此也限制了OkHttp的请求不会超过64，也就是就算我们设置了Integer.MAX_VALUE，对我们的性能也不会有影响。

其次，我们executorService线程池里执行的为AsyncCall，我们来看一看AsyncCall：
```
// 继承自Runnable
public abstract class NamedRunnable implements Runnable {
  protected final String name;

  public NamedRunnable(String format, Object... args) {
    this.name = Util.format(format, args);
  }

  @Override public final void run() {
    String oldName = Thread.currentThread().getName();
    Thread.currentThread().setName(name);
    try {
        // 实际上就是将run()方法的执行交给了execute()方法，进行了一层包装
      execute();
    } finally {
      Thread.currentThread().setName(oldName);
    }
  }

  protected abstract void execute();
}

// 其继承自NamedRunnable，因此此Runnable真正执行的代码在 execute()方法中
final class AsyncCall extends NamedRunnable {
    private final Callback responseCallback;

    AsyncCall(Callback responseCallback) {
        super("OkHttp %s", redactedUrl());
        this.responseCallback = responseCallback;
    }

    String host() {
        return originalRequest.url().host();
    }

    Request request() {
        return originalRequest;
    }

    RealCall get() {
        return RealCall.this;
    }

    @Override
    protected void execute() {
        boolean signalledCallback = false;
        try {
            // 通过拦截器链获得响应，具体后续详细讲解
            Response response = getResponseWithInterceptorChain();
            if (retryAndFollowUpInterceptor.isCanceled()) {
                signalledCallback = true;
                // 失败回调
                responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
            } else {
                signalledCallback = true;
                // 成功回调
                responseCallback.onResponse(RealCall.this, response);
            }
        } catch (IOException e) {
            if (signalledCallback) {
                // Do not signal the callback twice!
                Platform.get().log(INFO, "Callback failure for " + toLoggableString(), e);
            } else {
                eventListener.callFailed(RealCall.this, e);
                // 失败回调
                responseCallback.onFailure(RealCall.this, e);
            }
        } finally {
            // 异步任务执行结束
            client.dispatcher().finished(this);
        }
    }
}
```
通过源码可以看出，最后调用了AsyncCall的execute()来发起请求，并在execute()方法中执行了我们上面看到的，同样在同步请求中执行的getResponseWithInterceptorChain()方法通过拦截器链来获取响应。

我们再来看一下同步/异步请求结束后的finished：
```
    // 异步请求finished
    void finished(AsyncCall call) {
        // 注意：参数3 true
        finished(runningAsyncCalls, call, true);
    }

    // 同步请求finished
    void finished(RealCall call) {
        // 注意：参数3 false
        finished(runningSyncCalls, call, false);
    }
    
    private <T> void finished(Deque<T> calls, T call, boolean promoteCalls) {
        int runningCallsCount;
        Runnable idleCallback;
        synchronized (this) {
            // 从正在运行的同步/异步队列中移除任务，如果队列中没有则抛出异常
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            // 同步跳过这一步，一步则执行这一步
            if (promoteCalls) promoteCalls();
            runningCallsCount = runningCallsCount();
            idleCallback = this.idleCallback;
        }

        if (runningCallsCount == 0 && idleCallback != null) {
            idleCallback.run();
        }
    }
    
    // 异步执行
    private void promoteCalls() {
        // 已经运行最大容量，则返回
        if (runningAsyncCalls.size() >= maxRequests) return; 
        // 没有准备执行的异步任务则返回
        if (readyAsyncCalls.isEmpty()) return; 
        // 遍历准备执行的异步请求队列
        for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
            AsyncCall call = i.next(); // 取出下一个异步任务
            // 如果与共享主机的正在运行的呼叫数 < 5
            if (runningCallsForHost(call) < maxRequestsPerHost) {
                i.remove(); // 移除
                // 添加进正在运行的异步队列
                runningAsyncCalls.add(call);
                // 立马在线程池中执行此异步请求
                executorService().execute(call);
            }

            if (runningAsyncCalls.size() >= maxRequests) return; // Reached max capacity.
        }
    }
```
我们可以看出runningAsyncCalls和readyAsyncCalls队列，是通过方法promoteCalls()来将等待执行的任务（readyAsyncCalls中的元素）添加进runningAsyncCalls队列并执行。

![](https://user-gold-cdn.xitu.io/2019/8/20/16caaab431278085?w=1013&h=653&f=jpeg&s=79796)

至此，同步异步请求答题流程已经走完，接下来看一下OkHTTP设计之妙——拦截器。
## getResponseWithInterceptorChain()
```
// RealCall类：
    Response getResponseWithInterceptorChain() throws IOException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        // 我们自己添加的拦截器（ApplicationInterceptor(应用拦截器)）
        interceptors.addAll(client.interceptors());
        // 请求重定向拦截器：失败重连等
        interceptors.add(retryAndFollowUpInterceptor);
        // 桥接拦截器
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        // 缓存拦截器
        interceptors.add(new CacheInterceptor(client.internalCache()));
        // 链接拦截器
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            // NetworkInterceptor（网络拦截器）
            interceptors.addAll(client.networkInterceptors());
        }
        // 真正调用网络请求的拦截器
        interceptors.add(new CallServerInterceptor(forWebSocket));
        // 拦截器链,注意：第5个参数 index == 0
        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,originalRequest, this, eventListener, client.connectTimeoutMillis(),
        client.readTimeoutMillis(), client.writeTimeoutMillis());

        return chain.proceed(originalRequest);
    }
```
从getResponseWithInterceptorChain()方法的源码中可以看出，拦截器分为应用拦截器、网络拦截器，这两类均为我们自己构建OkhttpClient时添加的。不过我们本文的重点并不是这两类拦截器，而是OkHttp本身的5个拦截器，而这5个拦截器也是整个OkHtp的精华之一。

我们可以看出，源码中将所有拦截器都add进List集合中，并当作参数传入RealInterceptorChain，即拦截器链中，然后调用proceed方法，那我们来看一下这些拦截器是如何串联起来的：
```
// RealInterceptorChain类：
    @Override
    public Response proceed(Request request) throws IOException {
        return proceed(request, streamAllocation, httpCodec, connection);
    }

    public Response proceed(Request request, StreamAllocation streamAllocation
        , HttpCodec httpCodec,RealConnection connection) throws IOException {
        
        ...

        // 调用链中的下一个拦截器。注意：第5个参数 index = index + 1
        RealInterceptorChain next = new RealInterceptorChain(interceptors, streamAllocation,httpCodec,
            connection, index + 1, request, call, eventListener, connectTimeout, readTimeout,writeTimeout);
        // 从getResponseWithInterceptorChain()中我们知道index初始化为0
        // 获取当前位置拦截器
        Interceptor interceptor = interceptors.get(index);
        // 执行当前位置拦截器，并把下一个位置的拦截器链传入
        Response response = interceptor.intercept(next);

        ...

        return response;
    }
    
// RetryAndFollowUpInterceptor类：
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Call call = realChain.call();
        EventListener eventListener = realChain.eventListener();
        // 初始化分配流对象：OkHtpp请求的各种组件的封装类
        StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
                createAddress(request.url()), call, eventListener, callStackTrace);
        this.streamAllocation = streamAllocation;

        int followUpCount = 0;
        Response priorResponse = null;
        while (true) {
            ...

            Response response;
            boolean releaseConnection = true;
            try {
                // 执行拦截器链的 proceed 方法
                response = realChain.proceed(request, streamAllocation, null, null);
                releaseConnection = false;
            } 
            
            ......
            
        }
    }
```
从这段代码两个类的两部分中可以看出各个拦截器是由拦截器链串联起来的，上述代码中以RetryAndFollowUpInterceptor拦截器为例，由拦截器链的方法proceed开始，按照顺序调用各个拦截器，并且每个拦截器中都会继续调用下一个拦截器链对象的proceed，从而将所有拦截器串联起来，最终经过所有拦截器后获取到响应信息。
请求流程图如下：
![](https://user-gold-cdn.xitu.io/2019/8/20/16caf40ad7baa96c?w=1123&h=794&f=jpeg&s=207820)

借鉴一张感觉比较完整的的：

![](https://user-gold-cdn.xitu.io/2019/8/20/16caf4740e42bc49?w=1459&h=683&f=png&s=126809)
接下来我们可以开始分别深入了解一下这些拦截器的实现原理及功能。
## RetryAndFollowUpInterceptor
```
@Override
public Response intercept(Chain chain) throws IOException {
    // 获取我们构建的请求
    Request request = chain.request();
    // 1. 初始化一个socket连接分配流对象
    streamAllocation = new StreamAllocation(
            client.connectionPool(), createAddress(request.url()), callStackTrace);
    // 计数器
    int followUpCount = 0;
    Response priorResponse = null;
    // 开启死循环，用于执行第一个拦截器或者请求的失败重连
    while (true) {
        // 如果请求已经被取消了，释放连接池的资源
        if (canceled) {
            streamAllocation.release();
            throw new IOException("Canceled");
        }

        Response response = null;
        boolean releaseConnection = true;
        try {
            // 2. 执行下一个拦截器，即BridgeInterceptor
            response = ((RealInterceptorChain) chain).proceed(request, streamAllocation, null, null);
            // 先不释放链接，因为可能要复用
            releaseConnection = false;
        } catch (RouteException e) { // 连接地址失败的异常
            /**
             * 3. 如果有异常，判断是否要恢复
             * 不在继续连接的情况：
             *  1. 应用层配置不在连接，默认为true
             *  2. 请求Request出错不能继续使用
             *  3. 是否可以恢复的
             *      3.1、协议错误（ProtocolException）
             *      3.2、中断异常（InterruptedIOException）
             *      3.3、SSL握手错误（SSLHandshakeException && CertificateException）
             *      3.4、certificate pinning错误（SSLPeerUnverifiedException）
             *  4. 没用更多线路可供选择
            */
            if (!recover(e.getLastConnectException(), false, request)) {
                throw e.getLastConnectException();
            }
            releaseConnection = false;
            continue;
        } catch (IOException e) {
            // 判断网络请求是否已经开始
            boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
            // 判断是否能够恢复，也就是是否要重试
            if (!recover(e, requestSendStarted, request)) throw e;
            releaseConnection = false;
            continue;
        } finally {
            // 释放连接
            if (releaseConnection) {
                streamAllocation.streamFailed(null);
                streamAllocation.release();
            }
        }

        // priorResponse如果存在。则构建
        if (priorResponse != null) {
            response = response.newBuilder()
                    .priorResponse(priorResponse.newBuilder()
                            .body(null)
                            .build())
                    .build();
        }
        /**
         * 4. 根据返回结果response，来检查是否需要进行重定向操作
         *      或者是否需要继续完善请求，例如证书验证等等
         * 是否需要进行请求重定向，是根据http请求的响应码来决定的，
         * 因此，在followUpRequest方法中，将会根据响应userResponse，获取到响应码，
         * 并从连接池StreamAllocation中获取连接，然后根据当前连接，得到路由配置参数Route。
         *
         * followUpCount是用来记录我们发起网络请求的次数的
         *  为什么我们发起一个网络请求，可能okhttp会发起多次呢？
         *      例如：https的证书验证，我们需要经过：发起 -> 验证 ->响应，
         *      三个步骤需要发起至少两次的请求，或者我们的网络请求被重定向，
         *      在我们第一次请求得到了新的地址后，再向新的地址发起网络请求。
         * */
        Request followUp = followUpRequest(response);

        if (followUp == null) {
            if (!forWebSocket) {
                streamAllocation.release();
            }
            // 返回结果
            return response;
        }
        // 5. 不需要重定向，关闭响应流
        closeQuietly(response.body());
        // 6. 重定向或者失败重连，是否超过最大限制 MAX_FOLLOW_UPS == 20
        if (++followUpCount > MAX_FOLLOW_UPS) {
            streamAllocation.release();
            throw new ProtocolException("Too many follow-up requests: " + followUpCount);
        }
        // 如果body内容只能发送一次，释放连接
        if (followUp.body() instanceof UnrepeatableRequestBody) {
            streamAllocation.release();
            throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
        }
        // 7. 检查重定向（失败重连）请求，和当前的请求，是否为同一个连接
        if (!sameConnection(response, followUp.url())) {
            // 释放之前你的url地址连接
            streamAllocation.release();
            // 创建新的网络请求封装对象StreamAllocation
            streamAllocation = new StreamAllocation(
                    client.connectionPool(), createAddress(followUp.url()), callStackTrace);
        } else if (streamAllocation.codec() != null) {
            throw new IllegalStateException("Closing the body of " + response
                    + " didn't close its backing stream. Bad interceptor?");
        }
        // 更新下一次的网络请求对象
        request = followUp;
        // 保存上一次的请求结果
        priorResponse = response;
    }
}
```
具体讲解大部分都在代码中说明，简单来说明一下此拦截器的作用：

    1. 初始化一个连接流对象
    2. 调用下一个拦截器
    3. 根据异常结果或者响应结果判断是否需要重新请求
## BridgeInterceptor
```
@Override
public Response intercept(Chain chain) throws IOException {
    Request userRequest = chain.request();
    Request.Builder requestBuilder = userRequest.newBuilder();
    // 构建可以用于发送网络请求的Request
    // ------------------主要构建完整的请求头 start------------------------
    RequestBody body = userRequest.body();
    if (body != null) {
        MediaType contentType = body.contentType();
        if (contentType != null) {
            requestBuilder.header("Content-Type", contentType.toString());
        }

        long contentLength = body.contentLength();
        if (contentLength != -1) {
            requestBuilder.header("Content-Length", Long.toString(contentLength));
            requestBuilder.removeHeader("Transfer-Encoding");
        } else {
            requestBuilder.header("Transfer-Encoding", "chunked");
            requestBuilder.removeHeader("Content-Length");
        }
    }

    if (userRequest.header("Host") == null) {
        requestBuilder.header("Host", hostHeader(userRequest.url(), false));
    }

    if (userRequest.header("Connection") == null) {
        // 开启TCP连接后不会立马关闭连接，而是存活一段时间
        requestBuilder.header("Connection", "Keep-Alive"); 
    }
     // 如果我们没有指定编码的格式，默认使用gzip
    boolean transparentGzip = false;
    if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
        transparentGzip = true;
        requestBuilder.header("Accept-Encoding", "gzip");
    }
    // 把之前的cookie存在header里
    List<Cookie> cookies = cookieJar.loadForRequest(userRequest.url());
    if (!cookies.isEmpty()) {
        requestBuilder.header("Cookie", cookieHeader(cookies));
    }

    if (userRequest.header("User-Agent") == null) {
        requestBuilder.header("User-Agent", Version.userAgent());
    }
// ------------------主要构建完整的请求头 end------------------------

    // 调用下一个拦截器
    Response networkResponse = chain.proceed(requestBuilder.build());
    
    // 响应头， 如果没有自定义配置cookieJar == null，则什么都不做，有则保存新的cookie
    // 将服务器返回来的Response转化为开发者使用的Response（类似于解压的过程）
    HttpHeaders.receiveHeaders(cookieJar, userRequest.url(), networkResponse.headers());
    // 构建Response
    Response.Builder responseBuilder = networkResponse.newBuilder()
            .request(userRequest);
    /**
     * 是否转换为解压Response
     * 条件：
     *  1.判断服务器是否支持gzip压缩格式
     *  2.判断服务器响应是否使用gzip压缩
     *  3.是否有响应体
     */
    if (transparentGzip
            && "gzip".equalsIgnoreCase(networkResponse.header("Content-Encoding"))
            && HttpHeaders.hasBody(networkResponse)) {
        // 转换成解压数据源
        GzipSource responseBody = new GzipSource(networkResponse.body().source());
        Headers strippedHeaders = networkResponse.headers().newBuilder()
                .removeAll("Content-Encoding")
                .removeAll("Content-Length")
                .build();
        responseBuilder.headers(strippedHeaders);
        String contentType = networkResponse.header("Content-Type");
        responseBuilder.body(new RealResponseBody(contentType, -1L, Okio.buffer(responseBody)));
    }

    return responseBuilder.build();
}
```
具体讲解大部分都在代码中说明，简单来说明一下此拦截器的作用：

    1. 负责将用户构建的一个Request请求转换为能够进行网络访问的请求
    2. 将这个符合网络请求的Resquest进行网络请求（即调用下一个拦截器）
    3. 将网络请求回来的响应Response转化为用户可用的Response（解压）
## CacheInterceptor
此拦截器是用来缓存请求Request和响应Response数据的拦截器，此拦截器起作用需要用户调用
new OkHttpClient.Builder().cache(new Cache(new File(getExternalCacheDir()), 100 * 1024 * 1024)) 来设置缓存路径和缓存的大小。

在看拦截器的源码之前我们先来了解几个概念：

1. Cache类中的 InternalCache （内部缓存）
2. DiskLruCache 硬盘缓存
3. OkHttp使用Okio处理各种流操作(替代Io流)：Okio中有两个关键的接口，Sink和Source，这两个接口都继承了Closeable接口；而Sink可以简单的看做OutputStream，Source可以简单的看做InputStream。而这两个接口都是支持读写超时设置的。

### DiskLruCache

DiskLruCache是JakeWharton大神的杰作，它采用的是LRU算法，通过LRU算法对缓存进行管理，以最近最少使用作为管理的依据，删除最近最少使用的数据，保留最近最常用的数据。（此算法和OkHttp(大概是重写了部分)有些许不同，原理一致）

    DiskLruCache主要知识点：
        1. 简单使用
        2. journal(日志)文件的生成
        3. journal的介绍
        4. 写入缓存
        5. 读取缓存
        6. 删除缓存
        7.其它API

一. 简单使用
```
// demo例子：
    File directory = getExternalCacheDir();
    int appVersion = 1;
    int valueCount = 1;
    long maxSize = 10 * 1024;
    
    /*
     * 参数说明：
     *  File directory：缓存目录。
     *  int appVersion：应用版本号。
     *  int valueCount：一个key对应的缓存文件的数目
     *      ，如果我们传入的参数大于1，那么缓存文件后缀就是 .0 ， .1等。
     *  long maxSize：缓存容量上限。
     */
    DiskLruCache diskLruCache = DiskLruCache.open(directory, appVersion, valueCount, maxSize);

    // 构建写入缓存 Editor
    DiskLruCache.Editor editor = diskLruCache.edit(String.valueOf(System.currentTimeMillis()));
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(editor.newOutputStream(0));
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scenery);
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream);

    editor.commit();
    diskLruCache.flush();
    diskLruCache.close();
```
这个就是DiskLruCache的大致使用流程，简单看一下其文件创建:

二. 文件创建过程
```
public final class DiskLruCache implements Closeable {
    
     public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize) throws IOException {
        if (maxSize <= 0) {
          throw new IllegalArgumentException("maxSize <= 0");
        }
        if (valueCount <= 0) {
          throw new IllegalArgumentException("valueCount <= 0");
        }
    
        File backupFile = new File(directory, JOURNAL_FILE_BACKUP);
        //如果备份文件存在
        if (backupFile.exists()) {
          File journalFile = new File(directory, JOURNAL_FILE);
          // 如果journal文件存在，则把备份文件journal.bkp是删了
          if (journalFile.exists()) {
            backupFile.delete();
          } else {
            //如果journal文件不存在，则将备份文件命名为journal
            renameTo(backupFile, journalFile, false);
          }
        }
    
        DiskLruCache cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        
        //判断journal文件是否存在
        if (cache.journalFile.exists()) {
          //如果日志文件以及存在
          try {
            // 
            /**
             * 读取journal文件，根据记录中不同的操作类型进行相应的处理。
             * 通过读取journal文件的每一行，然后封装成entry对象，放到LinkedHashMap集合中。
             *  并且根据每一行不同的开头，设置entry的值。也就是说通过读取这个文件，
             *  我们把所有的在本地缓存的文件的key都保存到了集合中，这样我们用的时候就可以通过集合来操作了。
             */
            cache.readJournal();
            // 该方法主要用来计算当前的缓存总容量，并删除非法缓存记录以及该记录对应的文件。
            cache.processJournal();
            cache.journalWriter = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(cache.journalFile, true), Util.US_ASCII));
            return cache;
          } catch (IOException journalIsCorrupt) {
            System.out.println("DiskLruCache " + directory + " is corrupt: " + journalIsCorrupt.getMessage() + ", removing");
            cache.delete();
          }
        }
    
        //创建新的缓存目录
        directory.mkdirs();
        cache = new DiskLruCache(directory, appVersion, valueCount, maxSize);
        //调用新的方法建立新的journal文件
        cache.rebuildJournal();
        return cache;
      }
}
```
open方法，围绕着journal文件的创建和读写来展开的，那么journal文件是什么呢？

三. journal的介绍

我们如果去打开缓存目录，就会发现除了缓存文件，还会发现一个journal文件，journal文件用来记录缓存的操作记录的，如下所示：

        libcore.io.DiskLruCache
        1
        100
        2
     
        CLEAN 3400330d1dfc7f3f7f4b8d4d803dfcf6 832 21054
        DIRTY 335c4c6028171cfddfbaae1a9c313c52
        CLEAN 335c4c6028171cfddfbaae1a9c313c52 3934 2342
        REMOVE 335c4c6028171cfddfbaae1a9c313c52
        DIRTY 1ab96a171faeeee38496d8b330771a7a
        CLEAN 1ab96a171faeeee38496d8b330771a7a 1600 234
        READ 335c4c6028171cfddfbaae1a9c313c52
        READ 3400330d1dfc7f3f7f4b8d4d803dfcf6

我们来分析下这个文件的内容：

* 第一行：libcore.io.DiskLruCache，固定字符串。
* 第二行：1，DiskLruCache源码版本号。
* 第三行：1，App的版本号，通过open()方法传入进去的。
* 第四行：1，每个key对应几个文件，一般为1.
* 第五行：空行
* 第六行及后续行：缓存操作记录。

而源码中有4冲命令的记录：
```
/*
 * DIRTY 表示一个entry正在被写入。
 *  写入分两种情况，如果成功会紧接着写入一行CLEAN的记录；
 *  如果失败，会增加一行REMOVE记录。注意单独只有DIRTY状态的记录是非法的。
 */ 
private static final String DIRTY = "DIRTY";
private static final String REMOVE = "REMOVE";

// READ就是说明有一次读取的记录。
private static final String READ = "READ";

// CLEAN的后面还记录了文件的长度，注意可能会一个key对应多个文件，那么就会有多个数字。
// 当手动调用remove(key)方法的时候也会写入一条REMOVE记录。
private static final String CLEAN = "CLEAN";
```
四. 写入缓存

需要调用DiskLruCache的edit()方法来获取实例，接口如下所示：

    public Editor edit(String key) throws IOException （用法详见一. 简单使用）
    
可以看到，edit()方法接收一个参数key，这个key将会成为缓存文件的文件名，因为图片URL中可能包含一些特殊字符，这些字符有可能在命名文件时是不合法的。因此这里的参数key一般都会进行MD5编码，编码后的字符串肯定是唯一的，并且只会包含0-F这样的字符，完全符合文件的命名规则。

五. 读取缓存

读取的方法要比写入简单一些，主要是借助DiskLruCache的get()方法实现的，接口如下所示：
    
    // 返回一个缓存文件快照，包含缓存文件大小，输入流等信息。
    public synchronized Snapshot get(String key) throws IOException
    
该方法最终返回一个缓存文件快照，包含缓存文件大小，输入流等信息。利用这个快照我们就可以读取缓存文件了。只需要调用它的getInputStream()方法就可以得到缓存文件的输入流了。同样地，getInputStream()方法也需要传一个index参数，这里传入0就好。

六. 删除缓存

移除缓存主要是借助DiskLruCache的remove()方法实现的，接口如下所示：

    public synchronized boolean remove(String key) throws IOException
    
用法虽然简单，但是你要知道，这个方法我们并不应该经常去调用它。因为你完全不需要担心缓存的数据过多从而占用SD卡太多空间的问题，DiskLruCache会根据我们在调用open()方法时设定的缓存最大值来自动删除多余的缓存。只有你确定某个key对应的缓存内容已经过期，需要从网络获取最新数据的时候才应该调用remove()方法来移除缓存。

七. 其它API

    size() ：返回当前缓存路径下所有缓存数据的总字节数，以byte为单位
    flush() ：将内存中的操作记录同步到日志文件（也就是journal文件）当中
        注：并不是每次写入缓存都要调用一次flush()方法的，频繁地调用并不会带来任何好处，
        只会额外增加同步journal文件的时间。比较标准的做法就是在Activity的onPause()方法中去调用一次flush()方法就可以了。
    close() ：将DiskLruCache关闭掉，是和open()方法对应的一个方法。
        注：关闭掉了之后就不能再调用DiskLruCache中任何操作缓存数据的方法，通常只应该在Activity的onDestroy()方法中去调用close()方法。
    delete() ：将所有的缓存数据全部删除，比如说手动清理缓存功能

### InternalCache
```
// Cache类：
    Cache(File directory, long maxSize, FileSystem fileSystem) {
        this.internalCache = new InternalCache() {
            // 1.获取缓存的响应数据
            public Response get(Request request) throws IOException {
                return Cache.this.get(request);
            }

            public CacheRequest put(Response response) throws IOException {
                // 2.保存请求回来的响应数据
                return Cache.this.put(response);
            }

            public void remove(Request request) throws IOException {
                // 3.通过请求移除保存的响应数据
                Cache.this.remove(request);
            }

            public void update(Response cached, Response network) {
                // 4.更新缓存的响应数据
                Cache.this.update(cached, network);
            }

            public void trackConditionalCacheHit() {
                Cache.this.trackConditionalCacheHit();
            }

            public void trackResponse(CacheStrategy cacheStrategy) {
                Cache.this.trackResponse(cacheStrategy);
            }
        };
        // 硬盘缓存 DiskLruCache
        this.cache = DiskLruCache.create(fileSystem, directory, 201105, 2, maxSize);
    }
```
我们主要了解InternalCache的get和put方法，我们先看一下其put保存请求回来的响应Response数据，从上面代码我们能看到put方法实际上调用的是Cache类的put ：

一. put方法分析：
```
// Cache类：
    @Nullable
    CacheRequest put(Response response) {
        // 获取请求方法
        String requestMethod = response.request().method();
        if (HttpMethod.invalidatesCache(response.request().method())) {
            try {
                this.remove(response.request());
            } catch (IOException var6) {
            }

            return null;
        // 如果不是GET请求时返回的response，则不进行缓存
        } else if (!requestMethod.equals("GET")) { 
            return null;
        } else if (HttpHeaders.hasVaryAll(response)) {
            return null;
        } else {
            // 把response封装在Cache.Entry中，调用DiskLruCache的edit()返回editor
            Cache.Entry entry = new Cache.Entry(response);
            Editor editor = null;

            try {
                 // cache 从Cache类的构造方法中可以看出cache实际上就是 DiskLruCache
                 // 把url进行 md5()，并转换成十六进制格式
                // 将转换后的key作为DiskLruCache内部LinkHashMap的键值
                editor = this.cache.edit(key(response.request().url()));
                if (editor == null) {
                    return null;
                } else {
                    // 用editor提供的Okio的sink对文件进行写入
                    entry.writeTo(editor);
                    // 利用CacheRequestImpl写入body
                    return new Cache.CacheRequestImpl(editor);
                }
            } catch (IOException var7) {
                this.abortQuietly(editor);
                return null;
            }
        }
    }
```
根据上面的代码发现，OkHttp只针对GET请求时返回的response进行缓存。官方解释：非GET请求下返回的response也可以进行缓存，但是这样做的复杂性高，且效益低。 在获取DiskLruCache.Editor对象editor后，调用writeTo()把url、请求方法、响应首部字段等写入缓存，然后返回一个CacheRequestImpl实例，在CacheInterceptor的intercept()方法内部调用cacheWritingResponse()写入body，最后调用CacheRequestImpl的close()完成提交（实际内部调用了Editor # commit() ）。

接下来我们看一下edit和writeTo内部实现：
```
// DiskLruCache 类：
    public @Nullable Editor edit(String key) throws IOException {
        return edit(key, ANY_SEQUENCE_NUMBER);
    }

    synchronized Editor edit(String key, long expectedSequenceNumber) throws IOException {
        //内部主要是利用FileSystem处理文件，如果这里出现了异常，
        //在最后会构建新的日志文件，如果文件已存在，则替换
        initialize();
        //检测缓存是否已关闭
        checkNotClosed();
        //检测是否为有效key
        validateKey(key);
        //lruEntries是LinkHashMap的实例，先查找lruEntries是否存在
        Entry entry = lruEntries.get(key);
     
        if (expectedSequenceNumber != ANY_SEQUENCE_NUMBER && (entry == null
            || entry.sequenceNumber != expectedSequenceNumber)) {
            return null; // Snapshot is stale.
        }

        //如果有Editor在操作entry，返回null
        if (entry != null && entry.currentEditor != null) {
        return null; 
        }
        //如果需要，进行clean操作
        if (mostRecentTrimFailed || mostRecentRebuildFailed) {    
        executor.execute(cleanupRunnable);
        return null;
        }

        // 把当前key在对应文件中标记DIRTY状态，表示正在修改，
        //清空日志缓冲区，防止泄露
        journalWriter.writeUtf8(DIRTY).writeByte(' ').writeUtf8(key).writeByte('\n');
        journalWriter.flush();

        if (hasJournalErrors) {
        return null; // 如果日志文件不能编辑
        }
    
        //为请求的url创建一个新的DiskLruCache.Entry实例
        //并放入lruEntries中
        if (entry == null) {
        entry = new Entry(key);
        lruEntries.put(key, entry);
        }
    
        Editor editor = new Editor(entry);
        entry.currentEditor = editor;
        return editor;
    }
    
// Cache.Entry类：
    public void writeTo(Editor editor) throws IOException {
        BufferedSink sink = Okio.buffer(editor.newSink(0));
        // 以下都是利用sink进行写入操作
        sink.writeUtf8(this.url).writeByte(10);
        sink.writeUtf8(this.requestMethod).writeByte(10);
        sink.writeDecimalLong((long) this.varyHeaders.size()).writeByte(10);
        int i = 0;

        int size;
        for (size = this.varyHeaders.size(); i < size; ++i) {
            sink.writeUtf8(this.varyHeaders.name(i)).writeUtf8(": ").writeUtf8(this.varyHeaders.value(i)).writeByte(10);
        }

        sink.writeUtf8((new StatusLine(this.protocol, this.code, this.message)).toString()).writeByte(10);
        sink.writeDecimalLong((long) (this.responseHeaders.size() + 2)).writeByte(10);
        i = 0;

        for (size = this.responseHeaders.size(); i < size; ++i) {
            sink.writeUtf8(this.responseHeaders.name(i)).writeUtf8(": ").writeUtf8(this.responseHeaders.value(i)).writeByte(10);
        }

        sink.writeUtf8(SENT_MILLIS).writeUtf8(": ").writeDecimalLong(this.sentRequestMillis).writeByte(10);
        sink.writeUtf8(RECEIVED_MILLIS).writeUtf8(": ").writeDecimalLong(this.receivedResponseMillis).writeByte(10);
        if (this.isHttps()) {
            sink.writeByte(10);
            sink.writeUtf8(this.handshake.cipherSuite().javaName()).writeByte(10);
            this.writeCertList(sink, this.handshake.peerCertificates());
            this.writeCertList(sink, this.handshake.localCertificates());
            sink.writeUtf8(this.handshake.tlsVersion().javaName()).writeByte(10);
        }

        sink.close();
    }
```
接下来我们再看一看Cache.Entry构造方法：
```
    Entry(Response response) {
        this.url = response.request().url().toString();
        this.varyHeaders = HttpHeaders.varyHeaders(response);
        this.requestMethod = response.request().method();
        this.protocol = response.protocol();
        this.code = response.code();
        this.message = response.message();
        this.responseHeaders = response.headers();
        this.handshake = response.handshake();
        this.sentRequestMillis = response.sentRequestAtMillis();
        this.receivedResponseMillis = response.receivedResponseAtMillis();
    }
```
我们发现Cache.Entry构造方法中并没有Response的body(),那么我们的body是在哪缓存的呢，其实上面就有说明，其实Cache类的put方法有一个返回值 CacheRequest ，而CacheRequest正是后面用来缓存Response的body的关键，后续再详细介绍。

二. get方法分析：
```
// Cache类：
    @Nullable
    Response get(Request request) {
        //把url转换成key
        String key = key(request.url());
        DiskLruCache.Snapshot snapshot;
        Entry entry;
        try {
            //通过DiskLruCache的get()根据具体的key获取DiskLruCache.Snapshot实例
            snapshot = cache.get(key);
            if (snapshot == null) {
                return null;
            }
        } catch (IOException e) {
            // Give up because the cache cannot be read.
            return null;
        }

        try {
            //通过snapshot.getSource()获取一个Okio的Source
            entry = new Entry(snapshot.getSource(ENTRY_METADATA));
        } catch (IOException e) {
            Util.closeQuietly(snapshot);
            return null;
        }

        //根据snapshot获取缓存中的response
        Response response = entry.response(snapshot);

        if (!entry.matches(request, response)) {
            Util.closeQuietly(response.body());
            return null;
        }

        return response;
    }
    
// DiskLruCache类：
    public synchronized Snapshot get(String key) throws IOException {
        initialize();

        checkNotClosed();
        validateKey(key);
        //从lruEntries查找entry，
        Entry entry = lruEntries.get(key);
        if (entry == null || !entry.readable) return null;

        //得到Entry的快照值snapshot
        Snapshot snapshot = entry.snapshot();
        if (snapshot == null) return null;

        redundantOpCount++;
        journalWriter.writeUtf8(READ).writeByte(' ').writeUtf8(key).writeByte('\n');

        //如果redundantOpCount超过2000，且超过lruEntries的大小时，进行清理操作
        if (journalRebuildRequired()) {
            executor.execute(cleanupRunnable);
        }

        return snapshot;
    }

//DiskLruCache.Entry类：
    Snapshot snapshot() {
        if (!Thread.holdsLock(DiskLruCache.this)) throw new AssertionError();

        Source[] sources = new Source[valueCount];
        // Defensive copy since these can be zeroed out.
        long[] lengths = this.lengths.clone();
        try {
            //遍历已缓存的文件，生成相应的sources
            for (int i = 0; i < valueCount; i++) {
                sources[i] = fileSystem.source(cleanFiles[i]);
            }
            //创建Snapshot并返回
            return new Snapshot(key, sequenceNumber, sources, lengths);
        } catch (FileNotFoundException e) {
            // A file must have been deleted manually!
            for (int i = 0; i < valueCount; i++) {
                if (sources[i] != null) {
                    Util.closeQuietly(sources[i]);
                } else {
                    break;
                }
            }
            // Since the entry is no longer valid, remove it so the metadata is accurate (i.e. 
            // the cache
            // size.)
            try {
                removeEntry(this);
            } catch (IOException ignored) {
            }
            return null;
        }
    }
```
相比于put过程，get过程相对简单点。DiskLruCache.Snapshot是DiskLruCache.Entry的一个快照值，内部封装了DiskLruCache.Entry对应文件的Source，简单的说：根据条件从DiskLruCache.Entry找到相应的缓存文件，并生成Source，封装在Snapshot内部，然后通过snapshot.getSource()获取Source，对缓存文件进行读取操作。

总结：：经过分析InternalCache我们知道，Cache只是一个上层的执行者，内部真正的缓存是由DiskLruCache实现的。在DiskLruCache里面通过FileSystem，基于Okio的Sink/Source对文件进行流操作。

![](https://user-gold-cdn.xitu.io/2019/8/24/16cc2ae68eaf384e?w=240&h=240&f=png&s=36805)
### intercept拦截

接下来我们回到CacheInterceptor的拦截器方法intercept中继续分析：
```
    // 我们从RealCall的getResponseWithInterceptorChain()方法中，
    // 在add(new CacheInterceptor(client.internalCache()));时可知
    // intercept方法中的cache为Cache类中的InternalCache
    @Override
    public Response intercept(Chain chain) throws IOException {
        // 如果配置了缓存：优先从缓存中读取Response
        Response cacheCandidate = cache != null
                ? cache.get(chain.request()) // 我们熟悉的get方法，获取缓存
                : null;

        long now = System.currentTimeMillis();
        // 缓存策略，该策略通过某种规则来判断缓存是否有效
        // 1. 根据Request和之前缓存的Response得到CacheStrategy
        // 2. 根据CacheStrategy决定是请求网络还是直接返回缓存
        // 3. 如果2中决定请求网络，则在这一步将返回的网络响应和本地缓存对比，对本地缓存进行增删改操作
        CacheStrategy strategy =
                new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;

        if (cache != null) {
            cache.trackResponse(strategy);
        }

        if (cacheCandidate != null && cacheResponse == null) {
            closeQuietly(cacheCandidate.body());
        }

        // 如果根据缓存策略strategy禁止使用网络，并且缓存无效，直接返回空的Response
        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(504)
                    .message("Unsatisfiable Request (only-if-cached)")
                    .body(Util.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();
        }

        // 如果根据缓存策略strategy禁止使用网络，且有缓存则直接使用缓存
        if (networkRequest == null) {
            return cacheResponse.newBuilder()
                    .cacheResponse(stripBody(cacheResponse))
                    .build();
        }

        Response networkResponse = null;
        try { // 执行下一个拦截器，发起网路请求
            networkResponse = chain.proceed(networkRequest);
        } finally {
            // If we're crashing on I/O or otherwise, don't leak the cache body.
            if (networkResponse == null && cacheCandidate != null) {
                closeQuietly(cacheCandidate.body());
            }
        }

        // 如果我们也有缓存响应，那么我们正在进行条件获取。
        if (cacheResponse != null) {
             // 并且服务器返回304状态码（说明缓存还没过期或服务器资源没修改）
            if (networkResponse.code() == HTTP_NOT_MODIFIED) {
                // 构建缓存数据
                Response response = cacheResponse.newBuilder()
                        .headers(combine(cacheResponse.headers(), networkResponse.headers()))
                        .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
                       .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
                        .cacheResponse(stripBody(cacheResponse))
                        .networkResponse(stripBody(networkResponse))
                        .build();
                networkResponse.body().close();

                // Update the cache after combining headers but before stripping the
                // Content-Encoding header (as performed by initContentStream()).
                cache.trackConditionalCacheHit();
                cache.update(cacheResponse, response);
                return response;
            } else {
                closeQuietly(cacheResponse.body());
            }
        }
        // 如果网络资源已经修改：使用网络响应返回的最新数据
        Response response = networkResponse.newBuilder()
                .cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse))
                .build();
        // 将最新的数据缓存起来
        if (cache != null) {
            if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response,
                    networkRequest)) {
                // 我们熟悉的put 写入缓存操作
                CacheRequest cacheRequest = cache.put(response);
                // 写入Response的body
                return cacheWritingResponse(cacheRequest, response);
            }

            if (HttpMethod.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // The cache cannot be written.
                }
            }
        }

        return response;
    }
```
简单的总结一下上面的代码都做了些什么： 

1. 如果在初始化OkhttpClient的时候配置缓存，则从缓存中取caceResponse 
2. 将当前请求request和caceResponse 构建一个CacheStrategy对象 
3. CacheStrategy这个策略对象将根据相关规则来决定caceResponse和Request是否有效，如果无效则分别将caceResponse和request设置为null 
4. 经过CacheStrategy的处理(步骤3），如果request和caceResponse都置空，直接返回一个状态码为504，且body为Util.EMPTY_RESPONSE的空Respone对象 
5. 经过CacheStrategy的处理(步骤3），resquest 为null而cacheResponse不为null，则直接返回cacheResponse对象 
6. 执行下一个拦截器发起网路请求， 
7. 如果服务器资源没有过期（状态码304）且存在缓存，则返回缓存 
8. 将网络返回的最新的资源（networkResponse）缓存到本地，然后返回networkResponse. 

我们上面还遗留了一个Response的body的缓存没有分析，那么我们看一看cacheWritingResponse方法的实现：
```
// CacheInterceptor类：
    private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)throws IOException {
        // 有些应用会返回空体;为了兼容性，我们将其视为空缓存请求。
        if (cacheRequest == null) return response;
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null) return response;
        // 获取response.body()的BufferedSource
        final BufferedSource source = response.body().source();
        // 构建用来存储response.body()的BufferedSink
        final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);
        // 注意：用于真正写入Response的body
        Source cacheWritingSource = new Source() {
            boolean cacheRequestClosed;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead;
                try {
                    // 从byteCount个字段到sink中并删除
                    bytesRead = source.read(sink, byteCount);
                } catch (IOException e) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        cacheRequest.abort(); // Failed to write a complete cache response.
                    }
                    throw e;
                }

                if (bytesRead == -1) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        cacheBody.close(); // 缓存response的body完成
                    }
                    return -1; // 写完返回-1
                }
                // 将读到sink中的source(response的body数据)拷贝到cacheBody中
                sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                cacheBody.emitCompleteSegments();
                return bytesRead;
            }

            @Override
            public Timeout timeout() {
                return source.timeout();
            }

            @Override
            public void close() throws IOException {
                if (!cacheRequestClosed
                        && !discard(this, HttpCodec.DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
                    cacheRequestClosed = true;
                    cacheRequest.abort();
                }
                source.close();
            }
        };

        String contentType = response.header("Content-Type");
        long contentLength = response.body().contentLength();
        return response.newBuilder()
                .body(new RealResponseBody(contentType, contentLength, Okio.buffer(cacheWritingSource))) // 注意最后一个参数，后面会说明
                .build();
    }
```
从这段代码中可能会有些疑惑，source是如何在cacheWritingSource这个内部类的read方法中缓存完成的，那么我们就要看cacheWritingSource被传递到哪里，并且被谁所调用的read方法啦。我们从最后的return可以看出来cacheWritingSource被封装到Response返回啦，我们上面讲过整个的拦截器链最终会将Response返回到异步请求的回调onResponse方法中或者作为同步请求的返回值。那么我们最终对Response的调用也就只有Response的body()的string()方法啦。那么我们来研究一下这个方法都干了什么？
```
// ResponseBody ==> 通过调用Response.body()获取
    public final String string() throws IOException {
        BufferedSource source = source();
        try {
            Charset charset = Util.bomAwareCharset(source, charset());
            // 从source中读取结果字符串
            return source.readString(charset);
        } finally {
            Util.closeQuietly(source);
        }
    }
```
ResponseBody其实只是一个抽象类，而其实现类为RealResponseBody，从RealResponseBody中发现source是从其构造方法中初始化的：
```
// RealResponseBody类：
    private final BufferedSource source;

    public RealResponseBody(
            @Nullable String contentTypeString, long contentLength, BufferedSource source) {
        this.contentTypeString = contentTypeString;
        this.contentLength = contentLength;
        this.source = source;
    }
```
那么我们的RealResponseBody是什么时候初始化的呢？我们现在在讨论的是缓存的Response，因此缓存Response肯定是从我们缓存拦截器CacheInterceptor返回来的，所以我们上面cacheWritingResponse方法中的返回值Response在构建过程中，其实就是初始化RealResponseBody的地方。因此我们此时的source就是我们在cacheWritingResponse方法的返回值传入的Okio.buffer(cacheWritingSource)。而Okio.buffer(cacheWritingSource)方法返回的是RealBufferedSource类（并传入cacheWritingSource），因此Response.body().string()方法里
source.readString(charset)调用的实际上就是RealBufferedSource类的readString方法。
```
// RealBufferedSource类
    
    public final Buffer buffer = new Buffer();
    public final Source source;
    // 终于看到我们想看到的source，此source就是传进来的cacheWritingSource（用来写入Response的body缓存的匿名内部类）
    RealBufferedSource(Source source) {
        if (source == null) throw new NullPointerException("source == null");
        this.source = source;
    }
    
    @Override
    public String readString(Charset charset) throws IOException {
        if (charset == null) throw new IllegalArgumentException("charset == null");
        // 写入全部，
        buffer.writeAll(source);
        return buffer.readString(charset);
    }
    
// Buffer类，在RealBufferedSource类的成员函数中初始化
    @Override
    public long writeAll(Source source) throws IOException {
        if (source == null) throw new IllegalArgumentException("source == null");
        long totalBytesRead = 0;
        // 关注此处的for循环，如果read != -1 则一直轮询，
        // 因此一直执行cacheWritingSource的read写入Response的body数据，直到写完返回-1
        for (long readCount; (readCount = source.read(this, Segment.SIZE)) != -1; ) {
            totalBytesRead += readCount;
        }
        return totalBytesRead;
    }
```
总结一下：

1. CacheInterceptor的intercept方法中会将最新Response缓存（执行方法：cacheWritingResponse）
2. cacheWritingResponse方法内部有一个匿名内部类（cacheWritingSource）来真正用来写入Response 的body缓存。
3. 由于cacheWritingResponse方法回来Response（其中ResponseBody包含Okio.buffer(cacheWritingSource)，即BufferedSource）给缓存拦截器，而缓存拦截器一直往上面的拦截器传递，直到返回到同步请求的返回值或者异步请求的onResponse方法中。
4. 在我们调用Response.body().string()方法时，触发BufferedSource（实际上是RealBufferedSource）的readString。
5. 而readString会调用Buffer的writeAll（传入cacheWritingSource），进行for循环来执行第2步中所有的匿名内部类（cacheWritingSource）中的read方法写入Response缓存

![](https://user-gold-cdn.xitu.io/2019/8/24/16cc2ab62a98e7d2?w=240&h=240&f=png&s=24472)

## RealConnection
RealConnection是Connection的实现类，Realconnection封装了底层Socket连接，同时使用 OKio（square公司的另一个独立的开源项目） 来进行鱼服务器交互数据的读写。首先看下它的成员属性：
```
    private final ConnectionPool connectionPool;
    private final Route route;

    //下面这些字段，通过connect()方法开始初始化，并且绝对不会再次赋值
 
    private Socket rawSocket; //底层Tcp Socket

    private Socket socket;  //应用层socket
    //握手（处理三次握手）
    private Handshake handshake;
    //协议
    private Protocol protocol;
    // http2的链接
    private Http2Connection http2Connection;
    //通过source和sink，与服务器交互的输入输出流
    private BufferedSource source;
    private BufferedSink sink;

    //下面这个字段是 属于表示链接状态的字段，并且有connectPool统一管理
    //如果noNewStreams被设为true，则noNewStreams一直为true，不会被改变，并且表示这个链接不会再创建新的stream流
    public boolean noNewStreams;

    //成功的次数
    public int successCount;
    
    //此链接可以承载最大并发流的限制，如果不超过限制，可以随意增加
    public int allocationLimit = 1;
    
    // allocations是关联StreamAllocation,它用来统计在一个连接上建立了哪些流，
    // 通过StreamAllocation的acquire方法和release方法可以将一个allcation对方添加到链表或者移除链表
    public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();
```
从其成员属性中可以看出，RealConnection中持有Socket连接，并且会保留有sink和source用来与服务器交互的输入输出流。因此如果拥有了一个RealConnection就代表了我们已经跟服务器有了一条通信链路（Socket链路）。并且三次握手也是实现在这个类中，其具体实现是在其connect方法中，此方法我们放到ConnectInterceptor拦截器中进行分析。

当使用OkHttp请求URL时，RealConnection的作用如下:

1. 它使用URL并配置了OkHttpClient来创建一个地址。这个地址指定我们如何连接到webserver。
它试图从连接池检索具有该地址的连接。
2. 如果它没有在连接池中找到连接，它会选择要尝试的路由。这通常意味着发出DNS请求以获取服务器的IP地址。然后在必要时选择TLS版本和代理服务器。
3. 如果是新的路由，它可以通过构建直接套接字连接、TLS隧道(HTTP代理上的HTTPS)或TLS连接来连接。它会在必要的时候建立握手。
4. 它发送HTTP请求并读取响应。
5. 如果连接有问题，OkHttp将选择另一个路由并再次尝试。这允许OkHttp在服务器地址的子集不可用时恢复。当池连接过时或尝试的TLS版本不受支持时，这点也很有受用。

## StreamAllocation
**HTTP的版本背景：**

HTTP的版本从最初的1.0版本，到后续的1.1版本，再到后续的google推出的SPDY,后来再推出2.0版本，http协议越来越完善。(ps:okhttp也是根据2.0和1.1/1.0作为区分，实现了两种连接机制)这里要说下http2.0和http1.0,1.1的主要区别，2.0解决了老版本(1.1和1.0)最重要两个问题：连接无法复用和head of line blocking (HOL)问题.2.0使用多路复用的技术，多个stream可以共用一个socket连接，每个tcp连接都是通过一个socket来完成的，socket对应一个host和port，如果有多个stream(也就是多个request)都是连接在一个host和port上，那么它们就可以共同使用同一个socket,这样做的好处就是可以减少TCP的一个三次握手的时间。在OKHttp里面，记录一次连接的是RealConnection，这个负责连接，在这个类里面用socket来连接，用HandShake来处理握手。

**3个概念：请求、连接、流**

我们要明白HTTP通信执行网络"请求"需要在"连接"上建立一个新的"流",我们将StreamAllocation称之流的桥梁，它负责为一次"请求"寻找"连接"并建立"流"，从而完成远程通信。所以说StreamAllocation与"请求"、"连接"、"流"都有关。

StreamAllocation的注释也详细讲述了，Connection是建立在Socket之上的物流通信信道，而Stream则是代表逻辑的流，至于Call是对一次请求过程的封装。之前也说过一个Call可能会涉及多个流(比如重定向或者auth认证等情况)。如果StreamAllocation要想解决上述问题，需要两个步骤，一是寻找连接，二是获取流。所以StreamAllocation里面应该包含一个Stream(OKHttp里面的流是HttpCodec)；还应该包含连接Connection。如果想找到合适的连接，还需要一个连接池ConnectionPool属性。所以应该有一个获取流的方法在StreamAllocation里面是newStream()；找到合适的流的方法findConnection()；还应该有完成请求任务的之后finish()的方法来关闭流对象，还有终止和取消等方法，以及释放资源的方法。

我们先来看一下其成员属性：
```
    /**
     * 地址指定一个webserver(如github.com)和连接到该服务器所需的所有静态配置:端口号、HTTPS设置和首选网络协议(如HTTP/2或SPDY)。
     * 共享相同地址的url也可以共享相同的底层TCP套接字连接。共享连接具有显著的性能优势:
     *  更低的延迟、更高的吞吐量(由于TCP启动缓慢)和节约的电量。OkHttp使用ConnectionPool自动重用HTTP/1.x的连接和HTTP/2和SPDY的多路连接。
     */
    public final Address address; // 地址
    /**
     * 路由提供了实际连接到web服务器所需的动态信息。这是要尝试的特定IP地址(由DNS查询发现)、
     *  要使用的确切代理服务器(如果使用的是ProxySelector)以及要协商的TLS版本(用于HTTPS连接)。
     *
     * 一个地址可能有很多路由线路。例如，托管在多个数据中心中的web服务器可能在其DNS响应中产生多个IP地址。
     * */
    private Route route; // 路由
    private final ConnectionPool connectionPool;  // 连接池
    private final Object callStackTrace; // 日志

    private final RouteSelector routeSelector; // 路由选择器
    private int refusedStreamCount;  // 拒绝的次数
    private RealConnection connection;  // 连接
    private boolean released;  // 是否已经被释放
    private boolean canceled  // 是否被取消了
    private HttpCodec codec; // 连接所需要的流
```
从其成员属性中其实就可以看出StreamAllocation实际上就是，OkHtpp请求的各种组件的封装类。StreamAllocation相关的： 1.找到合适的连接 2.获取流的方法newStream() 3.找到合适的流的方法findConnection()我们都放在ConnectInterceptor拦截器中分析。

## HttpCodec
从StreamAllocation中我们已经提过HttpCodec其实就是“请求、连接、流”中的流，而HttpCodec只是接口，其两个实现类分别为Http1Codec和Http2Codec，分别对应Http1.1协议以及Http2.0协议。我们本文主要看一看Http1Codec：
```
    // 配置此流的客户端。对于HTTPS代理隧道，可以为null。
    final OkHttpClient client;
    // 拥有此流的流分配。对于HTTPS代理隧道，可以为null。
    final StreamAllocation streamAllocation;
    // 与服务器交互的输入输出流
    final BufferedSource source;
    final BufferedSink sink;
    // 当前流的状态，STATE_IDLE：空闲连接已准备好写入请求标头
    int state = STATE_IDLE;
    // 标题限制，HEADER_LIMIT：256 * 1024
    private long headerLimit = HEADER_LIMIT;
    
    public Http1Codec(OkHttpClient client
            , StreamAllocation streamAllocation, BufferedSource source, BufferedSink sink) {
        this.client = client;
        this.streamAllocation = streamAllocation;
        this.source = source;
        this.sink = sink;
    }
```
从Http1Codec的成员和构造方法可以看出，在初始化Http1Codec时就已经将与服务器交互的sink和source传入，用于最后一个拦截器CallServerInterceptor真正的发送请求和获取响应。

## ConnectionPool
在整个OkHttp的流程中，我们在哪里看到过ConnectionPool的身影呢？
1. 在OKHttpClient.Builder的构造方法里面，对ConnectionPool进行了初始化
2. 我们还在StreamAllocation的newStream方法看到过ConnectionPool。
3. StreamAllocation在调用findConnection方法寻找一个可以使用Connection，这里也涉及到ConnectionPool。findConnection方法在寻找Connection时，首先会尝试复用StreamAllocation本身的Connection,如果这个Connection不可用的话，那么就会去ConnectionPool去寻找合适的Connection。

总的来说，ConnectionPool负责所有的连接，包括连接的复用，以及无用连接的清理。OkHttp会将客户端和服务端所有的连接都抽象为Connection（实际实现类为RealConnection），而ConnectionPool就是为了管理所有Connection而设计的，其实际作用：在其时间允许的范围内复用Connection，并对其清理回收。外部通过调用get方法来获取一个可以使用Connection对象,通过put方法添加一个新的连接。

**get方法**
```
// ConnectionPool类：
    //  一个线性 collection，支持在两端插入和移除元素。
    // 名称 Deque 是“double ended queue（双端队列）”的缩写
    private final Deque<RealConnection> connections = new ArrayDeque<>();

    @Nullable
    RealConnection get(Address address, StreamAllocation streamAllocation, Route route) {
        assert (Thread.holdsLock(this));
        // 遍历connections
        for (RealConnection connection : connections) {
            // 查看该connection是否符合条件
            if (connection.isEligible(address, route)) {
                streamAllocation.acquire(connection, true);
                return connection;
            }
        }
        return null;
    }
    
// RealConnection类：
    // 此连接承载的当前流
    public final List<Reference<StreamAllocation>> allocations = new ArrayList<>();

    public boolean isEligible(Address address, @Nullable Route route) {
        // 当前Connection拥有的StreamAllocation是否超过的限制
        if (allocations.size() >= allocationLimit || noNewStreams) return false;

        // 地址的非主机（host）字段是否重叠（一样）
        if (!Internal.instance.equalsNonHost(this.route.address(), address)) return false;

        // 主机（host）是否完全匹配
        if (address.url().host().equals(this.route().address().url().host())) {
            return true;
        }

        // 此时我们没有主机名匹配。但是，如果满足我们的连接合并要求，我们仍然可以提供请求。

        // 1. 此连接必须是HTTP / 2。
        if (http2Connection == null) return false;

        // 2. 路由必须共享IP地址。这要求我们为两个主机提供DNS地址，这只发生在路由规划之后。我们无法合并使用代理的连接，因为代理不告诉我们源服务器的IP地址。
        if (route == null) return false;
        if (route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (this.route.proxy().type() != Proxy.Type.DIRECT) return false;
        if (!this.route.socketAddress().equals(route.socketAddress())) return false;

        // 3. 此连接的服务器证书必须涵盖新主机。
        if (route.address().hostnameVerifier() != OkHostnameVerifier.INSTANCE) return false;
        if (!supportsUrl(address.url())) return false;

        // 4. 证书固定必须与主机匹配。
        try {
            address.certificatePinner().check(address.url().host(), handshake().peerCertificates());
        } catch (SSLPeerUnverifiedException e) {
            return false;
        }

        return true;
    }
    
// StreamAllocation类：
    public void acquire(RealConnection connection, boolean reportedAcquired) {
        assert (Thread.holdsLock(connectionPool));
        if (this.connection != null) throw new IllegalStateException();
        // 保留连接
        this.connection = connection;
        this.reportedAcquired = reportedAcquired;
        // 将此分配流add进allocations中，用于RealConnection.isEligible方法判断当前Connection拥有的StreamAllocation是否超过的限制
        connection.allocations.add(new StreamAllocationReference(this, callStackTrace));
    }
```
简单总结一下：

1. isEligible方法（判断遍历的连接是否符合条件，即是否可复用）：
    
        1.如果这个 Connection 已经分配的数量(即 拥有的StreamAllocation)超过了分配限制或者被标记 则不符合。
        2.接着调用 equalsNonHost，主要是判断 Address 中非主机（host）字段是否重叠（一样），如果有不同的则不符合。
        3.然后就是判断 host 是否相同，如果相同(并且1和2也符合)那么对于当前的Address来说，这个Connection 便是可重用的。
        4.如果1、2、3都不符合，则若依旧满足某些条件，此连接仍可复用，具体满足的条件查看上面代码注解
2. acquire方法（StreamAllocation类）：

        1.保存遍历connections获取的可重用的连接
        2.将此StreamAllocation类的弱引用StreamAllocationReference添加add进此重用连接，判断当前Connection拥有的StreamAllocation是否超过的限制
        3.此方法保留的连接将被用于findConnection方法（上面ConnectInterceptor部分有说明）

**put方法**
```
    void put(RealConnection connection) {
        assert (Thread.holdsLock(this));
        // 是否开启异步的清理任务
        if (!cleanupRunning) {
            cleanupRunning = true;
            executor.execute(cleanupRunnable);
        }
        // add进connections
        connections.add(connection);
    }
```
put方法很简单，直接将Connection对象添加到connections双端队列。不过这里有一个地方需要注意，就是如果cleanupRunning为false，就会想线程池里面添加一个cleanupRunnable，这里的目的进行清理操作。此清理操作马上就分析。

**cleanup：清理无用的连接**
```
    private final Runnable cleanupRunnable = new Runnable() {
        @Override
        public void run() {
            // 这个cleanupRunnable是一个死循环的任务，只要cleanup方法不返回-1，就会一直执行。
            while (true) {
                // 调用cleanup查找并清理无用连接（返回以纳米为单位的持续时间）
                long waitNanos = cleanup(System.nanoTime());
                if (waitNanos == -1) return;
                // 当cleanup方法没有返回-1，当前的Runnable就会进入睡眠状态。
                if (waitNanos > 0) {
                    long waitMillis = waitNanos / 1000000L;
                    waitNanos -= (waitMillis * 1000000L);
                    synchronized (ConnectionPool.this) {
                        try {
                            // 等待上一次cleanup计算出的最长空闲的连接距离驱逐到期的时间
                            ConnectionPool.this.wait(waitMillis, (int) waitNanos);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
    };

    /**
     * 对此池执行维护，如果超出保持活动限制或空闲连接限制，则驱逐已空闲的连接最长。
     * 返回以纳米为单位的持续时间，直到下一次调用此方法为止。 如果不需要进一步清理，则返回 -1。
     */
    long cleanup(long now) {
        int inUseConnectionCount = 0;
        int idleConnectionCount = 0;
        RealConnection longestIdleConnection = null;
        long longestIdleDurationNs = Long.MIN_VALUE;

        // 找到要驱逐的连接，或下次驱逐到期的时间。
        synchronized (this) {
            for (Iterator<RealConnection> i = connections.iterator(); i.hasNext(); ) {
                RealConnection connection = i.next();

                // 如果正在使用该连接，请跳过继续搜索。
                // 用于清理可能泄露的StreamAllocation并返回正在使用此连接的 StreamAllocation的数量
                if (pruneAndGetAllocationCount(connection, now) > 0) {
                    inUseConnectionCount++;
                    continue;
                }
                // 空闲连接记住
                idleConnectionCount++;

                long idleDurationNs = now - connection.idleAtNanos;
                // 判断是否是最长空闲时间的连接
                if (idleDurationNs > longestIdleDurationNs) {
                    longestIdleDurationNs = idleDurationNs;
                    longestIdleConnection = connection;
                }
            }
            // 若当前Connection已经超过了最大的空闲时间
            // 或者空闲连接数大于最大空闲连接数量，应该被回收
            if (longestIdleDurationNs >= this.keepAliveDurationNs
                    || idleConnectionCount > this.maxIdleConnections) {
                // 将其从列表中删除，然后在下面（同步块的外部）将其关闭。
                connections.remove(longestIdleConnection);
            } else if (idleConnectionCount > 0) {
                // 返回保活时长 - 最长空闲时间的连接当前存活的时间（即该连接还有多久需要被清理）
                return keepAliveDurationNs - longestIdleDurationNs;
            } else if (inUseConnectionCount > 0) {
                // 所有连接都在使用中。说明所有连接都需要至少是保活时长才会被清理
                return keepAliveDurationNs;
            } else {
                // 无连接，空闲或正在使用中。
                cleanupRunning = false;
                return -1;
            }
        }
        // 3. 关闭连接的socket
        // 代码执行到此处说明此Connection已经超过了最大的空闲时间，应该被回收
        closeQuietly(longestIdleConnection.socket());

        // 继续清理
        return 0;
    }
    
    private int pruneAndGetAllocationCount(RealConnection connection, long now) {
        List<Reference<StreamAllocation>> references = connection.allocations;
        // 遍历当前RealConnection中保存的StreamAllocation的弱引用
        for (int i = 0; i < references.size(); ) {
            Reference<StreamAllocation> reference = references.get(i);
            // 若StreamAllocation的弱引用不为空，则跳过继续
            if (reference.get() != null) {
                i++;
                continue;
            }
            
            // 若StreamAllocation的弱引用为空
            StreamAllocation.StreamAllocationReference streamAllocRef =
                    (StreamAllocation.StreamAllocationReference) reference;
            String message = "A connection to " + connection.route().address().url()
                    + " was leaked. Did you forget to close a response body?";
            Platform.get().logCloseableLeak(message, streamAllocRef.callStackTrace);
            // 则需要移除该位置的引用
            references.remove(i);
            connection.noNewStreams = true;

            // 若references为空，即该连接已经没有了StreamAllocation使用，则该连接可以被清理
            if (references.isEmpty()) {
                connection.idleAtNanos = now - keepAliveDurationNs;
                return 0;
            }
        }

        return references.size();
    }
```
逻辑总结：
1. 遍历所有的连接，对每个连接调用 pruneAndGetAllocationCount 判断其是否闲置的连接。如果是正在使用中，则直接遍历一下个。
2. 对于闲置的连接，判断是否是当前空闲时间最长的。
3. 对于当前空闲时间最长的连接，如果其超过了设定的最长空闲时间（5分钟）或者是最大的空闲连接的数量（5个），则清理此连接。否则计算下次需要清理的时间，这样 cleanupRunnable 中的循环变会睡眠相应的时间，醒来后继续清理。

## ConnectInterceptor
在执行完CacheInterceptor之后会执行下一个拦截器——ConnectInterceptor，那么我们来看一下其intercept方法中的源码：
```
    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Request request = realChain.request();
        // 从拦截器链里得到StreamAllocation对象
        // 此StreamAllocation对象实际上是拦截器链的第二个参数，是在第一个拦截器中初始化的
        StreamAllocation streamAllocation = realChain.streamAllocation();

        // We need the network to satisfy this request. Possibly for validating a conditional GET.
        boolean doExtensiveHealthChecks = !request.method().equals("GET");
        /**
         * 用来编码Request，解码Response
         *  它有对应的两个子类， Http1Codec和Http2Codec， 分别对应Http1.1协议以及Http2.0协议，本文主要学习前者。
         *  在Http1Codec中主要包括两个重要的属性，即source和sink，它们分别封装了socket的输入和输出，
         *  CallServerInterceptor正是利用HttpCodec提供的I/O操作完成网络通信。
         * */
        HttpCodec httpCodec = streamAllocation.newStream(client, chain, doExtensiveHealthChecks);
        // 获取RealConnetion，实际网络Io传输对象（实际上此步很简单，只是返回上一步代码中获取到的connection）
        RealConnection connection = streamAllocation.connection();
        // 执行下一个拦截器
        return realChain.proceed(request, streamAllocation, httpCodec, connection);
    }
```
这个拦截器东西就这么多？哈哈，那是想多了，这个拦截器中的东西可都藏的深，有料的很呀。我们分别来看一下HttpCodec和RealConnection的获取过程吧。
```
// StreamAllocation类：
    public HttpCodec newStream(OkHttpClient client, Interceptor.Chain chain, boolean doExtensiveHealthChecks) {
        //1. 获取设置的连接超时时间，读写超时的时间，以及是否进行重连。 
        int connectTimeout = chain.connectTimeoutMillis();
        int readTimeout = chain.readTimeoutMillis();
        int writeTimeout = chain.writeTimeoutMillis();
        int pingIntervalMillis = client.pingIntervalMillis();
        boolean connectionRetryEnabled = client.retryOnConnectionFailure();

        try {
            // 2. 获取健康可用的连接
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout,
                    writeTimeout, pingIntervalMillis, connectionRetryEnabled,
                    doExtensiveHealthChecks);
            //3. 通过ResultConnection初始化，对请求以及结果 编解码的类（分http 1.1 和http 2.0）。
            // 这里主要是初始化，在后面一个拦截器才用到这相关的东西。
            HttpCodec resultCodec = resultConnection.newCodec(client, chain, this);

            synchronized (connectionPool) {
                codec = resultCodec;
                // 返回HttpCodec
                return resultCodec;
            }
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }
```
从上面代码中来看，这个方法好像就做了两件事：

1. 调用findHealthyConnection获取一个RealConnection对象。 
2. 通过获取到的RealConnection来生成一个HttpCodec对象并返回之。

那么我们接着看findHealthyConnection方法：
```
// StreamAllocation类：
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout,
            int writeTimeout, int pingIntervalMillis,boolean connectionRetryEnabled,
                            boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            // 获取RealConnection对象
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout,
                    pingIntervalMillis, connectionRetryEnabled);

            // 如果这是一个全新的连接，我们可以跳过广泛的健康检查。
            synchronized (connectionPool) {
                if (candidate.successCount == 0) {
                    // 直接返回
                    return candidate;
                }
            }

            /**
             * 对链接池中不健康的链接做销毁处理
             *  不健康的RealConnection条件为如下几种情况：
             *      RealConnection对象 socket没有关闭
             *      socket的输入流没有关闭
             *      socket的输出流没有关闭
             *      http2时连接没有关闭
             * */
            if (!candidate.isHealthy(doExtensiveHealthChecks)) {
                // 销毁资源（该方法中会调用deallocate(解除分配)方法
                //  获取需要释放的Socket连接，并执行closeQuietly方法关闭该Socket）
                noNewStreams();
                continue;
            }

            return candidate;
        }
    }
```
代码中可以看出获取RealConnection对象的操作又交给了findConnection方法：
```
// StreamAllocation类：
    private RealConnection findConnection(int connectTimeout, int readTimeout, 
            int writeTimeout,int pingIntervalMillis
            , boolean connectionRetryEnabled) throws IOException {
        boolean foundPooledConnection = false;
        RealConnection result = null;
        Route selectedRoute = null;
        Connection releasedConnection;
        Socket toClose;
        // 1. 同步线程池，来获取里面的连接
        synchronized (connectionPool) {
            // 2. 做些判断，是否已经释放，是否编解码类为空，是否用户已经取消
            if (released) throw new IllegalStateException("released");
            if (codec != null) throw new IllegalStateException("codec != null");
            if (canceled) throw new IOException("Canceled");

            // (尝试复用)尝试使用已分配的连接。我们需要在这里小心，因为我们已经分配的连接可能已被限制创建新流。
            releasedConnection = this.connection;
            toClose = releaseIfNoNewStreams();
            if (this.connection != null) {
                // We had an already-allocated connection and it's good.
                result = this.connection;
                releasedConnection = null;
            }
            if (!reportedAcquired) {
                // If the connection was never reported acquired, don't report it as released!
                releasedConnection = null;
            }

            if (result == null) {
                /**
                 * 4. 尝试在连接池中获取一个连接，get方法中会直接调用，注意最后一个参数为空
                 *
                 * Internal 是一个抽象类，而该类的实现则在OkHttpClient的static{}静态代码块中（为一匿名内部类）
                 *  而其get方法实际上会调onnectionPool连接池中的get方法使用一个for循环，在连接池里面，寻找合格的连接
                 *  而合格的连接会通过，StreamAllocation中的acquire方法，更新connection的值。
                 * */
                Internal.instance.get(connectionPool, address, this, null);
                if (connection != null) {
                    foundPooledConnection = true;
                    result = connection;
                } else {
                    selectedRoute = route;
                }
            }
        }
        closeQuietly(toClose);

        if (releasedConnection != null) {
            eventListener.connectionReleased(call, releasedConnection);
        }
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
        }
        if (result != null) {
            // 如果我们找到已经分配或池化的连接，我们就完成了。
            return result;
        }

        // 如果我们需要选择路线，请选择一个。这是一个阻止操作。
        boolean newRouteSelection = false;
        if (selectedRoute == null && (routeSelection == null || !routeSelection.hasNext())) {
            newRouteSelection = true;
            // 对于线路Route的选择，可以深究一下这个RouteSeletor
            routeSelection = routeSelector.next();
        }
        //5. 继续线程池同步下去获取连接
        synchronized (connectionPool) {
            if (canceled) throw new IOException("Canceled");

            if (newRouteSelection) {
                // 6. 现在我们有了一组IP地址(线路Route)，再次尝试从池中获取连接。
                List<Route> routes = routeSelection.getAll();
                for (int i = 0, size = routes.size(); i < size; i++) {
                    Route route = routes.get(i);
                    Internal.instance.get(connectionPool, address, this, route);
                    if (connection != null) {
                        foundPooledConnection = true;
                        result = connection;
                        this.route = route;
                        break;
                    }
                }
            }
            // 没有找到
            if (!foundPooledConnection) {
                if (selectedRoute == null) {
                    selectedRoute = routeSelection.next();
                }
                
                // 创建连接并立即将其分配给此分配。这时可能异步cancel（）会中断我们即将进行的握手。
                route = selectedRoute;
                refusedStreamCount = 0;
                // 7. 如果前面这么寻找，都没在连接池中找到可用的连接，那么就新建一个
                result = new RealConnection(connectionPool, selectedRoute);
                // 更新connection，即RealConnection
                acquire(result, false);
            }
        }

        // 如果我们第二次发现了汇集连接，我们就完成了。
        if (foundPooledConnection) {
            eventListener.connectionAcquired(call, result);
            return result;
        }

        /**
         * 8. 做TCP + TLS握手。这是一个阻止操作。
            调用RealConnection的connect方法打开一个Socket链接
         *  这里就是就是连接的操作了，终于找到连接的正主了，这里会调用RealConnection的连接方法，进行连接操作。
         *     如果是普通的http请求，会使用Socket进行连接
         *     如果是https，会进行相应的握手，建立通道的操作。
         * */
        result.connect(connectTimeout, readTimeout, writeTimeout, pingIntervalMillis,
                connectionRetryEnabled, call, eventListener);
        routeDatabase().connected(result.route());

        Socket socket = null;
        synchronized (connectionPool) {
            reportedAcquired = true;

            // 9. 最后就是同步加到 连接池里面了
            Internal.instance.put(connectionPool, result);

            // 最后加了一个多路复用的判断，这个是http2才有的
            // 如果另外的多路复用连接在同时创建，则释放此连接，用另外的链接
            if (result.isMultiplexed()) {
                socket = Internal.instance.deduplicate(connectionPool, address, this);
                result = connection;
            }
        }
        closeQuietly(socket);

        eventListener.connectionAcquired(call, result);
        return result;
    }
```
这段代码有点多，具体讲解在代码注释当中，简单总结一下：

1. 首先做了点判断是否已取消、编码codec是否为空等
2. 复用连接，能复用则复用
3. 若第2步不能复用，则从连接池ConnectionPool中获取一个
4. 若第3步获取的connection不为空，则返回
5. 若第3步获取的connection为空则继续从连接池中获取连接(此次和第3步的区别为，第二次传Route参数)，若返回不为空则返回
6. 若前5步依旧没有获取到可用的connection，则重新创建一个，并放入连接池ConnectionPool中
7. 最终调用RealConnection的connect方法打开一个Socket链接，已供下一个拦截器使用

接下来我们继续了解一下RealConnection的connect连接操作：
```
// RealConnection类：
    public void connect(int connectTimeout, int readTimeout, int writeTimeout,
                        int pingIntervalMillis, boolean connectionRetryEnabled, Call call,
                        EventListener eventListener) {
        // protocol（连接协议）是用来检查此连接是否已经建立
        if (protocol != null) throw new IllegalStateException("already connected");

        RouteException routeException = null;
        // ConnectionSpec指定了Socket连接的一些配置
        List<ConnectionSpec> connectionSpecs = route.address().connectionSpecs();
        // 连接规格选择器（用于选择连接，比如：隧道连接和Socket连接）
        ConnectionSpecSelector connectionSpecSelector = new ConnectionSpecSelector(connectionSpecs);

        if (route.address().sslSocketFactory() == null) {
            if (!connectionSpecs.contains(ConnectionSpec.CLEARTEXT)) {
                throw new RouteException(new UnknownServiceException(
                        "CLEARTEXT communication not enabled for client"));
            }
            String host = route.address().url().host();
            if (!Platform.get().isCleartextTrafficPermitted(host)) {
                throw new RouteException(new UnknownServiceException(
                        "CLEARTEXT communication to " + host + " not permitted by network " +
                                "security policy"));
            }
        }

        while (true) {
            try {
                // 是否执行隧道连接，requiresTunnel()方法实现其实很简单：判断address的sslSocketFactory是否为空并且proxy代理类型是否为Http
                if (route.requiresTunnel()) {
                    connectTunnel(connectTimeout, readTimeout, writeTimeout, call, eventListener);
                    if (rawSocket == null) {
                        // We were unable to connect the tunnel but properly closed down our
                        // resources.
                        break;
                    }
                } else {
                    // 执行Socket连接
                    connectSocket(connectTimeout, readTimeout, call, eventListener);
                }
                // 建立协议
                establishProtocol(connectionSpecSelector, pingIntervalMillis, call, eventListener);
                eventListener.connectEnd(call, route.socketAddress(), route.proxy(), protocol);
                break;
            } catch (IOException e) {
                closeQuietly(socket);
                closeQuietly(rawSocket);
                socket = null;
                rawSocket = null;
                source = null;
                sink = null;
                handshake = null;
                protocol = null;
                http2Connection = null;

                eventListener.connectFailed(call, route.socketAddress(), route.proxy(), null, e);

                if (routeException == null) {
                    routeException = new RouteException(e);
                } else {
                    routeException.addConnectException(e);
                }

                if (!connectionRetryEnabled || !connectionSpecSelector.connectionFailed(e)) {
                    throw routeException;
                }
            }
        }

        if (route.requiresTunnel() && rawSocket == null) {
            ProtocolException exception = new ProtocolException("Too many tunnel connections " +
                    "attempted: "
                    + MAX_TUNNEL_ATTEMPTS);
            throw new RouteException(exception);
        }

        if (http2Connection != null) {
            synchronized (connectionPool) {
                allocationLimit = http2Connection.maxConcurrentStreams();
            }
        }
    }
    
    /**
     * 是否所有工作都是通过代理隧道构建HTTPS连接。这里的问题是代理服务器可以发出认证质询，然后关闭连接。
     */
    private void connectTunnel(int connectTimeout, int readTimeout, int writeTimeout
        , Call call,EventListener eventListener) throws IOException {
        //1、创建隧道请求对象
        Request tunnelRequest = createTunnelRequest();
        HttpUrl url = tunnelRequest.url();
        //for循环： MAX_TUNNEL_ATTEMPTS == 21
        for (int i = 0; i < MAX_TUNNEL_ATTEMPTS; i++) {
            //2、打开socket链接
            connectSocket(connectTimeout, readTimeout, call, eventListener);
            //3、请求开启隧道并返回tunnelRequest(开启隧道会用到Socket连接中的sink和source)
            tunnelRequest = createTunnel(readTimeout, writeTimeout, tunnelRequest, url);
            //4、成功开启了隧道，跳出while循环
            if (tunnelRequest == null) break;

            // 隧道未开启成功，关闭相关资源，继续while循环    
            closeQuietly(rawSocket);
            rawSocket = null;
            sink = null;
            source = null;
            eventListener.connectEnd(call, route.socketAddress(), route.proxy(), null);
        }
    }

    /**
     * 完成在原始套接字上构建完整HTTP或HTTPS连接所需的所有工作。
     */
    private void connectSocket(int connectTimeout, int readTimeout, Call call,
                               EventListener eventListener) throws IOException {
        Proxy proxy = route.proxy();
        Address address = route.address();
        //1、初始化Socket
        rawSocket = proxy.type() == Proxy.Type.DIRECT || proxy.type() == Proxy.Type.HTTP
                ? address.socketFactory().createSocket()
                : new Socket(proxy);// 使用SOCKS的代理服务器
        
        eventListener.connectStart(call, route.socketAddress(), proxy);
        rawSocket.setSoTimeout(readTimeout);
        try {
            //2、打开socket链接
            Platform.get().connectSocket(rawSocket, route.socketAddress(), connectTimeout);
        } catch (ConnectException e) {
            ConnectException ce = new ConnectException("Failed to connect to " + route.socketAddress());
            ce.initCause(e);
            throw ce;
        }
        
        try {
            // 注意：Sink可以简单的看做OutputStream，Source可以简单的看做InputStream
            // 而这里的sink和source，被用于打开隧道连接和最后一个拦截器用于真正的网络请求发送和获取响应
            source = Okio.buffer(Okio.source(rawSocket));
            sink = Okio.buffer(Okio.sink(rawSocket));
        } catch (NullPointerException npe) {
            if (NPE_THROW_WITH_NULL.equals(npe.getMessage())) {
                throw new IOException(npe);
            }
        }
    }
    
    private Request createTunnel(int readTimeout, int writeTimeout, Request tunnelRequest,
                                 HttpUrl url) throws IOException {
        // 拼接CONNECT命令
        String requestLine = "CONNECT " + Util.hostHeader(url, true) + " HTTP/1.1";
        while (true) {//又一个while循环
            //对应http/1.1 编码HTTP请求并解码HTTP响应
            Http1Codec tunnelConnection = new Http1Codec(null, null, source, sink);
            //发送CONNECT，请求打开隧道链接，
            tunnelConnection.writeRequest(tunnelRequest.headers(), requestLine);
            //完成链接
            tunnelConnection.finishRequest();
            //构建response，操控的是inputStream流
            Response response = tunnelConnection.readResponseHeaders(false)
                    .request(tunnelRequest)
                    .build();

            switch (response.code()) {
                case HTTP_OK:
                    return null;
                case HTTP_PROXY_AUTH://表示服务器要求对客户端提供访问证书，进行代理认证
                    //进行代理认证
                    tunnelRequest = route.address().proxyAuthenticator().authenticate(route,
                            response);
                    //代理认证不通过
                    if (tunnelRequest == null)
                        throw new IOException("Failed to authenticate with proxy");

                    //代理认证通过，但是响应要求close，则关闭TCP连接此时客户端无法再此连接上发送数据
                    if ("close".equalsIgnoreCase(response.header("Connection"))) {
                        return tunnelRequest;
                    }
                    break;

            }
        }
    }
    
    private void establishProtocol(ConnectionSpecSelector connectionSpecSelector) throws IOException {
        //如果不是ssl
        if (route.address().sslSocketFactory() == null) {
            protocol = Protocol.HTTP_1_1;
            socket = rawSocket;
            return;
        }
        //如果是sll
        connectTls(connectionSpecSelector);
        //如果是HTTP2
        if (protocol == Protocol.HTTP_2) {
            socket.setSoTimeout(0); // HTTP/2 connection timeouts are set per-stream.
            http2Connection = new Http2Connection.Builder(true)
                    .socket(socket, route.address().url().host(), source, sink)
                    .listener(this)
                    .build();
            http2Connection.start();
        }
    }
```
**什么是隧道呢？** 隧道技术（Tunneling）是HTTP的用法之一，使用隧道传递的数据（或负载）可以是不同协议的数据帧或包，或者简单的来说隧道就是利用一种网络协议来传输另一种网络协议的数据。比如A主机和B主机的网络而类型完全相同都是IPv6的网，而链接A和B的是IPv4类型的网络,A和B为了通信，可以使用隧道技术，数据包经过Ipv4数据的多协议路由器时，将IPv6的数据包放入IPv4数据包；然后将包裹着IPv6数据包的IPv4数据包发送给B，当数据包到达B的路由器，原来的IPv6数据包被剥离出来发给B。 

SSL隧道：SSL隧道的初衷是为了通过防火墙来传输加密的SSL数据，此时隧道的作用就是将非HTTP的流量（SSL流量）传过防火墙到达指定的服务器。

**怎么打开隧道？** HTTP提供了一个CONNECT方法 ,它是HTTP/1.1协议中预留给能够将连接改为管道方式的代理服务器，该方法就是用来建议一条web隧道。客户端发送一个CONNECT请求给隧道网关请求打开一条TCP链接，当隧道打通之后，客户端通过HTTP隧道发送的所有数据会转发给TCP链接，服务器响应的所有数据会通过隧道发给客户端。 
（注：以来内容来源参考《计算机网络第五版》和《HTTP权威指南》第八章的有关内容，想深入了解的话可以查阅之。） 
关于CONNECT在HTTP 的首部的内容格式，可以简单如下表示： CONNECT hostname:port HTTP/1.1 

这部分就不深入分析啦，感兴趣的小伙伴自行查询吧。

## CallServerInterceptor
在Okhttp拦截器链上CallServerInterceptor拦截器是最后一个拦截器，该拦截器前面的拦截器ConnectInterceptor主要负责打开TCP链接。而CallServerInterceptor的主要功能就是—向服务器发送请求，并最终返回Response对象供客户端使用。

>小知识点：100-continue 是用于客户端在发送 post 数据给服务器时，征询服务器是否处理 post 的数据，如果不处理，客户端则不上传 post 数据，正常情况下服务器收到请求后，返回 100 或错误码。
```
    @Override
    public Response intercept(Chain chain) throws IOException {
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        // 获取http请求流（于上一个拦截器创建）
        HttpCodec httpCodec = realChain.httpStream();
        StreamAllocation streamAllocation = realChain.streamAllocation();
        RealConnection connection = (RealConnection) realChain.connection();
        Request request = realChain.request();

        long sentRequestMillis = System.currentTimeMillis();

        realChain.eventListener().requestHeadersStart(realChain.call());
        // 向服务器发送请求
        httpCodec.writeRequestHeaders(request);
        realChain.eventListener().requestHeadersEnd(realChain.call(), request);

        Response.Builder responseBuilder = null;
        // 检测是否有请求body
        if (HttpMethod.permitsRequestBody(request.method()) && request.body() != null) {
            // 如果请求中有“Expect：100-continue”标头，请在发送请求正文之前等待“HTTP / 1.1 100 继续”响应。
            // 如果我们没有得到它，请返回我们所做的事情（例如4xx响应）而不传输请求体。
            if ("100-continue".equalsIgnoreCase(request.header("Expect"))) {
                httpCodec.flushRequest();
                realChain.eventListener().responseHeadersStart(realChain.call());
                // 构建responseBuilder对象
                responseBuilder = httpCodec.readResponseHeaders(true);
            }

            if (responseBuilder == null) {
                // 如果满足“Expect：100-continue”期望，请向服务器发送请求body
                realChain.eventListener().requestBodyStart(realChain.call());
                long contentLength = request.body().contentLength();
                CountingSink requestBodyOut =
                        new CountingSink(httpCodec.createRequestBody(request, contentLength));
                BufferedSink bufferedRequestBody = Okio.buffer(requestBodyOut);
                // 写入请求体到bufferedRequestBody中
                request.body().writeTo(bufferedRequestBody);
                // 将所有缓冲的字节推送到其最终目标，并释放此接收器保存的资源。
                bufferedRequestBody.close();
                realChain.eventListener()
                        .requestBodyEnd(realChain.call(), requestBodyOut.successfulCount);
            } else if (!connection.isMultiplexed()) {
                // 如果未满足“Expect：100-continue”期望，则阻止重用HTTP / 1 连接。否则，我们仍然有义务将请求正文传输给使连接保持一致状态。
                streamAllocation.noNewStreams();
            }
        }
        // 实际是调用了 sink.flush(), 来刷数据
        httpCodec.finishRequest();
        // 读取响应头信息，状态码等
        if (responseBuilder == null) {
            realChain.eventListener().responseHeadersStart(realChain.call());
            responseBuilder = httpCodec.readResponseHeaders(false);
        }
        // 构建Response, 写入本次Request，握手情况，请求时间，得到的结果时间
        Response response = responseBuilder
                .request(request)
                .handshake(streamAllocation.connection().handshake())
                .sentRequestAtMillis(sentRequestMillis)
                .receivedResponseAtMillis(System.currentTimeMillis())
                .build();

        int code = response.code();
        if (code == 100) {
            // 服务器发送了100-continue，即使我们没有请求。也再次尝试阅读实际的回复
            responseBuilder = httpCodec.readResponseHeaders(false);

            response = responseBuilder
                    .request(request)
                    .handshake(streamAllocation.connection().handshake())
                    .sentRequestAtMillis(sentRequestMillis)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();

            code = response.code();
        }

        realChain.eventListener()
                .responseHeadersEnd(realChain.call(), response);
        // 通过状态码判断以及是否webSocket判断，是否返回一个空的body
        if (forWebSocket && code == 101) {
            // Connection is upgrading, but
            // we need to ensure interceptors see a non-null
            // response body.
            response = response.newBuilder()
                    .body(Util.EMPTY_RESPONSE)
                    .build();
        } else {
            response = response.newBuilder()
                    // 返回读取响应正文的流，并构建客户端可用的RealResponseBody
                    .body(httpCodec.openResponseBody(response))
                    .build();
        }
        // 如果设置了连接 close ,断开连接
        if ("close".equalsIgnoreCase(response.request().header("Connection"))
                || "close".equalsIgnoreCase(response.header("Connection"))) {
            streamAllocation.noNewStreams();
        }
        // HTTP 204(no content) 代表响应报文中包含若干首部和一个状态行，但是没有实体的主体内容。
        // HTTP 205(reset content) 表示响应执行成功，重置页面（Form表单），方便用户下次输入
        // 这里做了同样的处理，就是抛出协议异常。
        if ((code == 204 || code == 205) && response.body().contentLength() > 0) {
            throw new ProtocolException(
                    "HTTP " + code + " had non-zero Content-Length: " + response.body().contentLength());
        }

        return response;
    }
```
从CallServerInterceptor拦截器的代码中看到OkHttp是通过HttpCodec来发送请求与获取响应的，那么我们分别来看一看这两步操作：
1. 发送请求
```
// Http1Codec类：
    @Override
    public void writeRequestHeaders(Request request) throws IOException {
        // 返回请求状态行，如“GET / HTTP / 1.1”。
        String requestLine = RequestLine.get(
                request, streamAllocation.connection().route().proxy().type());
        // 写入请求
        writeRequest(request.headers(), requestLine);
    }

    /**
     * 通过OkIO的Sink对象（该对象可以看做Socket的OutputStream对象）来向服务器发送请求的。
     */
    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (state != STATE_IDLE) throw new IllegalStateException("state: " + state);
        sink.writeUtf8(requestLine).writeUtf8("\r\n");
        for (int i = 0, size = headers.size(); i < size; i++) {
            sink.writeUtf8(headers.name(i))
                    .writeUtf8(": ")
                    .writeUtf8(headers.value(i))
                    .writeUtf8("\r\n");
        }
        sink.writeUtf8("\r\n");
        state = STATE_OPEN_REQUEST_BODY;
    }
```
我们知道HTTP支持post,delete,get,put等方法，而post，put等方法是需要请求体的（在Okhttp中用RequestBody来表示）。所以接着writeRequestHeaders之后Okhttp对请求体也做了响应的处理，从上面分析处我们也知道请求体是通过RequestBody的writeTo方法发送出去的(实际上是调用bufferedRequestBody对象的write方法，RequestBody的实例可能是FormBody或者是自定义的ReqeustBody)：
```
// 使用post简单示例：
        // 构建RequestBody（FormBody是RequestBody实现类）
        FormBody.Builder formBody = new FormBody.Builder();
        if(mParams != null && !mParams.isEmpty()) {
            for (Map.Entry<String,String> entry: mParams.entrySet()) {
                formBody.add(entry.getKey(),entry.getValue());
            }
        }
        // 构建RequestBody并将传入的参数保存在FormBody的encodedNames和encodedValues两个成员集合内
        RequestBody form = formBody.build();
        // 添加请求头
        Request.Builder builder = new Request.Builder();
        if(mHeader != null && !mHeader.isEmpty()) {
            for (Map.Entry<String,String> entry: mHeader.entrySet()) {
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        // 创建请求的Request 对象
        final Request request = builder
                .post(form)
                .url(mUrl)
                .build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendFailure();
                LoggerUtil.d("onFailure :  "+e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) {
                responseProcess(response);
            }
        });
    
//  FormBody类 —— 写入请求体：
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        writeOrCountBytes(sink, false);
    }


    private long writeOrCountBytes(@Nullable BufferedSink sink, boolean countBytes) {
        long byteCount = 0L;

        Buffer buffer;
        if (countBytes) {
            buffer = new Buffer();
        } else {
            buffer = sink.buffer();
        }
        // 将请求体写入sink的缓存
        for (int i = 0, size = encodedNames.size(); i < size; i++) {
            if (i > 0) buffer.writeByte('&');
            buffer.writeUtf8(encodedNames.get(i));
            buffer.writeByte('=');
            buffer.writeUtf8(encodedValues.get(i));
        }

        if (countBytes) {
            byteCount = buffer.size();
            buffer.clear();
        }

        return byteCount;
    }
```
可以看出请求体是通过writeTo方法写入sink缓存内，最后会通过bufferedRequestBody.close();方法将请求体发送到服务器并释放资源（拦截器逻辑中有说明）。

2. 获取响应信息
```
// Http1Codec类：
    @Override
    public Response.Builder readResponseHeaders(boolean expectContinue) throws IOException {
        if (state != STATE_OPEN_REQUEST_BODY && state != STATE_READ_RESPONSE_HEADERS) {
            throw new IllegalStateException("state: " + state);
        }

        try {
            // HTTP响应状态行，如“HTTP / 1.1 200 OK”
            StatusLine statusLine = StatusLine.parse(readHeaderLine());

            Response.Builder responseBuilder = new Response.Builder()
                    .protocol(statusLine.protocol) // http协议版本
                    .code(statusLine.code) // http响应状态码
                    .message(statusLine.message) // http的message :like "OK" or "Not Modified"
                    .headers(readHeaders()); // 读取响应报头

            if (expectContinue && statusLine.code == HTTP_CONTINUE) {
                return null;
            } else if (statusLine.code == HTTP_CONTINUE) {
                state = STATE_READ_RESPONSE_HEADERS;
                return responseBuilder;
            }

            state = STATE_OPEN_RESPONSE_BODY;
            return responseBuilder;
        } catch (EOFException e) {
            // 服务器在发送响应之前结束流。
            IOException exception =
                    new IOException("unexpected end of stream on " + streamAllocation);
            exception.initCause(e);
            throw exception;
        }
    }

    private String readHeaderLine() throws IOException {
        // 通过source读取
        String line = source.readUtf8LineStrict(headerLimit);
        headerLimit -= line.length();
        return line;
    }
    
    public Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        // 读取响应报头数据，响应报头和响应正文数据之间是有空行分隔开的，当读取到的数据为空行时表示响应报头读取完毕
        for (String line; (line = readHeaderLine()).length() != 0; ) {
            Internal.instance.addLenient(headers, line);
        }
        return headers.build();
    }
```
可以看出上面代码只是获取了响应头部分的数据，我们再来看一下读取响应正文的代码：
```
// CallServerInterceptor#intercept：
    response = response.newBuilder()
        // 上面分析时说明过此处为构建客户端可用的响应体RealResponseBody
        .body(httpCodec.openResponseBody(response))
      .build();

// Http1Codec类：
    @Override
    public ResponseBody openResponseBody(Response response) throws IOException {
        streamAllocation.eventListener.responseBodyStart(streamAllocation.call);
        String contentType = response.header("Content-Type");
        // 判断是否有响应体（可从响应头信息中判断）
        if (!HttpHeaders.hasBody(response)) {
            Source source = newFixedLengthSource(0);
            return new RealResponseBody(contentType, 0, Okio.buffer(source));
        }
        // 有响应体，根据不同情况，构造对应的Socket的InputStream的Source对象（用于后面获取响应体）
        
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            Source source = newChunkedSource(response.request().url());
            return new RealResponseBody(contentType, -1L, Okio.buffer(source));
        }

        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            Source source = newFixedLengthSource(contentLength);
            return new RealResponseBody(contentType, contentLength, Okio.buffer(source));
        }

        return new RealResponseBody(contentType, -1L, Okio.buffer(newUnknownLengthSource()));
    }
```
逻辑很简单，openResponseBody将Socket的输入流InputStream对象交给OkIo的Source对象(在本篇博文中只需简单的将Sink作为Socket的输入流，Source作为Socket的输入流看待即可），然后封装成RealResponseBody（该类是ResponseBody的子类）作为Response的body。那么我们怎么通过这个body来获取服务器发送过来的字符串呢？我们上面在分析缓存拦截器时提到过，我们获取网络数据最后一步其实就是通过调用ResponseBody.string()方法：
```
// ResponseBody类：
    public final String string() throws IOException {
        BufferedSource source = source();
        try {
            Charset charset = Util.bomAwareCharset(source, charset());
            //InputStream 读取数据
            return source.readString(charset);
        } finally {
            Util.closeQuietly(source);
        }
    }
```
在此处调用source.readString不仅来读取服务器的数据还需要缓存通过缓存拦截器缓存响应体(具体详看上方分析的缓存拦截器CacheInterceptor)。需要注意的是该方法最后调用closeQuietly来关闭了当前请求的InputStream输入流，所以string()方法只能调用一次,再次调用的话会报错，毕竟输入流已经关闭了。

至此，经历一周的时间，终于分析完整个流程，不过实际上还有一部分没有去深入了解，比如：路由、路由选择器、连接规格选择器等等，留待后续研究吧。


![](https://user-gold-cdn.xitu.io/2019/8/24/16cc2a8642da6e6d?w=220&h=220&f=png&s=35322)

**参考链接：**

https://blog.csdn.net/chunqiuwei/column/info/16213

https://juejin.im/post/5a6da6e7f265da3e303cbcb6

https://www.jianshu.com/p/5bcdcfe9e05c

https://www.jianshu.com/p/c963617ea6bc

https://www.jianshu.com/p/6166d28983a2

...

<font color=#ff0000>（注：若有什么地方阐述有误，敬请指正。欢迎指点交流）</font>