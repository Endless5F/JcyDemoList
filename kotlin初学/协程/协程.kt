package main.kotlin.协程

/**
 * Created by Administrator on 2018/4/28.
 * 协程 Coroutine  ：各个子任务协同合作
 *  协作程序，解决异步问题，非抢占式
 *  应用层完成调度
 * 解决问题：
 *  异步代码像同步代码一样直观
 *  简化异步代码异常处理
 *  轻量级的并发方案
 *
 *  协程基本API
 *      createCoroutine：创建协程
 *      startCoroutine:启动协程，若没创建，则先创建协程
 *      suspendCoroutine：挂起协程，真正执行耗时操作的方法
 *
 *      Continuation 接口：延续，继续执行方法
 *        （运行控制类，负责结果和异常的返回）
 *          resume --》 正常继续执行，传入结果
 *          resumeWithException --》 异常继续执行，需要try catach捕获，传入异常
 *      CoroutineContext 接口 ：协程上下文 ，携带资源，配合ContinuationIntercepter篡改continuation
 *        （运行上下文，资源持有，运行调度）
 *      ContinuationIntercepter 接口
 *        (协程控制拦截器   可用来处理协程调度)
 */
