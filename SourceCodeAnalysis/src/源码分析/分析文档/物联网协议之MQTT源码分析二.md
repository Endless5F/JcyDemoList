此篇文章继上一篇物联网协议之MQTT源码分析(一)而写的第二篇MQTT发布消息以及接收Broker消息的源码分析，想看MQTT连接的小伙伴可以去看我上一篇哦。

https://juejin.im/post/5cd66c4af265da037516bec3

## MQTT发布消息
MQTT发布消息是由MqttAndroidClient类的publish函数执行的，我们来看看这个函数：
```
// MqttAndroidClient类：
    @Override
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos,
                                      boolean retained, Object userContext,
                                      IMqttActionListener callback)
            throws MqttException, MqttPersistenceException {
        // 将消息内容、qos消息等级、retained消息是否保留封装成MqttMessage
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        // 每一条消息都有自己的token
        MqttDeliveryTokenAndroid token = new MqttDeliveryTokenAndroid(
                this, userContext, callback, message);
        String activityToken = storeToken(token);
        IMqttDeliveryToken internalToken = mqttService.publish(clientHandle,
                topic, payload, qos, retained, null, activityToken);
        token.setDelegate(internalToken);
        return token;
    }
```
从上面代码可以看出，发布消息需要topic消息主题、payload消息内容、callback回调监听等，经由mqttService.publish继续执行发布操作：
```
// MqttService类：MQTT唯一组件
    public IMqttDeliveryToken publish(String clientHandle, String topic,
                                      byte[] payload, int qos, boolean retained,
                                      String invocationContext, String activityToken)
            throws MqttPersistenceException, MqttException {
        MqttConnection client = getConnection(clientHandle);
        return client.publish(topic, payload, qos, retained, invocationContext,
                activityToken);
    }
```
MqttConnection在上一篇中讲解过，MQTT的连接会初始化一个MqttConnection，并保存在一个Map集合connections中，并通过getConnection(clientHandle)方法获取。很明显我们要接着看client.publish函数啦：
```
// MqttConnection类：
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos,
                                      boolean retained, String invocationContext,
									  String activityToken) {
		// 用于发布消息，是否发布成功的回调
        final Bundle resultBundle = new Bundle();
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
                MqttServiceConstants.SEND_ACTION);
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
                activityToken);
        resultBundle.putString(
                MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
                invocationContext);

        IMqttDeliveryToken sendToken = null;

        if ((myClient != null) && (myClient.isConnected())) {
            // 携带resultBundle数据，用于监听回调发布消息是否成功
            IMqttActionListener listener = new MqttConnectionListener(
                    resultBundle);
            try {
                MqttMessage message = new MqttMessage(payload);
                message.setQos(qos);
                message.setRetained(retained);
                sendToken = myClient.publish(topic, payload, qos, retained,
                        invocationContext, listener);
                storeSendDetails(topic, message, sendToken, invocationContext,
                        activityToken);
            } catch (Exception e) {
                handleException(resultBundle, e);
            }
        } else {
            resultBundle.putString(MqttServiceConstants.CALLBACK_ERROR_MESSAGE,
                    NOT_CONNECTED);
            service.traceError(MqttServiceConstants.SEND_ACTION, NOT_CONNECTED);
            service.callbackToActivity(clientHandle, Status.ERROR, resultBundle);
        }

        return sendToken;
    }
```
这段代码中很明显可以看出发布的操作又交给了myClient.publish方法，那myClient是谁呢？上一篇文章中讲过myClient是MqttAsyncClient，是在MQTT连接时在MqttConnection类的connect方法中初始化的，详情请看上一篇。
```
// MqttAsyncClient类：
    public IMqttDeliveryToken publish(String topic, byte[] payload, int qos
        , boolean retained,Object userContext,
        IMqttActionListener callback) throws MqttException,MqttPersistenceException {
        MqttMessage message = new MqttMessage(payload);
        message.setQos(qos);
        message.setRetained(retained);
        return this.publish(topic, message, userContext, callback);
    }
    
    public IMqttDeliveryToken publish(String topic, MqttMessage message
        , Object userContext,
        IMqttActionListener callback) throws MqttException,MqttPersistenceException {
        final String methodName = "publish";
        // @TRACE 111=< topic={0} message={1}userContext={1} callback={2}
        log.fine(CLASS_NAME, methodName, "111", new Object[]{topic, userContext, callback});

        // Checks if a topic is valid when publishing a message.
        MqttTopic.validate(topic, false/* wildcards NOT allowed */);

        MqttDeliveryToken token = new MqttDeliveryToken(getClientId());
        token.setActionCallback(callback);
        token.setUserContext(userContext);
        token.setMessage(message);
        token.internalTok.setTopics(new String[]{topic});

        MqttPublish pubMsg = new MqttPublish(topic, message);
        comms.sendNoWait(pubMsg, token);

        // @TRACE 112=<
        log.fine(CLASS_NAME, methodName, "112");

        return token;
    }
```
从这段代码中可以看到，现在把把topic和message封装成了MqttPublish类型的消息，并继续由comms.sendNoWait执行，comms是ClientComms，ClientComms是在初始化MqttAsyncClient的构造方法中初始化的，详情看上一篇。
```
// ClientComms类：
    public void sendNoWait(MqttWireMessage message, MqttToken token) throws MqttException {
        final String methodName = "sendNoWait";
        // 判断状态或者消息类型
        if (isConnected() ||
                (!isConnected() && message instanceof MqttConnect) ||
                (isDisconnecting() && message instanceof MqttDisconnect)) {
            if (disconnectedMessageBuffer != null && disconnectedMessageBuffer.getMessageCount() != 0) {
                //@TRACE 507=Client Connected, Offline Buffer available, but not empty. Adding 
                // message to buffer. message={0}
                log.fine(CLASS_NAME, methodName, "507", new Object[]{message.getKey()});
                if (disconnectedMessageBuffer.isPersistBuffer()) {
                    this.clientState.persistBufferedMessage(message);
                }
                disconnectedMessageBuffer.putMessage(message, token);
            } else {
                // 现在不是disconnect因此，逻辑走这里
                this.internalSend(message, token);
            }
        } else if (disconnectedMessageBuffer != null) {
            //@TRACE 508=Offline Buffer available. Adding message to buffer. message={0}
            log.fine(CLASS_NAME, methodName, "508", new Object[]{message.getKey()});
            if (disconnectedMessageBuffer.isPersistBuffer()) {
                this.clientState.persistBufferedMessage(message);
            }
            disconnectedMessageBuffer.putMessage(message, token);
        } else {
            //@TRACE 208=failed: not connected
            log.fine(CLASS_NAME, methodName, "208");
            throw ExceptionHelper.createMqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
    }
    
    void internalSend(MqttWireMessage message, MqttToken token) throws MqttException {
        final String methodName = "internalSend";
        ...
        try {
            // Persist if needed and send the message
            this.clientState.send(message, token);
        } catch (MqttException e) {
            // 注意此处代码***
            if (message instanceof MqttPublish) {
                this.clientState.undo((MqttPublish) message);
            }
            throw e;
        }
    }
```
comms.sendNoWait方法中又调用了本类中的internalSend方法，并且在internalSend方法中又调用了clientState.send(message, token)方法继续发布。ClientState对象是在ClientComms初始化的构造方法中初始化的。此处需要注意一下catch里的代码，下面会具体说明。
```
// ClientState类：
    public void send(MqttWireMessage message, MqttToken token) throws MqttException {
        final String methodName = "send";
        ...

        if (message instanceof MqttPublish) {
            synchronized (queueLock) {
                /**
                 * 注意这里：actualInFlight实际飞行中>maxInflight最大飞行中
                 * maxInflight：是我们在自己代码中通过连接选项MqttConnectOptions.setMaxInflight();设置的，默认大小为10
                */
                if (actualInFlight >= this.maxInflight) {
                    //@TRACE 613= sending {0} msgs at max inflight window
                    log.fine(CLASS_NAME, methodName, "613",
                            new Object[]{new Integer(actualInFlight)});

                    throw new MqttException(MqttException.REASON_CODE_MAX_INFLIGHT);
                }

                MqttMessage innerMessage = ((MqttPublish) message).getMessage();
                //@TRACE 628=pending publish key={0} qos={1} message={2}
                log.fine(CLASS_NAME, methodName, "628",
                        new Object[]{new Integer(message.getMessageId()),
                                new Integer(innerMessage.getQos()), message});
                /**
                 * 根据自己设置的qos等级，来决定是否需要恢复消息
                 * 这里需要说明一下qos等级区别：
                 *  qos==0，至多发送一次，不进行重试，Broker不会返回确认消息。
                 *  qos==1，至少发送一次，确保消息到达Broker，Broker需要返回确认消息PUBACK
                 *  qos==2，Broker肯定会收到消息，且只收到一次，qos==1可能会发送重复消息
                */
                switch (innerMessage.getQos()) {
                    case 2:
                        outboundQoS2.put(new Integer(message.getMessageId()), message);
                        persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
                        break;
                    case 1:
                        outboundQoS1.put(new Integer(message.getMessageId()), message);
                        persistence.put(getSendPersistenceKey(message), (MqttPublish) message);
                        break;
                }
                tokenStore.saveToken(token, message);
                pendingMessages.addElement(message);
                queueLock.notifyAll();
            }
        } else {
            ...
        }
    }
```
这段代码中我们发现了一个可能需要我们自己设置的属性maxInflight，如果实际发送中的消息大于maxInflight约束的最大的话就会抛出MqttException异常，那么这个异常catch里是怎么处理的呢，这就要往回看一步代码啦，上面已经提示过需要注意ClientComms类中internalSend方法中的catch里的代码：
```
    if (message instanceof MqttPublish) {
        this.clientState.undo((MqttPublish) message);
    }
```
可以很明确的看出若消息类型是MqttPublish，则执行clientState.undo((MqttPublish) message)方法，我们前面说过消息已经在MqttAsyncClient类的publish方法中把topic和message封装成了MqttPublish类型的消息，因此此处会执行undo方法：
```
// ClientState类：
    protected void undo(MqttPublish message) throws MqttPersistenceException {
        final String methodName = "undo";
        synchronized (queueLock) {
            //@TRACE 618=key={0} QoS={1} 
            log.fine(CLASS_NAME, methodName, "618",
                    new Object[]{new Integer(message.getMessageId()),
                            new Integer(message.getMessage().getQos())});

            if (message.getMessage().getQos() == 1) {
                outboundQoS1.remove(new Integer(message.getMessageId()));
            } else {
                outboundQoS2.remove(new Integer(message.getMessageId()));
            }
            pendingMessages.removeElement(message);
            persistence.remove(getSendPersistenceKey(message));
            tokenStore.removeToken(message);
            if (message.getMessage().getQos() > 0) {
                //Free this message Id so it can be used again
                releaseMessageId(message.getMessageId());
                message.setMessageId(0);
            }

            checkQuiesceLock();
        }
    }
```
代码已经很明显了，就是把大于maxInflight这部分消息remove移除掉，因此在实际操作中要注意自己的Mqtt消息的发布会不会在短时间内达到maxInflight默认的10的峰值，若能达到，则需要手动设置一个适合自己项目的范围阀值啦。

