package com.android.javalib.jvm;


import java.io.IOException;

/**
 * @author jiaochengyun
 * Java 虚拟机内存位置示例
 * <p>
 * 反编译字节码文件，查看结构：
 * 1. javac -encoding UTF-8 JvmMemoryClass.java
 * 2. javap -v JvmMemoryClass.class
 * <p>
 * Java虚拟机内存管理：
 * 1. 虚拟机栈：Stack Frame栈帧：入栈、出栈
 * 2. 程序计数器(Program Counter)：存储字节码执行到的位置
 * 3. 本地方法栈：主要用于执行本地方法
 * 4. 堆(Heap)：JVM管理的最大一块内存
 * 5. 方法区(Method Area)：存储元信息。永久代(Permanent Generation，此区域很少产生垃圾回收)Jdk1.7之前，一些人将方法区也称为永久代，并且1.7之前JVM确实也是这么做的。但是从Jdk1.8开始，已经彻底废弃了永久代，使用元空间(meta space)。
 * 6. 运行时常量池：方法区一部分，编译期间生成class文件时的一些字面量和符号引用等等，在class加载完成之后都会进入方法区的运行时常量池进行存放。
 * 7. 直接内存(Direct Memory)：不属于Java虚拟机内部，不占Java虚拟机内存，由操作系统直接分配和回收，从Jdk1.8开始的“元空间”就在直接内存中，而此部分大小，受限于本地内存限制。
 * 8. 字符串常量池：Class常量池是静态的，存在于.class文件中。运行时常量池是动态的，在.class文件被加载到方法区后生成。字符串常量池独立于常量池。
 * 【字符串常量池】独立于【运行时常量池】
 * 【字符串常量池】即【String Pool】实际是一种由C++实现的Map，结构上类似于Hashtable，区别在于其无法自动扩容
 * <p>
 * 在JDK1.6及更早版本中【String Pool】位于【方法区】
 * 在JDK1.7中【String Pool】位于【堆】
 * 在JDK1.8中【String Pool】仍位于【堆】
 * <p>
 * 在JDK1.7中【运行时常量池】位于【方法区】
 * 在JDK1.8中【运行时常量池】位于【元空间】
 * <p>
 * String的intern方法使用：一个初始为空的字符串池，它由类String独自维护。
 * 当调用 intern方法时，如果池已经包含一个等于此String对象的字符串（用equals(oject)方法确定），则返回池中的字符串。
 * 否则，将此String对象添加到池中，并返回此String对象的引用。 对于任意两个字符串s和t，当且仅当s.equals(t)为true时，s.instan() == t.instan才为true。
 * <p>
 * 注：对象的数据分为两部分：实例的真实数据(存在于堆中)和class类的元数据(存在于方法区中，只此一份，即无论该class的对象有几个，而元数据只此一份)
 */
public class JvmMemoryClass {

    /**
     * 编译期：finalInt(符号引用)和 1(字面量) 位于Class常量池
     * 运行期：finalInt(符号引用)和 1(字面量) 位于该类的运行时常量池
     * 类实例化：finalInt(符号引用)位于堆中，1(字面量) 位于该类的运行时常量池
     * Oracle的HotSpot虚拟机，堆中每个对象存储分两部分：一部分为该对象的实际数据，另一部分为指向方法区的指针(即，类实例化中的字面量形式)
     */
    public final int finalInt = 1;
    /**
     * 同上
     */
    public final float finalFloat = 1.0f;
    /**
     * 编译期：finalStr(符号引用)和 "abc"(字面量) 位于Class常量池
     * 运行期：finalStr(符号引用)和 "abc"(字面量) 位于该类的字符串常量池
     * 类实例化：finalStr(符号引用)位于堆中，"abc"(字面量) 位于该类的字符串常量池
     */
    public final String finalStr = "abc";
    /**
     * 编译期：finalObj(符号引用)和 Ljava/lang/Object(字面量) 位于Class常量池
     * 运行期：finalObj(符号引用)和 Ljava/lang/Object(字面量) 位于该类的字符串常量池
     * 类实例化：finalObj(符号引用)位于堆中，Ljava/lang/Object(字面量) 位于堆中
     */
    public final Object finalObj = new Object();
    /**
     * 同上
     */
    public final String finalStringObj = new String();


    /**
     * 静态变量类在准备阶段附默认值，在类初始阶段附原始值
     */
    public static int staticInt = 1;
    /**
     * 同上
     */
    public static float staticFloat = 1.0f;
    /**
     * 类实例化：staticStr(符号引用)位于方法区，"abc" 位于字符串常量池
     */
    public static String staticStr = "abc";
    /**
     * 类实例化：staticObj(符号引用)位于方法区，Ljava/lang/Object 位于堆中
     */
    public static Object staticObj = new Object();
    /**
     * 类实例化：staticStringObj(符号引用)位于方法区，Ljava/lang/String 位于堆中，"str"位于字符串常量池
     */
    public static String staticStringObj = new String("str");

