## 一、Android绘制原理及工具选择
### 绘制原理
1. 硬件

        CPU负责计算显示内容（视图的创建，布局计算，图片解码，文本绘制等）
        GPU负责棚格化（UI元素绘制到屏幕上，也就是将一些组件，如button,bitmap拆分成不同的像素进行显示，然后完成绘制，比较耗时）
2. 原则：16ms发出VSync信号触发UI渲染，大多数的Android设备屏幕刷新帧率：60Hz
### 优化工具
1. Systrace

        关注Frames
        正常：绿色圆点
        丢帧：黄色或红色
        Alerts栏（自动分析标注异常问题的条目）
    参考文档：https://www.jianshu.com/p/f83d84dcd0b8 （Android Systrace使用介绍）
2. Layout Inspector：使用详见——PerformanceAnalysis/性能分析/布局优化Layout Inspector工具的使用.png
3. Choreographer：获取fps，线上使用，具备实时性(使用条件：Api 16之后)。使用方法：Choreographer.getInstance().postFrameCallback();

        private var mStartFrameTime: Long = 0
        private var mFrameCount = 0
        private val MONITOR_INTERVAL = 160L //单次计算FPS使用160毫秒
        private val MONITOR_INTERVAL_NANOS = MONITOR_INTERVAL * 1000L * 1000L
        private val MAX_INTERVAL = 1000L //设置计算fps的单位时间间隔1000ms,即fps/s;

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_layout)
            getFPS()
        }

        @SuppressLint("ObsoleteSdkInt")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private fun getFPS() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                return
            }
            Choreographer.getInstance().postFrameCallback(object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    if (mStartFrameTime == 0L) {
                        mStartFrameTime = frameTimeNanos
                    }
                    val interval = frameTimeNanos - mStartFrameTime
                    if (interval > MONITOR_INTERVAL_NANOS) {
                        val fps = (mFrameCount.toLong() * 1000L * 1000L).toDouble() / interval * MAX_INTERVAL
                        Log.e("fps", "fps = $fps")
                        mFrameCount = 0
                        mStartFrameTime = 0
                    } else {
                        ++mFrameCount
                    }

                    Choreographer.getInstance().postFrameCallback(this)
                }
            })
        }
    具体使用demo详见：com.android.performanceanalysis.activity.LayoutActivity
## 二、Android布局加载原理
1. 布局加载源码追踪：setContentView-->LayoutInflater-->inflate-->getLayout-->createViewFromTag-->Factory-->createView-->反射

    性能瓶颈：布局文件的解析(是通过XmlResourceParser进行的，属于IO操作)、创建view对象(反射)
2. LayoutInflater.Factory：LayoutInflater包含Factory和Factory2两个，Factory和Factory2:Factory2继承自Factory，并且多了一个参数parent

        // setContentView执行流程中必经的部分代码：
            View view;
            if (mFactory2 != null) {
                view = mFactory2.onCreateView(parent, name, context, attrs);
            } else if (mFactory != null) {
                view = mFactory.onCreateView(name, context, attrs);
            } else {
                view = null;
            }

            if (view == null && mPrivateFactory != null) {
                view = mPrivateFactory.onCreateView(parent, name, context, attrs);
            }

            if (view == null) {
                final Object lastContext = mConstructorArgs[0];
                mConstructorArgs[0] = context;
                try {
                    if (-1 == name.indexOf('.')) {
                        view = onCreateView(parent, name, attrs);
                    } else {
                        view = createView(name, null, attrs);
                    }
                } finally {
                    mConstructorArgs[0] = lastContext;
                }
            }
        通过源码中可以了解到setContentView的过程最终是通过LayoutInflater完成的，而在创建布局(onCreateView)则是会首先判断LayoutInflater.Factory2和LayoutInflater.Factory是否为空的逻辑。
    优化说明：

        LayoutInflater可看作CreateView的一个Hook(挂钩，可通过上面部分源码中分析得来)
        可定制CreateView的过程：例如全局替换自定义Textview等
        示例代码：带有Compat表示的是兼容类，有比较好的兼容性
            LayoutInflaterCompat.setFactory2(LayoutInflater.from(this), new LayoutInflater.Factory2() {
                @Override
                public View onCreateView(String parent, Context context, AttributeSet attrs) {
                    return null;
                }

                @Override
                public View onCreateView(View parent, String name, Context context, AttributeSet attributeSet) {

                    AppCompatDelegate delegate = getDelegate();
                    View view = delegate.createView(parent, name, context, attributeSet);
                    if (view != null && (view instanceof TextView)) {
                        ((TextView) view).setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                    return view;
                }
            });
## 三、优雅获取界面布局耗时
1. 常规方式：覆写方法、手动埋点，获取每个界面加载耗时
2. AOP/ArtHook实现

        // AOP的实现方式：
            // 切面点:Activity的setContentView
            @Around("execution(* android.app.Activity.setContentView(..))")
            public void getSetContentViewTime(ProceedingJoinPoint joinPoint) {
                Signature signature = joinPoint.getSignature();
                String name = signature.toShortString();
                long time = System.currentTimeMillis();
                try {
                    joinPoint.proceed();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
            }
    ArtHook实现：参考App内存优化的ARTHook优雅检测不合理图片的实现方式。
3. 获取任一控件的耗时：低侵入性、使用LayoutInflater.Factory

        // 带有Compat表示的是兼容类，有比较好的兼容性
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new LayoutInflater.Factory2() {
            @Override
            public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                //替换示例：将布局中的某一个控件替换成我们自定义的控件（如Textview）伪代码如下
                if (TextUtils.equals(name, "TextView")) {
                    // 生成自定义TextView，然后将Textview return回去
                }

                //每个控件的耗时    可以将此方法放入base类onCreate中  必须在super.onCreate(savedInstanceState)之前设置才有效
                long time = System.currentTimeMillis();
                View view = getDelegate().createView(parent, name, context, attrs);
                LogUtils.i(name + " cost " + (System.currentTimeMillis() - time));
                return view;
            }

            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                return null;
            }
        });