我们继续说clientState.send(message, token)方法里的逻辑，代码中注释中也说明了Mqtt会根据qos等级来决定消息到达机制

**qos等级**
* qos==0，至多发送一次，不进行重试，Broker不会返回确认消息，消息可能会丢失。
* qos==1，至少发送一次，确保消息到达Broker，Broker需要返回确认消息PUBACK，可能会发送重复消息
* qos==2，Broker肯定会收到消息，且只收到一次

根据qos等级，若qos等于1和2，则讲消息分别加入Hashtable类型的outboundQoS1和outboundQoS2中，已在后续逻辑中确保消息发送成功并到达。

<font color=#ff0000>注：qos等级优先级没有maxInflight高，从代码中可以看出，会先判断maxInflight再区分qos等级</font>

代码的最后讲消息添加进Vector类型的pendingMessages里，在上一篇中我们可以了解到MQTT的发射器是轮询检查pendingMessages里是否存在数据，若存在则通过socket的OutputStream发送出去。并且会通过接收器接收从Broker发送回来的数据。

## 监听Broker返回的消息之数据
发送我们就不看源码啦，接收我们再看一下源码，通过源码看一看数据是怎么回到我们自己的回调里的：
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

        while (running && (in != null)) {
            try {
                //@TRACE 852=network read message
                log.fine(CLASS_NAME, methodName, "852");
                receiving = in.available() > 0;
                MqttWireMessage message = in.readMqttWireMessage();
                receiving = false;

                // 消息是否属于Mqtt确认类型
                if (message instanceof MqttAck) {
                    token = tokenStore.getToken(message);
                    // token一般不会为空，前面已经保存过
                    if (token != null) {
                        synchronized (token) {
                            // ...
                            clientState.notifyReceivedAck((MqttAck) message);
                        }
                    } 
                    ...
            } finally {
                receiving = false;
                runningSemaphore.release();
            }
        }
    }
