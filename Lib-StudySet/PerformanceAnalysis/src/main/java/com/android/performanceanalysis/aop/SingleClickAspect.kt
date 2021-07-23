package com.android.performanceanalysis.aop

import android.icu.util.Calendar
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.android.performanceanalysis.BuildConfig
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

//使用@Aspect注解标示这是一个切面类
@Aspect
class SingleClickAspect {
    //@Pointcut来标识所要寻找的切点，就是我们定义的@ SingleClick注解
    @Pointcut("execution(@com.android.performanceanalysis.aop.SingleClick * *(..))")//方法切入点
    fun methodAnnotated() {

    }

    /**
     * joinPoint.proceed() 执行注解所标识的代码
     * @After 可以在方法前插入代码
     * @Before 可以在方法后插入代码
     * @Around 可以在方法前后各插入代码
     */
    @RequiresApi(Build.VERSION_CODES.N)
    @Around("methodAnnotated()")
    //@Throws这个注解不必在意，这个是kotlin的注解，标识该方法可以抛出异常
    @Throws(Throwable::class)
    fun aroundJoinPoint(joinPoint: ProceedingJoinPoint) {
        //获取系统当前时间
        val currentTime = Calendar.getInstance().timeInMillis
        //当前时间-上次记录时间>过滤的时间 过滤掉600毫秒内的连续点击
        //表示该方法可以执行
        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "currentTime:" + currentTime)
            }
            //将刚进入方法的时间赋值给上次点击时间
            lastClickTime = currentTime
            //执行原方法
            joinPoint.proceed()
        }
    }

    companion object {
        const val TAG = "SingleClickAspect"
        const val MIN_CLICK_DELAY_TIME = 600
        var lastClickTime = 0L
    }
}