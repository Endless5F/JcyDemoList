package com.android.performanceanalysis.utils;

import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/28.
 */

public class FileUtils {
    //获取sd卡绝对路径
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static File file = new File(path, "aabbcc.txt");
    public static String sp = "------";

    public static void writeup(String str, boolean b) {

        if (!file.isDirectory()) {
            //内存输入字节流
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(str.getBytes());
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file, b);
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = byteArrayInputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, len);
                }
                byteArrayInputStream.close();
                fileOutputStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 一个蠢萌蠢萌的读取文本的方法
     */
    public static List<String> readstr(File file) {
        ArrayList<String> strings = new ArrayList<>();
        if (file.exists() && !file.isDirectory()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
//                int len = 0;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
                String read = "";
                while ((read = bufferedReader.readLine()) != null) {
                    strings.add(read);
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return strings;
    }

    /***
     * 分割的方法
     */
    public static String[] getstr(String str) {
        //123213
        String[] strings = new String[2];
        if (str.length() > sp.length() + 1) {
            int i = str.indexOf("-");
            if (i != -1) {
                String substring = str.substring(0, i);
                strings[0] = substring;
            }
            int i1 = str.lastIndexOf("-");
            if (i1 != -1) {
                String substring = str.substring(i1 + 1, str.length());
                strings[1] = substring;
            }
        }
        return strings;
    }

    public static void createFileDir(File file){
        if (file.exists()){
            return;
        }else {
            File dir = file;
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * 读取文件的内容
     * 读取指定文件的内容
     * @param path 为要读取文件的绝对路径
     * @return 以行读取文件后的内容。
     * @since  1.0
     */
    public static final String getFileContent(String path) throws IOException
    {
        String filecontent = "";
        try {
            File f = new File(path);
            if (f.exists()) {
                FileReader fr = new FileReader(path);
                BufferedReader br = new BufferedReader(fr); //建立BufferedReader对象，并实例化为br
                String line = br.readLine(); //从文件读取一行字符串
                //判断读取到的字符串是否不为空
                while (line != null) {
                    filecontent += line + "\n";
                    line = br.readLine(); //从文件中继续读取一行数据
                }
                br.close(); //关闭BufferedReader对象
                fr.close(); //关闭文件
            }

        }
        catch (IOException e) {
            throw e;
        }
        return filecontent;
    }

    /**
     * 清空指定目录中的文件。
     * 这个方法将尽可能删除所有的文件，但是只要有一个文件没有被删除都会返回false。
     * 另外这个方法不会迭代删除，即不会删除子目录及其内容。
     * @param directory 要清空的目录
     * @return 目录下的所有文件都被成功删除时返回true，否则返回false.
     * @since  1.0
     */
    public static boolean emptyDirectory(File directory) {
        boolean result = true;
        File[] entries = directory.listFiles();
        for (int i = 0; i < entries.length; i++) {
            if (!entries[i].delete()) {
                result = false;
            }
        }
        return result;
    }

    /**
     * 清空指定目录中的文件。
     * 这个方法将尽可能删除所有的文件，但是只要有一个文件没有被删除都会返回false。
     * 另外这个方法不会迭代删除，即不会删除子目录及其内容。
     * @param directoryName 要清空的目录的目录名
     * @return 目录下的所有文件都被成功删除时返回true，否则返回false。
     * @since  1.0
     */
    public static boolean emptyDirectory(String directoryName) {
        File dir = new File(directoryName);
        return emptyDirectory(dir);
    }

    /**
     * 删除指定目录及其中的所有内容。
     * @param dirName 要删除的目录的目录名
     * @return 删除成功时返回true，否则返回false。
     * @since  1.0
     */
    public static boolean deleteDirectory(String dirName) {
        return deleteDirectory(new File(dirName));
    }

    /**
     * 删除指定目录及其中的所有内容。
     * @param dir 要删除的目录
     * @return 删除成功时返回true，否则返回false。
     * @since  1.0
     */
    public static boolean deleteDirectory(File dir) {
        if ( (dir == null) || !dir.isDirectory()) {
            throw new IllegalArgumentException("Argument " + dir +
                    " is not a directory. ");
        }
        File[] entries = dir.listFiles();
        int sz = entries.length;
        for (int i = 0; i < sz; i++) {
            if (entries[i].isDirectory()) {
                if (!deleteDirectory(entries[i])) {
                    return false;
                }
            }
            else {
                if (!entries[i].delete()) {
                    return false;
                }
            }
        }
        if (!dir.delete()) {
            return false;
        }
        return true;
    }

    /**
     * 得到文件的类型。
     * 实际上就是得到文件名中最后一个“.”后面的部分。
     * @param fileName 文件名
     * @return 文件名中的类型部分
     * @since  1.0
     */
    public static String getTypePart(String fileName) {
        int point = fileName.lastIndexOf('.');
        int length = fileName.length();
        if (point == -1 || point == length - 1) {
            return "";
        }
        else {
            return fileName.substring(point + 1, length);
        }
    }

    /**
     * 得到文件的类型。
     * 实际上就是得到文件名中最后一个“.”后面的部分。
     * @param file 文件
     * @return 文件名中的类型部分
     * @since  1.0
     */
    public static String getFileType(File file) {
        return getTypePart(file.getName());
    }

    /**
     * InputStrem 转byte[]
     *
     * @param in
     * @return
     */
    public static byte[] readStreamToBytes(InputStream in) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024 * 8];
        int length = -1;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        out.flush();
        byte[] result = out.toByteArray();
        in.close();
        out.close();
        return result;
    }

    /**
     * 写入文件
     *
     * @param in
     * @param file
     */
    public static void writeFile(InputStream in, File file) throws IOException {
        BufferedInputStream bis=new BufferedInputStream(in);
        BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(file));
        int by=0;
        while((by=bis.read())!=-1){
            bos.write(by);
        }
        close(bis,bos);
    }

    /**
     * 拷贝文件
     * 如果目标文件不存在将会自动创建
     *
     * @param srcFile  原文件
     * @param saveFile 目标文件
     * @return 是否拷贝成功
     */
    public static boolean copyFile(final File srcFile, final File saveFile) {
        File parentFile = saveFile.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs())
                return false;
        }
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            outputStream = new BufferedOutputStream(new FileOutputStream(saveFile));
            byte[] buffer = new byte[1024 * 4];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            close(inputStream, outputStream);
        }
        return true;
    }

    /**
     * 修改文件的最后访问时间。
     * 如果文件不存在则创建该文件。
     * <b>目前这个方法的行为方式还不稳定，主要是方法有些信息输出，这些信息输出是否保留还在考虑中。</b>
     * @param file 需要修改最后访问时间的文件。
     * @since  1.0
     */
    public static void touch(File file) {
        long currentTime = System.currentTimeMillis();
        if (!file.exists()) {
            System.err.println("file not found:" + file.getName());
            System.err.println("Create a new file:" + file.getName());
            try {
                if (file.createNewFile()) {
                    System.out.println("Succeeded!");
                }
                else {
                    System.err.println("Create file failed!");
                }
            }
            catch (IOException e) {
                System.err.println("Create file failed!");
                e.printStackTrace();
            }
        }
        boolean result = file.setLastModified(currentTime);
        if (!result) {
            System.err.println("touch failed: " + file.getName());
        }
    }

    /**
     * 修改文件的最后访问时间。
     * 如果文件不存在则创建该文件。
     * <b>目前这个方法的行为方式还不稳定，主要是方法有些信息输出，这些信息输出是否保留还在考虑中。</b>
     * @param fileName 需要修改最后访问时间的文件的文件名。
     * @since  1.0
     */
    public static void touch(String fileName) {
        File file = new File(fileName);
        touch(file);
    }

    /**
     * 修改文件的最后访问时间。
     * 如果文件不存在则创建该文件。
     * <b>目前这个方法的行为方式还不稳定，主要是方法有些信息输出，这些信息输出是否保留还在考虑中。</b>
     * @param files 需要修改最后访问时间的文件数组。
     * @since  1.0
     */
    public static void touch(File[] files) {
        for (int i = 0; i < files.length; i++) {
            touch(files[i]);
        }
    }

    /**
     * 修改文件的最后访问时间。
     * 如果文件不存在则创建该文件。
     * <b>目前这个方法的行为方式还不稳定，主要是方法有些信息输出，这些信息输出是否保留还在考虑中。</b>
     * @param fileNames 需要修改最后访问时间的文件名数组。
     * @since  1.0
     */
    public static void touch(String[] fileNames) {
        File[] files = new File[fileNames.length];
        for (int i = 0; i < fileNames.length; i++) {
            files[i] = new File(fileNames[i]);
        }
        touch(files);
    }

    /**
     * 关闭流
     *
     * @param closeables Closeable
     */
    @SuppressWarnings("WeakerAccess")
    public static void close(Closeable... closeables) {
        if (closeables == null || closeables.length == 0)
            return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