```
从代码中可以看出，Broker返回来的数据交给了clientState.notifyReceivedAck方法：
```
// ClientState类：
    protected void notifyReceivedAck(MqttAck ack) throws MqttException {
        final String methodName = "notifyReceivedAck";
        ...

        MqttToken token = tokenStore.getToken(ack);
        MqttException mex = null;

        if (token == null) {
            ...
        } else if (ack instanceof MqttPubRec) {
            // qos==2 是返回
            MqttPubRel rel = new MqttPubRel((MqttPubRec) ack);
            this.send(rel, token);
        } else if (ack instanceof MqttPubAck || ack instanceof MqttPubComp) {
            // qos==1/2 消息移除前通知的结果
            notifyResult(ack, token, mex);
            // Do not remove publish / delivery token at this stage
            // do this when the persistence is removed later 
        } else if (ack instanceof MqttPingResp) {
            // 连接心跳数据消息
            ...
        } else if (ack instanceof MqttConnack) {
            // MQTT连接消息
            ...
        } else {
            notifyResult(ack, token, mex);
            releaseMessageId(ack.getMessageId());
            tokenStore.removeToken(ack);
        }

        checkQuiesceLock();
    }
```
从上面注释可知，发布的消息qos==0，返回结果是直接走else，而qos==1/2，确认消息也最终会走到notifyResult(ack, token, mex)方法中：
```
    protected void notifyResult(MqttWireMessage ack, MqttToken token, MqttException ex) {
        final String methodName = "notifyResult";
        // 取消阻止等待令牌的任何线程，并保存ack
        token.internalTok.markComplete(ack, ex);
        // 通知此令牌已收到响应消息，设置已完成状态，并通过isComplete()获取状态
        token.internalTok.notifyComplete();

        // 让用户知道异步操作已完成，然后删除令牌
        if (ack != null && ack instanceof MqttAck && !(ack instanceof MqttPubRec)) {
            //@TRACE 648=key{0}, msg={1}, excep={2}
            log.fine(CLASS_NAME, methodName, "648", new Object[]{token.internalTok.getKey(), ack,ex});
            // CommsCallback类
            callback.asyncOperationComplete(token);
        }
        // 有些情况下，由于操作失败，因此没有确认
        if (ack == null) {
            //@TRACE 649=key={0},excep={1}
            log.fine(CLASS_NAME, methodName, "649", new Object[]{token.internalTok.getKey(), ex});
            callback.asyncOperationComplete(token);
        }
    }
    
