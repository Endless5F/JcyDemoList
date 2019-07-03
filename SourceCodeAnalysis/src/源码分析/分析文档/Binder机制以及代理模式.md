**  简单说一下，第一次写文章，有点不习惯，从下定决心看一看Android的系统源码开始，看过了简单的系统源码如何修改编译，简单了解了点Linux内核驱动的一点点知识，随后跟着老罗的Android系统源代码情景分析一书看了看Android的启动流程以及Activity跳转，由于这些知识点都需要对Binder机制的进行了解，因此决定看一看Binder机制，本文从Service跨进程间访问的角度了解，由于IBinder.Stub使用代理模式，因此本文也对代理模式简单阐述。<font color=#ff0000>（注：若有什么地方阐述有误，敬请指正。）</font>

## 代理模式
> 代理模式的定义：代理模式属于结构型模式，指的是为其他对象提供一种代理以控制对这个对象的访问。在某些情况下，一个对象不适合或者不能直接引用另一个对象，而代理对象可以在客户端和目标对象之间起到中介的作用。

> 应用场景：当Client为了实现Subject的目标而直接访问RealSubject存在问题的时候（比如对象创建开销很大，或者某些操作需要安全控制，或者需要进程外的访问），就需要Proxy来代替Subject来执行request操作。<font color=#ff0000>例如：AOP切面编程 AIDL跨进程间通讯等</font>

代理模式的主要角色如下：
* 抽象（Subject）类：通过接口或抽象类声明真实主题和代理对象实现的业务方法。
* 真实（Real Subject）类：实现了抽象主题中的具体业务，是代理对象所代表的真实对象，是最终要引用的对象。
* 代理（Proxy）类：提供了与真实主题相同的接口，其内部含有对真实主题的引用，它可以访问、控制或扩展真实主题的功能。

