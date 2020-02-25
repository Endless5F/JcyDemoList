# ArrayMap&amp;SparseArray&amp;序列化

## 基础
### 关键字transient
作用：将不需要序列化的属性前添加关键字transient，序列化对象的时候，这个属性就不会被序列化。
#### 深入分析transient关键字

- （1）transient底层实现的原理是什么？
- （2）被transient关键字修饰过得变量真的不能被序列化嘛？
- （3）静态变量能被序列化吗？被transient关键字修饰之后呢？

**1、transient底层实现原理是什么？**
java的serialization提供了一个非常棒的存储对象状态的机制，说白了serialization就是把对象的状态存储到硬盘上 去，等需要的时候就可以再把它读出来使用。有些时候像银行卡号这些字段是不希望在网络上传输的，transient的作用就是把这个字段的生命周期仅存于调用者的内存中而不会写到磁盘里持久化，意思是transient修饰的age字段，他的生命周期仅仅在内存中，不会被写到磁盘中。
**2、被transient关键字修饰过得变量真的不能被序列化嘛？**
想要解决这个问题，首先还要再重提一下对象的序列化方式：
Java序列化提供两种方式，一种是实现Serializable接口、另一种是实现Exteranlizable接口。 Exteranlizable接口需要重写writeExternal和readExternal方法，它的效率比Serializable高一些，并且可以决定哪些属性需要序列化，但是对大量对象，或者重复对象，则效率低。
使用Exteranlizable接口实现序列化时，我们自己指定那些属性是需要序列化的，只要实现了Externalizable接口，哪一个属性被序列化使我们手动去指定的，即使是transient关键字修饰也不起作用。
**3、静态变量能被序列化吗？没被transient关键字修饰之后呢？**
这个我可以提前先告诉结果，静态变量是不会被序列化的，即使没有transient关键字修饰。
原因：静态变量在全局区，而序列化操作的是堆内存，所以JVM查找这个静态变量的值，是从全局区查找的，而不是磁盘上，由于无法在堆内存中查找静态变量，因此即使没有transient关键字修饰静态变量也不会被序列化的。
### 序列化与反序列化

- 序列化：指把堆内存中的 Java 对象数据，通过某种方式把对象存储到磁盘文件中或者传递给其他网络节点（在网络上传输）。这个过程称为序列化。通俗来说就是将数据结构或对象转换成二进制串的过程。
- 反序列化：把磁盘文件中的对象数据或者把网络节点上的对象数据，恢复成Java对象模型的过程。也就是将在序列化过程中所生成的二进制串转换成数据结构或者对象的过程。
#### Java 怎么进行序列化？

- ①、需要做序列化的对象的类，必须实现序列化接口：Java.lang.Serializable 接口（这是一个标志接口，没有任何抽象方法），Java 中大多数类都实现了该接口，比如：String，Integer
- ②、底层会判断，如果当前对象是 Serializable 的实例，才允许做序列化，Java对象 instanceof Serializable 来判断。
- ③、在 Java 中使用对象流来完成序列化和反序列化
  - **ObjectOutputStream：**通过 writeObject()方法做序列化操作
  - **ObjectInputStream：**通过 readObject() 方法做反序列化操作
```
public class User implements Serializable {
    private static final long serialVersionUID = -4454266436543306544L;
    public int userId;
    public String userName;
    public User(int userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }
}

//序列化过程
User user = new User(1, "Tom");
ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("cache.txt"));
out.writeObject(user);
out.close();
//反序列化过程
ObjectInputStream in = new ObjectInputStream(new FileInputStream("cache.txt"));
User newuser = (User) in.readObject();
in.close();
```
##### serialVersionUID
上面User类中声明了一个serialVersionUID，实际上这个serialVersionUID不是必需的，我们不声明这个serialVersionUID同样可以实现序列化，但是这将会对反序列化过程产生影响，具体会产生什么影响呢？
解决这个问题前我们先提一个问题，为什么需要serialVersionUID呢？
> 因为静态成员变量属于类不属于对象，不会参与序列化过程，使用transient关键字标记的成员变量也不参与序列化过程。 （PS：关键字transient，这里简单说明一下，Java的serialization提供了一种持久化对象实例的机制。当持久化对象时，可能有一个特殊的对象数据成员，我们不想用serialization机制来保存它。为了在一个特定对象的一个域上关闭serialization，可以在这个域前加上关键字transient。当一个对象被序列化的时候，transient型变量的值不包括在序列化的表示中，然而非transient型的变量是被包括进去的）

这个时候又有一个疑问serialVersionUID是静态成员变量不参与序列化过程，那么它的存在与否有什么影响呢？
> 具体过程是这样的：序列化操作的时候系统会把当前类的serialVersionUID写入到序列化文件中，当反序列化时系统会去检测文件中的serialVersionUID，判断它是否与当前类的serialVersionUID一致，如果一致就说明序列化类的版本与当前类版本是一样的，可以反序列化成功，否则失败。

