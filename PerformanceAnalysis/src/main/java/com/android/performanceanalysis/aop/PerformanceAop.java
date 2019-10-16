package com.android.performanceanalysis.aop;

import android.util.Log;

import com.android.performanceanalysis.utils.LogUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * AOP术语：
 * Advice：通知、增强处理，就是你想要的功能，也就是上面说的日志、耗时计算等。
 * <p>
 * JoinPoint：连接点，允许你通知（Advice）的地方，那可就真多了，基本每个方法的前、后（两者都有也行），或抛出异常是时都可以是连接点。
 * AspectJ还可以让你在构造器或属性注入时都行，不过一般情况下不会这么做，只要记住，和方法有关的前前后后都是连接点。
 * <p>
 * Pointcut：切入点，上面说的连接点的基础上，来定义切入点，你的一个类里，有15个方法，
 * 那就有十几个连接点了对吧，但是你并不想在所有方法附件都使用通知（使用叫织入，下面再说），你只是想让其中几个，
 * 在调用这几个方法之前、之后或者抛出异常时干点什么，那么就用切入点来定义这几个方法，让切点来筛选连接点，选中那几个你想要的方法。
 * <p>
 * Aspect：切面，切面是通知和切入点的结合。现在发现了吧，没连接点什么事，
 * 连接点就是为了让你好理解切点搞出来的，明白这个概念就行了。通知说明了干什么和什么时候干
 * （什么时候通过before，after，around等AOP注解就能知道），而切入点说明了在哪干（指定到底是哪个方法），这就是一个完整的切面定义。
 * <p>
 * 织入（weaving）： 把切面应用到目标对象来创建新的代理对象的过程。
 * <p>
 * <p>
 * <p>
 * AOP 注解
 *
 * @Aspect：声明切面，标记类
 * @Pointcut(切点表达式)：定义切点，标记方法
 * @Before(切点表达式)：前置通知，切点之前执行
 * @Around(切点表达式)：环绕通知，切点前后执行
 * @After(切点表达式)：后置通知，切点之后执行
 * @AfterReturning(切点表达式)：返回通知，切点方法返回结果之后执行
 * @AfterThrowing(切点表达式)：异常通知，切点抛出异常时执行
 */

@Aspect
public class PerformanceAop {

    /**
     * advice：是切面功能的实现，它是切点的真正执行的地方。
     * 比如像写日志到一个文件中，advice（包括：before、after、around等）在jointpoint处插入代码到应用程序中。
     *
     * @Before 这是一个advice
     * execution 这是一个Join Point
     * (* android.app.Activity.on**(..)" 这是一个正则表达式
     * 第一个*表示返回值(任意类型) - 方法的路径(通过正则匹配) - ()表示方法的参数，可以指定类型
     * onActivityMethodBefore 表示切入点逻辑执行的方法（aop要插入的代码）
     * <p>
     * ()方法参数说明：
     * ()	表示方法没有任何参数
     * (..)	表示匹配接受任意个参数的方法
     * (..,java.lang.String)	表示匹配接受java.lang.String类型的参数结束，且其前边可以接受有任意个参数的方法
     * (java.lang.String,..)	表示匹配接受java.lang.String类型的参数开始，且其后边可以接受任意个参数的方法
     * (*,java.lang.String)	表示匹配接受java.lang.String类型的参数结束，且其前边接受有一个任意类型参数的方法
     */
    @Before("execution(* com.android.performanceanalysis.activity.MainActivity.on**(..))")
    public void onActivityMethodBefore(JoinPoint joinPoint) throws Throwable {
        Signature signature = joinPoint.getSignature();
        String name = signature.toShortString();
        LogUtils.d("onActivityMethodBefore " + name);
    }

    @Before("execution(* android.app.Activity.on**(..))")
    public void onResumeMethod(JoinPoint joinPoint) throws Throwable {
        Log.i("helloAOP", "aspect:::" + joinPoint.getSignature());
    }


    @Around("call(* com.android.performanceanalysis.LaunchApplication.init**(..))")
    public void getTime(ProceedingJoinPoint joinPoint) {
        // 获取切点处的签名
        Signature signature = joinPoint.getSignature();
        // 获取切点处的方法名
        String name = signature.toShortString();
        long time = System.currentTimeMillis();
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
    }
}