## 四、布局加载优化实战
1. 背景介绍：布局文件读取慢(IO过程)、创建View慢(反射，比new慢3倍)
2. 解决方案：

    * 根本性：Java代码写布局，本质上解决了xml上的性能问题，但是不便于开发、可维护性差
    * 缓解方案：AsyncLayoutInflater——异步Inflater，先在WorkThread加载布局，然后回调主线程。节约主线程时间

            // setContentView(R.layout.activity_layout);
            new AsyncLayoutInflater(MainActivity.this).inflate(R.layout.activity_layout
                    ,null, new AsyncLayoutInflater.OnInflateFinishedListener() {
                @Override
                public void onInflateFinished(@NonNull View view, int i, @Nullable ViewGroup viewGroup) {

                }
            });
    总结：

        侧面缓解卡顿
        使用了AsyncLayoutInflater就失去了向下兼容的特性，不能设置 LayoutInflater.Factory(自定义解决)
        注意view中不能有依赖主线程的操作
3. X2C：保留xml优点，解决性能问题（开发人员写xml,加载java代码），原理：通过APT编译期翻译xml为java代码

        依赖：
        annotationProcessor 'com.zhangyue.we:x2c-apt:1.1.2'
        implementation 'com.zhangyue.we:x2c-lib:1.0.6'

        使用方式：
        @Xml(layouts = "activity_main")//添加Xml注解标明使用布局
        public class MainActivity extends AppCompatActivity {

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                setTheme(R.style.AppTheme);
                super.onCreate(savedInstanceState);
        //        setContentView(R.layout.activity_main);
                //使用X2C.setContentView，传入上下文和布局文件
                X2C.setContentView(MainActivity.this, R.layout.activity_main);
            }
        }
    x2c问题：部分属性Java不支持、失去了系统的兼容（AppCompat）如：TextView、ImageView系统在高版本向低版本有兼容。解决办法：修改x2c源码，如发现是TextView直接new AppCompatTextView。

    参考文档：https://github.com/iReaderAndroid/X2C （github项目源码）
## 五、视图绘制优化实战
1. 优化布局层级及复杂度

        布局绘制回顾：
            测量：确定大小(遍历视图树，确认viewgroup和view元素的大小)
            布局：确定位置(遍历视图树，每个viewgroup根据测量阶段的大小确认自己的位置)
            绘制：绘制视图(视图树中的每个对象都会创建一个canvas对象，向GPU发送绘制命令)

        性能瓶颈：
            每个阶段耗时
            自顶向下的遍历
            触发多次测量或者绘制(如嵌套RelativeLayout)

        优化：减少布局层级和复杂度
        准则：减少view树层级、宽而浅，避免窄而深
        ConstraintLayout：实现几乎完全扁平化布局、构建复杂布局性能更高、具有RelativeLayout和LinearLayout特性
        其他：不嵌套使用elativeLayout、不在嵌套LinearLayout中使用weight、merge标签可减少一个层级（只能用于根view）
2. 避免过度绘制(可打开 开发者选项--调试GPU过度绘制 选项查看)：一个像素最好只被绘制一次、调试GPU过度绘制、蓝色可接受

        方法：
            去掉多余背景色，减少复杂shape使用
            避免层级叠加(控件不要重叠)
            自定义view使用clipRect屏蔽被遮盖View绘制
3. 其他

        ViewStub:高效占位符、延迟初始化(没有测量和布局的过程)
        onDraw中避免：创建大量对象、耗时操作
        TextView优化：参考文档：https://blog.csdn.net/self_study/article/details/42297573 （android textview settext卡顿深层次原因）
