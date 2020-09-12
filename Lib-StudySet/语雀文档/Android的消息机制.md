# Android的消息机制

## Android的消息机制概述
Android的消息机制主要是指Handler的运行机制以及所附带的MessageQueue和Looper的工作过程，这三者实际上是一个整体，只不过我们在开发的时候比较接触多的是Handler而已，Handler的主要作用是将一个任务切换到某个指定的线程中去执行，那么Android为什么要提供这种功能呢？这是因为android的UI规范不允许子线程更新UI，否则会抛出异常，ViewRootImpl对UI的操作做了验证，这个验证工作是由ViewRootImpl的checkThread来完成的。系统提供Handler，主要的原因就是解决在子线程中无法访问UI的矛盾。

系统为什么不允许在子线程访问UI呢？这是因为Android的UI控件不是线程安全的，如果在多线程中并发访问可能会导致UI控件处于不可预期的状态，那为什么系统不对UI控件访问加上锁机制呢？缺点有两个，首先加上锁后会让UI访问的逻辑变得复杂，其次是会降低UI的访问频率，所以最简单搞笑就是采用单线程模型来处理UI操作。

Handler工作原理：Handler的创建会采用当前线程的Lopper来构建内部的消息循环系统，如果没有，就会报错。
如何解决这个问题，只需要为当前线程创建一个looper即可，或者在一个有Lopper的线程中创建Handler也行。
Handler创建完毕后这个时候内部的Lopper以及MeaasgeQueue也可以和Handler一起协同工作，然后通过Handler的post方法将一个Runnable投递到Handler内部的Lopper中去处理，也可以通过Handler的send方法发送一个消息，这个消息同样会在Lopper中去处理，其实post方法最终还是通过send方法来完成的，接下来我们来看下send方法的工作过程，当Handler的send被调用的时候，会他向MessageQueu的enqueueMessage方法将这个消息放入消息队列，然后Lopper发现新消息到来时，就会处理这个消息，最终消息的Runnable或者Handler的handlerMessage方法就被调用，注意Lopper是运行在创建Handler所在的线程中，这样Handler中的业务就会被切换到所在线程执行了。
## Android的消息机制分析
### ThreadLocal的工作原理
#### ThreadLocal是什么
ThreadLocal是一个线程内部的数据存储类，它是一个数据结构，有点像HashMap，可以保存"key : value"键值对，但是一个ThreadLocal只能保存一个，并且各个线程的数据互不干扰。在日常开发中用到ThreadLocal的地方较少，但是在某些特殊的场景下，通过ThreadLocal可以轻松的实现一些看起来很复杂的功能，这一点在android的源码中也有所体现，比如Lopper，ActivityThread以及AMS中都是用到了ThreadLocal，这个不好描述，一般来说，某一个数据是以线程为作用域并且不同线程具有不同的Lopper，这个时候通过ThreadLocal就可以轻松的实现Looper在线程中的存取，如果不采取ThreadLocal，那么系统就必须提供一个全局的哈希表来Handler查找指定线程的Lopper，这样一来就必须提供一个类似于LooperManager的类了，但是系统并没有这么做而是选择了ThreadLocal，这就是ThreadLocal的好处。
#### ThreadLocal的使用
```
ThreadLocal<String> localName = new ThreadLocal();
localName.set("This is Threadocal");
String name = localName.get();
```
在线程1中初始化了一个ThreadLocal对象localName，并通过set方法，保存了一个值`"This is Threadocal"`，同时在线程1中通过`localName.get()`可以拿到之前设置的`"This is Threadocal"`值，但是如果在线程2中，拿到的将是一个null。ThreadLocal保证了各个线程的数据互不干扰。

看看`set(T value)`和`get()`方法的源码：
```
public void set(T value) {
		// 获取当前线程
    Thread t = Thread.currentThread();
    // 根据当前线程获取ThreadLocalMap对象
    ThreadLocalMap map = getMap(t);
    if (map != null)
        map.set(this, value);
    else
        createMap(t, value);
}

public T get() {
    Thread t = Thread.currentThread();
    ThreadLocalMap map = getMap(t);
    if (map != null) {
        ThreadLocalMap.Entry e = map.getEntry(this);
        if (e != null) {
            @SuppressWarnings("unchecked")
            T result = (T)e.value;
            return result;
        }
    }
    return setInitialValue();
}

ThreadLocalMap getMap(Thread t) {
    return t.threadLocals;
}

// 创建ThreadLocalMap对象
void createMap(Thread t, T firstValue) {
		// 初始化ThreadLocalMap对象，绑定到当前线程的threadLocals成员属性中
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```
可以发现，每个线程中都有一个`ThreadLocalMap`数据结构，当执行set方法时，其值是保存在当前线程的`threadLocals`变量中，当执行get方法时，是从当前线程的`threadLocals`变量获取。所以在线程1中set的值，对线程2来说是摸不到的，而且在线程2中重新set的话，也不会影响到线程1中的值，保证了线程之间不会相互干扰。
#### ThreadLoalMap
ThreadLoalMap是一个类似HashMap的数据结构，但是在ThreadLocal中，并没实现Map接口。

