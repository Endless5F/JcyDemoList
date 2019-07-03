最近回顾了一下数据结构，发现一直在使用HasMap，但是大学期间针对哈希表的概念老师当时竟然跳过啦，因此重新了解了一下哈希表的基础知识。有个不错的讲解视频：
https://v.youku.com/v_show/id_XNDAyNTA3NTAwOA==.html

顺便也了解了一下HashMap的原理，是如何使用哈希表的，感兴趣的小伙伴可以看一下：
https://www.cnblogs.com/xiaoxi/p/7233201.html 和     https://www.cnblogs.com/chengxiao/p/6059914.html

先放两张哈希表的结构吧：

* JDK1.8之前，以及JDK1.8之后，同一个hash值的节点数小于8时
![](https://user-gold-cdn.xitu.io/2019/5/24/16ae82718699dacc?w=541&h=456&f=png&s=69148)

* JDK1.8之后，当同一个hash值的节点数不小于8时，不再采用单链表形式存储，而是采用红黑树
![](https://user-gold-cdn.xitu.io/2019/5/24/16ae827b0f58b85f?w=369&h=424&f=png&s=110096)

本文主要还是看一看再研究哈希表过程中，比较有意思的哈希的加密算法（非对称加密）

哈希函数：Hash，一般翻译做散列、杂凑，或音译为哈希，就是把任意长度的输入（又叫做预映射， pre-image），通过散列算法，变换成固定长度的输出，该输出就是散列值。这种转换是一种压缩映射，也就是，散列值的空间通常远小于输入的空间，不同的输入可能会散列成相同的输出，而不可能从散列值来唯一的确定输入值。简单的说就是一种将任意长度的消息压缩到某一固定长度的消息摘要的函数。

本文主要讲解两种常用的哈希函数MD5和SHA-256(目前最流行的安全加密算法)

## MD5

MD5消息摘要算法（英语：MD5 Message-Digest Algorithm），一种被广泛使用的密码散列函数，可以产生出一个128位（16字节）的散列值（hash value），用于确保信息传输完整一致。MD5由美国密码学家罗纳德·李维斯特（Ronald Linn Rivest）设计，于1992年公开，用以取代MD4算法。

**算法步骤：**

1、数据填充

对消息进行数据填充，使消息的长度对512取模得448，设消息长度为X，即满足X mod 512=448。根据此公式得出需要填充的数据长度。

填充方法：在消息后面进行填充，填充第一位为1，其余为0。

2、添加消息长度

在第一步结果之后再填充上原消息的长度，可用来进行的存储长度为64位。如果消息长度大于264，则只使用其低64位的值，即（消息长度 对 264取模）。

经过上面两步的处理，现在的信息的位长=N*512+448+64=(N+1）*512，即长度恰好是512的整数倍。这样做的原因是为满足后面处理中对信息长度的要求。

3、数据处理：分组处理

* 准备需要用到的数据：

    4个常数： 
    
        A = 0x67452301, B = 0x0EFCDAB89, C = 0x98BADCFE, D = 0x10325476;

    4个函数：(&是与,|是或,~是非,^是异或) 
        
        F(X,Y,Z)=(X & Y) | ((~X) & Z); G(X,Y,Z)=(X & Z) | (Y & (~Z));   
        H(X,Y,Z)=X ^ Y ^ Z;   I(X,Y,Z)=Y ^ (X | (~Z));

* 具体操作：
 1. 首先将消息以512位为一分组进行处理，分为N组
 2. 将每组消息N(i)进行4轮变换（四轮主循环），以上面所说4个常数首先赋值给a、b、c、d为起始变量进行计算，重新输出4个变量，并重新赋值给a、b、c、d四个值。
 3. 以第2步获得的新的a、b、c、d四个值，再进行下一分组的运算，如果已经是最后一个分组，则这4个变量的最后结果按照从低内存到高内存排列起来，共128位，这就是MD5算法的输出。

<font color=#ff0000>第二步详细介绍：</font>主循环有四轮（MD4只有三轮），每轮循环都很相似。每轮主循环都有16轮次循环，16轮次循环：其实是将每组512位分为16组，每次操作对a、b、c和d中的其中三个作一次非线性函数运算，然后将所得结果加上第四个变量，文本的一个子分组和一个常数。再将所得结果向左环移一个不定的数，并加上a、b、c或d中之一。最后用该结果取代a、b、c或d中之一。

<font color=#ff0000>注(a、b、c或d)</font>：可被称为消息摘要部分信息，因此上面四个非线性函数，是针对a、b、c或d运算

*** 四轮主循环主要的运算函数为下面四个包含了上面四个非线程函数的函数：

设Mj表示消息的第j个子分组（从0到15），<<< s表示循环左移s位，则四种操作为： 
FF(a,b,c,d,Mj,s,ti)表示a=b+((a+(F(b,c,d)+Mj+ti)<<< s) 

GG(a,b,c,d,Mj,s,ti)表示a=b+((a+(G(b,c,d)+Mj+ti)<<< s) 

HH(a,b,c,d,Mj,s,ti)表示a=b+((a+(H(b,c,d)+Mj+ti)<<< s) 

II(a,b,c,d,Mj,s,ti)表示a=b+((a+(I(b,c,d)+Mj+ti)<<< s) 

此四个函数中的前四个位置的a,b,c,d同理X、Y、Z等，不仅仅代表上面所说的消息摘要的信息a,b,c,d，可理解为代码中的四个参数。而参数一：就是本次次循环需要求的值。具体可通过下面具体的64步了解。

![](https://user-gold-cdn.xitu.io/2019/5/24/16ae5a154ab5ed2d?w=593&h=670&f=jpeg&s=63255)
此图只选了FF函数说明一下，16轮次循环每一步FF函数中前四个参数a,b,c,d的变化，同时根据上面所阐述的，参数一即为此次函数所求值可知，FF会根据16组消息M(i)、以及s和ti，循环的计算消息摘要的a,b,c,d四个值。（GG、HH、II函数同理）

这四轮（64步）是： 
```
第一轮 
FF(a,b,c,d,M0,7,0xd76aa478) 
FF(d,a,b,c,M1,12,0xe8c7b756) 
FF(c,d,a,b,M2,17,0×242070db) 
FF(b,c,d,a,M3,22,0xc1bdceee) 
FF(a,b,c,d,M4,7,0xf57c0faf) 
FF(d,a,b,c,M5,12,0×4787c62a) 
FF(c,d,a,b,M6,17,0xa8304613) 
FF(b,c,d,a,M7,22,0xfd469501) 
FF(a,b,c,d,M8,7,0×698098d8) 
FF(d,a,b,c,M9,12,0×8b44f7af) 
FF(c,d,a,b,M10,17,0xffff5bb1) 
FF(b,c,d,a,M11,22,0×895cd7be) 
FF(a,b,c,d,M12,7,0×6b901122) 
FF(d,a,b,c,M13,12,0xfd987193) 
FF(c,d,a,b,M14,17,0xa679438e) 
FF(b,c,d,a,M15,22,0×49b40821) 
第二轮 
GG(a,b,c,d,M1,5,0xf61e2562) 
GG(d,a,b,c,M6,9,0xc040b340) 
GG(c,d,a,b,M11,14,0×265e5a51) 
GG(b,c,d,a,M0,20,0xe9b6c7aa) 
GG(a,b,c,d,M5,5,0xd62f105d) 
GG(d,a,b,c,M10,9,0×02441453) 
GG(c,d,a,b,M15,14,0xd8a1e681) 
GG(b,c,d,a,M4,20,0xe7d3fbc8) 
GG(a,b,c,d,M9,5,0×21e1cde6) 
GG(d,a,b,c,M14,9,0xc33707d6) 
GG(c,d,a,b,M3,14,0xf4d50d87) 
GG(b,c,d,a,M8,20,0×455a14ed) 
GG(a,b,c,d,M13,5,0xa9e3e905) 
GG(d,a,b,c,M2,9,0xfcefa3f8) 
GG(c,d,a,b,M7,14,0×676f02d9) 
GG(b,c,d,a,M12,20,0×8d2a4c8a) 
第三轮 
HH(a,b,c,d,M5,4,0xfffa3942) 
HH(d,a,b,c,M8,11,0×8771f681) 
HH(c,d,a,b,M11,16,0×6d9d6122) 
HH(b,c,d,a,M14,23,0xfde5380c) 
HH(a,b,c,d,M1,4,0xa4beea44) 
HH(d,a,b,c,M4,11,0×4bdecfa9) 
HH(c,d,a,b,M7,16,0xf6bb4b60) 
HH(b,c,d,a,M10,23,0xbebfbc70) 
HH(a,b,c,d,M13,4,0×289b7ec6) 
HH(d,a,b,c,M0,11,0xeaa127fa) 
HH(c,d,a,b,M3,16,0xd4ef3085) 
HH(b,c,d,a,M6,23,0×04881d05) 
HH(a,b,c,d,M9,4,0xd9d4d039) 
HH(d,a,b,c,M12,11,0xe6db99e5) 
HH(c,d,a,b,M15,16,0×1fa27cf8) 
HH(b,c,d,a,M2,23,0xc4ac5665) 
第四轮 
II(a,b,c,d,M0,6,0xf4292244) 
II(d,a,b,c,M7,10,0×432aff97) 
II(c,d,a,b,M14,15,0xab9423a7) 
II(b,c,d,a,M5,21,0xfc93a039) 
II(a,b,c,d,M12,6,0×655b59c3) 
II(d,a,b,c,M3,10,0×8f0ccc92) 
II(c,d,a,b,M10,15,0xffeff47d) 
II(b,c,d,a,M1,21,0×85845dd1) 
II(a,b,c,d,M8,6,0×6fa87e4f) 
II(d,a,b,c,M15,10,0xfe2ce6e0) 
II(c,d,a,b,M6,15,0xa3014314) 
II(b,c,d,a,M13,21,0×4e0811a1) 
II(a,b,c,d,M4,6,0xf7537e82) 
II(d,a,b,c,M11,10,0xbd3af235) 
II(c,d,a,b,M2,15,0×2ad7d2bb) 
II(b,c,d,a,M9,21,0xeb86d391) 
```
所有这些完成之后，将A，B，C，D分别加上a，b，c，d。然后用下一分组数据继续运行算法，最后的输出是A，B，C和D的级联。 
把这四个数A -> B -> C -> D按照从低内存到高内存排列起来，共128位，这就是MD5算法的输出。

**Java代码实现：**
```
public class MD5{
    /*
    *四个链接变量：寄存器中数值(小端模式)// 真实数值
    */
    private final int A=0x67452301;// 真实A=0×01234567
    private final int B=0xefcdab89;// 真实B=0×89abcdef 
    private final int C=0x98badcfe;// 真实C=0xfedcba98 
    private final int D=0x10325476;// 真实D=0×76543210 
    /*
    *ABCD的临时变量
    */
    private int Atemp,Btemp,Ctemp,Dtemp;
     
    /*
    * 常量ti：即代码中的K[i]
    *   公式:floor(abs(sin(i+1))×(2pow32)
    *   ti是4294967296*abs(sin(i))的整数部分,i的单位是弧度。 
    *   4294967296 == 2的32次方
    * 由于是常量，可以在计算时直接嵌入数据。
    */
    private final int K[]={
        0xd76aa478,0xe8c7b756,0x242070db,0xc1bdceee,
        0xf57c0faf,0x4787c62a,0xa8304613,0xfd469501,0x698098d8,
        0x8b44f7af,0xffff5bb1,0x895cd7be,0x6b901122,0xfd987193,
        0xa679438e,0x49b40821,0xf61e2562,0xc040b340,0x265e5a51,
        0xe9b6c7aa,0xd62f105d,0x02441453,0xd8a1e681,0xe7d3fbc8,
        0x21e1cde6,0xc33707d6,0xf4d50d87,0x455a14ed,0xa9e3e905,
        0xfcefa3f8,0x676f02d9,0x8d2a4c8a,0xfffa3942,0x8771f681,
        0x6d9d6122,0xfde5380c,0xa4beea44,0x4bdecfa9,0xf6bb4b60,
        0xbebfbc70,0x289b7ec6,0xeaa127fa,0xd4ef3085,0x04881d05,
        0xd9d4d039,0xe6db99e5,0x1fa27cf8,0xc4ac5665,0xf4292244,
        0x432aff97,0xab9423a7,0xfc93a039,0x655b59c3,0x8f0ccc92,
        0xffeff47d,0x85845dd1,0x6fa87e4f,0xfe2ce6e0,0xa3014314,
        0x4e0811a1,0xf7537e82,0xbd3af235,0x2ad7d2bb,0xeb86d391};
    /*
    *向左位移数,由于是常量，也可以在计算时直接嵌入数据。
    *（此数据有规律，实际代码中可以对此进行优化，即改变此部分值）。
    */
    private final int s[]={7,12,17,22,7,12,17,22,7,12,17,22,7,
        12,17,22,5,9,14,20,5,9,14,20,5,9,14,20,5,9,14,20,
        4,11,16,23,4,11,16,23,4,11,16,23,4,11,16,23,6,10,
        15,21,6,10,15,21,6,10,15,21,6,10,15,21};
     
    /*
    *初始化函数
    */
    private void init(){
        Atemp=A;
        Btemp=B;
        Ctemp=C;
        Dtemp=D;
    }
    /*
    *移动一定位数
    */
    private int shift(int a,int s){
        return(a<<s)|(a>>>(32-s));//右移的时候，高位一定要补零，而不是补充符号位
    }
    /*
    *主循环
    */
    private void MainLoop(int M[]){
        int F,g;
        int a=Atemp;
        int b=Btemp;
        int c=Ctemp;
        int d=Dtemp;
        for(int i = 0; i < 64; i ++){
            if(i<16){
                F=(b&c)|((~b)&d);
                g=i;
            }else if(i<32){
                F=(d&b)|((~d)&c);
                g=(5*i+1)%16;
            }else if(i<48){
                F=b^c^d;
                g=(3*i+5)%16;
            }else{
                F=c^(b|(~d));
                g=(7*i)%16;
            }
            int tmp=d;
            d=c;
            c=b;
            b=b+shift(a+F+K[i]+M[g],s[i]);
            a=tmp;
        }
        Atemp=a+Atemp;
        Btemp=b+Btemp;
        Ctemp=c+Ctemp;
        Dtemp=d+Dtemp;
     
    }
    /*
    *填充函数
    *处理后应满足bits≡448(mod512),字节就是bytes≡56（mode64)
    *填充方式为先加一个0,其它位补零
    *最后加上64位的原来长度
    */
    private int[] add(String str){
        int num=((str.length()+8)/64)+1;//以512位，64个字节为一组
        int strByte[]=new int[num*16];//64/4=16，所以有16个整数
        for(int i=0;i<num*16;i++){//全部初始化0
            strByte[i]=0;
        }
        int    i;
        for(i=0;i<str.length();i++){
            strByte[i>>2]|=str.charAt(i)<<((i%4)*8);//一个整数存储四个字节，小端序
        }
        strByte[i>>2]|=0x80<<((i%4)*8);//尾部添加1
        /*
        *添加原长度，长度指位的长度，所以要乘8，然后是小端序，所以放在倒数第二个,这里长度只用了32位
        */
        strByte[num*16-2]=str.length()*8;
        return strByte;
    }
    /*
    *调用函数
    */
    public String getMD5(String source){
        init();
        int strByte[]=add(source);
        for(int i = 0; i < strByte.length / 16; i++){
            int num[] = new int[16];
            for(int j = 0; j < 16; j++){
                num[j] = strByte[i*16+j];
            }
            MainLoop(num);
        }
        return changeHex(Atemp)+changeHex(Btemp)+changeHex(Ctemp)+changeHex(Dtemp);
    }
    /*
    *整数变成16进制字符串
    */
    private String changeHex(int a){
        String str="";
        for(int i=0;i<4;i++){
            str+=String.format("%2s", Integer.toHexString(((a>>i*8)%(1<<8))&0xff)).replace(' ', '0');
 
        }
        return str;
    }
    /*
    *单例
    */
    private static MD5 instance;
    public static MD5 getInstance(){
        if(instance==null){
            instance=new MD5();
        }
        return instance;
    }
     
    private MD5(){};
     
    public static void main(String[] args){
        String str=MD5.getInstance().getMD5("");
        System.out.println(str);
    }
}
```
**MD5算法的不足：**

现在看来，MD5已经较老，散列长度通常为128位，随着计算机运算能力提高，找到“碰撞”是可能的。因此，在安全要求高的场合不使用MD5。

2004年，王小云教授证明MD5数字签名算法可以产生碰撞。2007年，Marc Stevens，Arjen K. Lenstra和Benne de Weger进一步指出通过伪造软件签名，可重复性攻击MD5算法。研究者使用前缀碰撞法（chosen-prefix collision），使程序前端包含恶意程序，利用后面的空间添上垃圾代码凑出同样的MD5 Hash值。2007年，荷兰埃因霍芬技术大学科学家成功把2个可执行文件进行了MD5碰撞，使得这两个运行结果不同的程序被计算出同一个MD5。2008年12月科研人员通过MD5碰撞成功生成了伪造的SSL证书，这使得在https协议中服务器可以伪造一些根CA的签名。

MD5被攻破后，在Crypto2008上， Rivest提出了MD6算法，该算法的Block size为512 bytes(MD5的Block Size是512 bits), Chaining value长度为1024 bits, 算法增加了并行 机制，适合于多核CPU。 在安全性上，Rivest宣称该算法能够抵抗截至目前已知的所有的 攻击（包括差分攻击）。

## SHA-256

**SHA256简介**

SHA256是SHA-2下细分出的一种算法

SHA-2，名称来自于安全散列算法2（英语：Secure Hash Algorithm 2）的缩写，一种密码散列函数算法标准，由美国国家安全局研发，属于SHA算法之一，是SHA-1的后继者。

SHA-2下又可再分为六个不同的算法标准

包括了：SHA-224、SHA-256、SHA-384、SHA-512、SHA-512/224、SHA-512/256。

这些变体除了生成摘要的长度 、循环运行的次数等一些微小差异外，算法的基本结构是一致的。

对于任意长度的消息，SHA256都会产生一个256bit长的哈希值，称作消息摘要。这个摘要相当于是个长度为32个字节的数组，通常用一个长度为64的十六进制字符串来表示

**SHA256算法步骤：**

* 初始化常量(初始化缓存：8个哈希初值)和算法中用到的64个常量

    8个哈希初值：是对自然数中前8个质数（2,3,5,7,11,13,17,19）的平方根的小数部分取前32位，
    此8个哈希初值位SHA256算法中的最小运算单元称为“字”（Word），一个字是32位。

        A=0x6A09E667 , B=0xBB67AE85 , C=0x3C6EF372 , D=0xA54FF53A, 
        E=0x510E527F , F=0x9B05688C , G=0x1F83D9AB , H=0x5BE0CD19 。
    64个算法常量：对自然数中前64个质数(2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97…)的立方根的小数部分取前32位
    
        0x428a2f98UL, 0x71374491UL, 0xb5c0fbcfUL, 0xe9b5dba5UL, 0x3956c25bUL,
        0x59f111f1UL, 0x923f82a4UL, 0xab1c5ed5UL, 0xd807aa98UL, 0x12835b01UL,
        0x243185beUL, 0x550c7dc3UL, 0x72be5d74UL, 0x80deb1feUL, 0x9bdc06a7UL,
        0xc19bf174UL, 0xe49b69c1UL, 0xefbe4786UL, 0x0fc19dc6UL, 0x240ca1ccUL,
        0x2de92c6fUL, 0x4a7484aaUL, 0x5cb0a9dcUL, 0x76f988daUL, 0x983e5152UL,
        0xa831c66dUL, 0xb00327c8UL, 0xbf597fc7UL, 0xc6e00bf3UL, 0xd5a79147UL,
        0x06ca6351UL, 0x14292967UL, 0x27b70a85UL, 0x2e1b2138UL, 0x4d2c6dfcUL,
        0x53380d13UL, 0x650a7354UL, 0x766a0abbUL, 0x81c2c92eUL, 0x92722c85UL,
        0xa2bfe8a1UL, 0xa81a664bUL, 0xc24b8b70UL, 0xc76c51a3UL, 0xd192e819UL,
        0xd6990624UL, 0xf40e3585UL, 0x106aa070UL, 0x19a4c116UL, 0x1e376c08UL,
        0x2748774cUL, 0x34b0bcb5UL, 0x391c0cb3UL, 0x4ed8aa4aUL, 0x5b9cca4fUL,
        0x682e6ff3UL, 0x748f82eeUL, 0x78a5636fUL, 0x84c87814UL, 0x8cc70208UL,
        0x90befffaUL, 0xa4506cebUL, 0xbef9a3f7UL, 0xc67178f2UL
  
* 预处理：附加填充比特和附加长度值

    1、附加填充比特
    
        在报文末尾进行填充，使报文长度在对512取模以后的余数是448
        填充是这样进行的：先补第一个比特为1，然后都补0，直到长度满足对512取模后余数是448。
        需要注意的是，信息必须进行填充，也就是说，即使长度已经满足对512取模后余数是448，
        补位也必须要进行，这时要填充512个比特。因此，填充是至少补一位，最多补512位。
    2、附加长度值
    
        附加长度值就是将原始数据（第一步填充前的消息）的长度信息补到已经进行了填充操作的消息后面。
        SHA256用一个64位的数据来表示原始消息的长度。
        因此，通过SHA256计算的消息长度必须要小于2^64，当然绝大多数情况这足够大了。
        而SHA-384和SHA-512，消息长度最大可接近2^128
* 分组运算

![](https://user-gold-cdn.xitu.io/2019/5/24/16ae7a11c8e09a7b?w=640&h=452&f=png&s=35806)
先上一张通用图吧

1、分组运算所需的6个函数

    Ch(x,y,z) = ((x & y) ^ ((~x) & z))
    Maj(x,y,z) = ((x & y) ^ (x & z) ^ (y & z))
    E0(x) = (x >> 2 | x << 30) ^ (x >> 13 | x << 19) ^ (x >> 22 | x << 10);
    E1(x) = (x >> 6 | x << 26) ^ (x >> 11 | x << 21) ^ (x >> 25 | x << 7)
    Q0(x) = (x >> 7 | x << 25) ^ (x >> 18 | x << 14) ^ (x >> 3)
    Q1(x) = (x >> 17 | x << 15) ^ (x >> 19 | x << 13) ^ (x >> 10)
2、将消息分解成512-bit大小的块，分成N组，每组都会经过上面6个函数的循环运算

3、构造64个字（word）：将上述分完组的块，每块分解为16个32-bit的big-endian的字，记为w[0], …, w[15]，即前16个字直接由消息的第i个块分解得到，而其余的字由如下迭代公式得到：
    
    Wt = Q1(Wt-2) + Wt-7 + Q0(Wt-15) + Wt-16
4、进行64次循环：算法使用64个32位字的消息列表、8个32位工作变量以及8个32位字的散列值。

* 初始化工作变量

    a = A // H(0)0 = A   
    b = B // H(0)1 = B   
    c = C // H(0)2 = C  
    d = D // H(0)3 = D  
    e = E // H(0)4 = E  
    f = F // H(0)5 = F  
    g = G // H(0)6 = G  
    h = H // H(0)7 = H

* 执行散列计算

    For t = 0 to 63
    T1 = h + E1(e) + CH(e,f,g) + Kt + Wt    
    T2 = E0(a) + MAj(a,b,c) 
    h = g   
    g = f   
    f = e   
    e = d + T1  
    d = c   
    c = b   
    b = a   
    a = T1 + T2 

* 计算中间散列值   

    H(i)0 = a + H(i-1)0 

    H(i)1 = b + H(i-1)1 

    H(i)2 = c + H(i-1)2 

    H(i)3 = d + H(i-1)3 

    H(i)4 = e + H(i-1)4 

    H(i)5 = f + H(i-1)5 

    H(i)6 = g + H(i-1)6

    H(i)7 = h + H(i-1)7 

在对所有消息分组完成上述计算之后，计算最终输出。对于SHA-256，是所有H(N)0、H(N)1到H(N)7的串联。对于SHA-224，则是H(N)0、H(N)1直到H(N)6的串联。

**C++的代码实现：**

```
借鉴一份别人写好的C++实现(下方已标明引用地址)

//SHA-256
/*理解算法最重要，最好自己动手实现试试看，可以使用MFC写一个简单的交互界面*/
 
#include <iostream> 
#include <cstdio>
#include <cstdlib>
 
using namespace std;
 
#define SHA256_ROTR(a,b) (((a>>(32-b))&(0x7fffffff>>(31-b)))|(a<<b))
#define SHA256_SR(a,b) ((a>>b)&(0x7fffffff>>(b-1)))
#define SHA256_Ch(x,y,z) ((x&y)^((~x)&z))
#define SHA256_Maj(x,y,z) ((x&y)^(x&z)^(y&z))
#define SHA256_E0(x) (SHA256_ROTR(x,30)^SHA256_ROTR(x,19)^SHA256_ROTR(x,10))
#define SHA256_E1(x) (SHA256_ROTR(x,26)^SHA256_ROTR(x,21)^SHA256_ROTR(x,7))
#define SHA256_Q0(x) (SHA256_ROTR(x,25)^SHA256_ROTR(x,14)^SHA256_SR(x,3))
#define SHA256_Q1(x) (SHA256_ROTR(x,15)^SHA256_ROTR(x,13)^SHA256_SR(x,10))
char* StrSHA256(const char* str, long long length, char* sha256){
    char *pp, *ppend;
    long l, i, W[64], T1, T2, A, B, C, D, E, F, G, H, H0, H1, H2, H3, H4, H5, H6, H7;
    H0 = 0x6a09e667, H1 = 0xbb67ae85, H2 = 0x3c6ef372, H3 = 0xa54ff53a;
    H4 = 0x510e527f, H5 = 0x9b05688c, H6 = 0x1f83d9ab, H7 = 0x5be0cd19;
    long K[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
    };
    l = length + ((length % 64 > 56) ? (128 - length % 64) : (64 - length % 64));
    if (!(pp = (char*)malloc((unsigned long)l))) return 0;
    for (i = 0; i < length; pp[i + 3 - 2 * (i % 4)] = str[i], i++);
    for (pp[i + 3 - 2 * (i % 4)] = 128, i++; i < l; pp[i + 3 - 2 * (i % 4)] = 0, i++);
    *((long*)(pp + l - 4)) = length << 3;
    *((long*)(pp + l - 8)) = length >> 29;
    for (ppend = pp + l; pp < ppend; pp += 64){
        for (i = 0; i < 16; W[i] = ((long*)pp)[i], i++);
        for (i = 16; i < 64; W[i] = (SHA256_Q1(W[i - 2]) + W[i - 7] + SHA256_Q0(W[i - 15]) + W[i - 16]), i++);
        A = H0, B = H1, C = H2, D = H3, E = H4, F = H5, G = H6, H = H7;
        for (i = 0; i < 64; i++){
            T1 = H + SHA256_E1(E) + SHA256_Ch(E, F, G) + K[i] + W[i];
            T2 = SHA256_E0(A) + SHA256_Maj(A, B, C);
            H = G, G = F, F = E, E = D + T1, D = C, C = B, B = A, A = T1 + T2;
        }
        H0 += A, H1 += B, H2 += C, H3 += D, H4 += E, H5 += F, H6 += G, H7 += H;
    }
    free(pp - l);
    sprintf(sha256, "%08X%08X%08X%08X%08X%08X%08X%08X", H0, H1, H2, H3, H4, H5, H6, H7);
    return sha256;
}
char* FileSHA256(const char* file, char* sha256){
 
    FILE* fh;
    char* addlp, T[64];
    long addlsize, j, W[64], T1, T2, A, B, C, D, E, F, G, H, H0, H1, H2, H3, H4, H5, H6, H7;
    long long length, i, cpys;
    void *pp, *ppend;
    H0 = 0x6a09e667, H1 = 0xbb67ae85, H2 = 0x3c6ef372, H3 = 0xa54ff53a;
    H4 = 0x510e527f, H5 = 0x9b05688c, H6 = 0x1f83d9ab, H7 = 0x5be0cd19;
    long K[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2,
    };
    fh = fopen(file, "rb");
    fseek(fh, 0, SEEK_END);
    length = _ftelli64(fh);
    addlsize = (56 - length % 64 > 0) ? (64) : (128);
    if (!(addlp = (char*)malloc(addlsize))) return 0;
    cpys = ((length - (56 - length % 64)) > 0) ? (length - length % 64) : (0);
    j = (long)(length - cpys);
    if (!(pp = (char*)malloc(j))) return 0;
    fseek(fh, -j, SEEK_END);
    fread(pp, 1, j, fh);
    for (i = 0; i < j; addlp[i + 3 - 2 * (i % 4)] = ((char*)pp)[i], i++);
    free(pp);
    for (addlp[i + 3 - 2 * (i % 4)] = 128, i++; i < addlsize; addlp[i + 3 - 2 * (i % 4)] = 0, i++);
    *((long*)(addlp + addlsize - 4)) = length << 3;
    *((long*)(addlp + addlsize - 8)) = length >> 29;
    for (rewind(fh); 64 == fread(W, 1, 64, fh);){
        for (i = 0; i < 64; T[i + 3 - 2 * (i % 4)] = ((char*)W)[i], i++);
        for (i = 0; i < 16; W[i] = ((long*)T)[i], i++);
        for (i = 16; i < 64; W[i] = (SHA256_Q1(W[i - 2]) + W[i - 7] + SHA256_Q0(W[i - 15]) + W[i - 16]), i++);
        A = H0, B = H1, C = H2, D = H3, E = H4, F = H5, G = H6, H = H7;
        for (i = 0; i < 64; i++){
            T1 = H + SHA256_E1(E) + SHA256_Ch(E, F, G) + K[i] + W[i];
            T2 = SHA256_E0(A) + SHA256_Maj(A, B, C);
            H = G, G = F, F = E, E = D + T1, D = C, C = B, B = A, A = T1 + T2;
        }
        H0 += A, H1 += B, H2 += C, H3 += D, H4 += E, H5 += F, H6 += G, H7 += H;
    }
    for (pp = addlp, ppend = addlp + addlsize; pp < ppend; pp = (long*)pp + 16){
        for (i = 0; i < 16; W[i] = ((long*)pp)[i], i++);
        for (i = 16; i < 64; W[i] = (SHA256_Q1(W[i - 2]) + W[i - 7] + SHA256_Q0(W[i - 15]) + W[i - 16]), i++);
        A = H0, B = H1, C = H2, D = H3, E = H4, F = H5, G = H6, H = H7;
        for (i = 0; i < 64; i++){
            T1 = H + SHA256_E1(E) + SHA256_Ch(E, F, G) + K[i] + W[i];
            T2 = SHA256_E0(A) + SHA256_Maj(A, B, C);
            H = G, G = F, F = E, E = D + T1, D = C, C = B, B = A, A = T1 + T2;
        }
        H0 += A, H1 += B, H2 += C, H3 += D, H4 += E, H5 += F, H6 += G, H7 += H;
    }
    free(addlp); fclose(fh);
    sprintf(sha256, "%08X%08X%08X%08X%08X%08X%08X%08X", H0, H1, H2, H3, H4, H5, H6, H7);
    return sha256;
}
 
char* StrSHA256(const char* str, long long length, char* sha256);
 
int main(void){
    char text[256];
    cout<<"请输入原文：\n" ;
    while(cin>>text) 
    {
        cout<<"请输入原文：\n" ;
        char sha256[256];
        StrSHA256(text,sizeof(text)-1,sha256);  // sizeof()包含了末尾的终止符'\0'故 -1
        cout<<"执行SHA-256算法后的结果如下：\n";
        puts(sha256);
        
    }
    
    system("pause");
    return 0;
}
```

参考资料：

百度百科

https://zh.wikipedia.org/wiki/SHA-2

https://www.cnblogs.com/chars/p/4983291.html

https://blog.csdn.net/u011583927/article/details/80905740 

https://blog.csdn.net/cbacq/article/details/78337127

<font color=#ff0000>(注：若有什么地方阐述有误，敬请指正。)</font>