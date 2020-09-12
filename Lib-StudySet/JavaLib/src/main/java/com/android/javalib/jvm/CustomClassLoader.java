package com.android.javalib.jvm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 创建自定义加载器，继承ClassLoader
 */
public class CustomClassLoader extends ClassLoader {
    private String classLoaderName;
    private String path;
    private final String fileExtension = ".class";

    public CustomClassLoader(String classLoaderName) {
        super();        //将系统类当做该类的父加载器
        this.classLoaderName = classLoaderName;
    }

    public CustomClassLoader(ClassLoader parent, String classLoaderName) {
        super(parent);      //显式指定该类的父加载器
        this.classLoaderName = classLoaderName;
    }

    public CustomClassLoader(ClassLoader parent) {
        super(parent);      //显式指定该类的父加载器
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public Class <?> findClass(String className) {
        System.out.println("calssName=" + className);
        className = className.replace(".", File.separator);
        byte[] data = loadClassData(className);
        // 解析字节数组为Class对象
        return defineClass(className, data, 0, data.length); //define方法为父类方法
    }

    private byte[] loadClassData(String name) {
        InputStream is = null;
        byte[] data = null;
        ByteArrayOutputStream baos = null;
        try {
            is = new FileInputStream(new File(this.path + name + this.fileExtension));
            baos = new ByteArrayOutputStream();
            int ch;
            while (-1 != (ch = is.read())) {
                baos.write(ch);
            }
            data = baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }
    }

    public static void main(String[] args) {
        //父亲是系统类加载器，根据父类委托机制，JvmMemoryClass由系统类加载器加载了
        CustomClassLoader loader1 = new CustomClassLoader("loader1");
        test(loader1);
    }

    public static void test(ClassLoader classLoader) {
        Class<?> clazz = null;
        try {
            clazz = classLoader.loadClass("com.android.javalib.jvm.JvmMemoryClass");
            //loadClass是父类方法，在方法内部调用findClass
            System.out.println(clazz.hashCode());
            Object object = clazz.newInstance();
            System.out.println(object);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}