不知不觉已经跟MQTT打交道半年了，才惊醒的发现我也算从事的物联网方法(Android端)，一直以来只是单纯的使用MQTT连接、发布和接收消息等，也没在乎其Client(Android)端的源码是怎样实现的，要不是最近项目出现一个小问题困扰了很久可能也不会引发我看一看MQTT的源码实现。好啦让我们开始了解MQTT的神奇之处吧。<font color=#ff0000>(注：若有什么地方阐述有误，敬请指正。)</font>

## 前言
阅读本文前，默认读者已经熟知MQTT的Android端使用，Client代表客户端，Broker代表服务端，此篇源码分析主要以MQTT客户端和服务端建立连接过程为主线讲解。基础了解Mqtt报文格式等，可以参考下MQTT协议官网中文地址：

https://mcxiaoke.gitbooks.io/mqtt-cn/content/mqtt/02-ControlPacketFormat.html

org.eclipse.paho工程源码分析涉及到的类：
* MqttAndroidClient
* MqttService
* MqttConnection
* MqttAsyncClient
* ConnectActionListener
* ClientComms
* CommsSender
* CommsReceiver
* ClientState
* CommsCallback

## 源码分析准备
为方便分析源码先贴上一段工程里连接MQTT的代码：
```
// 自己工程中关于MQTT连接类：
String uri = "";
if(isSSL){
    uri = "ssl://" + ip + ":" + port;
} else{
    uri = "tcp://" + ip + ":" + port;
}

MqttConnectOptions conOpt = new MqttConnectOptions();
try{
    conOpt.setSocketFactory(get2SSLSocketFactory(clientIns, ins, keypassword, keypassword));
} catch(MqttSecurityException e){
    e.printStackTrace();
}
conOpt.setUserName("mqttservice");
char[] password = "mqttservice".toCharArray();
conOpt.setPassword(password);
conOpt.setConnectionTimeout(5);
conOpt.setCleanSession(false);//设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
conOpt.setKeepAliveInterval(60);//The default value is 60 seconds
String mClientId = NetUtil.getLocalMacAddress();// 获取本地网络mac地址
String[] clientIds = new String[1];
clientIds[0]=mClientId;
clientInfo =uri +mClientId;
mMqttCallback =new MqttConnectCallback(mContext, clientInfo);
myClient =new MqttAndroidClient(mContext, uri, mClientId);
myClient.setCallback(mMqttCallback);
// IMqttActionListener的实现类，动态赋值为连接状态CONNECT
final ActionListener callback = new ActionListener(ActionType.CONNECT);

String topic = "/client/" + UUID.randomUUID().toString();
int qos = 0;
boolean retained = true;
try{
    // 设置遗嘱消息：当客户端断开连接时，发送给相关的订阅者的遗嘱消息
    // 具体了解遗嘱消息请参考：https://www.jianshu.com/p/a5c6b768ed55
    conOpt.setWill(topic, "offline".getBytes(), qos, retained);
} catch(Exception e){
    callback.onFailure(null, e);
}
try{
    myClient.connect(conOpt, null, callback);
} catch(Exception e){
    callback.onFailure(null, e);
}
```
根据上述代码可以看出，MQTT的连接是由MqttAndroidClient的connect函数而起，MqttAndroidClient对象初始化时传入了uri和mClientId，调用connect函数时传入了符合自己需求的Mqtt连接选项，因此我们先来看下MqttAndroidClient的connect函数：
## MqttAndroidClient
```
// MqttAndroidClient类：
@Override
public IMqttToken connect(MqttConnectOptions options, Object userContext,
                          IMqttActionListener callback) throws MqttException {

    IMqttToken token = new MqttTokenAndroid(this, userContext, callback);

    connectOptions = options;
    connectToken = token;

    /*
     * 实际的连接取决于我们在这里启动和绑定的服务，
     * 但是在serviceConnection的onServiceConnected()方法运行（异步）之前
     * 我们实际上不能使用它，所以连接本身发生在onServiceConnected()方法上
     */
    if (mqttService == null) { // First time - must bind to the service
        Intent serviceStartIntent = new Intent();
        serviceStartIntent.setClassName(myContext, SERVICE_NAME);
        Object service = myContext.startService(serviceStartIntent);
        if (service == null) {
            IMqttActionListener listener = token.getActionCallback();
            if (listener != null) {
                listener.onFailure(token, new RuntimeException(
                        "cannot start service " + SERVICE_NAME));
            }
        }

        // We bind with BIND_SERVICE_FLAG (0), leaving us the manage the lifecycle
        // until the last time it is stopped by a call to stopService()
        myContext.bindService(serviceStartIntent, serviceConnection,
                Context.BIND_AUTO_CREATE);

        if (!receiverRegistered) registerReceiver(this);
    } else {
        // 线程池执行
        pool.execute(new Runnable() {
            @Override
            public void run() {
                doConnect();
                //Register receiver to show shoulder tap.
                if (!receiverRegistered) registerReceiver(MqttAndroidClient.this);
            }

        });
    }
    return token;
}

    /**
     * 绑定MqttService服务的回调
     */
    private final class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mqttService = ((MqttServiceBinder) binder).getService();
            bindedService = true;
            // 最后还是执行的该方法
            doConnect();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mqttService = null;
        }
    }

    // Listener for when the service is connected or disconnected
    private final MyServiceConnection serviceConnection = new MyServiceConnection();
```
这个函数会启动paho mqtt唯一一个组件MqttService，这个组件不支持跨进程调用，如果需要将MqttService放在其他进程，需要将和mqtt相关的调用全部放在同一个进程内。由于需要使用MqttService组件中的函数，需要在启动MqttService后对MqttService进行绑定。如果服务已经启动，则直接执行建立连接操作。这时候建立的连接仅仅是网络连接，不是mqtt协议连接。由上面代码可以看出，无论是MqttService是否启动并绑定，最终都是调用doConnect()方法继续执行连接操作。
```
// MqttAndroidClient类：
    private void doConnect() {
        if (clientHandle == null) {
            clientHandle = mqttService.getClient(serverURI, clientId, myContext.getApplicationInfo().packageName,persistence);
        }
        mqttService.setTraceEnabled(traceEnabled);
        mqttService.setTraceCallbackId(clientHandle);

        String activityToken = storeToken(connectToken);
        try {
            mqttService.connect(clientHandle, connectOptions, null,
                    activityToken);
        } catch (MqttException e) {
            IMqttActionListener listener = connectToken.getActionCallback();
            if (listener != null) {
                listener.onFailure(connectToken, e);
            }
        }
    }
```
直到此时出现了activityToken, connectToken, clientHandle，不要慌，我们来一个一个分析。MqttAndroidClient的connect函数时，会生成connectToken，具体生成如下:
```
// MqttAndroidClient类：
    IMqttToken token = new MqttTokenAndroid(this, userContext, callback);

    connectOptions = options;
    connectToken = token;
```
token机制在paho mqtt实现中扮演着十分重要的角色，负责消息各种回调的实现，后面章节再单独分析paho mqtt的token机制。再来看一下clientHandle的来源：
```
// MqttService类：
    public String getClient(String serverURI, String clientId
        , String contextId, MqttClientPersistence persistence) {
        String clientHandle = serverURI + ":" + clientId+":"+contextId;
        if (!connections.containsKey(clientHandle)) {
            MqttConnection client = new MqttConnection(this, serverURI,
                clientId, persistence, clientHandle);
            connections.put(clientHandle, client);
        }
        return clientHandle;
  }
```
clientHandle是serverURI + ":" + clientId+":"+contextId组合形成的字符串，contextId是应用包名。此段代码中引入了一个新的类MqttConnection，而MqttConnection代表着Mqtt的连接实例，MqttService内部使用connections记录每一个连接实例。最后了解下activityToken，我们看下storeToken(connectToken)函数：
```
// MqttAndroidClient类：
    private synchronized String storeToken(IMqttToken token) {
        tokenMap.put(tokenNumber, token);
        return Integer.toString(tokenNumber++);
    }
```
MqttAndroidClient内部使用tokenMap记录每次调用生成的token, 将tokenNumber返回。activityToken会传入MqttConnection中，并保存于MqttConnection类中connect函数的Bundle变量resultBundle里，而resultBundle最终会被用于发送广播触发我们connect、publish、subscribe等的回调监听。这里暂时先了解这些，我们接着看执行完doConnect函数后，函数调用到了MqttService组件中的connect函数：
## MqttService
```
// MqttService类：
    public void connect(String clientHandle, MqttConnectOptions connectOptions,
      String invocationContext, String activityToken)
      throws MqttSecurityException, MqttException {
	  	MqttConnection client = getConnection(clientHandle);
	  	client.connect(connectOptions, null, activityToken);
  }
  
  private MqttConnection getConnection(String clientHandle) {
    MqttConnection client = connections.get(clientHandle);
    if (client == null) {
      throw new IllegalArgumentException("Invalid ClientHandle");
    }
    return client;
  }
```
看到clientHandle是不是有点熟悉，上面我们讲过connections将生成的MqttConnection实例保存起来，这一步通过getConnection重新获取。接下来，代码来到了MqttConnection.connect函数中：
```
// MqttConnection类：
    public void connect(MqttConnectOptions options, String invocationContext,
                        String activityToken) {

        connectOptions = options;
        reconnectActivityToken = activityToken;
        
        // //根据自己设置的连接选项cleanSession，判断是否清除历史消息
        if (options != null) {
            cleanSession = options.isCleanSession();
        }

        if (connectOptions.isCleanSession()) {
            // discard old data
            service.messageStore.clearArrivedMessages(clientHandle);
        }

        service.traceDebug(TAG, "Connecting {" + serverURI + "} as {" + clientId + "}");
        final Bundle resultBundle = new Bundle();
        // 将activityToken保存至resultBundle，验证上面所叙述的activityToken
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
                activityToken);
        resultBundle.putString(
                MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
                invocationContext);
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
                MqttServiceConstants.CONNECT_ACTION);

        try {
            if (persistence == null) {
                // ask Android where we can put files
                //2016.12 zhn change:for no permissions
                File myDir = service.getFilesDir();//File myDir = service.getExternalFilesDir(TAG);

                if (myDir == null) {
                    // No external storage, use internal storage instead.
                    myDir = service.getDir(TAG, Context.MODE_PRIVATE);

                    if (myDir == null) {
                        resultBundle.putString(
                                MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
                                "Error! No external and internal storage available");
                        resultBundle.putSerializable(
                                MqttServiceConstants.CALLBACK_EXCEPTION,
								new MqttPersistenceException());
                        service.callbackToActivity(clientHandle, Status.ERROR,
                                resultBundle);
                        return;
                    }
                }

                // 用它来设置MQTT客户端持久性存储
                persistence = new MqttDefaultFilePersistence(
                        myDir.getAbsolutePath());
            }
            // 用于监听连接成功的回调
            IMqttActionListener listener = new MqttConnectionListener(resultBundle) {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    doAfterConnectSuccess(resultBundle);
                    service.traceDebug(TAG, "connect success!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    resultBundle.putString(
                            MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
                            exception.getLocalizedMessage());
                    resultBundle.putSerializable(
                            MqttServiceConstants.CALLBACK_EXCEPTION, exception);
                    service.traceError(TAG,
                            "connect fail, call connect to reconnect.reason:"
                                    + exception.getMessage());

                    doAfterConnectFail(resultBundle);
                }
            };

            if (myClient != null) {//如果已经创建过MqttAsyncClient，即之前就调用过本connect()方法
                if (isConnecting) {//上次调用的connect()还在连接中，不做处理，等待connect()结果
                    service.traceDebug(TAG,
                            "myClient != null and the client is connecting. Connect return " +"directly.");
                    service.traceDebug(TAG, "Connect return:isConnecting:" + isConnecting +
							".disconnected:" + disconnected);
                } else if (!disconnected) {//当前已处于长连接，提示连接成功
                    service.traceDebug(TAG, "myClient != null and the client is connected and " +"notify!");
                    doAfterConnectSuccess(resultBundle);
                } else {//之前的连接未成功或者已掉线，重新尝试连接
                    service.traceDebug(TAG, "myClient != null and the client is not connected");
                    service.traceDebug(TAG, "Do Real connect!");
                    setConnectingState(true);
                    myClient.connect(connectOptions, invocationContext, listener);
                }
            }else { // if myClient is null, then create a new connection
                alarmPingSender = new AlarmPingSender(service);//用于发送心跳包
                // 创建MqttAsyncClient
                myClient = new MqttAsyncClient(serverURI, clientId,
                        persistence, alarmPingSender);
                myClient.setCallback(this);

                service.traceDebug(TAG, "Do Real connect!");
                // 设置连接状态
                setConnectingState(true);
                // 连接
                myClient.connect(connectOptions, invocationContext, listener);
            }
        } catch (Exception e) {
            service.traceError(TAG, "Exception occurred attempting to connect: " + e.getMessage());
            setConnectingState(false);
            handleException(resultBundle, e);
        }
    }
```
从上面代码以及注释中可知，这段代码主要作用就是新建了MqttAsyncClient对象，然后注册了回调函数，然后去执行connect函数，同时将状态置为正在连接状态。接下来就分析下MqttAsyncClient.connect函数：
## MqttAsyncClient
```
// MqttAsyncClient类：
        public IMqttToken connect(MqttConnectOptions options
            , Object userContext, IMqttActionListener callback)
			throws MqttException, MqttSecurityException {
		final String methodName = "connect";
		// 状态判断
		if (comms.isConnected()) {
			throw ExceptionHelper.createMqttException(MqttException.REASON_CODE_CLIENT_CONNECTED);
		}
		if (comms.isConnecting()) {
			throw new MqttException(MqttException.REASON_CODE_CONNECT_IN_PROGRESS);
		}
		if (comms.isDisconnecting()) {
			throw new MqttException(MqttException.REASON_CODE_CLIENT_DISCONNECTING);
		}
		if (comms.isClosed()) {
			throw new MqttException(MqttException.REASON_CODE_CLIENT_CLOSED);
		}
		if (options == null) {
			options = new MqttConnectOptions();
		}
		this.connOpts = options;
		this.userContext = userContext;
		final boolean automaticReconnect = options.isAutomaticReconnect();

		// @TRACE 103=cleanSession={0} connectionTimeout={1} TimekeepAlive={2}
		// userName={3} password={4} will={5} userContext={6} callback={7}
		log.fine(CLASS_NAME, methodName, "103",
				new Object[] { Boolean.valueOf(options.isCleanSession()), new Integer(options.getConnectionTimeout()),
						new Integer(options.getKeepAliveInterval()), options.getUserName(),
						((null == options.getPassword()) ? "[null]" : "[notnull]"),
						((null == options.getWillMessage()) ? "[null]" : "[notnull]"), userContext, callback });
		// 设置网络连接
		comms.setNetworkModules(createNetworkModules(serverURI, options));
		// 设置重连回调
		comms.setReconnectCallback(new MqttReconnectCallback(automaticReconnect));

		// Insert our own callback to iterate through the URIs till the connect
		// succeeds
		MqttToken userToken = new MqttToken(getClientId());
		// 初始化连接动作侦听器connectActionListener
		ConnectActionListener connectActionListener = new ConnectActionListener(this, persistence, comms, options,
				userToken, userContext, callback, reconnecting);
		userToken.setActionCallback(connectActionListener);
		userToken.setUserContext(this);

		// If we are using the MqttCallbackExtended, set it on the
		// connectActionListener
		if (this.mqttCallback instanceof MqttCallbackExtended) {
			connectActionListener.setMqttCallbackExtended((MqttCallbackExtended) this.mqttCallback);
		}

		comms.setNetworkModuleIndex(0);
		// 连接动作侦听器继续执行connect
		connectActionListener.connect();

		return userToken;
	}
```
MqttAsyncClient.connect函数的主要作用是设置了网络连接模块，设置重连回调，最后执行connectActionListener.connect函数。这段代码又引进来一个新的类ClientComms，我们先来看下ClientComms的初始化：
```
// MqttAsyncClient类：
        public MqttAsyncClient(String serverURI, String clientId
            , MqttClientPersistence persistence,MqttPingSender pingSender, ScheduledExecutorService executorService) throws MqttException {
		final String methodName = "MqttAsyncClient";
		...
		// 创建大小为10的线程池
		this.executorService = executorService;
		if (this.executorService == null) {
			this.executorService = Executors.newScheduledThreadPool(10);
		}
		...
		// 初始化ClientComms，并传入大小为10的线程池
		this.comms = new ClientComms(this
		    , this.persistence, pingSender,this.executorService);
		this.persistence.close();
		this.topics = new Hashtable();
	}
// 	ClientComms类中：
	public ClientComms(IMqttAsyncClient client, MqttClientPersistence persistence, MqttPingSender pingSender, ExecutorService executorService) throws MqttException {
		this.conState = DISCONNECTED;
		this.client 	= client;
		this.persistence = persistence;
		this.pingSender = pingSender;
		this.pingSender.init(this);
		this.executorService = executorService;

		this.tokenStore = new CommsTokenStore(getClient().getClientId());
		this.callback 	= new CommsCallback(this);
		this.clientState = new ClientState(persistence, tokenStore, this.callback, this, pingSender);

		callback.setClientState(clientState);
		log.setResourceName(getClient().getClientId());
	}
```
可以看出ClientComms是在MqttAsyncClient初始化时完成初始化的，并且将心跳的发送器pingSender和大小为10的线程池一起传入ClientComms。ClientComms类的初始化中又初始化了CommsTokenStore、CommsCallback和ClientState几个类。我们再来看下重连回调，重连代码有点多，我们只关注一下重连的回调函数即可：
```
// MqttReconnectCallback类（MqttAsyncClient类中的内部类）：
        class MqttReconnectCallback implements MqttCallbackExtended {
		...
		// 连接失败，重连时会调用该方法
		public void connectionLost(Throwable cause) {
			if (automaticReconnect) {
				// Automatic reconnect is set so make sure comms is in resting
				// state
				comms.setRestingState(true);
				reconnecting = true;
				startReconnectCycle();
			}
		}
        ...
	}
	
	private void startReconnectCycle() {
		String methodName = "startReconnectCycle";
		// @Trace 503=Start reconnect timer for client: {0}, delay: {1}
		log.fine(CLASS_NAME, methodName, "503", new Object[] { this.clientId, new Long(reconnectDelay) });
		reconnectTimer = new Timer("MQTT Reconnect: " + clientId);
		reconnectTimer.schedule(new ReconnectTask(), reconnectDelay);
	}
	
	private class ReconnectTask extends TimerTask {
		private static final String methodName = "ReconnectTask.run";

		public void run() {
			// @Trace 506=Triggering Automatic Reconnect attempt.
			log.fine(CLASS_NAME, methodName, "506");
			attemptReconnect();
		}
	}
	
	private void attemptReconnect() {
		final String methodName = "attemptReconnect";
		...
		try {
			connect(this.connOpts, this.userContext, new MqttReconnectActionListener(methodName));
		} catch (MqttSecurityException ex) {
			// @TRACE 804=exception
			log.fine(CLASS_NAME, methodName, "804", null, ex);
		} catch (MqttException ex) {
			// @TRACE 804=exception
			log.fine(CLASS_NAME, methodName, "804", null, ex);
		}
	}
	
	class MqttReconnectActionListener implements IMqttActionListener {
		...
		public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			...
			if (reconnectDelay < 128000) {
			    //reconnectDelay初始值为1000,每次重连失败时*2
				reconnectDelay = reconnectDelay * 2;
			}
			rescheduleReconnectCycle(reconnectDelay);
		}
		...
	}
```
自动重连的实现主要在的attemptReconnect()方法里，重连失败会继续重连直到连接成功，不过重连的间隔时间会随着重连次数增加最大到128s。最后我们再分析一下网络连接的设置createNetworkModules函数：
```
// MqttAsyncClient类:
            protected NetworkModule[] createNetworkModules(String address, MqttConnectOptions options)
			throws MqttException, MqttSecurityException {
		final String methodName = "createNetworkModules";
		// @TRACE 116=URI={0}
		log.fine(CLASS_NAME, methodName, "116", new Object[] { address });

		NetworkModule[] networkModules = null;
		String[] serverURIs = options.getServerURIs();
		String[] array = null;
		if (serverURIs == null) {
			array = new String[] { address };
		} else if (serverURIs.length == 0) {
			array = new String[] { address };
		} else {
			array = serverURIs;
		}

		networkModules = new NetworkModule[array.length];
		for (int i = 0; i < array.length; i++) {
			networkModules[i] = createNetworkModule(array[i], options);
		}

		log.fine(CLASS_NAME, methodName, "108");
		return networkModules;
	}
```
options实例在建立连接的过程中，我们也仅仅是设置了和连接相关的一些状态，并没有设置serverURI，故options.getServerURIS返回为null。NetworkModule为paho定义的接口，规定了网络模块需要实现的方法。目前paho定义的网络连接模块有TCPNetworkModule，SSLNetworkModule，WebsocketNetworkModule，WebSocketSecureNetworkModule，可以看下createNetworkModule根据uri使用的协议类型创建对应的NetworkModule。创建完所有的NetworkModule后，执行comms.setNetworknModule(0)，先使用第一个NetworkModule进行连接。comms是ClientComms类型的实例，在paho的实现中占有非常重要的地位，后序部分会进行分析。来看下createNetwokModule函数的实现：
```
// MqttAsyncClient类:
        private NetworkModule createNetworkModule(String address, MqttConnectOptions options) throws MqttException, MqttSecurityException {
		final String methodName = "createNetworkModule";

		NetworkModule netModule;
		SocketFactory factory = options.getSocketFactory();

		int serverURIType = MqttConnectOptions.validateURI(address);

		URI uri;
		try {
			uri = new URI(address);
			...
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Malformed URI: " + address + ", " + e.getMessage());
		}

		String host = uri.getHost();
		int port = uri.getPort(); // -1 if not defined

		switch (serverURIType) {
		case MqttConnectOptions.URI_TYPE_TCP :
			...
			netModule = new TCPNetworkModule(factory, host, port, clientId);
			((TCPNetworkModule)netModule).setConnectTimeout(options.getConnectionTimeout());
			break;
		case MqttConnectOptions.URI_TYPE_SSL:
			...
			netModule = new SSLNetworkModule((SSLSocketFactory) factory, host, port, clientId);
			...
			break;
		case MqttConnectOptions.URI_TYPE_WS:
			...
			netModule = new WebSocketNetworkModule(factory, address, host, port, clientId);
			((WebSocketNetworkModule)netModule).setConnectTimeout(options.getConnectionTimeout());
			break;
		case MqttConnectOptions.URI_TYPE_WSS:
			...
			netModule = new WebSocketSecureNetworkModule((SSLSocketFactory) factory, address, host, port, clientId);
			...
			break;
		default:
			log.fine(CLASS_NAME,methodName, "119", new Object[] {address});
			netModule = null;
		}
		return netModule;
	}
```
可以看出，createNetwokModule函数主要是根据serverURIType来判断需要使用TCPNetworkModule，SSLNetworkModule，WebsocketNetworkModule，WebSocketSecureNetworkModule中的那种网络模块实现网络连接。