// Token类：
    protected void markComplete(MqttWireMessage msg, MqttException ex) {
        final String methodName = "markComplete";
        //@TRACE 404=>key={0} response={1} excep={2}
        log.fine(CLASS_NAME, methodName, "404", new Object[]{getKey(), msg, ex});

        synchronized (responseLock) {
            // ACK means that everything was OK, so mark the message for garbage collection.
            if (msg instanceof MqttAck) {
                this.message = null;
            }
            this.pendingComplete = true;
            // 将消息保存在response成员变量中，并通过getWireMessage()方法获取消息msg
            this.response = msg;
            this.exception = ex;
        }
    }
// Token类：
    protected void notifyComplete() {
        ...
        synchronized (responseLock) {
            ...
            if (exception == null && pendingComplete) {
                // 设置已完成，并通过isComplete()获取状态
                completed = true;
                pendingComplete = false;
            } else {
                pendingComplete = false;
            }

            responseLock.notifyAll();
        }
        ...
    }
```
此时已将MqttWireMessage消息保存到token中，异步操作已完成，调用回调监听CommsCallback里的asyncOperationComplete方法：
```
// CommsCallback类：
    public void asyncOperationComplete(MqttToken token) {
        final String methodName = "asyncOperationComplete";

        if (running) {
            // invoke callbacks on callback thread
            completeQueue.addElement(token);
            synchronized (workAvailable) {
                // @TRACE 715=new workAvailable. key={0}
                log.fine(CLASS_NAME, methodName, "715", new Object[]{token.internalTok.getKey()});
                workAvailable.notifyAll();
            }
        } else {
            // invoke async callback on invokers thread
            try {
                handleActionComplete(token);
            } catch (Throwable ex) {
                // Users code could throw an Error or Exception e.g. in the case
                // of class NoClassDefFoundError
                // @TRACE 719=callback threw ex:
                log.fine(CLASS_NAME, methodName, "719", null, ex);

                // Shutdown likely already in progress but no harm to confirm
                clientComms.shutdownConnection(null, new MqttException(ex));
            }
        }
    }