**类图如下：**
![](https://user-gold-cdn.xitu.io/2019/4/9/16a026975054627b?w=925&h=472&f=png&s=181906)

**代码示例：**
```
/**
 * 代理接口
 */
public interface ProxyInterface {
	public abstract void handlingEvents();// 处理事件
}

/**
 * 真实类
 */
public class RealClass implements ProxyInterface {

	@Override
	public void handlingEvents() {
	    System.out.println("正在处理事件中......");
	}
}

/**
 * 代理类
 */
public class ProxyClass implements ProxyInterface {
	private ProxyInterface real;
	
	public Pursuit(ProxyInterface real) {
	    this.real = real;
	}
 
	@Override
	public void handlingEvents() {
	    // todo 执行真实类方法之前可以在此做处理
	    real.handlingEvents();
	    // todo 执行真实类方法之后可以在此做处理
	}
}

/**
 * 调用
 */
public static void main(String[] args) {
    RealClass real = new RealClass();
    ProxyClass daili = new ProxyClass(real);
    daili.handlingEvents();
}
```
**代码说明：**

上述代码示例中，代理类调用真实类的同名方法，此时可以对真实类中的方法执行前后进行处理，比如：计算真实类中方法执行时间或者在真实类方法执行前后分别打印Log等等...，而在AIDL生成的Stub类中的代理类中，就在跨进程间方法执行前后分别调用了写入和读取驱动操作，此话题后续再详细分析。代理类的好处：代理类实现和真实类一样的接口，因此不同代理类实现同一接口的代理类之间，也可以相互代理，下面用代码说明一下：
```
/**
 * 代理类1
 */
public class ProxyClass1 implements ProxyInterface {
	private ProxyInterface real;
	
	public Pursuit(ProxyInterface real) {
	    this.real = real;
	}
 
	@Override
	public void handlingEvents() {
	    System.out.println("方法前");
	    real.handlingEvents();
	    System.out.println("方法后");
	}
}

/**
 * 代理类2
 */
public class ProxyClass2 implements ProxyInterface {
	private ProxyInterface real;
	
	public Pursuit(ProxyInterface real) {
	    this.real = real;
	}
 
	@Override
	public void handlingEvents() {
	    long startTime=System.currentTimeMillis();
	    real.handlingEvents();
	    long endTime=System.currentTimeMillis();
	    float excTime=(float)(endTime-startTime)/1000;
	    System.out.println("执行时间："+excTime+"s");
	}
}

/**
 * 调用
 */
public static void main(String[] args) {
    RealClass real = new RealClass();
    ProxyClass1 daili1 = new ProxyClass1(real);
    ProxyClass2 daili2 = new ProxyClass2(daili1);
    daili2.handlingEvents();
}
```
  上述示例代码中，只是用计时以及打印来表示代理模式的用法以及部分场景，当然场景不止这些，先放出一部分AIDL动态生成的Stub类的代理类中的部分代码：
  ```
  /**
    首先就是创建了3个对象_data 输入对象，_reply输出对象，_result返回值对象然后把参数信息 写入到_data里，
    接着就调用了transact这个方法 来发送rpc请求，然后接着当前线程挂起， 服务端的onTransace方法才被调用，
    调用结束以后 当前线程继续执行，直到从_reply中取出rpc的返回结果 然后返回_reply的数据
    注：有返回值的方法才有_result对象
  */
  android.os.Parcel _data = android.os.Parcel.obtain();
  android.os.Parcel _reply = android.os.Parcel.obtain();
  java.util.List<com.example.administrator.aidlmessagetest.Person> _result;
  try {
    // 执行方法前
    _data.writeInterfaceToken(DESCRIPTOR);
    if ((person != null)) {
        _data.writeInt(1);
        person.writeToParcel(_data, 0);
    } else {
        _data.writeInt(0);
    }
    // 真实调用跨进程间的方法
    mRemote.transact(Stub.TRANSACTION_addPerson, _data, _reply, 0);
    // 执行方法后
    _reply.readException();
    _result = _reply.createTypedArrayList(com.example.administrator.aidlmessagetest.Person.CREATOR);
  } finally {
    _reply.recycle();
    _data.recycle();
  }
  return _result;
  ```
**动态代理：**

现在要生成某一个对象的代理对象，这个代理对象通常也要编写一个类来生成，所以首先要编写用于生成代理对象的类。在java中如何用程序去生成一个对象的代理对象呢，java在JDK1.5之后提供了一个"java.lang.reflect.Proxy"类，通过"Proxy"类提供的一个newProxyInstance方法用来创建一个对象的代理对象。

Proxy类中的方法：(<font color=#ff0000>最常用的方法就是：newProxyInstance</font>)
* 方法 1: 该方法用于获取指定代理对象所关联的InvocationHandler
static InvocationHandler getInvocationHandler(Object proxy) 

* 方法 2：该方法用于获取关联于指定类装载器和一组接口的动态代理类的类对象
static Class getProxyClass(ClassLoader loader, Class[] interfaces) 

* 方法 3：该方法用于判断指定类是否是一个动态代理类
static boolean isProxyClass(Class cl) 

* 方法 4：该方法用于为指定类装载器、一组接口及调用处理器生成动态代理类实例
static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h)

JDK中的动态代理是通过反射类Proxy以及InvocationHandler回调接口实现的;但是，JDK中所要进行动态代理的类必须要实现一个接口，也就是说只能对该类所实现接口中定义的方法进行代理，这在实际编程中具有一定的局限性，而且使用反射的效率也并不是很高。

JDK动态代理的方案：
* JDK自带的(Proxy)
* ASM (Bytecode Proxy)：
ASM在创建class字节码的过程中，操纵的级别是底层JVM的汇编指令级别，这要求ASM使用者要对class组织结构和JVM汇编指令有一定的了解（需要手动生成）。
* CGLIB (基于ASM包装)
* JAVAASSIST (Proxy / BytecodeProxy)：
Javassist是一个开源的分析、编辑和创建Java字节码的类库。
是由东京工业大学的数学和计算机科学系的 Shigeru Chiba （千叶 滋）所创建的。它已加入了开放源代码JBoss 应用服务器项目,通过使用Javassist对字节码操作为JBoss实现动态AOP框架。javassist是jboss的一个子项目，其主要的优点，在于简单，而且快速。直接使用java编码的形式，而不需要了解虚拟机指令，就能动态改变类的结构，或者动态生成类。

Bytecode Proxy：直接生成二进制字节码格式的代理类

动态代理方案性能对比：
1. ASM和JAVAASSIST Bytecode Proxy生成方式不相上下，都很快，是CGLIB的5倍。
2. CGLIB次之，是JDK自带的两倍。
3. JDK自带的再次之，因JDK1.6对动态代理做了优化，如果用低版本JDK更慢，要注意的是JDK也是通过字节码生成来实现动态代理的，而不是反射。
4. JAVAASSIST Proxy动态代理接口最慢，比JDK自带的还慢。

(这也是为什么网上有人说JAVAASSIST比JDK还慢的原因，用JAVAASSIST最好别用它提供的动态代理接口，而可以考虑用它的字节码生成方式)

差异原因：
各方案生成的字节码不一样，像JDK和CGLIB都考虑了很多因素，以及继承或包装了自己的一些类，
所以生成的字节码非常大，而我们很多时候用不上这些，而手工生成的字节码非常小，所以速度快。


![](https://user-gold-cdn.xitu.io/2019/4/10/16a0545580d221e1?w=261&h=248&f=png&s=66739)

                                        哈哈哈，盗张图片清醒一下
接下来重新进入JDK提供的动态代理方式：Proxy.newProxyInstance(ClassLoader loader,Class<?>[] interfaces,InvocationHandler handler),这个方法有3个参数，下面分别来看一看：
* loader：用哪个类加载器去加载代理对象
* interfaces：动态代理类需要实现的接口
* handler：动态代理方法在执行时，会调用handler里面的invoke方法去执行，并且若需要在真实类方法执行前后做相应处理，也是在InvocationHandler里，如下面代码所示：
```
public class TestInvacationHandler implements InvocationHandler {
    private final ProxyInterface proxy;
    public TestInvacationHandler(ProxyInterface proxy){
        this.proxy = proxy;
    }
    /**
        proxy：就是代理对象，newProxyInstance方法的返回对象
        method：调用的方法
        args: 方法中的参数
    */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 方法执行前
        System.out.println("---------before-------");
        // 调用方法
        Object invoke = method.invoke(proxy, args);
        // 方法执行后
        System.out.println("---------after-------");
    }
}
```
结合上面的ProxyInterface RealClass以及TestInvacationHandler完整调用：
```
public static void main(String[] args) {
    ProxyInterface real = new RealClass();
    ProxyInterface proInterface = (ProxyInterface)Proxy
        .newProxyInstance(real.getClass().getClassLoader()
        ,RealClass.class.getInterfaces(), new TestInvacationHandler(real));
    proInterface.handlingEvents();
}
```
至此，礼敬，代理模式到此已结束，若有什么地方阐述有误，敬请指正。

## Binder机制
![](https://user-gold-cdn.xitu.io/2019/4/10/16a05616a968d5e7?w=500&h=395&f=png&s=138024)
继续盗图，哈哈哈，接下来终于到了Binder机制啦！！！先来张图清爽一下

![](https://user-gold-cdn.xitu.io/2019/4/10/16a067cadd4afb32?w=2892&h=1532&f=jpeg&s=376951)

此图为Service跨进程间通讯绑定服务时序图，Service绑定流程中也牵扯着IBinder调用，比如IActivityManager（ActivityManagerService）。

**Binder机制基本了解：**
* 进程通信
    1. 进程隔离：出于安全考虑，一个进程不能操作另一个进程的数据，进而一个操作系统必须具备进程隔离这个特性。在Linux系统中，虚拟内存机制为每个进程分配了线性连续的内存空间，操作系统将这种虚拟内存空间映射到物理内存空间，每个进程有自己的虚拟内存空间，进而不能操作其他进程的内存空间，每个进程只能操作自己的虚拟内存空间，只有操作系统才有权限操作物理内存空间。进程隔离保证了每个进程的内存安全，但是在大多数情形下，不同进程间的数据通讯是不可避免的，因此操作系统必须提供跨进程通信机制。
    进程空间分为内核空间和用户空间，内核空间（Kernel）是系统内核运行的空间。用户空间（User Space）是用户程序运行的空间，他们之间是隔离的。内核有访问的所有权限，用户空间也可以通过系统接口去访问内核空间。用户空间可以通过内核空间（类似于中介者）进行相互访问。
    2. Binder机制优点：
    
        传输性能好：
    
        * Socket：是一个通用接口，导致其传输效率低，开销大
        * 共享内存：虽然在传输时不需要拷贝数据，但其控制机制复杂
        * Binder：复杂数据类型传递可以复用内存，需要拷贝1次数据
        * 管道和消息队列：采用存储转发方式，至少需要拷贝2次数据，效率低

        稳定性：

        * Binder基于C/S架构，Server端和Client端相对独立，稳定性好。
        * 共享内存没有Server端和Client端的区分，可能存在同步死锁等问题。Binder稳定性优于共享内存。

        安全性高：
        
        * 传统的进程：通信方式对于通信双方的身份并没有做出严格的验证，只有在上层协议上进行架设
        * Binder机制：从协议本身就支持对通信双方做身份校检，因而大大提升了安全性
    
    3. Binder机制实现原理图：（借图,下面会注明来处）
![](https://user-gold-cdn.xitu.io/2019/4/10/16a070eb35f87109?w=1117&h=1220&f=png&s=275006)

**Binder机制之AIDL：**

接下来根据生成的aidl文件源码分析跨进程间通讯（aidl生成：new->AIDL->AIDL File-> Build）
原谅我又把别人写好的借来（下面会注明来处）
```
/**
    标准的代理模式，代理类为Proxy，此文件为动态生成，因此Stub Proxy都为固定类名
*/
package com.example.administrator.aidlmessagetest;

//从前面几行就能看出来 生成的代码是一个 interface ，只不过这个interface是 android.os.IInterface 的子类！
public interface IPersonManager extends android.os.IInterface {

    //这个接口里 有一个静态的抽象类Stub
    //这个Stub是Binder的子类，并且实现了IPersonManager 这个接口
    public static abstract class Stub extends android.os.Binder implements com.example.administrator.aidlmessagetest.IPersonManager {
    
        //这个东西就是唯一的binder标示 可以看到就是IPersonManager的全路径名
        private static final java.lang.String DESCRIPTOR = "com.example.administrator.aidlmessagetest.IPersonManager";

        /**
         * 这个就是Stub的构造方法，我们如果写好aidl文件以后 写的service里面 是怎么写的？
         * private final IPersonManager.Stub mBinder = new IPersonManager.Stub() {}
         * 我们都是这么写的 对吧~~所以想想我们的service里面的代码 就能辅助理解 这里的代码了
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        //这个方法 其实就做了一件事，如果是同一个进程，那么就返回Stub对象本身
        //如果不是同一个进程，就返回Stub.Proxy这个代理对象了
        public static com.example.administrator.aidlmessagetest.IPersonManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            //如果是同1个进程，也就是说进程内通信的话 我们就返回括号内里的对象
            if (((iin != null) && (iin instanceof com.example.administrator.aidlmessagetest.IPersonManager))) {
                return ((com.example.administrator.aidlmessagetest.IPersonManager) iin);
            }
            //如果不是同一进程，是2个进程之间相互通信，那我们就得返回这个Stub.Proxy 看上去叫Stub 代理的对象了
            return new com.example.administrator.aidlmessagetest.IPersonManager.Stub.Proxy(obj);
        }

        //返回当前对象
        @Override
        public android.os.IBinder asBinder() {
            return this;
        }
        
        /**
         * 只有在跨进程通信的时候 才会调用这个方法 ，同一个进程是不会调用的。
         *
         * 首先 我们要明白 这个方法 一般情况下都是返回true的，
         * 也只有返回true的时候才有意义，如果返回false了就代表这个方法执行失败
         * 所以我们通常是用这个方法来做权限认证的，其实也很好理解，既然是多进程通信，
         * 那么我们服务端的进程当然不希望谁都能过来调用所以权限认证是必须的，权限认证先略过
         * 
         * 除此之外 ，onTransact 这个方法就是运行在Binder线程池中的，
         * 一般就是客户端发起请求，然后android底层代码把这个客户端发起的请求封装成3个参数，
         * 来调用这个onTransact方法，第一个参数code就代表客户端想要调用服务端方法的标志位。
         * 其实也很好理解 服务端可能有n个方法 每个方法都有一个对应的int值来代表，
         * 这个code就是这个int值，用来标示客户端想调用的服务端的方法
         * data就是方法参数，reply就是方法返回值。都很好理解
         *
         * 其实隐藏了很重要的一点，这个方法既然是运行在binder线程池中的，
         * 所以在这个方法里面调用的服务器方法也是运行在Binder线程池中的，
         * 所以我们要记得 如果你的服务端程序有可能和多个客户端相联的话，
         * 你方法里使用的那些参数 必须要是支持异步的，否则的话值就会错乱了！
         * 这点一定要记住！结论就是Binder方法 一定要是同步方法！！！！！！
        */
        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_getPersonList: {
                    data.enforceInterface(DESCRIPTOR);
                    java.util.List<com.example.administrator.aidlmessagetest.Person> _result = this.getPersonList();
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                }
                case TRANSACTION_addPerson: {
                    data.enforceInterface(DESCRIPTOR);
                    com.example.administrator.aidlmessagetest.Person _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.example.administrator.aidlmessagetest.Person.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.addPerson(_arg0);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        // 只有在跨进程通信的情况下  才会返回这个代理的对象
        private static class Proxy implements com.example.administrator.aidlmessagetest.IPersonManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            public java.lang.String getInterfaceDescriptor() {
                return DESCRIPTOR;
            }

            /**
             * 这里有2个方法 一个getPersonList 一个addPerson，我们就分析一个方法就可以了
             *
             * 首先就是创建了3个对象，
             * _data 输入对象，_reply输出对象，_result返回值对象，把参数信息写入到_data里，
             * 接着就调用了transact这个方法来发送rpc请求，然后接着当前线程挂起，
             * 服务端的onTransace方法才被调用，调用结束*以后当前线程继续执行，
             * 直到从_reply中取出rpc的返回结果然后返回_reply的数据所以这里我们就要注意了，
             * 客户端发起调用远程请求时，当前客*户端的线程就会被挂起了，
             * 所以如果一个远程方法 很耗时，
             * 我们客户端就一定不能在ui main线程里在发起这个rpc请求，不然就anr了。
             *
             * 注：这2个方法运行在客户端！！！！！！！！！！！！！！！！
             * 注：若方法无返回值，则无_result返回值对象
            */
            @Override
            public java.util.List<com.example.administrator.aidlmessagetest.Person> getPersonList() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.util.List<com.example.administrator.aidlmessagetest.Person> _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getPersonList, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArrayList(com.example.administrator.aidlmessagetest.Person.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void addPerson(com.example.administrator.aidlmessagetest.Person person) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((person != null)) {
                        _data.writeInt(1);
                        person.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_addPerson, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_getPersonList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_addPerson = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    }

    public java.util.List<com.example.administrator.aidlmessagetest.Person> getPersonList() throws android.os.RemoteException;

    public void addPerson(com.example.administrator.aidlmessagetest.Person person) throws android.os.RemoteException;
}
```
bindService时序图中绑定服务操作是使用IBinder进行的，那么IBinder是如何获取的，什么时候初始化好的？从上述代码分析来看，只能从Stub()的构造方法入手，Stub构造方法一调用其父类构造方法顺便也会被调用，其父类构造方法代码如下：
```
public Binder() {
    // private static native long getNativeBBinderHolder();
    // getNativeBBinderHolder 为native方法
    mObject = getNativeBBinderHolder();
    NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, mObject);

    if (FIND_POTENTIAL_LEAKS) {
        final Class<? extends Binder> klass = getClass();
        if ((klass.isAnonymousClass() || klass.isMemberClass() || klass.isLocalClass()) &&
            (klass.getModifiers() & Modifier.STATIC) == 0) {
            Log.w(TAG, "The following Binder class should be static or leaks might occur: "     +klass.getCanonicalName());
        }
    }
}
```
从Binder构造方法可以看出，只要Stub被初始化就会调用Binder构造方法，从而会调用native层c/c++代码初始化一个IBinder，并从内核Binder驱动中进行注册。

继续看上述aidl生成的代码，要明确的是Proxy代理类是在客户端(即调用端)，所以里面的方法也都是客户端调用，而onTransact方法则是在服务端(即目标端，跨进程访问端)。其方法调用过程就是：Proxy类方法(即客户端)调用mRemote.transact(),会触发目标端执行Binder::onTransact()(即上述代码中的onTransact方法)，我们可以看看transact()方法和onTransact()方法的参数对比：
```
/*
    参数一：用来标识指令，即调用的什么方法。需要客户端和服务端约定好code码。
    参数二：来自发送端的数据包。包含参数信息
    参数三：来自发送端的接收包，往这个包中写数据，就相当于给发送端返回数据。
    参数四：特殊操作标识。
*/
public boolean transact(int code, Parcel data, Parcel reply, int flags){}
public boolean onTransact(int code, Parcel data, Parcel reply, int flags){}
```
可以看出来，两个是成对的操作。mRemote.transact()操作是一个阻塞式的操作，就是说在这个方法执行返回成功后，直接从reply中读取的数据就是远程端在Binder::onTransact()中填充的数据。而编译器自动帮我们生成的onTransact()中，会读取data中数据，然后调用对应的方法。大致调用情况如下图：

![](https://user-gold-cdn.xitu.io/2019/4/10/16a07f20127cb84c?w=1101&h=448&f=png&s=30922)

至此，完毕。

本文参考博客：
* https://blog.csdn.net/carson_ho/article/details/73560642
* https://www.cnblogs.com/punkisnotdead/p/5163464.html
* ......

想研究更底层Binder机制可参考：https://www.jianshu.com/p/fe816777f2cf