现在可以继续往下继续分析connectActionListener.connect()函数啦：
## ConnectActionListener
```
// ConnectActionListener类：
public void connect() throws MqttPersistenceException {
    MqttToken token = new MqttToken(client.getClientId());
    token.setActionCallback(this);
    token.setUserContext(this);
    // 打开持久化存储
    persistence.open(client.getClientId(), client.getServerURI());

    if (options.isCleanSession()) {
      persistence.clear();
    }
    // 设置版本
    if (options.getMqttVersion() == MqttConnectOptions.MQTT_VERSION_DEFAULT) {
      options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
    }
    
    try {
        // 开始连接
        comms.connect(options, token);
    }
    catch (MqttException e) {
      onFailure(token, e);
    }
  }
```
从这段代码中可以看出，连接已交给comms.connect(options, token)函数，而comms的初始化上面也提到过，ClientComms是在MqttAsyncClient初始化时完成初始化的
## ClientComms
```
// ClientComms类：
    public void connect(MqttConnectOptions options, MqttToken token) throws MqttException {
        final String methodName = "connect";
        synchronized (conLock) {
            if (isDisconnected() && !closePending) {
                //@TRACE 214=state=CONNECTING
                log.fine(CLASS_NAME,methodName,"214");
                // 设置连接状态
                conState = CONNECTING;
                conOptions = options;
                // 构建CONNECT数据包
                MqttConnect connect = new MqttConnect(client.getClientId(),
                        conOptions.getMqttVersion(),
                        conOptions.isCleanSession(),
                        conOptions.getKeepAliveInterval(),
                        conOptions.getUserName(),
                        conOptions.getPassword(),
                        conOptions.getWillMessage(),
                        conOptions.getWillDestination());    
                // 设置clientState属性
                this.clientState.setKeepAliveSecs(conOptions.getKeepAliveInterval());
                this.clientState.setCleanSession(conOptions.isCleanSession());
                this.clientState.setMaxInflight(conOptions.getMaxInflight());
                tokenStore.open();
                ConnectBG conbg = new ConnectBG(this, token, connect, executorService);
                conbg.start();
            }else {
                ...
            }
        }
    }
```
从comms.connect函数的代码中可以看出，最后调用了conbg.start()函数，而ConnectBG是实现了Runnable的类，并且运行在线程池中：
```
// ClientComms类：
    private class ConnectBG implements Runnable {
        ...
        void start() {
            executorService.execute(this);
        }
        
        public void run() {
            Thread.currentThread().setName(threadName);
	    final String methodName = "connectBG:run";
	    MqttException mqttEx = null;
	    //@TRACE 220=>
	    log.fine(CLASS_NAME, methodName, "220");
	    
	    try {
	  	  // Reset an exception on existing delivery tokens.
	  	  // This will have been set if disconnect occured before delivery was
	  	  // fully processed.
	  	  MqttDeliveryToken[] toks = tokenStore.getOutstandingDelTokens();
	  	  for (int i=0; i<toks.length; i++) {
	  	 	 toks[i].internalTok.setException(null);
	  	  }
	    
	  	  // Save the connect token in tokenStore as failure can occur before send
	  	  tokenStore.saveToken(conToken,conPacket);
	    
	  	  // 启动网络模块，发起网络连接
	  	  NetworkModule networkModule = networkModules[networkModuleIndex];
	  	  networkModule.start();
	  	  // 连接完成后，启动receiver，负责从broker接收消息
	  	  receiver = new CommsReceiver(clientComms, clientState, tokenStore, networkModule.getInputStream());
	  	  receiver.start("MQTT Rec: "+getClient().getClientId(), executorService);
	  	  // 连接完成后，启动sender，负责向broker发送消息
	  	  sender = new CommsSender(clientComms, clientState, tokenStore, networkModule.getOutputStream());
	  	  sender.start("MQTT Snd: "+getClient().getClientId(), executorService);
	  	  // 连接完成后，启动回调监听
	  	  /**
	  	   * CommsCallback：接收器和外部API之间的桥接。此类由Receiver调用
	  	   *    ，然后将以comms为中心的MQTT消息对象转换为外部API可理解的。
	  	  */
	  	  callback.start("MQTT Call: "+getClient().getClientId(), executorService);
	  	  // 向broker发送CONNECT数据包
	  	  internalSend(conPacket, conToken);
	    } catch (MqttException ex) {
	  	  //@TRACE 212=connect failed: unexpected exception
	  	  log.fine(CLASS_NAME, methodName, "212", null, ex);
	  	  mqttEx = ex;
	    } catch (Exception ex) {
	  	  //@TRACE 209=connect failed: unexpected exception
	  	  log.fine(CLASS_NAME, methodName, "209", null, ex);
	  	  mqttEx =  ExceptionHelper.createMqttException(ex);
	    }
	    
	    if (mqttEx != null) {
	  	  shutdownConnection(conToken, mqttEx);
	    }
        }
    }
```
从conbg.start()函数中可以看出，在线程池启动运行了ConnectBG，因此现在所有的操作来到了ConnectBG的run()函数中，run()里启动了网络模块、接收broker消息和发送消息的Runnable(CommsReceiver和CommsSender)、回调监听。此处需要说明一下NetworkModule为接口，实现它的子类调用start()方法，其实就是启动Socket连接，而CommsReceiver、CommsSender和callback都是与ConnectBG一样，皆是实现了Runnable的子类，运行于线程池中。最后调用internalSend方法发送CONNECT数据包：
```
// ClientComms类：
    void internalSend(MqttWireMessage message, MqttToken token) throws MqttException {
	......
	try {
		// Persist if needed and send the message
		this.clientState.send(message, token);
	} catch(MqttException e) {
		......
	}
    }
```
clientState负责在receiver和sender之间进行消息处理，可以将sender看做是clientState的消费者， receiver负责接收来自broker的消息。接下来看看clientState.send(message, token)函数：
## ClientState
```
// ClientState类：
public void send(MqttWireMessage message, MqttToken token) throws MqttException {
	final String methodName = "send";
	......
	if (token != null ) {
		try {
			token.internalTok.setMessageID(message.getMessageId());
		} catch (Exception e) {
		}
	}
		
	if (message instanceof MqttPublish) {
		......
	} else {
		//@TRACE 615=pending send key={0} message {1}
		log.fine(CLASS_NAME,methodName,"615", new Object[]{new Integer(message.getMessageId()), message});
		
		if (message instanceof MqttConnect) {
			synchronized (queueLock) {
				// Add the connect action at the head of the pending queue ensuring it jumps
				// ahead of any of other pending actions.
				tokenStore.saveToken(token, message);
				pendingFlows.insertElementAt(message,0);
				queueLock.notifyAll();
			}
		} else {
			......
		}
	}
}
```
我们本文分析的源码为MQTT连接，因此消息肯定是MqttConnect，send函数将消息体添加到pendingFlows中，等待sender的调度并发送。sender时Runnable实例，看下sender是如何调度发送的，以下是sender的run函数：
## CommsSender
```
// CommsSender类中：
    public void run() {
        sendThread = Thread.currentThread();
        sendThread.setName(threadName);
        final String methodName = "run";
        MqttWireMessage message = null;

        try {
            runningSemaphore.acquire();
        } catch (InterruptedException e) {
            running = false;
            return;
        }

        try {
            // 轮询不断取消息，out为OutputStream的Socket
            while (running && (out != null)) {
                try {
                    //从 clientState中取消息
                    message = clientState.get();
                    if (message != null) {
                        //@TRACE 802=network send key={0} msg={1}
                        log.fine(CLASS_NAME, methodName, "802", new Object[]{message.getKey(),message});
                        // mqttAck为响应消息
                        if (message instanceof MqttAck) {
                            out.write(message);// 写数据
                            out.flush();// 发送
                        } else {
                            MqttToken token = tokenStore.getToken(message);
                            // While quiescing the tokenstore can be cleared so need
                            // to check for null for the case where clear occurs
                            // while trying to send a message.
                            if (token != null) {
                                synchronized (token) {
                                    out.write(message);// 写数据
                                    try {
                                        out.flush();// 发送
                                    } catch (IOException ex) {
                                        // The flush has been seen to fail on disconnect of a SSL
                                        // socket
                                        // as disconnect is in progress this should not be 
                                        // treated as an error
                                        if (!(message instanceof MqttDisconnect)) {
                                            throw ex;
                                        }
                                    }
                                    clientState.notifySent(message);
                                }
                            }
                        }
                    } else { // null message
                        //@TRACE 803=get message returned null, stopping}
                        log.fine(CLASS_NAME, methodName, "803");

                        running = false;
                    }
                } catch (MqttException me) {
                    handleRunException(message, me);
                } catch (Exception ex) {
                    handleRunException(message, ex);
                }
            } // end while
        } finally {
            running = false;
            runningSemaphore.release();
        }

        //@TRACE 805=<
        log.fine(CLASS_NAME, methodName, "805");
    }
```
可以看出sender不断循环从clientState获取待发送的消息，clientState.get函数大家可以自行分析。
MQTT连接的消息发送出去啦，前面说到receiver是负责接收broker发送回来的数据的，receiver也是Runnable类型，看下receiver的run函数实现：
## CommsReceiver
```
// CommsReceiver类中：
    public void run() {
        recThread = Thread.currentThread();
        recThread.setName(threadName);
        final String methodName = "run";
        MqttToken token = null;

        try {
            runningSemaphore.acquire();
        } catch (InterruptedException e) {
            running = false;
            return;
        }
        轮询不断等待消息，in为InputStream的Socket
        while (running && (in != null)) {
            try {
                //@TRACE 852=network read message
                log.fine(CLASS_NAME, methodName, "852");
                receiving = in.available() > 0;
                MqttWireMessage message = in.readMqttWireMessage();
                receiving = false;

                // mqttAck为响应消息
                if (message instanceof MqttAck) {
                    token = tokenStore.getToken(message);
                    if (token != null) {
                        synchronized (token) {
                            // Ensure the notify processing is done under a lock on the token
                            // This ensures that the send processing can complete  before the
                            // receive processing starts! ( request and ack and ack processing
                            // can occur before request processing is complete if not!
                            clientState.notifyReceivedAck((MqttAck) message);
                        }
                    } else if (message instanceof MqttPubRec || message instanceof MqttPubComp || message instanceof MqttPubAck) {
                        ...
                    } else {
                        throw new MqttException(MqttException.REASON_CODE_UNEXPECTED_ERROR);
                    }
                } else {
                    if (message != null) {
                        // A new message has arrived
                        clientState.notifyReceivedMsg(message);
                    }
                }
            } 
            ......
        }
    }
```
receiver收到消息后，响应消息的消息类型为MqttAck，由于CONACK数据包是MqttAck类型，且token不为null，故会执行clientState.notifyReceivedAck函数.
```
// ClientState类：
    protected void notifyReceivedAck(MqttAck ack) throws MqttException {
        final String methodName = "notifyReceivedAck";
        this.lastInboundActivity = System.currentTimeMillis();

        // @TRACE 627=received key={0} message={1}
        log.fine(CLASS_NAME, methodName, "627", new Object[]{
                new Integer(ack.getMessageId()), ack});

        MqttToken token = tokenStore.getToken(ack);
        MqttException mex = null;

        if (token == null) {
            // @TRACE 662=no message found for ack id={0}
            log.fine(CLASS_NAME, methodName, "662", new Object[]{
                    new Integer(ack.getMessageId())});
        } else if (ack instanceof MqttPubRec) {
            // Complete the QoS 2 flow. Unlike all other
            // flows, QoS is a 2 phase flow. The second phase sends a
            // PUBREL - the operation is not complete until a PUBCOMP
            // is received
            MqttPubRel rel = new MqttPubRel((MqttPubRec) ack);
            this.send(rel, token);
        } else if (ack instanceof MqttPubAck || ack instanceof MqttPubComp) {
            // QoS 1 & 2 notify users of result before removing from
            // persistence
            notifyResult(ack, token, mex);
            // Do not remove publish / delivery token at this stage
            // do this when the persistence is removed later 
        } else if (ack instanceof MqttPingResp) {
            synchronized (pingOutstandingLock) {
                pingOutstanding = Math.max(0, pingOutstanding - 1);
                notifyResult(ack, token, mex);
                if (pingOutstanding == 0) {
                    tokenStore.removeToken(ack);
                }
            }
            //@TRACE 636=ping response received. pingOutstanding: {0}                            
            
            log.fine(CLASS_NAME, methodName, "636", new Object[]{new Integer(pingOutstanding)});
        } else if (ack instanceof MqttConnack) {
            int rc = ((MqttConnack) ack).getReturnCode();
            // 根据CONACK数据包中的返回码判断协议连接是否已经建立，0表示服务端接受连接，协议正常建立。
            if (rc == 0) {
                synchronized (queueLock) {
                    if (cleanSession) {
                        clearState();
                        // Add the connect token back in so that users can be  
                        // notified when connect completes.
                        tokenStore.saveToken(token, ack);
                    }
                    inFlightPubRels = 0;
                    actualInFlight = 0;
                    restoreInflightMessages();
                    connected();
                }
            } else {
                mex = ExceptionHelper.createMqttException(rc);
                throw mex;
            }

            clientComms.connectComplete((MqttConnack) ack, mex);
            notifyResult(ack, token, mex);
            tokenStore.removeToken(ack);

            // Notify the sender thread that there maybe work for it to do now
            synchronized (queueLock) {
                queueLock.notifyAll();
            }
        } else {
            notifyResult(ack, token, mex);
            releaseMessageId(ack.getMessageId());
            tokenStore.removeToken(ack);
        }

        checkQuiesceLock();
    }
    
    public void connected() {
        final String methodName = "connected";
        //@TRACE 631=connected
        log.fine(CLASS_NAME, methodName, "631");
        // 设置连接完成状态
        this.connected = true;
        // 开始心跳
        pingSender.start(); //Start ping thread when client connected to server.
    }
```
notifyReceivedAck函数中，处理各种broker返回消息，而连接消息处理最后会到connected()连接完成的方法中，该方法设置连接完成状态以及开始发送心跳。
至此，MQTT连接源码分析已完成。