```
CommsCallback是Mqtt连接就已经开始一直运行，因此running为true，所以现在已经将token添加进了completeQueue完成队列中，CommsCallback跟发射器一样，一直轮询等待数据，因此此时completeQueue已有数据，此时CommsCallback的run函数则会有接下来的操作：
```
// CommsCallback类：
    public void run() {
        ...
        while (running) {
            try {
                ...
                if (running) {
                    // Check for deliveryComplete callbacks...
                    MqttToken token = null;
                    synchronized (completeQueue) {
                        // completeQueue不为空
                        if (!completeQueue.isEmpty()) {
                            // 获取第一个token
                            token = (MqttToken) completeQueue.elementAt(0);
                            completeQueue.removeElementAt(0);
                        }
                    }
                    if (null != token) {
                        // token不为null，执行handleActionComplete
                        handleActionComplete(token);
                    }
                    ...
                }

                if (quiescing) {
                    clientState.checkQuiesceLock();
                }

            } catch (Throwable ex) {
                ...
            } finally {
                ...
            }
        }
    }
    
    private void handleActionComplete(MqttToken token)
            throws MqttException {
        final String methodName = "handleActionComplete";
        synchronized (token) {
            // 由上面已经，isComplete()已设置为true
            if (token.isComplete()) {
                // Finish by doing any post processing such as delete 
                // from persistent store but only do so if the action
                // is complete
                clientState.notifyComplete(token);
            }
            // 取消阻止任何服务员，如果待完成，现在设置完成
            token.internalTok.notifyComplete();
            if (!token.internalTok.isNotified()) {
 				...
				// 现在调用异步操作完成回调
				fireActionEvent(token);
			}
			...
        }
    }
