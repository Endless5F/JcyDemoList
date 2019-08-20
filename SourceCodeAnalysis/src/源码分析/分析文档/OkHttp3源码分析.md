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
        // 初始化分类流：OkHtpp请求的各种组件的封装类
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
    // 1. 初始化一个socket连接流对象
    streamAllocation = new StreamAllocation(
            client.connectionPool(), createAddress(request.url()), callStackTrace);

    int followUpCount = 0;
    Response priorResponse = null;
    while (true) { // 开启死循环，用于执行第一个拦截器或者请求的失败重连
        if (canceled) {
            streamAllocation.release();
            throw new IOException("Canceled");
        }

        Response response = null;
        boolean releaseConnection = true;
        try {
            // 2. 执行下一个拦截器，即BridgeInterceptor
            response = ((RealInterceptorChain) chain).proceed(request, streamAllocation, null, null);
            releaseConnection = false;
        } catch (RouteException e) {
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
            boolean requestSendStarted = !(e instanceof ConnectionShutdownException);
            if (!recover(e, requestSendStarted, request)) throw e;
            releaseConnection = false;
            continue;
        } finally {
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
         * 4. 来检查是否需要进行重定向操作
         * 是否需要进行请求重定向，是根据http请求的响应码来决定的，
         * 因此，在followUpRequest方法中，将会根据响应userResponse，获取到响应码，
         * 并从连接池StreamAllocation中获取连接，然后根据当前连接，得到路由配置参数Route。
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

        if (followUp.body() instanceof UnrepeatableRequestBody) {
            streamAllocation.release();
            throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
        }
        // 7. 检查重定向（失败重连）请求，和当前的请求，是否为同一个连接
        if (!sameConnection(response, followUp.url())) {
            streamAllocation.release();
            streamAllocation = new StreamAllocation(
                    client.connectionPool(), createAddress(followUp.url()), callStackTrace);
        } else if (streamAllocation.codec() != null) {
            throw new IllegalStateException("Closing the body of " + response
                    + " didn't close its backing stream. Bad interceptor?");
        }

        request = followUp;
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

    boolean transparentGzip = false;
    if (userRequest.header("Accept-Encoding") == null && userRequest.header("Range") == null) {
        transparentGzip = true;
        requestBuilder.header("Accept-Encoding", "gzip");
    }

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
    
    // 响应头， 如果没有自定义配置cookieJar == null，则什么都不做
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