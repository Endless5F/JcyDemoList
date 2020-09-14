package com.android.baselibrary.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;
import static java.lang.Thread.getDefaultUncaughtExceptionHandler;

/**
 * @date 2018/6/30
 * description
 * 描述 崩溃处理工具，Crash发生时拦截Crash信息，做自己的处理
 * 保存CPU数据时若不关闭进程，就算退出了所有Activity也会继续保存CPU数据
 * 因此必须在退出后结束进程
 * 创建人 jcy
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private static String DRINAME = null;
    public static final String CRASH_LOG_NAME = "crash_log.txt";
    public static final String CPU_MEM_INFO_NAME = "cpu_mem_info.txt";

    private static final String TAG = "CrashHandler";
    private static CrashHandler INSTANCE = new CrashHandler();
    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter_1 = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.CHINA);
    private DateFormat formatter_2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();
    private UncaughtExceptionHandler mDefaultHandler;
    private TopCommandThread mTopCommandThread;
    private Context mContext;
    private boolean saveExceptionAndCpu;
    private boolean autoRestart;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
        DRINAME = AppUtils.getApp().getExternalFilesDir("Crash").toString();
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context
     */
    public void init(Context context, boolean saveExceptionAndCpu, boolean autoRestart) {
            mContext = context.getApplicationContext();
            this.autoRestart = autoRestart;
            this.saveExceptionAndCpu = saveExceptionAndCpu;
            //更改默认的异常捕获handler
            mDefaultHandler = getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(this);
            if (saveExceptionAndCpu) {
                //需要保存异常与CPU信息
                //我们将文件保存在自己的Android data下面4.4以上是不需要读写权限的。

            }
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //打印Exception Log,使用原生的Log
        Log.e(TAG, "Thread: " + thread.getName(), ex);

        if (saveExceptionAndCpu) { // 需要写入文件
            // 中断mTopCommandThread
            mTopCommandThread.interrupt();
            // 保存日志文件
            String fileName = saveCrashInfo2File(ex);
        }

        if (autoRestart) { // 需要重启
            Class clazz = getBaseActivity();
            if (null != clazz) {
                // 若不是主界面崩溃，重启Activity
                Intent intent = new Intent(mContext, clazz);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        }
    }

    /**
     * 专门进行CPU 内存信息等收集的类
     */
    private class TopCommandThread extends Thread {

        private static final String COMMAND_TOP = "top -t -m 5 -s cpu";

        @Override
        public void run() {
            try {
                Process p = Runtime.getRuntime().exec(COMMAND_TOP);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while (!isInterrupted()) { //非阻塞过程中通过判断中断标志来退出
                    line = bufferedReader.readLine();
                    if (line != null && !"".equals(line)) {
                        if (line.contains(mContext.getPackageName())) {
                            writeStringToFile(DRINAME + CPU_MEM_INFO_NAME, line + "\r\n");
                        }
                    }
                }
                bufferedReader.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void updateFiles() {
            try {
                File dir = new File(DRINAME);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file_log = new File(DRINAME + CRASH_LOG_NAME);
                if (!file_log.exists()) {
                    file_log.createNewFile();
                }

                final File file_cpu = new File(DRINAME + CPU_MEM_INFO_NAME);
                if (!file_cpu.exists()) {
                    file_cpu.createNewFile();
                    writeHeaderInfo(file_cpu);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private void writeHeaderInfo(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String headerInfo = "PID   进程id\r\n" +
                "TID   线程id\r\n" +
                "USER   进程所有者的用户id\r\n" +
                "PR    优先级\r\n" +
                "NI    进程的优先级别数值\r\n" +
                "CPU%  当前瞬时CPU占用率\r\n" +
                "S     进程状态:D=不可中断的睡眠状态, R=运行, S=睡眠, T=跟踪/停止, Z=僵尸进程\r\n" +
                "VSS   Virtual Set Size  虚拟耗用内存（包含共享库占用的内存）\r\n" +
                "RSS   Resident Set Size 实际使用物理内存（包含共享库占用的内存）\r\n" +
                "PCY   调度策略优先级，SP_BACKGROUND/SP_FOREGROUND\r\n" +
                "Thread  线程的名称\r\n" +
                "Proc  进程的名称\r\n" +
                "Create time  " + formatter_1.format(new Date()) + "\r\n" +
                "-----------------------------------------------------------------\r\n" +
                "TIME             PID   TID USER     PR  NI CPU% S     VSS     RSS PCY Thread          Proc\r\n";
        writeStringToFile(file, headerInfo);
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG,"an error occured when collect package info", e);
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
                Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG,"an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    public String saveCrashInfo2File(Throwable ex) {
        //收集设备参数信息
        collectDeviceInfo(mContext);
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
                updateFiles();
                writeStringToFile(DRINAME + CRASH_LOG_NAME, sb.toString());
            return CRASH_LOG_NAME;
        } catch (Exception e) {
            Log.e(TAG,"an error occured while writing file...", e);
        }
        return null;
    }

    /**
     * The component launched as the first activity in the task.
     */
    public Class getBaseActivity() {
        ActivityManager manager = (ActivityManager) AppUtils.getApp().getSystemService(ACTIVITY_SERVICE);
        String className = manager.getRunningTasks(1).get(0).baseActivity.getClassName();
        if (className.equals(manager.getRunningTasks(1).get(0).topActivity.getClassName())) {
            //若主界面就崩溃，返回null防止循环重启
            return null;
        }
        Class cls = null;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return cls;
    }

    /**
     * 将字符串写入文件
     *
     * @param file 文件
     * @param data     将写入信息
     */
    public static final void writeStringToFile(File file, String data) {
        try {
            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(data);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 将字符串写入文件
     *
     * @param filePath 文件路径
     * @param data     将写入信息
     */
    public static final void writeStringToFile(String filePath, String data) {
        try {
            FileWriter fw = new FileWriter(filePath, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(data);
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}