```
run中调用了handleActionComplete函数，接着后调用了clientState.notifyComplete()方法和fireActionEvent(token)方法，先看notifyComplete()：
 ```
 // ClientState类：
    protected void notifyComplete(MqttToken token) throws MqttException {

        final String methodName = "notifyComplete";
        // 获取保存到Token中的Broker返回的消息，上面有说明
        MqttWireMessage message = token.internalTok.getWireMessage();

        if (message != null && message instanceof MqttAck) {
            ...
            MqttAck ack = (MqttAck) message;

            if (ack instanceof MqttPubAck) {
                // qos==1,用户通知现在从持久性中删除
                persistence.remove(getSendPersistenceKey(message));
                persistence.remove(getSendBufferedPersistenceKey(message));
                outboundQoS1.remove(new Integer(ack.getMessageId()));
                decrementInFlight();
                releaseMessageId(message.getMessageId());
                tokenStore.removeToken(message);
                // @TRACE 650=removed Qos 1 publish. key={0}
                log.fine(CLASS_NAME, methodName, "650",
                        new Object[]{new Integer(ack.getMessageId())});
            } else if (ack instanceof MqttPubComp) {
                ...
            }

            checkQuiesceLock();
        }
    }
 ```
 再来看fireActionEvent(token)方法：
 ```
 // CommsCallback类：
    public void fireActionEvent(MqttToken token) {
        final String methodName = "fireActionEvent";

        if (token != null) {
            IMqttActionListener asyncCB = token.getActionCallback();
            if (asyncCB != null) {
                if (token.getException() == null) {
                    ...
                    asyncCB.onSuccess(token);
                } else {
                    ...
                    asyncCB.onFailure(token, token.getException());
                }
            }
        }
    }
 ```
 从这段代码中终于能看到回调onSuccess和onFailure的方法啦，那asyncCB是谁呢？
 ```
 // MqttToken类：
    public IMqttActionListener getActionCallback() {
        return internalTok.getActionCallback();
    }
// Token类：
    public IMqttActionListener getActionCallback() {
		return callback;
	}
 ```
 看到这，一脸懵逼，这到底是谁呢，其实我们可以直接看这个回调设置方法，看看是从哪设置进来的就可以啦：
 ```
 // Token类：
    public void setActionCallback(IMqttActionListener listener) {
		this.callback  = listener;
	}
 // MqttToken类：
	public void setActionCallback(IMqttActionListener listener) {
		internalTok.setActionCallback(listener);
	}
// ConnectActionListener类：
    public void connect() throws MqttPersistenceException {
        // 初始化MqttToken
        MqttToken token = new MqttToken(client.getClientId());
        // 将此类设置成回调类
        token.setActionCallback(this);
        token.setUserContext(this);

        ...
    }
 ```
 其实早在MQTT连接时，就已经将此callback设置好，因此asyncCB就是ConnectActionListener，所以此时就已经走到了ConnectActionListener类里的onSuccess和onFailure的方法中，我们只挑一个onSuccess看一看：
 ```