## resultBundle
现在我们回头看一下前面说的resultBundle，前面说到resultBundle最终会被用于发送广播触发我们connect、publish、subscribe等的回调监听。我们先取一处简单说明一下，前面也说到MqttConnection.connect函数中IMqttActionListener listener用于监听连接成功的回调。

简单说明下listener调用过程：listener会被传入MqttAsyncClient类里，随后又通过初始化ConnectActionListener类并保存于其成员变量userCallback中，最后是在ConnectActionListener里的onSuccess和onFailure两回调方法中调用了listener的onSuccess和onFailure两个方法，而ConnectActionListener里的onSuccess和onFailure两函数一般是CommsCallback类所调用（也会被MqttTokenAndroid类的notifyComplete函数调用，notifyComplete函数被MqttAndroidClient类的simpleAction和disconnected两方法调用，而simpleAction函数又会被连接、订阅、解除订阅、发送等调用，暂时只简单说一下这种情况）。上面代码注释中也说过CommsCallback是接收器和外部API之间的桥接。此类由Receiver调用，然后将以comms为中心的MQTT消息对象转换为外部API可理解的。 CommsReceiver接收器里轮询会调用ClientState.notifyReceivedAck((MqttAck)message);函数，该函数里有几种消息会调用notifyResult(ack, token, mex);函数，notifyResult方法对调用(CommsCallback)callback.asyncOperationComplete(token);对CommsCallback里成员变量completeQueue(Vector)进行addElement操作，而CommsCallback的run方法又是一直轮询监听completeQueue里是否有元素，有则调用handleActionComplete方法--》fireActionEvent方法--》ConnectActionListener里的onSuccess和onFailure。
大致流程参考如图：