InvalidClassException：
![](//upload-images.jianshu.io/upload_images/1319879-4d3591c29450aa87?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp#align=left&display=inline&height=103&originHeight=103&originWidth=1200&status=done&style=none&width=1200)
接下来我们回答声明serialVersionUID对进行序列化有啥影响？
> 如果不手动指定serialVersionUID的值，反序列化时当前类有所改变，比如增加或者删除了某些成员变量，那么系统就会重新计算当前类的hash值并且把它赋值给serialVersionUID，这个时候当前类的serialVersionUID就和序列化的数据中的serialVersionUID不一致，于是反序列化失败。所以我们手动指定serialVersionUID的值能很大程度上避免了反序列化失败。

### Android中的Parcelable
```
public class Book implements Parcelable {
    public int bookId;
    public String bookName;
    public Book() {
    }
    public Book(int bookId, String bookName) {
        this.bookId = bookId;
        this.bookName = bookName;
    }
    //从序列化后的对象中创建原始对象
    protected Book(Parcel in) {
        bookId = in.readInt();
        bookName = in.readString();
    }
    public static final Creator<Book> CREATOR = new Creator<Book>() {
        //从序列化后的对象中创建原始对象
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }
        //指定长度的原始对象数组
        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    //返回当前对象的内容描述。如果含有文件描述符，返回1，否则返回0，几乎所有情况都返回0
    @Override
    public int describeContents() {
        return 0;
    }
    //将当前对象写入序列化结构中，其flags标识有两种（1|0）。
    //为1时标识当前对象需要作为返回值返回，不能立即释放资源，几乎所有情况下都为0.
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookId);
        dest.writeString(bookName);
    }
    @Override
    public String toString() {
        return "[bookId=" + bookId + ",bookName='" + bookName + "']";
    }
}
```
虽然Serializable可以将数据持久化在磁盘，但其在内存序列化上开销比较大（PS：Serializable在序列化的时候会产生大量的临时变量，从而引起频繁的GC），而内存资源属于android系统中的稀有资源（android系统分配给每个应用的内存开销都是有限的），为此android中提供了Parcelable接口来实现序列化操作，在使用内存的时候，Parcelable比Serializable性能高，所以推荐使用Parcelable。
Parcelable内部包装了可序列化的数据，可以在Biander中自由的传输，从代码中可以看出，在序列化过程中需要实现的功能有序列化，反序列化和内容描述。序列化功能是由writetoParcel方法来完成，最终是通过Parcel中的一系列write方法来完成的。反序列化功能是由CREATOR方法来完成，其内部标明了如何创建序列化对象和数组，并通过Parcel的一系列read方法来完成反序列化过程（PS：write和read的顺序必须一致~！）；内容描述功能是有describeContents方法来完成，几乎所有情况下这个方法都应该返回0，仅当当前对象中存在文件描述符时，此方法返回1。系统已经给我们提供了许多实现了Parcelable接口类，他们都是可以直接序列化的，比如Intent，Bundle，Bitmap等，同时List和Map也支持序列化，提前是他们里面的每个元素都是可以序列化的。
### [Java的深拷贝和浅拷贝](https://www.cnblogs.com/ysocean/p/8482979.html)
#### 创建对象的5种方式

- 　**①、通过 new 关键字：**这是最常用的一种方式，通过 new 关键字调用类的有参或无参构造方法来创建对象。比如 Object obj = new Object();
- 　**②、通过 Class 类的 newInstance() 方法：**这种默认是调用类的无参构造方法创建对象。比如 Person p2 = (Person) Class.forName("com.ys.test.Person").newInstance();
- 　**③、通过 Constructor 类的 newInstance 方法：**这和第二种方法类时，都是通过反射来实现。通过 java.lang.relect.Constructor 类的 newInstance() 方法指定某个构造器来创建对象。

　　Person p3 = (Person) Person.class.getConstructors()[0].newInstance();
　　实际上第二种方法利用 Class 的 newInstance() 方法创建对象，其内部调用还是 Constructor 的 newInstance() 方法。

- 　**④、利用 Clone 方法：**Clone 是 Object 类中的一个方法，通过 对象A.clone() 方法会创建一个内容和对象 A 一模一样的对象 B，clone 克隆，顾名思义就是创建一个一模一样的对象出来。

　　Person p4 = (Person) p3.clone();

- 　**⑤、反序列化：**序列化是把堆内存中的 Java 对象数据，通过某种方式把对象存储到磁盘文件中或者传递给其他网络节点（在网络上传输）。而反序列化则是把磁盘文件中的对象数据或者把网络节点上的对象数据，恢复成Java对象模型的过程。
#### Clone 方法
Java 的深拷贝和浅拷贝，其实现方式正是通过调用 Object 类的 clone() 方法来完成，这是一个native 方法。
注意：调用对象的 clone 方法，必须要让类实现 Cloneable 接口，并且覆写 clone 方法。
#### 浅拷贝
创建一个新对象，然后将当前对象的非静态字段复制到该新对象，如果字段是值类型的，那么对该字段执行复制；如果该字段是引用类型的话，则复制引用但不复制引用的对象。因此，原始对象及其副本引用同一个对象。
#### 深拷贝
创建一个新对象，然后将当前对象的非静态字段复制到该新对象，无论该字段是值类型的还是引用类型，都复制独立的一份。当你修改其中一个对象的任何内容时，都不会影响另一个对象的内容。
#### 如何实现深拷贝？
Object 类提供的 clone 是只能实现 浅拷贝的。
深拷贝的原理：让原始对象和克隆之后的对象所具有的引用类型属性不是指向同一块堆内存。
##### ①、让每个引用类型属性内部都重写clone() 方法
clone() 方法麻烦一些，需要将所有涉及到的类实现声明式接口 Cloneable，并覆盖Object类中的clone()方法，并设置作用域为public（这是为了其他类可以使用到该clone方法）。
##### ②、利用序列化
序列化是将对象写到流中便于传输，而反序列化则是把对象从流中读取出来。这里写到流中的对象则是原始对象的一个拷贝，因为原始对象还存在 JVM 中，所以我们可以利用对象的序列化产生克隆对象，然后通过反序列化获取这个对象。
注意：每个需要序列化的类都要实现 Serializable 接口，如果有某个属性不需要序列化，可以将其声明为 transient，即将其排除在克隆属性之外。
```
//深度拷贝
public Object deepClone() throws Exception{
    // 序列化
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(bos);
    oos.writeObject(this);
    // 反序列化
    ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
    ObjectInputStream ois = new ObjectInputStream(bis);
    return ois.readObject();
}
```
### [数组复制速度](https://blog.csdn.net/qq_34834846/article/details/97174521)
**System.arraycopy() **vs** clone() **vs** Arrays.copyof() **vs** for()**
**结论：**在数组的长度不是很大的时候，基本遵循的规律：System.arraycopy >Object.clone>Arrays.copyOf > for
#### System.arraycopy()**（浅拷贝）**
在主流高性能的JVM上（HotSpot VM系、IBM J9 VM系、JRockit系等等），可以认为System.arraycopy()在拷贝数组时是可靠高效的——如果发现不够高效的情况，请报告performance bug，肯定很快就会得到改进。java.lang.System.arraycopy()方法在Java代码里声明为一个native方法。所以最native的实现方式就是通过JNI调用JVM里的native代码来实现。但实际上在高性能的JVM里，System.arraycopy() （以及相关的 java.util.Arrays.copyOf() ）都会被实现为intrinsic method，JVM内会对它做特殊的优化处理，在优化后并不会通过JNI那种慢速方式来实现。
以HotSpot VM为例，它的Server Compiler（C2）会在编译System.arraycopy()的调用点的时候，编译时判断传入参数的类型（以及可能有常量值的情况）来判断要选用哪种具体实现方式，大部分情况下都会调用高度优化的手写汇编实现的stub，尽可能使用当前CPU所提供的SIMD指令来提高速度。
#### Object.clone()**（浅拷贝）**
它也是native方法，也有@HotSpotIntrinsicCandidate注解，那为啥速度比上面的大哥慢呢？这就要看到官方解释的一句：
It indicates that an annotated method may be (but is not guaranteed to be) intrinsified by the HotSpot VM
注意用词：may be (but is not guaranteed to be)，是的，clone()方法就是悲催的but，它并没有被手工写在JVM里面，所以它不得不走JNI的路子，所以它就成了2哥。
#### Arrays.copyof()**（浅拷贝）**
也有注解 @HotSpotIntrinsicCandidate，但它甚至不是native方法，所以这个注解也就是混子，也更印证了2哥的待遇，而且可以很明显的看到里面本质是调用了大哥  System.arraycopy()来实现的，所以效率垫底也是妥妥的。
#### for()
这个就可以退出群聊吧！for(）无论是基本数据类型还是引用数据类型统统是深复制，而且其也不是封装的函数，所以退出群聊妥妥的。
## ArrayMap
ArrayMap是一个<key,value>映射的数据结构，它设计上更多的是考虑内存的优化，内部是使用两个数组进行数据存储，一个数组记录key的hash值，另外一个数组记录Value值，它和SparseArray一样，也会对key使用二分法进行从小到大排序，在添加、删除、查找数据的时候都是先使用二分查找法得到相应的index，然后通过index来进行添加、查找、删除等操作，所以，应用场景和SparseArray的一样，如果在数据量比较大的情况下，那么它的性能将退化至少50%。

ArrayMap的内部主要依靠mHashes以及mArray，构造函数中可以看出和HashMap一样一上来创建时并不会分配内存空间。此处的mHashes就是存放HashCode的数组，mArray时存放key与Value的数组。

与 HashMap 不同的是，它是直接实现自接口 map。同样，存储 key-value 的方式也不同。ArrayMap 是通过数组直接存储了所有的 key-value。其中，mHashes  在 index 处存储了 key 的 hash code，而 mArray 则在 hash code 的 index<<1 处存储 key，在 index<<1 + 1 处存储 value。简单点说就是偶数处存储 key，相邻奇数处存储 value。
#### 二分查找优化
```
static int binarySearch(int[] array, int size, int value) {
    int lo = 0;
    int hi = size - 1;

    while(lo <= hi) {
    	  // 计算中间值，使用无符号右移，而非 /
        int mid = lo + hi >>> 1;
        int midVal = array[mid];
        if (midVal < value) {
            lo = mid + 1;
        } else {
            if (midVal <= value) {
                return mid;
            }

            hi = mid - 1;
        }
    }

    return ~lo;
}
```
**>>>和>>区别：**
> >>>表示不带符号向右移动二进制数，移动后前面统统补0；
> >>表示带符号移动，没有<<<这种运算符，因为左移都是补零，没有正负数的区别。
> 例如 -12 的二进制为：1111  1111  1111  1111  1111  1111  1111  0100；
> -12 >> 3 即带符号右移3位，结果是：1111  1111  1111  1111  1111  1111  1111  1110，十进制为： -2；
> -12 >>> 3 就是右移三位，前面补零，为：0001  1111  1111  1111  1111  1111  1111  1110，十进制为：536870910。

#### [ArrayMap#indexOf(key, hash)](https://www.jianshu.com/p/916f36ede575)
indexOf会根据mHash(存储所有key.hashCode()数组)、hash、mSize(mHash数组的长度)通过二分查找(折半查找)找到当前hash代表的key数据存储的位置。
![5828513-c265fc5cc9c5c4d4.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1582449123638-53b70631-e5f4-4c7b-9b7c-dba6227f404d.jpeg#align=left&display=inline&height=314&name=5828513-c265fc5cc9c5c4d4.jpg&originHeight=314&originWidth=784&size=56698&status=done&style=none&width=784)
上面图说， index == 0 时 和 index == 1时的 hash code 是一样的，说明 key1 与 key2 的 hash code 是一样的，也就是存在 hash 冲突了。那么，如上，这里的解决办法就是 hash code 存储了 2 份，而 key-value 分别存储一份。
#### [ArrayMap#put(K key, V value)](https://www.jianshu.com/p/916f36ede575)
```
public V put(K key, V value) {
    final int osize = mSize;
    // 1.计算 hash code 并获取 index
    final int hash;
    int index;
    if (key == null) {
        // 为空直接取 0
        hash = 0;
        index = indexOfNull();
    } else {
        // 否则取 Object.hashCode()
        hash = mIdentityHashCode ? System.identityHashCode(key) : key.hashCode();
        index = indexOf(key, hash);
    }
    // 2.如果 index 大于等于 0 ，说明之前存在相同的 hash code 且 key 也相同，则直接覆盖
    if (index >= 0) {
        index = (index<<1) + 1;
        final V old = (V)mArray[index];
        mArray[index] = value;
        return old;
    }
    // 3.如果没有找到则上面的 indexOf() 或者  indexOfNull() 就会返回一个负数，
	// 而这个负数就是由将要插入的位置 index 取反得到的，所以这里再次取反就变成了将进行插入的位置
    index = ~index;
    // 4.判断是否需要扩容
    if (osize >= mHashes.length) {
    		// 扩容：若osize>=8，则扩容一半，否则，若>=4则扩容为8否则为4
        final int n = osize >= (BASE_SIZE*2) ? (osize+(osize>>1))
                : (osize >= BASE_SIZE ? (BASE_SIZE*2) : BASE_SIZE);

        if (DEBUG) Log.d(TAG, "put: grow from " + mHashes.length + " to " + n);

        final int[] ohashes = mHashes;
        final Object[] oarray = mArray;
        // 5.申请新的空间
        allocArrays(n);

        if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
            throw new ConcurrentModificationException();
        }

        if (mHashes.length > 0) {
            if (DEBUG) Log.d(TAG, "put: copy 0-" + osize + " to 0");
            // 将数据复制到新的数组中
            System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
            System.arraycopy(oarray, 0, mArray, 0, oarray.length);
        }
        // 6.释放旧的数组
        freeArrays(ohashes, oarray, osize);
    }

    if (index < osize) {
        // 7.如果index在当前size之内，则需要将index开始的数据移到index + 1 处，以腾出index的位置
        if (DEBUG) Log.d(TAG, "put: move " + index + "-" + (osize-index)
                + " to " + (index+1));
        System.arraycopy(mHashes, index, mHashes, index + 1, osize - index);
        System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
    }

    if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
        if (osize != mSize || index >= mHashes.length) {
            throw new ConcurrentModificationException();
        }
    }
    // 8.然后根据计算得到的 index 分别插入 hash，key，以及 code
    mHashes[index] = hash;
    mArray[index<<1] = key;
    mArray[(index<<1)+1] = value;
    mSize++;
    return null;
}
```

- mHashs 数组以升序的方式保存了所有的 hash code。
- hashCode 必然可能存在冲突，这里是怎么解决的呢？这个是由上面的第 3 步和第 7 步所决定。第 3 步是得出应该插入的 index 的位置，而第 7 步则是如果 index < osize ，则说明原来 mArrays 中必然已经存在相同 hashCode 的值了，那么就把数据全部往后移一位，从而在 mHashs 中插入多个相同的 hash code 并且一定是连接在一起的，而在 mArrays 中插入新的 key 和 value，最终得以解决 hash 冲突。
- mHashs 数组以升序的方式保存了所有的 hash code，若插入一个数据，该数据的hash值恰好在中间某个位置，如何处理呢？这个问题实际上和上面hashCode 冲突是一样的答案，因为hashCode 冲突，若冲突点在中间某个位置，则需要将数据全部往后移一位。
#### [ArrayMap#removeAt(**int **index)](https://www.jianshu.com/p/916f36ede575)
```
public V removeAt(int index) {
    final Object old = mArray[(index << 1) + 1];
    final int osize = mSize;
    final int nsize;
    // 如果 size 小于等于1 ，移除后数组长度将为 0。
	// 为了压缩内存，这里直接将mHashs 以及 mArray 置为了空数组
    if (osize <= 1) {
        // Now empty.
        if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to 0");
        final int[] ohashes = mHashes;
        final Object[] oarray = mArray;
        mHashes = EmptyArray.INT;
        mArray = EmptyArray.OBJECT;
        freeArrays(ohashes, oarray, osize);
        nsize = 0;
    } else {
        // size > 1 的情况，则先将 size - 1
        nsize = osize - 1;
        if (mHashes.length > (BASE_SIZE*2) && mSize < mHashes.length/3) {
            // 如果上面的条件符合，那么就要进行数据的压缩。 
            // Shrunk enough to reduce size of arrays.  We don't allow it to
            // shrink smaller than (BASE_SIZE*2) to avoid flapping between
            // that and BASE_SIZE.
            final int n = osize > (BASE_SIZE*2) ? (osize + (osize>>1)) : (BASE_SIZE*2);

            if (DEBUG) Log.d(TAG, "remove: shrink from " + mHashes.length + " to " + n);

            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);

            if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
                throw new ConcurrentModificationException();
            }

            if (index > 0) {
                if (DEBUG) Log.d(TAG, "remove: copy from 0-" + index + " to 0");
                System.arraycopy(ohashes, 0, mHashes, 0, index);
                System.arraycopy(oarray, 0, mArray, 0, index << 1);
            }
            if (index < nsize) {
                if (DEBUG) Log.d(TAG, "remove: copy from " + (index+1) + "-" + nsize
                        + " to " + index);
                System.arraycopy(ohashes, index + 1, mHashes, index, nsize - index);
                System.arraycopy(oarray, (index + 1) << 1, mArray, index << 1,
                        (nsize - index) << 1);
            }
        } else {
            if (index < nsize) {
                // 如果 index 在 size 内，则将数据往前移一位
                if (DEBUG) Log.d(TAG, "remove: move " + (index+1) + "-" + nsize
                        + " to " + index);
                System.arraycopy(mHashes, index + 1, mHashes, index, nsize - index);
                System.arraycopy(mArray, (index + 1) << 1, mArray, index << 1,
                        (nsize - index) << 1);
            }
            // 然后将最后一位数据置 null
            mArray[nsize << 1] = null;
            mArray[(nsize << 1) + 1] = null;
        }
    }
    if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
        throw new ConcurrentModificationException();
    }
    mSize = nsize;
    return (V)old;
}
```
一般情况下删除一个数据，只需要将 index 后面的数据都往 index 方向移一位，然后删除末位数即可。而如果当前的数组中的条件达到  mHashs 的长度(mHashes.length)大于 BASE_SIZE * _2 (8) 且实际大小(_mSize_)又小于其长度(_mHashes.length_)的 1/3，那么就要进行数据的压缩。而压缩后的空间至少也是 BASE_SIZE * _2 的大小。
#### ArrayMap的内存优化
这个主要是两个方法，allocArrays(**int **size)和freeArrays(**int**[] hashes, Object[] array, **int **size)来控制的。
**关键点：**其实就是freeArrays方法中，传入的是oldhashes和oldarray。以及新的mSize。
这两个方法的作用基本上就是当长度不够用，我们需要废弃掉老的数组，使用新的数组的时候，把老的数组（包含mHashes和mArray）的数据添加到oldArray当中，然后把oldArray赋值给mBaseCache（4个长度），如果再有新的ArrayMap创建数组空间的时候，如果还是申请4个的空间，那么优先使用缓存下来的这个。
同理，mTwiceBaseCache是缓存8个长度的数组空间的。
也就是说，这些缓存空间是留给其它的ArrayMap的或者当前ArrayMap扩充到8位以及收缩到4位或者8位时。
```
@Nullable
static Object[] mBaseCache;
static int mBaseCacheSize;
@Nullable
static Object[] mTwiceBaseCache;
static int mTwiceBaseCacheSize;

// 释放数组
private static void freeArrays(int[] hashes, Object[] array, int size) {
    Class var3;
    int i;
    // 如果长度为8
    if (hashes.length == 8) {
        var3 = ArrayMap.class;
        synchronized(ArrayMap.class) {
            if (mTwiceBaseCacheSize < 10) {
                array[0] = mTwiceBaseCache;
                array[1] = hashes;

                for(i = (size << 1) - 1; i >= 2; --i) {
                		// 数组所有位置至null
                    array[i] = null;
                }
								// 保留8位缓存数组，
                // 当新的ArrayMap或者当 当前ArrayMap扩容至8、缩容至8时，
                // 可以直接使用该缓存数组，而非重新new int[]去重新申请内存
                mTwiceBaseCache = array;
                ++mTwiceBaseCacheSize;
            }
        }
    } else if (hashes.length == 4) { // 同理8位的缓存数组
        var3 = ArrayMap.class;
        synchronized(ArrayMap.class) {
            if (mBaseCacheSize < 10) {
                array[0] = mBaseCache;
                array[1] = hashes;

                for(i = (size << 1) - 1; i >= 2; --i) {
                    array[i] = null;
                }

                mBaseCache = array;
                ++mBaseCacheSize;
            }
        }
    }
}

// 申请数组
private void allocArrays(int size) {
    Class var2;
    Object[] array;
    // 若需要申请的数据为8位
    if (size == 8) {
        var2 = ArrayMap.class;
        synchronized(ArrayMap.class) {
        		// mTwiceBaseCache 8位的缓存数组不为null
            if (mTwiceBaseCache != null) {
            		// 则直接使用该缓存数组
                array = mTwiceBaseCache;
                this.mArray = array;
                mTwiceBaseCache = (Object[])((Object[])array[0]);
                this.mHashes = (int[])((int[])array[1]);
                array[0] = array[1] = null;
                --mTwiceBaseCacheSize;
                return;
            }
        }
    } else if (size == 4) { // 同理8位缓存数组
        var2 = ArrayMap.class;
        synchronized(ArrayMap.class) {
            if (mBaseCache != null) {
                array = mBaseCache;
                this.mArray = array;
                mBaseCache = (Object[])((Object[])array[0]);
                this.mHashes = (int[])((int[])array[1]);
                array[0] = array[1] = null;
                --mBaseCacheSize;
                return;
            }
        }
    }
		// 若缓存数组位null或者需要申请的长度不为4位以及8位，则通过new int[]申请数组
    this.mHashes = new int[size];
    this.mArray = new Object[size << 1];
}
```
**小结：**

- mBaseCache和mTwiceBaseCache分别为4位缓存数组和8位缓存数组，而且两者均为static静态数组。
- freeArrays释放数组的方法中，首先将数组值重置为null，后判断长度若为4或者8，则将该清空的数组保存在mBaseCache和mTwiceBaseCache中，以备后面使用
- allocArrays申请数组的方法中，首先判断是否需要的长度为4或者8，若是并且缓存数组bumBaseCache和mTwiceBaseCachebu不为null，此时直接使用已在freeArrays中已将数组里数据置为null的数组。
- 若allocArrays申请数组的方法中，申请的长度不为4和8，则通过new int[]方法申请数组。
- mBaseCache和mTwiceBaseCache，内存优化实际上类似于所谓的池化(线程池)。
#### ArrayMap应用场景

- 1. 数据量不大，最好在千级以内
- 2. 数据结构类型为Map类型
#### [ArrayMap与HashMap对比](https://blog.csdn.net/zuo_er_lyf/article/details/90598937)

1. 查找效率:
- HashMap因为其根据hashcode的值直接算出index，所以其查找效率是随着数组长度增大而增加的。
- ArrayMap使用的是二分法查先找到hashcode所保存的位置index，然后再通过左移index找到key和value的位置，所以当数组长度每增加一倍时,就需要多进行一次判断,效率下降。

所以对于Map数量比较大的情况下,推荐使用

2. 扩容数量:
  - HashMap初始值16个长度,每次扩容的时候，直接通过new申请双倍的数组空间。
  - ArrayMap初始值为0，每次扩容的时候,如果size长度大于8时申请size*1.5个长度，大于4小于8时申请8个,小于4时申请4个。这样比较ArrayMap其实是申请了更少的内存空间，但是扩容的频率会更高。
  - 因此，如果当数据量比较大的时候,还是使用HashMap更合适，因为其扩容的次数要比ArrayMap少很多。
3. 扩容效率:
- HashMap每次扩容的时候时重新计算每个数组成员的位置,然后放到新的位置。
- ArrayMap则是直接使用System.arraycopy。
- 所以效率上肯定是ArrayMap更占优势。System.arraycopy也是把老的数组的对象一个一个的赋给新的数组。当然效率上肯定arraycopy更高，因为是直接调用的c层的代码。
4. 内存耗费：
- ArrayMap采用了一种独特的方式，能够重复的利用因为数据扩容而遗留下来的数组空间，方便下一个ArrayMap的使用。
- 而HashMap没有这种设计。由于ArrayMap只缓存了长度是4和8的时候，所以如果频繁的使用到Map，而且数据量都比较小的时候,ArrayMap无疑是相当的节省内存的。
5. 总结:
- 综上所述,数据量比较小,并且需要频繁的使用Map存储数据的时候,推荐使用ArrayMap。而数据量比较大的时候,则推荐使用HashMap。
#### [总结](https://www.jianshu.com/p/1fb660978b14)

- 每次插入时，**根据key的哈希值**，利用**二分查找**，去寻找key在`int[] mHashes`数组中的下标位置。
- 如果出现了**hash冲突**，则从需要从目标点向两头遍历，找到正确的index。
- 扩容时，会查看之前是否有**缓存**的 int[]数组和object[]数组
- 如果有，**复用**给mArray mHashes
- 扩容规则：如果容量大于8，则**扩容一半**。（类似ArrayList）
- 根据`key`的`hash`值在`mHashs`中的`index`，如何得到`key、value`在`mArray`中的下标位置呢？`key`的位置是`index*2`，`value`的位置是`index*2+1`,也就是说`mArray`是利用**连续的两位空间去存放`key、value`。**
- 根据元素数量和集合占用的空间情况，判断**是否要执行收缩操作**
- 如果 mHashes长度大于8，且 集合长度 小于当前空间的 1/3,则执行一个 shrunk，收缩操作，避免空间的浪费
- **类似ArrayList**，用**复制操作**去**覆盖**元素达到**删除**的目的。
##### 优点：
`ArrayMap`的设计是为了更加高效地利用内存，高效体现在以下几点

- `ArrayMap`使用更少的存储单元存储元素

`ArrayMap`使用`int`类型的数组存储hash，使用`Object`类型数组存储k-v键值对，相较于`HashMap`使用`Node`存储节点，`ArrayMap`存储一个元素占用的内存更小。
- `ArrayMap`在扩容时容量变化更小

`HashMap`在扩容的时候，通常会将容量扩大一倍，而`ArrayMap`在扩容的时候，如果元素个数超过8，最多扩大自己的1/2。
##### 缺点：

- 存储大量（超过1000）元素时比较耗时
- 在对元素进行查找或者确定待插入元素的位置时使用二分查找，当元素较多时，耗时较长
- 频繁扩容和缩容，可能会产生大量复制操作
- `ArrayMap`在扩容和缩容时需要移动元素，且扩容时容量变化比`HashMap`小，扩容和缩容的频率可能更高，元素数量过多时，元素的移动可能会对性能产生影响。
## SparseArray

```
private static final Object DELETED = new Object(); // DELETED是一个标志字段，用于判断是否删除
private boolean mGarbage = false; // 用于确定当前是否需要垃圾回收

private int[] mKeys; // mKeys数组用于存储key
private Object[] mValues; // mValues数组用于存储值
private int mSize; // 表示当前SparseArray有几个元素
```
### SparseArray延迟删除机制
```
public void removeAt(int index) {
   	if (mValues[index] != DELETED) {
    		// 将移除位置的值设置为DELETED，即标记一下
      	mValues[index] = DELETED;
        // 设置可垃圾回收的标志
				mGarbage = true;
		}
}

public void remove(int key) {
		delete(key);
}

public void delete(int key) {
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i >= 0) {
        if (mValues[i] != DELETED) {
        		// 将移除位置的值设置为DELETED，即标记一下
            mValues[i] = DELETED;
            // 设置可垃圾回收的标志
            mGarbage = true;
        }
    }
}


```
在移除数据时SparseArray并不是立马去移除，而是先标记，后通过gc()方法一次性回收：
                ![image.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1582456262718-58de75be-5d0e-4800-9d29-abc1dbead76a.png#align=left&display=inline&height=293&name=image.png&originHeight=585&originWidth=1090&size=78368&status=done&style=none&width=545)
通过上图可以看到，调用gc()方法的地方很多，这些方法的触发均可去回收无用数据，以及在回收的同时重置数据的位置。
### SparseArray扩容机制-GrowingArrayUtils#insert

```
public void put(int key, E value) {
		// 二分查找key位置，类似于ArrayMap查找key的hashCode
    int i = ContainerHelpers.binarySearch(mKeys, mSize, key);

    if (i >= 0) { // 找到后key的位置就是value在mValues数组中的位置
        mValues[i] = value;
    } else {
    		// 非操作，由于mKeys数组是升序排序的，因此二分查找时若没有找到，
        // 会根据key值返回一个该key应该插入在mKeys数组中的位置，不过是一个负值用来代表没有查到数据
        i = ~i;
				// 元素要添加的位置正好==DELETED，直接覆盖它的值即可。
        if (i < mSize && mValues[i] == DELETED) {
            mKeys[i] = key;
            mValues[i] = value;
            return;
        }

        if (mGarbage && mSize >= mKeys.length) {
        		// 延时回收
            gc();

            // 重新计算插入的位置
            i = ~ContainerHelpers.binarySearch(mKeys, mSize, key);
        }
				// 在指定位置i处，插入元素
        mKeys = GrowingArrayUtils.insert(mKeys, mSize, i, key);
        mValues = GrowingArrayUtils.insert(mValues, mSize, i, value);
        mSize++;
    }
}

// 前面二分查找帮我们找到了最佳位置，我们现在需要在这个最佳位置插入数据，
// 若这个最佳位置在数组中间，则把当前位置后面的所有数据后移一位再放入我们的数据
public static int[] insert(int[] array, int currentSize, int index, int element) {
    assert currentSize <= array.length;

    if (currentSize + 1 <= array.length) { // 如果数组长度能够容下直接在原数组上插入
    	// 调用了Java 的native方法，把array 从index开始的数复制到index+1上，
        System.arraycopy(array, index, array, index + 1, currentSize - index);
        // 空出来的那个位置直接放入我们要存入的值，也不是空出来，其实index上还是有数的，
        // 比如：{2,3,4,5,0,0}从index=1开始复制，复制长度为5，复制后的结果就是{2,3,3,4,5,0}了
        array[index] = element;
        return array;
    }

		// 这就是扩容了，新建了一个数组，长度*2
    int[] newArray = new int[growSize(currentSize)]; 
    
    // 新旧数组拷贝，先拷贝最佳位置之前的到新数组
    System.arraycopy(array, 0, newArray, 0, index);
    newArray[index] = element; // 直接在新数组上赋值
    // 然后拷贝旧数组最佳位置index起的所有数到新数组里面，只是做了分段拷贝而已
    System.arraycopy(array, index, newArray, index + 1, array.length - index);
    
    return newArray;
}
```
SparseArray的扩容实际上类似于ArrayMap，不过SparseArray没有缓存数组以及缩容机制。不过由于SparseArray插入数据时二分查找是根据mSize长度，即数组里有多少数据来计算插入位置，不关心结尾有多少null的位置，而且经常gc()回收无用数据，因此仅需要扩容机制实际上若数据量不大的情况下已经够用啦。

#### SparseArray 优点：

- 通过它的三兄弟可以避免存取元素时的装箱和拆箱
- 频繁的插入删除操作效率高（延迟删除机制保证了效率）
- 会定期通过gc函数来清理内存，内存利用率高
- 放弃hash查找，使用二分查找，更轻量
#### SparseArray缺点

- 二分查找的时间复杂度O(log n)，大数据量的情况下，效率没有HashMap高
- key只能是int 或者long
#### SparseArray应用场景：

- item数量为 <1000级别的
- 存取的value为指定类型的，比如boolean、int、long，可以避免自动装箱和拆箱问题。

## 总结-HashMap，ArrayMap，SparseArray

1. 查找：对一个数据或者是几个数据的查询.二者的差异还是非常小的.当数据量是100000条。查100000条的效率还是Map要快一点。数据量为10000的时候.这就差异性就更小.但是Map的查找的效率确实还是赢了一筹。
1. 正序插入：数据量小的时候，差异并不大（当然了，数据量小，时间基准小，确实差异不大），当数据量大于5000左右，SparseArray，最快，HashMap最慢，乍一看，好像SparseArray是最快的，但是要注意，这是顺序插入的。也就是SparseArray和Arraymap最理想的情况。
1. 倒叙插入：SparseArray与HashMap无论是怎样进行插入,数据量相同时，前者都要比后者要省下一部分内存，但是效率呢？在倒序插入的时候，数据量大的时候HashMap远超Arraymap和SparseArray。当然了，数据量小的时候，例如1000以下，这点时间差异也是可以忽略的。由于SparseArray每次在插入的时候都要使用二分查找判断是否有相同的值被插入。因此这种倒序的情况是SparseArray效率最差的时候。
1. 内存占用：SparseArray在内存占用方面的确要优于HashMap和ArrayMap不少，通过数据观察，大致节省30%左右，而ArrayMap的表现正如前面说的，优化作用有限，几乎和HashMap相同。
1. 在数据量小的时候一般认为1000以下，当你的key为int的时候，使用SparseArray确实是一个很不错的选择，内存大概能节省30%，相比用HashMap，因为它key值不需要装箱，所以时间性能平均来看也优于HashMap,建议使用！
1. ArrayMap相对于SparseArray，特点就是key值类型不受限，任何情况下都可以取代HashMap,但是通过研究和测试发现，ArrayMap的内存节省并不明显，也就在10%左右，但是时间性能确是最差的，当然了，1000以内的如果key不是int 可以选择ArrayMap。
## 