// ConnectActionListener类：
    public void onSuccess(IMqttToken token) {
        if (originalMqttVersion == MqttConnectOptions.MQTT_VERSION_DEFAULT) {
            options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_DEFAULT);
        }
        // 此时将Broker的数据保存进了userToken里
        userToken.internalTok.markComplete(token.getResponse(), null);
        userToken.internalTok.notifyComplete();
        userToken.internalTok.setClient(this.client); 

        comms.notifyConnect();

        if (userCallback != null) {
            userToken.setUserContext(userContext);
            userCallback.onSuccess(userToken);
        }

        if (mqttCallbackExtended != null) {
            String serverURI =
                    comms.getNetworkModules()[comms.getNetworkModuleIndex()].getServerURI();
            mqttCallbackExtended.connectComplete(reconnect, serverURI);
        }

    }
 ```
 这里的userCallback又是谁呢？上一篇其实说过的，userCallback其实就是MqttConnection.connect函数中IMqttActionListener listener，所以此时又来到了MqttConnection类里connect方法里的listener监听回调内：
 ```
 // MqttConnection类：
    public void connect(MqttConnectOptions options, String invocationContext,
                        String activityToken) {
        ...
        service.traceDebug(TAG, "Connecting {" + serverURI + "} as {" + clientId + "}");
        final Bundle resultBundle = new Bundle();
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTIVITY_TOKEN,
                activityToken);
        resultBundle.putString(
                MqttServiceConstants.CALLBACK_INVOCATION_CONTEXT,
                invocationContext);
        resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
                MqttServiceConstants.CONNECT_ACTION);
        try {
             ...
            // 此时逻辑已经来到这里
            IMqttActionListener listener = new MqttConnectionListener(
                    resultBundle) {

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // 执行如下代码：
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

            if (myClient != null) {
                if (isConnecting) {
                    ...
                } else {
                    service.traceDebug(TAG, "myClient != null and the client is not connected");
                    service.traceDebug(TAG, "Do Real connect!");
                    setConnectingState(true);
                    myClient.connect(connectOptions, invocationContext, listener);
                }
            }

            // if myClient is null, then create a new connection
            else {
                ...
                myClient.connect(connectOptions, invocationContext, listener);
            }
        } catch (Exception e) {
            ...
        }
    }
 ```
 由这段代码以及注释可以知道，现在以及执行到了MqttConnection类里的doAfterConnectSuccess方法里：
 ```
 // MqttConnection类:
    private void doAfterConnectSuccess(final Bundle resultBundle) {
        // 获取唤醒锁
        acquireWakeLock();
        service.callbackToActivity(clientHandle, Status.OK, resultBundle);
        deliverBacklog();
        setConnectingState(false);
        disconnected = false;
        // 释放唤醒锁
        releaseWakeLock();
    }
    
    private void deliverBacklog() {
        Iterator<StoredMessage> backlog = service.messageStore
                .getAllArrivedMessages(clientHandle);
        while (backlog.hasNext()) {
            StoredMessage msgArrived = backlog.next();
            Bundle resultBundle = messageToBundle(msgArrived.getMessageId(),
                    msgArrived.getTopic(), msgArrived.getMessage());
            // 关注下这个action，下面会用到
            resultBundle.putString(MqttServiceConstants.CALLBACK_ACTION,
                    MqttServiceConstants.MESSAGE_ARRIVED_ACTION);
            service.callbackToActivity(clientHandle, Status.OK, resultBundle);
        }
    }
 ```
 可以看到这个函数中调用了几个方法中的其中两个service.callbackToActivity(clientHandle, Status.OK, resultBundle);和deliverBacklog();，deliverBacklog()方法最后也是调用的service.callbackToActivity方法。所以直接看service.callbackToActivity：
 ```
 // MqttService类：
    void callbackToActivity(String clientHandle, Status status,
                            Bundle dataBundle) {
        // 发送广播
        Intent callbackIntent = new Intent(
                MqttServiceConstants.CALLBACK_TO_ACTIVITY);
        if (clientHandle != null) {
            callbackIntent.putExtra(
                    MqttServiceConstants.CALLBACK_CLIENT_HANDLE, clientHandle);
        }
        callbackIntent.putExtra(MqttServiceConstants.CALLBACK_STATUS, status);
        if (dataBundle != null) {
            callbackIntent.putExtras(dataBundle);
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(callbackIntent);
    }
 ```
 service.callbackToActivity方法其实就是发送广播，那谁来接收广播呢？其实接收广播的就在最开始的MqttAndroidClient，MqttAndroidClient继承自BroadcastReceiver，所以说MqttAndroidClient本身就是一个广播接收者，所以我们来看它的onReceive方法：
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
        // 判断消息的action类型
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
            // 发布成功与否的回调
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
 ```
 从代码和注释以及上面的deliverBacklog方法中可以知道，我们现在需要关注的action为MESSAGE_ARRIVED_ACTION，所以就可以调用方法messageArrivedAction(data)：
 ```
 // MqttAndroidClient类：
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
    
    @Override
    public void setCallback(MqttCallback callback) {
        this.callback = callback;
    }
 ```
 在messageArrivedAction方法中可以看到，我们最后调用了callback回调了messageArrived方法，那么
 callback通过上面下部分代码可以知道，其实这个callback就是我们上一篇文章中所说的我们初始化MqttAndroidClient后，通过方法setCallback设置的我们自己定义的实现MqttCallback接口的回调类。
 
 ## 监听Broker返回的消息之发布消息成功与否
 再看下sendAction(data)方法：
 ```
    private void sendAction(Bundle data) {
        IMqttToken token = getMqttToken(data); 
        // remove on delivery
        simpleAction(token, data);
    }
    
    private void simpleAction(IMqttToken token, Bundle data) {
        if (token != null) {
            Status status = (Status) data
                    .getSerializable(MqttServiceConstants.CALLBACK_STATUS);
            if (status == Status.OK) {
                // 如果发布成功回调此方法
                ((MqttTokenAndroid) token).notifyComplete();
            } else {
                Exception exceptionThrown =
                        (Exception) data.getSerializable(MqttServiceConstants.CALLBACK_EXCEPTION);
                // 发布失败回调
                ((MqttTokenAndroid) token).notifyFailure(exceptionThrown);
            }
        } else {
            if (mqttService != null) {
                mqttService.traceError(MqttService.TAG, "simpleAction : token is null");
            }
        }
    }
 ```
 接下来再看一看发布成功回调的MqttTokenAndroid的notifyComplete函数：
 ```
 // MqttTokenAndroid类：
    void notifyComplete() {
        synchronized (waitObject) {
            isComplete = true;
            waitObject.notifyAll();
            if (listener != null) {
                listener.onSuccess(this);
            }
        }
    }
 ```
 这里又调用了listener.onSuccess(this)方法，那么这个listener是谁？其实listener就是我们调用MqttAndroidClient类的publish发布的最后一个参数，即我们自定义的监听发布消息是否发布成功的回调类。上面在MqttConnection类的publish方法中封装过MqttServiceConstants.SEND_ACTION的Bundle数据，而此数据是被MqttConnection类里的MqttConnectionListener携带。所以MqttConnectionListener里的onSuccess被调用时就会调用service.callbackToActivity，继而到sendBroadcast发送广播，最后调用sendAction方法，回调自定义的IMqttActionListener的实现类。而MqttConnectionListener里的onSuccess是在CommsCallback类里的fireActionEvent方法中，往上走就到CommsCallback类的了handleActionComplete和run()函数。
 
 现在看是不是有点懵毕竟上面有两个 **监听Broker返回的消息**，一个是用来监听Broker发给客户端数据的监听，另一个是客户端发布消息是否发布成功的监听而已。两者都是使用MqttActionListener，不过前者在MqttActionListener监听回调里最后调用的是自定义的MqttCallback回调而已。并且两者监听的位置不一样，前者是在 MqttConnection类的connect时就已确认下来的，对于一个MQTT连接只会有一个，所以这个是一直用来监听数据的；而后者监听发布消息是否成功是每个publish都需要传入的，并在MqttConnection类里的publish初始化。这么讲是不是就清晰一些啦。
 
 哈哈，到此MQTT的publish发布以及接收Broker数据的源码分析也看完啦。
 
 <font color=#ff0000>(注：若有什么地方阐述有误，敬请指正。)</font>