在ThreadLoalMap中，也是初始化一个大小16的Entry数组，Entry对象用来保存每一个key-value键值对，只不过这里的key永远都是ThreadLocal对象，是不是很神奇，通过ThreadLocal对象的set方法，结果把ThreadLocal对象自己当做key，放进了ThreadLoalMap中。

为什么ThreadLoalMap里面默认需要使用一个大小16的Entry数组？
答：虽然一个ThreadLocal<T>对象只能保存一个类型的一个值，但是同一个线程中的所有ThreadLocal<T>对象对象都保存在Thread.threadLocals成员变量中，即保存在ThreadLoalMap的Entry数组中，因此ThreadLoalMap里面默认需要使用一个大小16的Entry数组，当数组大小不够时将会自动扩充。

![16119267b89518d3.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579782973240-09cd4a0e-ae59-4464-90aa-d12ff69c09b4.png#align=left&display=inline&height=480&name=16119267b89518d3.png&originHeight=480&originWidth=1068&size=31942&status=done&style=none&width=1068)
这里需要注意的是，ThreadLoalMap的Entry是继承WeakReference，和HashMap很大的区别是，Entry中没有next字段，所以就不存在链表的情况了。
#### hash冲突
没有链表结构，存储时发生hash冲突了怎么办？
答：每个ThreadLocal对象都有一个hash值`threadLocalHashCode`，每初始化一个ThreadLocal对象，hash值就增加一个固定的大小`0x61c88647`。
```
// 自定义哈希码（仅在ThreadLocalMap中有用）-- 可用于降低hash冲突
private final int threadLocalHashCode = nextHashCode();
// 下一个哈希码hashCode，保存当前ThreadLocal的Hash值(在ThreadLocalMap使用)。操作是原子性的。
private static AtomicInteger nextHashCode = new AtomicInteger();
// 表示连续分配的两个ThreadLocal实例的threadLocalHashCode值的增量 
private static final int HASH_INCREMENT = 0x61c88647;
// 返回下一个哈希码hashCode，即当前ThreadLocal的hash值
private static int nextHashCode() {
	// 获取并添加
	return nextHashCode.getAndAdd(HASH_INCREMENT);
}
```
ThreadLocalMap在插入过程中，根据ThreadLocal对象的hash值，定位到table中的位置i，过程如下：

1. 如果当前位置是空的，那么正好，就初始化一个Entry对象放在位置i上；
1. 不巧，位置i已经有Entry对象了，如果这个Entry对象的key正好是即将设置的key，那么重新设置Entry中的value；
1. 很不巧，位置i的Entry对象，和即将设置的key没关系，那么只能找下一个空位置；

这样的话，在get的时候，也会根据ThreadLocal对象的hash值，定位到table中的位置，然后判断该位置Entry对象中的key是否和get的key一致，如果不一致，就判断下一个位置。可以发现，set和get如果冲突严重的话，效率很低，因为ThreadLoalMap是Thread的一个属性，所以即使在自己的代码中控制了设置的元素个数，但还是不能控制其它代码的行为。
#### 会导致内存泄露？
ThreadLocal会导致内存泄露，原因如下：

- 首先ThreadLocal实例被线程的ThreadLocalMap实例持有，也可以看成被线程持有；
- 如果应用使用了线程池，那么之前的线程实例处理完之后出于复用的目的依然存活；

上面的逻辑是清晰的，可是ThreadLocal并不会产生内存泄露，因为ThreadLocalMap在选择key的时候，并不是直接选择ThreadLocal实例，而是ThreadLocal实例的弱引用。
```
static class Entry extends WeakReference<ThreadLocal<?>> {
    Object value;
    Entry(ThreadLocal<?> k, Object v) {
        super(k);
        value = v;
    }
}
```
所以实际上从ThreadLocal设计角度来说是不会导致内存泄露的。如果依旧不放心则可以使用ThreadLocal.remove();
## [Handler机制](https://juejin.im/post/5cd7b89be51d45475e613e59)
[https://juejin.im/post/5cd7b89be51d45475e613e59](https://juejin.im/post/5cd7b89be51d45475e613e59)
## 参考：
### 1. [理解Java中的ThreadLocal](https://droidyue.com/blog/2016/03/13/learning-threadlocal-in-java/)
### 2. [Java面试必问，ThreadLocal终极篇](https://juejin.im/post/5a64a581f265da3e3b7aa02d)