![](https://user-gold-cdn.xitu.io/2019/5/12/16aaa95a94e7b4fc?w=1123&h=794&f=jpeg&s=199082)

listener的onSuccess函数中里调用了doAfterConnectSuccess(resultBundle);
```
// MqttConnection类中：
    private void doAfterConnectSuccess(final Bundle resultBundle) {
        acquireWakeLock();
        service.callbackToActivity(clientHandle, Status.OK, resultBundle);
        deliverBacklog();
        setConnectingState(false);
        disconnected = false;
        releaseWakeLock();
    }
```
连接成功后会调用MqttService.callbackToActivity()，resultBundle就作为其中一个参数被传入，接下来我们看看这个方法的实现：
```
// MqttService类：
void callbackToActivity(String clientHandle, Status status, Bundle dataBundle) {
    Intent callbackIntent = new Intent(MqttServiceConstants.CALLBACK_TO_ACTIVITY);
    if (clientHandle != null) {
        callbackIntent.putExtra(MqttServiceConstants.CALLBACK_CLIENT_HANDLE, clientHandle);
    }
    callbackIntent.putExtra(MqttServiceConstants.CALLBACK_STATUS, status);
    if (dataBundle != null) {
        callbackIntent.putExtras(dataBundle);
    }
    LocalBroadcastManager.getInstance(this).sendBroadcast(callbackIntent);
}
```
callbackToActivity()方法用于发送本地广播，广播中携带resultBundle，其实包括publish、subscribe等行为不论成功失败都会调用此方法，发出一个指示行为类型及状态的本地广播。那么发送的广播是在哪接收的呢？其实前面一直没有说MqttAndroidClient类继承自BroadCastReceiver，因此我们查看其onReceive()方法：
```
// MqttAndroidClient类：
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle data = intent.getExtras();

        String handleFromIntent = data
                .getString(MqttServiceConstants.CALLBACK_CLIENT_HANDLE);

        if ((handleFromIntent == null)
                || (!handleFromIntent.equals(clientHandle))) {
            return;
        }

        String action = data.getString(MqttServiceConstants.CALLBACK_ACTION);

        if (MqttServiceConstants.CONNECT_ACTION.equals(action)) {
            connectAction(data);
        } else if (MqttServiceConstants.CONNECT_EXTENDED_ACTION.equals(action)) {
            connectExtendedAction(data);
        } else if (MqttServiceConstants.MESSAGE_ARRIVED_ACTION.equals(action)) {
            messageArrivedAction(data);
        } else if (MqttServiceConstants.SUBSCRIBE_ACTION.equals(action)) {
            subscribeAction(data);
        } else if (MqttServiceConstants.UNSUBSCRIBE_ACTION.equals(action)) {
            unSubscribeAction(data);
        } else if (MqttServiceConstants.SEND_ACTION.equals(action)) {
            sendAction(data);
        } else if (MqttServiceConstants.MESSAGE_DELIVERED_ACTION.equals(action)) {
            messageDeliveredAction(data);
        } else if (MqttServiceConstants.ON_CONNECTION_LOST_ACTION
                .equals(action)) {
            connectionLostAction(data);
        } else if (MqttServiceConstants.DISCONNECT_ACTION.equals(action)) {
            disconnected(data);
        } else if (MqttServiceConstants.TRACE_ACTION.equals(action)) {
            traceAction(data);
        } else {
            mqttService.traceError(MqttService.TAG, "Callback action doesn't exist.");
        }
    }
    
    private void messageArrivedAction(Bundle data) {
        if (callback != null) {
            String messageId = data
                    .getString(MqttServiceConstants.CALLBACK_MESSAGE_ID);
            String destinationName = data
                    .getString(MqttServiceConstants.CALLBACK_DESTINATION_NAME);

            ParcelableMqttMessage message = data
                    .getParcelable(MqttServiceConstants.CALLBACK_MESSAGE_PARCEL);
            try {
                if (messageAck == Ack.AUTO_ACK) {
                    callback.messageArrived(destinationName, message);
                    mqttService.acknowledgeMessageArrival(clientHandle, messageId);
                } else {
                    message.messageId = messageId;
                    callback.messageArrived(destinationName, message);
                }

                // let the service discard the saved message details
            } catch (Exception e) {
                // Swallow the exception
            }
        }
    }
```
data.getString(MqttServiceConstants.CALLBACK_ACTION)获取的就是我们前面存放在resultBundle中的action,然后根据action去调用对应的方法去回调callback的对应方法，例如：action为MESSAGE_ARRIVED_ACTION时，调用messageArrivedAction函数，如果需要监听action为MqttServiceConstants.MESSAGE_ARRIVED_ACTION的行为，则要求我们传入的callback必须为MqttCallback的实现，而如果需要监听action为MqttServiceConstants.CONNECT_EXTENDED_ACTION的行为，则要求我们传入的callback必须为MqttCallbackExtended的实现，MqttCallbackExtended是MqttCallback的子类。这里的callback就是我们建立连接前初始化MqttAndroidClient时设置的MqttCallback对象：
```
// 本文最初建立MQTT连接部分代码：
    // MqttConnectCallback为MqttCallback的实现类
    mMqttCallback =new MqttConnectCallback(mContext, clientInfo);
    myClient =new MqttAndroidClient(mContext, uri, mClientId);
    myClient.setCallback(mMqttCallback);
```
至此，分析完连接MQTT的源码，下一篇分析MQTT发布消息publish。

参考链接：

https://blog.csdn.net/rockstore/article/details/86602985

https://blog.csdn.net/Dovar_66/article/details/79496080

...

<font color=#ff0000>(注：若有什么地方阐述有误，敬请指正。)</font>