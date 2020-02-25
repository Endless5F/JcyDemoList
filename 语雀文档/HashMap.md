# HashMap

## 基础
### 数据结构
#### 时间复杂度
在[计算机科学](https://baike.baidu.com/item/%E8%AE%A1%E7%AE%97%E6%9C%BA%E7%A7%91%E5%AD%A6)中，**时间复杂性**，又称**时间复杂度**，[算法](https://baike.baidu.com/item/%E7%AE%97%E6%B3%95)的**时间复杂度**是一个[函数](https://baike.baidu.com/item/%E5%87%BD%E6%95%B0)，它定性描述该算法的运行时间。这是一个代表算法输入值的[字符串](https://baike.baidu.com/item/%E5%AD%97%E7%AC%A6%E4%B8%B2)的长度的函数。时间复杂度常用[大O符号](https://baike.baidu.com/item/%E5%A4%A7O%E7%AC%A6%E5%8F%B7)表述，不包括这个函数的低阶项和首项系数。使用这种方式时，时间复杂度可被称为是[渐近](https://baike.baidu.com/item/%E6%B8%90%E8%BF%91)的，亦即考察输入值大小趋近无穷时的情况。

　　相同大小的不同输入值仍可能造成算法的运行时间不同，因此我们通常使用算法的最坏情况复杂度，记为T(n)，定义为任何大小的输入n所需的最大运行时间。另一种较少使用的方法是平均情况复杂度，通常有特别指定才会使用。时间复杂度可以用函数T(n) 的自然特性加以分类，举例来说，有着T(n) =O(n) 的算法被称作“线性时间算法”；而T(n) =O(M^n) 和M= O(T(n)) ，其中M≥n> 1 的算法被称作“指数时间算法”。
　　一个算法花费的时间与算法中语句的执行次数成正比例，哪个算法中语句执行次数多，它花费时间就多。一个算法中的语句执行次数称为语句频度或时间频度。记为T(n)。
　　一般情况下，算法中基本操作重复执行的次数是问题规模n的某个函数，用T(n)表示，若有某个辅助函数f(n),使得当n趋近于无穷大时，T（n)/f (n)的极限值为不等于零的常数，则称f(n)是T(n)的同数量级函数。记作T(n)=O(f(n)),称O(f(n)) 为算法的渐进时间复杂度，简称时间复杂度。
在各种不同算法中，若算法中语句执行次数为一个常数，则时间复杂度为O(1),另外，在时间频度不相同时，时间复杂度有可能相同，如T(n)=n2+3n+4与T(n)=4n2+2n+1它们的频度不同，但时间复杂度相同，都为O(n2)。