    /**
     * 类实例化：位于方法区
     */
    public final static int finalStaticInt = 1;
    /**
     * 同上
     */
    public final static float finalStaticFloat = 1.0f;
    /**
     * 类实例化：finalStaticStr(符号引用)位于方法区，"abc"位于字符串常量池
     */
    public final static String finalStaticStr = "abc";
    /**
     * 类实例化：finalStaticObj(符号引用)位于方法区，Ljava/lang/Object 位于堆中
     */
    public final static Object finalStaticObj = new Object();
    /**
     * 类实例化：finalStaticStringObj(符号引用)位于方法区，Ljava/lang/String 位于堆中
     */
    public final static String finalStaticStringObj = new String();

    static {
        /*
         * 等同于静态的成员变量，只会初始化一次，方法区
         */
        int staticBlockInt = 2;
        String staticBlockString = "123";
        String staticBlockStringObj = new String("456");
    }

    {
        /*
         * 等同于在构造方法中初始化一致，每次初始化对象实例时，都会先于构造方法初始化
         */
        int blockInt = 2;
        String blockString = "567";
        String blockStringObj = new String("789");
        System.out.println(blockInt);
        System.out.println(blockString);
        System.out.println(blockStringObj);
    }

    public JvmMemoryClass() {
        System.out.println("构造方法");
    }

    public static void main(String[] args) throws IOException {
//        new JvmMemoryClass();
//        staticMethodNoParameters();
//        staticMethodHasParameters(1, "1");
        System.out.println(staticStr);
    }

    /**
     * 方法代码(Code)位于方法区(各种指令)
     * 局部变量符号引用和基础数据类型字面量位于局部变量表
     * 对象位于堆中
     * new String().intern() 位于字符串常量池中
     */
    public void methodNoParameters() {
        int localInt = 1;
        final int localFinalInt = 1;
        String localStr = "";
        final String localFinalStr = "";
        Object localObj = new Object();
        final Object localFinalObj = new Object();
        String localStringObj = new String();
        final String localFinalStringObj = new String();
        String localStringObjIntern = new String().intern();
        final String localFinalStringObjIntern = new String().intern();

        System.out.println(localInt);
        System.out.println(localFinalInt);
        System.out.println(localStr);
        System.out.println(localFinalStr);
        System.out.println(localObj);
        System.out.println(localFinalObj);
        System.out.println(localStringObj);
        System.out.println(localFinalStringObj);
        System.out.println(localStringObjIntern);
        System.out.println(localFinalStringObjIntern);
    }

    /**
     * 参数 a和b 符号引用位于局部变量表中，a的字面量在局部变量表中，b的字面量在字符串常量池中
     */
    public void methodHasParameters(int a, String b) {
        System.out.println(finalInt);
        System.out.println(finalFloat);
        System.out.println(finalStr);
        System.out.println(finalObj);
        System.out.println(finalStringObj);
        System.out.println(staticInt);
        System.out.println(staticFloat);
        System.out.println(staticStr);
        System.out.println(staticObj);
        System.out.println(staticStringObj);
        System.out.println(finalStaticInt);
        System.out.println(finalStaticFloat);
        System.out.println(finalStaticStr);
        System.out.println(finalStaticObj);
        System.out.println(finalStaticStringObj);
    }

    /**
     * 方法区
     */
    public static void staticMethodNoParameters() {
        int localInt = 1;
        final int localFinalInt = 1;
        String localStr = "";
        final String localFinalStr = "";
        Object localObj = new Object();
        final Object localFinalObj = new Object();
        String localStringObj = new String();
        final String localFinalStringObj = new String();
        String localStringObjIntern = new String().intern();
        final String localFinalStringObjIntern = new String().intern();

        System.out.println(localInt);
        System.out.println(localFinalInt);
        System.out.println(localStr);
        System.out.println(localFinalStr);
        System.out.println(localObj);
        System.out.println(localFinalObj);
        System.out.println(localStringObj);
        System.out.println(localFinalStringObj);
        System.out.println(localStringObjIntern);
        System.out.println(localFinalStringObjIntern);
    }

    /**
     * 参数方法区
     */
    public static void staticMethodHasParameters(int a, String b) {
        System.out.println(staticInt);
        System.out.println(staticFloat);
        System.out.println(staticStr);
        System.out.println(staticObj);
        System.out.println(staticStringObj);
        System.out.println(finalStaticInt);
        System.out.println(finalStaticFloat);
        System.out.println(finalStaticStr);
        System.out.println(finalStaticObj);
        System.out.println(finalStaticStringObj);
    }
}
