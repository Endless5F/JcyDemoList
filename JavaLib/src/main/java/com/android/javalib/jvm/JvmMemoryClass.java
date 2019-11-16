package com.android.javalib.jvm;


import java.io.IOException;

/**
 * @author jiaochengyun
 * Java 虚拟机内存位置示例
 * <p>
 * 反编译字节码文件，查看结构：
 * 1. javac -encoding UTF-8 JvmMemoryClass.java
 * 2. javap -v JvmMemoryClass.class
 */
public class JvmMemoryClass {

    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final int finalInt = 1;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final float finalFloat = 1.0f;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final String finalStr = "abc";
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final Object finalObj = new Object();
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final String finalStringObj = new String();


    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public static int staticInt = 1;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public static float staticFloat = 1.0f;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public static String staticStr = "abc";
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public static Object staticObj = new Object();
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public static String staticStringObj = new String();

    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final static int finalStaticInt = 1;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final static float finalStaticFloat = 1.0f;
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final static String finalStaticStr = "abc";
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final static Object finalStaticObj = new Object();
    /**
     * Java 6：
     * Java 7：
     * Java 8：
     */
    public final static String finalStaticStringObj = new String();

    static {
        /*
         * 等同于静态的成员变量，只会初始化一次
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
        new JvmMemoryClass();
        staticMethodNoParameters();
        staticMethodHasParameters(1, "1");
    }

    /**
     * Java 6：
     * Java 7：
     * Java 8：
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
     * Java 6：
     * Java 7：
     * Java 8：
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
     * Java 6：
     * Java 7：
     * Java 8：
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
     * Java 6：
     * Java 7：
     * Java 8：
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