算法的时间复杂度是衡量一个算法好坏的重要指标。一般情况下，随着规模n的增大，次数T(n)的增长较慢的算法为最优算法。常见时间复杂度从小到大依次排列：O(1) < O(log2n) < O(n) < O(n²）<O(n³) ····<O(n!)
```
例如：

(a) 1;      // 时间复杂度为O(1)

(b)for(i =1 ; i<=n ;i++){  x= x+1;}    // 时间复杂度为O(n)，称为线性阶

(c)for(i =1 ; i<=n ; i++）{for(j=1;j<=n;j++){  x=x+1 } }  // 时间复杂度为O(n²),称为平方阶
```
#### 空间复杂度
空间复杂度(Space Complexity)是对一个算法在运行过程中临时占用存储空间大小的量度，记做S(n)=O(f(n))。比如直接[插入排序](https://baike.baidu.com/item/%E6%8F%92%E5%85%A5%E6%8E%92%E5%BA%8F)的[时间复杂度](https://baike.baidu.com/item/%E6%97%B6%E9%97%B4%E5%A4%8D%E6%9D%82%E5%BA%A6/1894057)是O(n^2),空间复杂度是O(1) 。而一般的[递归](https://baike.baidu.com/item/%E9%80%92%E5%BD%92)算法就要有O(n)的空间复杂度了，因为每次递归都要存储返回信息。一个算法的优劣主要从算法的执行时间和所需要占用的存储空间两个方面[衡量](https://baike.baidu.com/item/%E8%A1%A1%E9%87%8F/483075)。
### 哈希
**Hash，一般翻译做“散列”，也有直接音译为“哈希”的，就是把任意长度的输入，通过散列算法，变换成固定长度的输出，该输出就是散列值。**这种转换是一种压缩映射，也就是，散列值的空间通常远小于输入的空间，不同的输入可能会散列成相同的输出，所以不可能从散列值来唯一的确定输入值。简单的说就是一种将任意长度的消息压缩到某一固定长度的消息摘要的函数。
所有散列函数都有如下一个基本特性：**根据同一散列函数计算出的散列值如果不同，那么输入值肯定也不同。但是，根据同一散列函数计算出的散列值如果相同，输入值不一定相同。**
**两个不同的输入值，根据同一散列函数计算出的散列值相同的现象叫做碰撞。**
常见的Hash函数有以下几个：

- 直接定址法：直接以关键字k或者k加上某个常数（k+c）作为哈希地址。
- 数字分析法：提取关键字中取值比较均匀的数字作为哈希地址。
- 除留余数法：用关键字k除以某个不大于哈希表长度m的数p，将所得余数作为哈希表地址。
- 分段叠加法：按照哈希表地址位数将关键字分成位数相等的几部分，其中最后一部分可以比较短。然后将这几部分相加，舍弃最高进位后的结果就是该关键字的哈希地址。
- 平方取中法：如果关键字各个部分分布都不均匀的话，可以先求出它的平方值，然后按照需求取中间的几位作为哈希地址。
- 伪随机数法：采用一个伪随机数当作哈希函数。

上面介绍过碰撞。衡量一个哈希函数的好坏的重要指标就是发生碰撞的概率以及发生碰撞的解决方案。任何哈希函数基本都无法彻底避免碰撞，常见的解决碰撞的方法有以下几种：

- 开放定址法：
  - 开放定址法就是一旦发生了冲突，就去寻找下一个空的散列地址，只要散列表足够大，空的散列地址总能找到，并将记录存入。
- 链地址法
  - 将哈希表的每个单元作为链表的头结点，所有哈希地址为i的元素构成一个同义词链表。即发生冲突时就把该关键字链在以该单元为头结点的链表的尾部。
- 再哈希法
  - 当哈希地址发生冲突用其他的函数计算另一个哈希函数地址，直到冲突不再产生为止。
- 建立公共溢出区
  - 将哈希表分为基本表和溢出表两部分，发生冲突的元素都放入溢出表中。
### HashMap 的数据结构
在Java1.7中，保存数据有两种比较简单的数据结构：数组和链表。**数组的特点是：寻址容易，插入和删除困难；而链表的特点是：寻址困难，插入和删除容易。**上面我们提到过，常用的哈希函数的冲突解决办法中有一种方法叫做链地址法，其实就是将数组和链表组合在一起，发挥了两者的优势，我们可以将其理解为链表的数组。
                     ![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581867639520-b842955e-b840-4235-a798-13857a2213dd.png#align=left&display=inline&height=456&originHeight=456&originWidth=541&size=0&status=done&style=none&width=541)

JDK1.8中，HashMap采用数组+链表+红黑树实现，当链表长度超过阈值（8）时，将链表转换为红黑树，这样大大减少了查找时间。在JDK1.8之前，HashMap采用数组+链表实现，即使用链表处理冲突，同一hash值的节点都存储在一个链表里。但是当位于一个桶中的元素较多，即hash值相等的元素较多时，通过key值依次查找的效率较低。

                                     ![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581867749956-29f87787-a408-49e8-885f-14af7d8eab0c.png#align=left&display=inline&height=424&originHeight=424&originWidth=369&size=0&status=done&style=none&width=369)
### [**什么是红黑树**](https://www.yuque.com/wuwei-zlysl/pbgoky/bde32a)
红黑树就是一种平衡的二叉查找树，说他平衡的意思是他不会变成“瘸子”，左腿特别长或者右腿特别长。除了符合二叉查找树的特性之外，还具体下列的特性：

1. 节点是红色或者黑色
1. 根节点是黑色
1. 每个叶子的节点都是黑色的空节点（NULL）
1. 每个红色节点的两个子节点都是黑色的。
1. 从任意节点到其每个叶子的所有路径都包含相同的黑色节点。

![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581868012866-c55c7829-5127-40da-a66e-33f155049434.jpeg#align=left&display=inline&height=363&originHeight=363&originWidth=640&size=0&status=done&style=none&width=640)
平衡二叉树要求对于每一个节点来说，它的左右子树的高度之差不能超过1，如果插入或者删除一个节点使得高度之差大于1，就要进行节点之间的旋转，将二叉树重新维持在一个平衡状态。这个方案很好的解决了二叉查找树退化成链表的问题，把插入，查找，删除的时间复杂度最好情况和最坏情况都维持在O(logN)。但是频繁旋转会使插入和删除牺牲掉O(logN)左右的时间，不过相对二叉查找树来说，时间上稳定了很多。
#### 平衡因子
平衡因子 = 左子树深度 - 右子树深度
#### **生成平衡二叉树**
转载自：[**一文读懂平衡二叉树｜技术头条**](https://baijiahao.baidu.com/s?id=1646617486319372351&wfr=spider&for=pc)
先按照生成二叉搜索树的方法构造二叉树，直至二叉树变得不平衡，即出现这样的节点：左子树与右子树的高度差大于1。至于如何调整，要看插入的导致二叉树不平衡的节点的位置。主要有四种调整方式：LL（左旋）、RR（右旋）、LR（先左旋再右旋）、RL（先右旋再左旋）。

1. 所谓LL（左旋）就是向左旋转一次，下图所示为最简洁的左旋（插入3导致值为1的节点不平衡）：

        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581925351441-8819a5f0-f817-4b90-a068-d5601a515aaa.jpeg#align=left&display=inline&height=275&originHeight=285&originWidth=621&size=0&status=done&style=none&width=600)
复杂的LL（左旋，插入13导致值为4的节点不平衡）：
        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581928699991-3f88724e-58a8-4a7c-91ce-95814ec988a3.jpeg#align=left&display=inline&height=204&originHeight=218&originWidth=640&size=0&status=done&style=none&width=600)
红色节点为插入后不平衡的节点，黄色部分为需要改变父节点的分支，左旋后，原红色节点的右孩子节点变成了根节点，红色节点变成了它的左孩子，而它原本的左孩子（黄色部分）不能丢，而此时红色节点的右孩子是空的，于是就把黄色部分放到了红色节点的右孩子的位置上。调整后该二叉树还是一棵二叉排序（搜索）树，因为黄色部分的值大于原来的根节点的值，而小于后来的根节点的值，调整后，黄色部分还是位于原来的根节点（红色节点）和后来的根节点之间。

2. 所谓RR（右旋）就是向右旋转一次，下图所示为最简洁的右旋（插入1导致值为3的节点不平衡）：

        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581925378165-d37461e9-8994-4ded-8e24-71bc58194410.jpeg#align=left&display=inline&height=273&originHeight=282&originWidth=620&size=0&status=done&style=none&width=600)
复杂的RR（右旋，插入1导致值为9的节点不平衡）：
        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581928756001-60ee43a0-5c2d-4d51-9084-0ab51eb86414.jpeg#align=left&display=inline&height=200&originHeight=213&originWidth=640&size=0&status=done&style=none&width=600)
红色节点为插入后不平衡的节点，黄色部分为需要改变父节点的分支，右旋后，原红色节点的左孩子节点变成了根节点，红色节点变成了它的右孩子，而它原本的右孩子（黄色部分）不能丢，而此时红色节点的左孩子是空的，于是就把黄色部分放到了红色节点的左孩子的位置上。调整后该二叉树还是一棵二叉排序（搜索）树，因为黄色部分的值小于原来的根节点的值，而大于后来的根节点的值，调整后，黄色部分还是位于后来的根节点和原来的根节点（红色节点）之间。

3. 所谓LR（先左旋再右旋）就是先将左子树左旋，再整体右旋，下图为最简洁的LR旋转（插入2导致值为3的节点不平衡）：

        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581925401596-f8a53ddc-4991-438d-82b4-6b963b8e1372.jpeg#align=left&display=inline&height=241&originHeight=257&originWidth=640&size=0&status=done&style=none&width=600)
复杂的LR旋转（插入8导致值为9的节点不平衡）：
        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581928788995-d810e8b6-d0fb-475b-8750-da1a203fe903.jpeg#align=left&display=inline&height=371&originHeight=396&originWidth=640&size=0&status=done&style=none&width=600)
先将红色节点的左子树左旋，红色节点的左子树的根原本是值为4的节点，左旋后变为值为6的节点，原来的根节点变成了左旋后根节点的左孩子，左旋后根节点原本的左孩子（蓝色节点）变成了原来的根节点的右孩子；再整体右旋，原来的根节点（红色节点）变成了右旋后的根节点的右孩子，右旋后的根节点原本的右孩子（黄色节点）变成了原来的根节点（红色节点）的左孩子。旋转完成后，仍然是一棵二叉排序（搜索）树。
**注：插入节点后若失衡，最小(失衡一侧的)右子树长，则先左转后右转。**

4. 所谓RL（先右旋再左旋）就是先将右子树右旋，再整体左旋，下图为最简洁的RL旋转（插入2导致值为1的节点不平衡）：

        ![](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581925423271-c04bebc5-0862-4cd5-8712-3cbf0a6aeab1.jpeg#align=left&display=inline&height=227&originHeight=242&originWidth=640&size=0&status=done&style=none&width=600)
复杂的RL旋转（插入F导致值为A的节点不平衡）：
           ![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581930313576-1c6469bb-1fd2-4385-ac26-44fae53dcbdf.png#align=left&display=inline&height=240&originHeight=240&originWidth=560&size=0&status=done&style=none&width=560)
先将A节点的右子树右旋，A节点的右子树的根原本是值为C的节点，右旋后变为值为D的节点；再整体左旋，原来的A根节点变成了左旋后的根节点D的左孩子。旋转完成后，仍然是一棵二叉排序（搜索）树。
**注：插入节点后若失衡，最小(失衡一侧的)左子树长，则先右转后左转。**
#### [平衡二叉树删除](https://www.jianshu.com/p/2a8f2b3511fd)

- **被删的结点是叶子结点**，则直接删除，若二叉树不平衡，则通过旋转调整平衡
  - ① 将该结点直接从树中删除；
  - ② 其父节点的子树高度的变化将导致父结点平衡因子的变化，通过向上检索并推算其父结点是否失衡；
  - ③ 如果其父结点未失衡，则继续向上检索推算其父结点的父结点是否失衡...如此反复②的判断，直到根结点；如果向上推算过程中发现了失衡的现象，则进行④的处理；
  - ④ 如果其父结点失衡，则判断是哪种失衡类型[LL、LR、RR、RL]，并对其进行相应的平衡化处理。如果平衡化处理结束后，发现与原来以父节点为根结点的树的高度发生变化，则继续进行②的检索推算；如果与原来以父结点为根结点的高度一致时，则可说明父结点的父结点及祖先结点的平衡因子将不会有变化，因此可以退出处理。
  - 注：在左子树上删除节点其实就相当于在右子树上插入节点。
- **被删的结点只有左子树或只有右子树**
  - ① 将左子树（右子树）替代原有删除结点的位置；
  - ② 结点C被删除后，则以C的父结点B为起始推算点，依此向上检索推算各结点（父、祖先）是否失衡；
  - ③ 如果其父结点未失衡，则继续向上检索推算其父结点的父结点是否失衡...如此反复②的判断，直到根结点；如果向上推算过程中发现了失衡的现象，则进行④的处理；
  - ④ 如果其父结点失衡，则判断是哪种失衡类型[LL、LR、RR、RL]，并对其进行相应的平衡化处理。如果平衡化处理结束后，发现与原来以父节点为根结点的树的高度发生变化，则继续进行②的检索推算；如果与原来以父结点为根结点的高度一致时，则可说明父结点的父结点及祖先结点的平衡因子将不会有变化，因此可以退出处理。
- **被删的结点既有左子树又有右子树**
  - ①  如果该节点的平衡因子为0或者1，则找到其左子树中具有最大值的节点max（**我们只讨论有序平衡二叉树，并且有序平衡二叉树中任意一个节点的左子树上的所有节点的值小于该节点的值，右子树上所有节点的值大于该节点的值**），将max的内容与x的内容交换（**只替换保存的真正的数据，不替换指针，平衡因子等用于管理目的的信息**），并且max即为新的要删除的节点。由于树是有序的，因而这样找到的节点要么是一个叶子节点，要么是一个没有右子树的节点。
  - ② 如果该节点的平衡因子为-1，则找到其右节点中具有最小值的节点min，将min的内容与x的内容交换，并且min即为新的要删除的节点。由于树是有序的，因而这样找到的节点要么是一个叶子节点，要么是一个没有左子树的节点。
## HashMap
### [hash方法](https://blog.csdn.net/cs729298/article/details/80403220)
#### hash方法的功能
hash方法的功能是根据Key来定位这个K-V在链表数组中的位置的。也就是hash方法的输入应该是个Object类型的Key，输出应该是个int类型的数组下标。
**基本原理：**只要调用Object对象的hashCode()方法，该方法会返回一个整数，然后用这个数对HashMap或者HashTable的容量进行取模就行了。只不过，在具体实现上，由两个方法`int hash(Object k)`和`int indexFor(int h, int length)`来实现。
> hash ：该方法主要是将Object转换成一个整型。
> indexFor ：该方法主要是将hash生成的整型转换成链表数组中的下标。

#### JDK 1.7中hash方法
JDK 1.7中，当我们要对一个链表数组中的某个元素进行增删的时候，首先要知道他应该保存在这个链表数组中的哪个位置，即他在这个数组中的下标。而hash()方法的功能就是根据Key来定位其在HashMap中的位置。HashTable、ConcurrentHashMap同理。
```
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }
 
    h ^= k.hashCode();
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
 
static int indexFor(int h, int length) {
    return h & (length-1);
}
```
前面我说过，`indexFor`方法其实主要是将hash生成的整型转换成链表数组中的下标。那么`return h & (length-1);`是什么意思呢？其实，他就是取模。Java之所有使用位运算(&)来代替取模运算(%)，最主要的考虑就是效率。**位运算(&)效率要比代替取模运算(%)高很多，主要原因是位运算直接对内存数据进行操作，不需要转成十进制，因此处理速度非常快。**
为什么可以使用位运算(&)来实现取模运算(%)呢？这实现的原理如下：
```
X % 2^n = X & (2^n – 1)
2^n表示2的n次方，也就是说，一个数对2^n取模 == 一个数和(2^n – 1)做按位与运算 。
假设n为3，则2^3 = 8，表示成2进制就是1000。2^3 -1 = 7 ，即0111。
此时X & (2^3 – 1) 就相当于取X的2进制的最后三位数。
从2进制角度来看，X / 8相当于 X >> 3，即把X右移3位，此时得到了X / 8的商，而被移掉的部分(后三位)，则是X % 8，也就是余数。
```
##### 小结：
HashMap的数据是存储在链表数组里面的。在对HashMap进行插入/删除等操作时，都需要根据K-V对的键值定位到他应该保存在数组的哪个下标中。而这个通过键值求取下标的操作就叫做哈希。HashMap的数组是有长度的，Java中规定这个长度只能是2的倍数，初始值为16。简单的做法是先求取出键值的hashcode，然后在将hashcode得到的int值对数组长度进行取模。为了考虑性能，Java总采用按位与操作实现取模操作。
接下来我们会发现，无论是用取模运算还是位运算都无法直接解决冲突较大的问题。比如：`CA11 0000`和`0001 0000`在对`0000 1111`进行按位与运算后的值是相等的。[![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581931950092-cdb62c69-72cd-4be8-948c-511d6f1e7db1.png#align=left&display=inline&height=148&originHeight=148&originWidth=422&size=0&status=done&style=none&width=422)](http://www.hollischuang.com/wp-content/uploads/2018/03/640-2.png)
两个不同的键值，在对数组长度进行按位与运算后得到的结果相同，这不就发生了冲突吗。那么如何解决这种冲突呢，来看下Java是如何做的。其中的主要代码部分如下：
```
1. h ^= k.hashCode();
2. h ^= (h >>> 20) ^ (h >>> 12);
3. return h ^ (h >>> 7) ^ (h >>> 4);
```
这段代码是为了对key的hashCode进行扰动计算，防止不同hashCode的高位不同但低位相同导致的hash冲突。简单点说，就是为了把高位的特征和低位的特征组合起来，降低哈希冲突的概率，也就是说，尽量做到任何一位的变化都能对最终得到的结果产生影响。
举个例子来说：我们现在想向一个HashMap中put一个K-V对，Key的值为“hollischuang”，经过简单的获取hashcode后，得到的值为“1011000110101110011111010011011”，如果当前HashMap的大小为16，即在不进行扰动计算的情况下，他最终得到的index结果值为11。由于15的二进制扩展到32位为“00000000000000000000000000001111”，所以，一个数字在和他进行按位与操作的时候，前28位无论是什么，计算结果都一样（因为0和任何数做与，结果都为0）。
> 其实，使用位运算代替取模运算，除了性能之外，还有一个好处就是可以很好的解决负数的问题。因为我们知道，hashcode的结果是int类型，而int的取值范围是-2^31 ~ 2^31 – 1，即[ -2147483648, 2147483647]；这里面是包含负数的，我们知道，对于一个负数取模还是有些麻烦的。如果使用二进制的位运算的话就可以很好的避免这个问题。首先，不管hashcode的值是正数还是负数。length-1这个值一定是个正数。那么，他的二进制的第一位一定是0（有符号数用最高位作为符号位，“0”代表“+”，“1”代表“-”），这样里两个数做按位与运算之后，第一位一定是个0，也就是，得到的结果一定是个正数。

#### HashTable In Java 7
```
1. private int hash(Object k) {
2. // hashSeed will be zero if alternative hashing is disabled.
3. return hashSeed ^ k.hashCode();
4. }
```
我们可以发现，很简单，相当于只是对k做了个简单的hash，取了一下其hashCode。而HashTable中也没有`indexOf`方法，取而代之的是这段代码：`int index = (hash & 0x7FFFFFFF) % tab.length;`。也就是说，HashMap和HashTable对于计算数组下标这件事，采用了两种方法。HashMap采用的是位运算，而HashTable采用的是直接取模。
> 为啥要把hash值和0x7FFFFFFF做一次按位与操作呢，主要是为了保证得到的index的第一位为0，也就是为了得到一个正数。因为有符号数第一位0代表正数，1代表负数。

我们前面说过，HashMap之所以不用取模的原因是为了提高效率。有人认为，因为HashTable是个线程安全的类，本来就慢，所以Java并没有考虑效率问题，就直接使用取模算法了呢？但是其实并不完全是，Java这样设计还是有一定的考虑在的，虽然这样效率确实是会比HashMap慢一些。
其实，HashTable采用简单的取模是有一定的考虑在的。这就要涉及到HashTable的构造函数和扩容函数了。由于篇幅有限，这里就不贴代码了，直接给出结论：
> HashTable默认的初始大小为11，之后每次扩充为原来的2n+1。
> 也就是说，HashTable的链表数组的默认大小是一个素数、奇数。之后的每次扩充结果也都是奇数。
> 由于HashTable会尽量使用素数、奇数作为容量的大小。当哈希表的大小为素数时，简单的取模哈希的结果会更加均匀。（这个是可以证明出来的，由于不是本文重点，暂不详细介绍，可参考：http://zhaox.github.io/algorithm/2015/06/29/hash）

至此，我们看完了Java 7中HashMap和HashTable中对于hash的实现，我们来做个简单的总结。

- HashMap默认的初始化大小为16，之后每次扩充为原来的2倍。
- HashTable默认的初始大小为11，之后每次扩充为原来的2n+1。
- 当哈希表的大小为素数时，简单的取模哈希的结果会更加均匀，所以单从这一点上看，HashTable的哈希表大小选择，似乎更高明些。因为hash结果越分散效果越好。
- 在取模计算时，如果模数是2的幂，那么我们可以直接使用位运算来得到结果，效率要大大高于做除法。所以从hash计算的效率上，又是HashMap更胜一筹。
- 但是，HashMap为了提高效率使用位运算代替哈希，这又引入了哈希分布不均匀的问题，所以HashMap为解决这问题，又对hash算法做了一些改进，进行了扰动计算。
#### ConcurrentHashMap In Java 7
```
1. private int hash(Object k) {
2. int h = hashSeed;
3. 
4. if ((0 != h) && (k instanceof String)) {
5. return sun.misc.Hashing.stringHash32((String) k);
6. }
7. 
8. h ^= k.hashCode();
9. 
10. // Spread bits to regularize both segment and index locations,
11. // using variant of single-word Wang/Jenkins hash.
12. h += (h <<  15) ^ 0xffffcd7d;
13. h ^= (h >>> 10);
14. h += (h <<   3);
15. h ^= (h >>>  6);
16. h += (h <<   2) + (h << 14);
17. return h ^ (h >>> 16);
18. }
19. 
20. int j = (hash >>> segmentShift) & segmentMask;
```
上面这段关于ConcurrentHashMap的hash实现其实和HashMap如出一辙。都是通过位运算代替取模，然后再对hashcode进行扰动。区别在于，ConcurrentHashMap 使用了一种变种的Wang/Jenkins 哈希算法，其主要母的也是为了把高位和低位组合在一起，避免发生冲突。至于为啥不和HashMap采用同样的算法进行扰动，我猜这只是程序员自由意志的选择吧。至少我目前没有办法证明哪个更优。
#### JDK 1.8中hash方法
在Java 8 之前，HashMap和其他基于map的类都是通过链地址法解决冲突，它们使用单向链表来存储相同索引值的元素。在最坏的情况下，这种方式会将HashMap的get方法的性能从`O(1)`降低到`O(n)`。为了解决在频繁冲突时hashmap性能降低的问题，Java 8中使用平衡树来替代链表存储冲突的元素。这意味着我们可以将最坏情况下的性能从`O(n)`提高到`O(logn)`。关于HashMap在Java 8中的优化，我后面会有文章继续深入介绍。
如果恶意程序知道我们用的是Hash算法，则在纯链表情况下，它能够发送大量请求导致哈希碰撞，然后不停访问这些key导致HashMap忙于进行线性查找，最终陷入瘫痪，即形成了拒绝服务攻击（DoS）。
关于Java 8中的hash函数，原理和Java 7中基本类似。Java 8中这一步做了优化，只做一次16位右位移异或混合，而不是四次，但原理是不变的。
```
1. static final int hash(Object key) {
2. int h;
3. return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
4. }
```
在JDK1.8的实现中，优化了高位运算的算法，通过hashCode()的高16位异或低16位实现的：(h = k.hashCode()) ^ (h >>> 16)，主要是从速度、功效、质量来考虑的。以上方法得到的int的hash值，然后再通过`h & (table.length -1)`来得到该对象在数据中保存的位置。
HashTable In Java 8
在Java 8的HashTable中，已经不在有hash方法了。但是哈希的操作还是在的，比如在put方法中就有如下实现：
```
1. int hash = key.hashCode();
2. int index = (hash & 0x7FFFFFFF) % tab.length;
```
这其实和Java 7中的实现几乎无差别，就不做过多的介绍了。
#### ConcurrentHashMap In Java 8
Java 8 里面的求hash的方法从hash改为了spread。实现方式如下：
```
1. static final int spread(int h) {
2. return (h ^ (h >>> 16)) & HASH_BITS;
3. }
```
Java 8的ConcurrentHashMap同样是通过Key的哈希值与数组长度取模确定该Key在数组中的索引。同样为了避免不太好的Key的hashCode设计，它通过如下方法计算得到Key的最终哈希值。不同的是，Java 8的ConcurrentHashMap作者认为引入红黑树后，即使哈希冲突比较严重，寻址效率也足够高，所以作者并未在哈希值的计算上做过多设计，只是将Key的hashCode值与其高16位作异或并保证最高位为0（从而保证最终结果为正整数）。

**CHM的并发优化历程：
**
```
概念引入：ConcurrentHashMap类 定位Segment
   注：" 1. m << n，结果为m * 2^n；2. m & (2^n - 1)，只要低n位一样则结果一样，都是2^n - 1。"
   ConcurrentHashMap 1.7之前使用分段锁Segment来保护不同段的数据，那么在插入和获取元素的时候，必须先通过哈希算法定位到Segment。
   ConcurrentHashMap会首先使用Wang/Jenkins hash的变种算法对元素的hashCode(key)进行一次再哈希。之所以进行再哈希，其目的是为了减少哈希冲突，使元素能够均匀的分布在不同的Segment上，从而提高容器的存取效率。
   假如哈希的质量差到极点，那么所有的元素都在一个Segment中，不仅存取元素缓慢，分段锁也会失去意义。若不通过再哈希而直接执行哈希计算。hash & 15，只要低位一样，无论高位是什么数，其哈希值总是一样为15。
   hash >>> segmentShift) & segmentMask//定位Segment所使用的hash算法，高位代表Segment的下标
   int index = hash & (tab.length - 1);// 定位HashEntry所使用的hash算法，tab.length为2^n-1，最小为16，符合注2中的说法，与低位的值，因此低位代表HashEntry的下标
   默认情况下segmentShift为28，segmentMask为15，再哈希后的数(上两行中的hash)最大是32位二进制数据，向右无符号移动28位，意思是让高4位参与到hash运算中，即高位代表Segment的下标，而低位代表HashEntry的下标。
```

       0. 前提(概念引入)：ConcurrentHashMap类中hash(key)获取的值中，高位代表Segment的下标，而低位代表HashEntry的下标
       1. JDK 1.5：分段锁，必要时加锁
           hash(key)算法质量差，30000以下的Segment的下标基本都是15，分段锁失去意义。
       2. JDK 1.6：分段锁，优化二次Hash算法
           hash(key)算法优化后，使元素能够均匀的分布在不同的Segment上。
       3. JDK 1.7：段懒加载，volatile & cas
           JDK 1.7之前Segment直接初始化，默认16个。JDK 1.7开始，需要哪个初始化哪个，因此1.7中实例化segment时为确保segment的可见性，大量使用了对数组的volatile(getObjectVolatile)
       4. JDK 1.8：摒弃段，基于 HashMap 原理的并发实现
           摒弃分段加锁，ConcurrentHashMap 中 synchronized 只锁定当前链表或红黑二叉树的首节点，只要节点 hash 不冲突，就不会产生并发。
### [**链表的实现**](https://www.cnblogs.com/xiaoxi/p/7233201.html)
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581933715714-713e4213-d69a-48c5-93dd-8fac21b4477d.png#align=left&display=inline&height=377&originHeight=734&originWidth=970&size=0&status=done&style=none&width=498)
Node是HashMap的一个内部类，实现了Map.Entry接口，本质是就是一个映射(键值对)。上图中的每个黑色圆点就是一个Node对象。来看具体代码：
```
//Node是单向链表，它实现了Map.Entry接口
static class Node<k,v> implements Map.Entry<k,v> {
    final int hash;
    final K key;
    V value;
    Node<k,v> next;
    //构造函数Hash值 键 值 下一个节点
    Node(int hash, K key, V value, Node<k,v> next) {
        this.hash = hash;
        this.key = key;
        this.value = value;
        this.next = next;
    }
 
    public final K getKey()        { return key; }
    public final V getValue()      { return value; }
    public final String toString() { return key + = + value; }
 
    public final int hashCode() {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }
 
    public final V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }
    //判断两个node是否相等,若key和value都相等，返回true。可以与自身比较为true
    public final boolean equals(Object o) {
        if (o == this)
            return true;
        if (o instanceof Map.Entry) {
            Map.Entry<!--?,?--> e = (Map.Entry<!--?,?-->)o;
            if (Objects.equals(key, e.getKey()) &&
                Objects.equals(value, e.getValue()))
                return true;
        }
        return false;
    }
}
```

可以看到，node中包含一个next变量，这个就是链表的关键点，hash结果相同的元素就是通过这个next进行关联的。
### [红黑树的实现](https://www.cnblogs.com/xiaoxi/p/7233201.html)
```
//红黑树
static final class TreeNode<k,v> extends LinkedHashMap.Entry<k,v> {
    TreeNode<k,v> parent;  // 父节点
    TreeNode<k,v> left; //左子树
    TreeNode<k,v> right;//右子树
    TreeNode<k,v> prev;    // needed to unlink next upon deletion
    boolean red;    //颜色属性
    TreeNode(int hash, K key, V val, Node<k,v> next) {
        super(hash, key, val, next);
    }
 
    //返回当前节点的根节点
    final TreeNode<k,v> root() {
        for (TreeNode<k,v> r = this, p;;) {
            if ((p = r.parent) == null)
                return r;
            r = p;
        }
    }
}
```

红黑树比链表多了四个变量，parent父节点、left左节点、right右节点、prev上一个同级节点，红黑树内容较多，不在赘述。
### [位桶](https://www.cnblogs.com/xiaoxi/p/7233201.html)
```
transient Node<k,v>[] table;//存储（位桶）的数组
```
HashMap类中有一个非常重要的字段，就是 Node[] table，即哈希桶数组，明显它是一个Node的数组。
有了以上3个数据结构，只要有一点数据结构基础的人，都可以大致联想到HashMap的实现了。首先有一个每个元素都是链表（可能表述不准确）的数组，当添加一个元素（key-value）时，就首先计算元素key的hash值，以此确定插入数组中的位置，但是可能存在同一hash值的元素已经被放在数组同一位置了，这时就添加到同一hash值的元素的后面，他们在数组的同一位置，但是形成了链表，所以说数组存放的是链表。而当链表长度太长时，链表就转换为红黑树，这样大大提高了查找的效率。
### [put 操作](https://blog.csdn.net/zjxxyz123/article/details/81111627)
put 操作的主要流程如下：
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581932972080-f46ae535-949b-49e7-97e9-efb96ef5b015.png#align=left&display=inline&height=1360&originHeight=1360&originWidth=1716&size=0&status=done&style=none&width=1716)

- ① 判断键值对数组table[i]是否为空或为null，否则执行resize()进行扩容；
- ② 根据键值key计算hash值得到插入的数组索引i，如果table[i]==null，直接新建节点添加，转向⑥，如果table[i]不为空，转向③；
- ③ 判断table[i]的首个元素是否和key一样，如果相同直接覆盖value，否则转向④，这里的相同指的是hashCode以及equals；
- ④ 判断table[i] 是否为treeNode，即table[i] 是否是红黑树，如果是红黑树，则直接在树中插入键值对，否则转向⑤；
- ⑤ 遍历table[i]，判断链表长度是否大于8，大于8的话把链表转换为红黑树，在红黑树中执行插入操作，否则进行链表的插入操作；遍历过程中若发现key已经存在直接覆盖value即可；
- ⑥ 插入成功后，判断实际存在的键值对数量size是否超多了最大容量threshold，如果超过，进行扩容。
```
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}
/**
 * Implements Map.put and related methods
 *
 * @param hash hash for key
 * @param key the key
 * @param value the value to put
 * @param onlyIfAbsent if true, don't change existing value
 * @param evict if false, the table is in creation mode.
 * @return previous value, or null if none
 */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                   boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    // 步骤①：tab为空则创建 
    // table未初始化或者长度为0，进行扩容
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    // 步骤②：计算index，并对null做处理  
    // (n - 1) & hash 确定元素存放在哪个桶中，桶为空，新生成结点放入桶中(此时，这个结点是放在数组中)
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    // 桶中已经存在元素
    else {
        Node<K,V> e; K k;
        // 步骤③：节点key存在，直接覆盖value 
        // 比较桶中第一个元素(数组中的结点)的hash值相等，key相等
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
                // 将第一个元素赋值给e，用e来记录
                e = p;
        // 步骤④：判断该链为红黑树 
        // hash值不相等，即key不相等；为红黑树结点
        else if (p instanceof TreeNode)
            // 放入树中
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        // 步骤⑤：该链为链表 
        // 为链表结点
        else {
            // 在链表最末插入结点
            for (int binCount = 0; ; ++binCount) {
                // 到达链表的尾部
                if ((e = p.next) == null) {
                    // 在尾部插入新结点
                    p.next = newNode(hash, key, value, null);
                    // 结点数量达到阈值，转化为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    // 跳出循环
                    break;
                }
                // 判断链表中结点的key值与插入的元素的key值是否相等
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    // 相等，跳出循环
                    break;
                // 用于遍历桶中的链表，与前面的e = p.next组合，可以遍历链表
                p = e;
            }
        }
        // 表示在桶中找到key值、hash值与插入元素相等的结点
        if (e != null) { 
            // 记录e的value
            V oldValue = e.value;
            // onlyIfAbsent为false或者旧值为null
            if (!onlyIfAbsent || oldValue == null)
                //用新值替换旧值
                e.value = value;
            // 访问后回调
            afterNodeAccess(e);
            // 返回旧值
            return oldValue;
        }
    }
    // 结构性修改
    ++modCount;
    // 步骤⑥：超过最大容量 就扩容 
    // 实际大小大于阈值则扩容
    if (++size > threshold)
        resize();
    // 插入后回调
    afterNodeInsertion(evict);
    return null;
}
```
### [resize方法](https://www.cnblogs.com/xiaoxi/p/7233201.html)

- ① 在jdk1.8中，resize方法是在hashmap中的键值对大于阀值时或者初始化时，就调用resize方法进行扩容；
- ② 每次扩展的时候，都是扩展2倍；
- ③ 扩展后Node对象的位置要么在原位置，要么移动到原偏移量两倍的位置。
```
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;//oldTab指向hash桶数组
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {//如果oldCap不为空的话，就是hash桶数组不为空
        if (oldCap >= MAXIMUM_CAPACITY) {//如果大于最大容量了，就赋值为整数最大的阀值
            threshold = Integer.MAX_VALUE;
            return oldTab;//返回
        }//如果当前hash桶数组的长度在扩容后仍然小于最大容量 并且oldCap大于默认值16
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold 双倍扩容阀值threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];//新建hash桶数组
    table = newTab;//将新数组的值复制给旧的hash桶数组
    if (oldTab != null) {//进行扩容操作，复制Node对象值到新的hash桶数组
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {//如果旧的hash桶数组在j结点处不为空，复制给e
                oldTab[j] = null;//将旧的hash桶数组在j结点处设置为空，方便gc
                if (e.next == null)//如果e后面没有Node结点
                    newTab[e.hash & (newCap - 1)] = e;//直接对e的hash值对新的数组长度求模获得存储位置
                else if (e instanceof TreeNode)//如果e是红黑树的类型，那么添加到红黑树中
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;//将Node结点的next赋值给next
                        if ((e.hash & oldCap) == 0) {//如果结点e的hash值与原hash桶数组的长度作与运算为0
                            if (loTail == null)//如果loTail为null
                                loHead = e;//将e结点赋值给loHead
                            else
                                loTail.next = e;//否则将e赋值给loTail.next
                            loTail = e;//然后将e复制给loTail
                        }
                        else {//如果结点e的hash值与原hash桶数组的长度作与运算不为0
                            if (hiTail == null)//如果hiTail为null
                                hiHead = e;//将e赋值给hiHead
                            else
                                hiTail.next = e;//如果hiTail不为空，将e复制给hiTail.next
                            hiTail = e;//将e复制个hiTail
                        }
                    } while ((e = next) != null);//直到e为空
                    if (loTail != null) {//如果loTail不为空
                        loTail.next = null;//将loTail.next设置为空
                        newTab[j] = loHead;//将loHead赋值给新的hash桶数组[j]处
                    }
                    if (hiTail != null) {//如果hiTail不为空
                        hiTail.next = null;//将hiTail.next赋值为空
                        newTab[j + oldCap] = hiHead;//将hiHead赋值给新的hash桶数组[j+旧hash桶数组长度]
                    }
                }
            }
        }
    }
    return newTab;
}
```
在扩充HashMap的时候，不需要像JDK1.7的实现那样重新计算hash，只需要看看原来的hash值新增的那个bit是1还是0就好了，是0的话索引没变（因为任何数与0与都依旧是0），是1的话index变成“原索引+oldCap”。
例如：n为table的长度，图（a）表示扩容前的key1和key2两种key确定索引位置的示例，图（b）表示扩容后key1和key2两种key确定索引位置的示例，其中hash1是key1对应的哈希与高位运算结果。
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581933550473-94fb2c5b-ea15-40a8-ba97-ebdb08eacd9c.png#align=left&display=inline&height=446&originHeight=446&originWidth=1632&size=0&status=done&style=none&width=1632)
元素在重新计算hash之后，因为n变为2倍，那么n-1的mask范围在高位多1bit(红色)，因此新的index就会发生这样的变化：
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581933559667-f17be44b-d38e-4b07-a137-85bded25d1b8.png#align=left&display=inline&height=202&originHeight=202&originWidth=1064&size=0&status=done&style=none&width=1064)
## 面试相关
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1581933621977-39709b90-06cd-4cae-9a03-9e0546a2cf04.png#align=left&display=inline&height=642&originHeight=642&originWidth=937&size=0&status=done&style=none&width=937)

- 为什么不直接采用经过hashCode（）处理的哈希码 作为 存储数组table的下标位置？

**答：**容易出现 哈希码 与 数组大小范围不匹配的情况，即 计算出来的哈希码可能 不在数组大小范围内，从而导致无法匹配存储位置

- 为什么采用 哈希码 与运算(&) （数组长度-1） 计算数组下标？

**答：**根据HashMap的容量大小（数组长度），按需取 哈希码一定数量的低位 作为存储的数组下标位置，从而 解决 “哈希码与数组大小范围不匹配” 的问题

- 为什么在计算数组下标前，需对哈希码进行二次处理：扰动处理？

**答：**加大哈希码低位的随机性，使得分布更均匀，从而提高对应数组存储下标位置的随机性 & 均匀性，最终减少Hash冲突

- HashMap的默认容量是多少，为什么这样设计？

   **   答：**length 的值为2 的整数次幂，h & (length - 1)相当于对 length 取模。这样提高了效率也使得数据分布更加均匀。
     为什么会更加均匀？length的值为偶数，length - 1 为奇数，则二进制位的最后一位为1，这样保证了h & (length - 1)的二进制数最后一位可能为1，也可能为0。如果为length为奇数，那么就会浪费一半的空间。

- hashmap 1.7为什么要先扩容再添加，1.8为什么是先添加再扩容

**答：**扩容后数据存储位置的计算方式也不一样：

  1. 在JDK1.7的时候是直接用hash值和需要扩容的二进制数进行&（这里就是为什么扩容的时候为啥一定必须是2的多少次幂的原因所在，因为如果只有2的n次幂的情况时最后一位二进制数才一定是1，这样能最大程度减少hash碰撞）（hash值 & length-1）。JDK1.7中的话，是先进行扩容后进行插入的，就是当你发现你插入的桶是不是为空，如果不为空说明存在值就发生了hash冲突，那么就必须得扩容，但是如果不发生Hash冲突的话，说明当前桶是空的（后面并没有挂有链表），那就等到下一次发生Hash冲突的时候在进行扩容，但是当如果以后都没有发生hash冲突产生，那么就不会进行扩容了，减少了一次无用扩容，也减少了内存的使用。
  1. 而在JDK1.8的时候分两种情况：

 	        1. 该结点下无链表或红黑树：和JDK1.7一致 e.hash & 新扩容的大小 - 1。
 	        2. 该结点下有链表或红黑树：数据存储位置 = 扩容前的原始位置 or 扩容前的原始位置+扩容的大小值。        但是这种方式就相当于只需要判断Hash值的"新增参与运算的位"是0还是1就直接迅速计算出了扩容后的储存方式。
       什么是"新增参与运算的位"？e.hash & length-1
   	        扩容前length = 16，则参与 hash & length-1 的位实际上就是 15的二进制1111四位，因为前面都是0
   	        扩容后length = 32，则参与 hash & length-1 的位实际上就是 32的二进制11111五位，其余前面都是0，因此若新参与运算的第一位，位0则数据仍保存原有位置，否则保存在：扩容前的原始位置+扩容的大小值   
      这样的好处是可以更快速的计算出了扩容后的储存位置。
     	JDK1.8先添加后扩容是因为，1.7中所有结点的存储位置都需要根据扩容后的大小重新计算位置，而1.8中不需要。

- 1.7&1.8插入数据的规则是什么？

**答：**JDK1.7用的是头插法，而JDK1.8及之后使用的都是尾插法，那么他们为什么要这样做呢？因为JDK1.7是用单链表进行的纵向延伸，当采用头插法时会容易出现逆序且环形链表死循环问题。但是在JDK1.8之后是因为加入了红黑树使用尾插法，能够避免出现逆序且链表死循环的问题。
     多线程下HashMap的死循环：
HashMap是采用链表解决Hash冲突，因为是链表结构，那么就很容易形成闭合的链路，这样在循环的时候只要有线程对这个HashMap进行get操作就会产生死循环。在单线程情况下，只有一个线程对HashMap的数据结构进行操作，是不可能产生闭合的回路的。那就只有在多线程并发的情况下才会出现这种情况，那就是在put操作的时候，如果size>initialCapacity*loadFactor，那么这时候HashMap就会进行rehash操作，随之HashMap的结构就会发生翻天覆地的变化。很有可能就是在两个线程在这个时候同时触发了rehash操作，产生了闭合的回路。
 	        即：put时，若多个线程都需要扩容，则此时线程一拿到时间片，rehash后转移原数据到新的hash表中时，带有链表的节点会从头到尾遍历插入新的链表中，由于1.7是头插法这就导致了原来链表的头结点变成了尾结点，即原来的头结点.next=尾结点；在线程切换至线程二时，此时线程二也需要rehash，此时链表就变成了尾.next=头，就造成了环形链表出现啦。不调用get方法不会出现问题，一旦调用get就有可能死循环。

- 为什么在JDK1.8中进行对HashMap优化的时候，把链表转化为红黑树的阈值是8,而不是7或者不是20呢（面试蘑菇街问过）？

    ** 答：**1). 如果选择6和8（如果链表小于等于6树还原转为链表，大于等于8转为树），中间有个差值7可以有效防止链表和树频繁转换。假设一下，如果设计成链表个数超过8则链表转换成树结构，链表个数小于8则树结构转换成链表，如果一个HashMap不停的插入、删除元素，链表个数在8左右徘徊，就会频繁的发生树转链表、链表转树，效率会很低。
         2). 还有一点重要的就是由于treenodes的大小大约是常规节点的两倍，因此我们仅在容器包含足够的节点以保证使用时才使用它们，当它们变得太小（由于移除或调整大小）时，它们会被转换回普通的node节点，容器中节点分布在hash桶中的频率遵循泊松分布，桶的长度超过8的概率非常非常小。所以作者应该是根据概率统计而选择了8作为阀值
## [Jdk 7 与 Jdk 8 中关于HashMap的对比](https://www.jianshu.com/p/8324a34577a0)

- 8时红黑树+链表+数组的形式，当桶内元素大于8时，便会树化
- hash值的计算方式不同
- 1.7 table在创建hashmap时分配空间，而1.8在put的时候分配，如果table为空，则为table分配空间。
- 在发生冲突，插入链中时，7是头插法，8是尾插法。
- 在resize操作中，7需要重新进行index的计算，而8不需要，通过判断相应的位是0还是1，要么依旧是原index，要么是oldCap + 原index。
- 数组元素 & 链表节点的 实现类：1.8 采用 `Node`类 实现，与 `1.7` 的对比（`Entry`类），仅仅只是换了名字

`HashMap` 的实现在 `JDK 1.7` 和 `JDK 1.8` 差别较大，具体区别如下
> 1. `JDK 1.8` 的优化目的主要是：减少 `Hash`冲突 & 提高哈希表的存、取效率
> 1. 关于  `JDK 1.7` 中  `HashMap` 的源码解析请看文章：[Java：手把手带你源码分析 HashMap 1.7](https://links.jianshu.com/go?to=http%3A%2F%2Fblog.csdn.net%2Fcarson_ho%2Farticle%2Fdetails%2F79373026)

### 数据结构
![](//upload-images.jianshu.io/upload_images/944365-1479fa30b86d2f6b.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp#align=left&display=inline&height=331&originHeight=331&originWidth=1200&status=done&style=none&width=1200)
### 获取数据时（获取数据 类似）
![](//upload-images.jianshu.io/upload_images/944365-375d272b7c41c09a.png?imageMogr2/auto-orient/strip|imageView2/2/w/1190/format/webp#align=left&display=inline&height=280&originHeight=280&originWidth=1190&status=done&style=none&width=1190)
### 扩容流程
![](https://cdn.nlark.com/yuque/0/2020/webp/754789/1581934884572-d75fc935-4fd7-451d-bffa-127d84df1a90.webp#align=left&display=inline&height=1950&originHeight=1950&originWidth=1120&size=0&status=done&style=none&width=1120)
