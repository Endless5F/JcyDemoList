# View事件体系

## View的基础知识
### View的位置参数
> View的位置主要由它的四个顶点来决定，分别对应于View的四个属性：top、left、right，bottom，其中top是左上角纵坐标，left是左上角横坐标，right是右下角横坐标，bottom是有下角纵坐标。需要注意的是，这些坐标都是相对于View的父容器来说的，因此它是一种相对坐标，View的坐标和父容器的关系如下图所示。在Android中，x轴和y轴的正别为右和下，这点不难理解，不仅仅是Android，大部分显示系统都是按照这个标准来定义坐标系的。

![20161025223848935.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579575115822-1b3083bd-ed5a-4327-b39c-f5799e03f3a1.png#align=left&display=inline&height=805&name=20161025223848935.png&originHeight=805&originWidth=994&size=15867&status=done&style=none&width=994)

```
宽：width= right- left
高：height = bottom - top
```

> 从Android3.0开始，View增加了额外的几个参数，**x,y，translationX,translationY**,其中x，y是View左上角的图标，而translationX,translationY是左上角相对父容器的便宜角量，这几个参数也是相对于父容器的坐标，并且translationX,translationY的默认值野0；和View的四个基本位置参数一样。

```
View也为我们提供了get/set方法这几个换算关系：
x = left + translationX
y = top + translationY
需要注意的是,View在平移的过程中，top和left表示在原始左上角的位置信息，
其值并不会发生什么，此时发生改变的是x,y,translationX,translationY,这四个参数
```
### MotionEvent和TouchSlop
#### MotionEvent

- ACTION_DOWN一手指刚接触屏幕
- ACTION_MOVE一—手指在屏幕上移动
- ACTION_UP——手机从屏幕上松开的一瞬间

系统提供了两组方法：getX/gety和 getRawX/getRawY。它们的区别其实很简单，getX/getY返回的是相对于当前View左上角的x和y坐标，而geiRawX/getRawY返回的是相对于手机屏幕左上角的x和y坐标。
#### TouchSlop
TouchSlop是系统所能识别出的被认为是滑动的最小距离，换句话说，当手指在屏慕上滑动时，如果两次滑动之间的距离小于这个常量，那么系统就不认为你是在进行滑动操作，原理很简单，滑动的距离太短，系统不认为他在滑动，这是一个常量，和设备无关，在不同的设备下这个值可能不同，通过如下方式即可获取这个常量：ViewConfigurtion.get(getContext()).getScaledTouchSlop,这个常量有什么意义呢?当我们在处理滑动时，可以利用这个常量来做一些过滤，比如当两次滑动事件的滑动距离小于这个值，我们就可以认为未达到常动距离的临界值，因此就可以认为它们不是滑动，这样做可以有更好的用户体验在fraweworks/base/core/res/va;ues/config.xml中，就有这个常量的定义。
#### VelocityTracker,GestureDetector和Scroller
参考：[https://juejin.im/post/5d160a4ce51d45773d46865a#heading-6](https://juejin.im/post/5d160a4ce51d45773d46865a#heading-6)

1. VelocityTracker：速度追踪，用于追踪手指在屏幕上滑动的速度，包括水平和竖直方向上的速度。
```
// View的onTouchEvent方法里追踪
VelocityTracker velocityTracker = VelocityTracker.obtain();
velocityTracker.addMovement(event);
/*
 * 获取当前速度
 * 	获取速度的之前必须先计算速度，即getXVelocity和getYVelocity这两个方法前面一定要
 * 	调用computeCurrentVelocity方法，第二点，这里的速度是指一段时间内手指滑动的屏幕像素，
 * 	比如将时间设置为1000ms时，在1s内，手指在水平方向手指滑动100像素，那么水平速度就是100，注意速度可以为负数，当手指从右向左滑动的时候为负
 * 速度公式：速度 = （终点位置 -  起点位置）/时间段
 *
 * computeCurrentVelocity这个方法的参数表示的是在一个时间内的速度，单位是毫秒
 */
velocityTracker.computeCurrentVelocity(1000);
int xVelocity = (int) velocityTracker.getXVelocity();
int yVelocity = (int) velocityTracker.getYVelocity();
// 最后，当不需要使用它的时候，需要调用clear方法来重置并回收内存:
velocityTracker.clear();
velocityTracker.recycle();
```
2. GestureDetector：手势检测，用于辅助检测用户的单击、滑动、长按、双击等行为。
```
// 首先，需要创建一个GestureDetector对象
GestureDetector mGestureDetector = new GestureDetector(this);
// 解决长按屏幕后无法拖动的现象
mGestureDetector.setIsLongpressEnabled(false);
// 接着，接管目标View的onTouchEvent方法，在待监听View的onTouchEvent方法中添加如下实现：
boolean consum = mGestureDetector.onTouchEvent(event);
return consum;
// 做完了上面两步，就可以有选择地实现OnGestureListener和OnDoubleTapListener中的方法了
```
OnGestureListener和OnDoubleTapListener接口中的方法如下：
![20161027211655148.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579578643437-3d2fc423-0fd3-45e6-bc25-45c227b1cd8b.png#align=left&display=inline&height=998&name=20161027211655148.png&originHeight=998&originWidth=1589&size=152331&status=done&style=none&width=1589)
日常开发中，比较常用的onSingleTapUp（单击），onFling（快速滑动），onScroll（推动），onLongPress（长按）和onDoubleTap（双击），另外要说明的是，在实际开发中可以不使用GestureDetector，完全可以自己在View中的onTouchEvent中去实现。

3. Scroller：弹性滑动对象，用于实现View的弹性滑动，我们知道，当使用View的scrollTo/scrollBy方法来进行滑动的时候，其过程是瞬间完成的，这个没有过度效果的滑动用户体验肯定是不好的，这个时候就可以用Scroller来实现过度效果的滑动，其过程不是瞬间完成的，而是在一定的时间间隔去完成的，Scroller本身是无法让View弹性滑动，他需要和View的computScrioll方法配合才能完成这个功能。
#### Scroller和VelocityTracker实战
```
class ThouchLinearLayout @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null
            , defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        // 空闲状态
        const val SCROLL_STATE_IDLE = 0
        // 滑动状态
        const val SCROLL_STATE_DRAGGING = 1
        // 滑动后自然沉降的状态
        const val SCROLL_STATE_SETTLING = 2

        // 无效手指，防止多指触控时，第一支手指抬起时造成的滑动晃动
        const val INVALID_POINTER = -1

        //f(x) = (x-1)^5 + 1 RecycleView源码中处理惯性滑动的插值器
        private val sQuinticInterpolator = Interpolator { t ->
            var t = t
            t -= 1.0f
            t * t * t * t * t + 1.0f
        }
    }

    private var initX = 0
    private var initY = 0
    private var interceptX = 0f
    private var interceptY = 0f
    private var isIntercept = false
    private var mScrollState = SCROLL_STATE_IDLE
    // 指定当前移动遵循的是哪一个手指
    private var mScrollPointerId = INVALID_POINTER
    private var touchRecyclerView: RecyclerView? = null

    private val mViewFlinger = ViewFlinger()
    private var velocityTracker : VelocityTracker? = null

    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
    private val mMinimumVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();

    fun setDragRecycleView(recyclerView: RecyclerView) {
        touchRecyclerView = recyclerView
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        Log.d("MotionEvent", "MotionEvent ${event.action}")
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                interceptX = event.x
                interceptY = event.y
                isIntercept = false
            }

            MotionEvent.ACTION_MOVE -> {
                val currX = event.x
                val currY = event.y
                val dx = currX - interceptX
                val dy = currY - interceptY
                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)
                Log.d("ACTION_MOVE", "ACTION_MOVE $isIntercept")
            }

            MotionEvent.ACTION_UP -> {
            }
        }
        return isIntercept || super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("MotionEvent", "MotionEvent ${event.action}")

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        }

        var eventAddedToVelocityTracker = false
        val vtev = MotionEvent.obtain(event)
        val action = event.actionMasked // 与 getAction() 类似，多点触控需要使用这个方法获取事件类型
        val actionIndex = event.actionIndex // 获取该事件是哪个指针(手指)产生的
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                initX = (event.x + 0.5).toInt()
                initY = (event.y + 0.5).toInt()
                setScrollState(SCROLL_STATE_IDLE)
                mScrollPointerId = event.getPointerId(0)
            }
            MotionEvent.ACTION_POINTER_DOWN -> { // 新落下的手指

            }
            MotionEvent.ACTION_MOVE -> {
                val currX = (event.x + 0.5).toInt()
                val currY = (event.y + 0.5).toInt()
                var dx = currX - initX
                val dy = currY - initY
//                isIntercept = abs(dx) > touchSlop && abs(dx) > abs(dy)
                if (mScrollState != SCROLL_STATE_DRAGGING && abs(dx) > touchSlop) {
                    setScrollState(SCROLL_STATE_DRAGGING)
                }
                Log.d("MotionEvent", "MotionEvent currX== $currX")
                if (mScrollState == SCROLL_STATE_DRAGGING/* && isScroll()*/) {
                    initX = currX
                    touchRecyclerView?.scrollBy(-dx, 0)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {

            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(vtev)
                eventAddedToVelocityTracker = true
                // 计算当前速度， 1000表示每秒像素数等
                velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                // 获取横向速度
                var xVelocity = velocityTracker?.getXVelocity()?.toInt()

                if (xVelocity == null || abs(xVelocity) <= mMinimumVelocity) {
                    xVelocity = 0
                }
                if (xVelocity != 0) {
                    mViewFlinger.fling(xVelocity.toInt())
                } else {
                    setScrollState(CustomScrollView.SCROLL_STATE_IDLE)
                }
                resetTouch()
            }
            MotionEvent.ACTION_CANCEL -> {
                resetTouch()
            }
        }

        if (!eventAddedToVelocityTracker) {
            velocityTracker!!.addMovement(vtev)
        }
        vtev.recycle()

        return true
    }

    private fun resetTouch() {
        if (velocityTracker != null) {
            velocityTracker!!.clear()
        }
    }

    private fun setScrollState(state: Int) {
        if (state == mScrollState) {
            return
        }
        mScrollState = state
        if (state != CustomScrollView.SCROLL_STATE_SETTLING) {
            mViewFlinger.stop()
        }
    }

    private inner class ViewFlinger : Runnable {
        private var mLastFlingX = 0
        private val mScroller: OverScroller
        private var mEatRunOnAnimationRequest = false
        private var mReSchedulePostAnimationCallback = false

        init {
            mScroller = OverScroller(context, sQuinticInterpolator)
        }

        override fun run() {
            disableRunOnAnimationRequests()
            val scroller = mScroller
            /**
             * 官方解释：当您想知道新位置时，请调用此方法。如果返回true，则动画尚未完成。
             * 个人解释：该方法配合mScroller.fling方法保存的需要滑动的距离和位置，
             *  通过Runnale不断轮询，根据滑动动画的插值器sQuinticInterpolator，获取每次轮询需要移动的距离，来返回当前的位置
             * */
            if (scroller.computeScrollOffset()) {
                val x = scroller.currX
                val dx = x - mLastFlingX
                mLastFlingX = x
                touchRecyclerView?.scrollBy(-dx, 0)
                postOnAnimation()
            }
            enableRunOnAnimationRequests()
        }

        fun fling(velocityX: Int) {
            mLastFlingX = 0
            setScrollState(CustomScrollView.SCROLL_STATE_SETTLING)
            // 官方解释：基于投掷手势开始滚动。行进的距离将取决于投掷的初始速度。
            // 个人理解：基于开始位置，以及横/纵瞬时速度，计算出横/纵最终滑动的距离以及位置并保存
            mScroller.fling(0, 0, velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)
            postOnAnimation()
        }

        fun stop() {
            removeCallbacks(this)
            // 停止动画
            mScroller.abortAnimation()
        }

        private fun disableRunOnAnimationRequests() {
            mReSchedulePostAnimationCallback = false
            mEatRunOnAnimationRequest = true
        }

        private fun enableRunOnAnimationRequests() {
            mEatRunOnAnimationRequest = false
            if (mReSchedulePostAnimationCallback) {
                postOnAnimation()
            }
        }

        internal fun postOnAnimation() {
            if (mEatRunOnAnimationRequest) {
                mReSchedulePostAnimationCallback = true
            } else {
                removeCallbacks(this)
                ViewCompat.postOnAnimation(this@ThouchLinearLayout, this)
            }
        }
    }
}

使用方法：header左右滑动，联动更新content(上下滑动)
// ll_touch.setDragRecycleView(rv_time)
<android.support.v7.widget.RecyclerView
    android:id="@+id/rv_time_header"
    android:layout_width="match_parent"
    android:layout_height="48dp" />

<com.android.customwidget.kotlin.widget.ThouchLinearLayout
    android:id="@+id/ll_touch"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/rv_content"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</com.android.customwidget.kotlin.widget.ThouchLinearLayout>
```
## View的滑动
### scrollTo/scrollBy
scrollBy实际上也是调用了scrolrTo方法，它实现了基于当前位置的相对滑动，而scrollTo则实现了基于所传递参数的绝对滑动，这个不难理解。利用scrollTo和scrollBy来实现View的滑动，这不是一件困难的事，但是我们要明白滑动过程，View内部的两个属性mScrollX和mScrollY的改变规则，这两个属性可以通过getScrollX和getScrollY方法分别得到。这里先简要概况一下：在滑动过程中，mScrollX的值总是等于View左边缘和View内容左边缘在水平方向的距离，而mScrollY的值总是等于View上边缘和View内容上边缘在竖直方向的距离。View边缘是指View的位置，由四个顶点组成，而View内容边缘是指View中的内容的边缘，scrolTo和scrollBy只能改变View内容的位置而不能变View在布局中的位置。mScrollX和mscrollY的单位为像素，并且当View左边缘在Veiw内容左边缘的右边时，mScrolX为正值，反之为负值；当View上边缘在View内容上边缘的下边时，mScrollY为正值，反之为负值。换句话说，如果从左向右滑动，那么mScrollX负值，反之为正值：如果从上往下滑动，那么mScrollY为负值，反之为正值。
### 动画
view.animate().translationX(100f)
ObjectAnimator.ofFloat(testButton,"translationX",0,100).setDuration(100).start();
### 改变布局参数
ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) testView.getLayoutParams();
layoutParams.width +=100;
layoutParams.leftMargin +=100;
testButton.requestLayout();
//或者testView.setLayoutParams(layoutParams);
### 各种滑动方式的对比

- scrollTo/scrollBy：操作简单，适合对View内容的滑动：
- 动画：操作简单，主要适用于没有交互的Visw和实现复杂的动画效果
- 改变布局参数：操作稍微复杂，适用于有交互的View
## 弹性滑动
### Scroller

```
public class CustomScrollerView extends LinearLayout {
    private Scroller mScroller;

    public CustomScrollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
    }

    public CustomScrollerView(Context context, @Nullable AttributeSet attrs, Scroller mScroller) {
        super(context, attrs);
        this.mScroller = mScroller;
    }

    //调用此方法滚动到目标位置
//    diffX = mStartX - mScroller.getCurrX();
//    diffY = mStartY - mScroller.getCurrY();
    public void smoothScrollTo(int destX,int destY) {
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int dx = destX - scrollX;
        int dy = destY - scrollY;
        Log.e("smoothScrollTo", getScrollX() + "");
        smoothScrollBy(dx, dy);
    }

    //调用此方法设置滚动的相对偏移
    public void smoothScrollBy(int dx, int dy) {
        //设置mScroller的滚动偏移量
        mScroller.startScroll(getScrollX(), getScrollY(), dx, dy);
        invalidate();//这里必须调用invalidate()才能保证computeScroll()会被调用，否则不一定会刷新界面，看不到滚动效果
    }

    @Override
    public void computeScroll() {
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset()) {
            //这里调用View的scrollTo()完成实际的滚动
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            //必须调用该方法，否则不一定能看到滚动效果
            postInvalidate();
        }
        super.computeScroll();
    }
}
```
### 动画
动画本身就是一种渐进的过程，因此通过他来实现滑动天然就具有弹性效果。
### 延时策略
延时策略。它的核心思想是通过发送一系列延时消息从而达到一种渐近式的效果，具体来说可以使用Handler或View的postDelayed方法，也可以使用线程的sleep方法。对于postDelayed方法来说，我们可以通过它来延时发送一个消息，然后在消息中来进行View的滑动，如果接连不断地发送这种延时消息，那么就可以实现弹性滑动的效果。对于sleep方法来说，通过在while循环中不断的滑动View和sleep，就可以实现弹性滑动的效果。
## View的事件分发
### 点击事件的传递规则
在介绍点击事件的传递规则之前，首先要明白这里要分析的对象是MotionEvent。所谓点击事件的事件分发，其实就是对MotionEvent事件的分发过程，即当一个MoonEvent产生了以后，系统需要把这个事件传递给一个具体的View，而这个传递的过程就是分发过程。点击事件的分发过程由三个很重要的分发来完成：dispatchTouchEvent、onInterceptTouchEvent和onTouchEvent。

- puhlic boolean dispatchTouchEvent(MotionEvent ev)

用来进行事件的分发。如果事件能够传递给当前View，那么此方法一定会被调用，返回结果受当前View的onTouchEvent和下级View的dispatchTouchEvent方法的影响，表示是否消耗当前事件。

- public boolean onInterceptTouchEven(MotionEvent event)

在上述方法内部调用，用来判断是否拦截某个事件，如果当前View拦截了某个事件，那么在同一个事件序列当中，此方法不会被再次调用，返回结果表示是否拦截当前事件.

- public boolean onTouchEvent(MotionEvent event)

在dispatchTouchEvent方法中调用，用来处理点击事件，返回结果表示是否消耗当前事件，如果不消耗，则在同一个事件序列中，当前View无法再次接收到事件。
```
// 伪代码
@Override
public boolean dispatchTouchEvent(MotionEvent ev) {
	boolean consume = false;
  if(onInterceptTouchEvent(ev)) {
  	consume = onTouchEvent(ev);
	} else {
  	consume = child.dispatchTouchEvent(ev);
	}
	return consume;
}
```
> 上述的伪代码已经将三者的区别说明了，我们可以大致的了解传递的规则就是，对于一个根ViewGroup来说，点击事件产生以后，首先传递给它，这时它的dispatchTouchEvent就会被调用，如果这个ViewGroup的onIntereptTouchEvent方法返回true就表示它要控截当前事件，接着事件就会交给这个ViewGroup处理，则他的onTouchEvent方法就会被调用；如果这个ViewGroup的onIntereptTouchEvent方法返回false就表示不需要拦截当前事件，这时当前事件就会继续传递给它的子元素，接着子元素的onIntereptTouchEvent方法就会被调用，如此反复直到事件被最终处理。
> 
> 当一个View需要处理事件时，如果它设置了OnTouchListener，那么OnTouchListener中的onTooch方法会被回调。这时事件如何处理还要看onTouch的返回值，如果返回false,那当前的View的方法OnTouchListener会被调用；如果返回true，那么onTouchEvent方法将不会被调用。由此可见，给View设置的OnTouchListener，其优先级比onTouchEvent要高，在onTouchEvent方法中，如果当前设置的有OnClickListener，那么它的onClick方法会被调用。可以看出，平时我们常用的OnClickListener，其优先级最低，即处于事尾端。
> 
> 当一个点击事件产生后，它的传递过程遵循如下顺序：Activity-->Window-->View，即事件总是先传递给Activity，Activity再传递给Window，最后Window再传递给顶级View顶级View接收到事件后，就会按照事件分发机制去分发事件。考虑一种情况，如果一个View的onTouchEvent返回false，那么它的父容器的onTouchEvent将会被调用，依此类推,如果所有的元素都不处理这个事件，那么这个事件将会最终传递给Activity处理，即Activity的onTouchEvent方法会被调用。这个过程其实也很好理解，我们可以换一种思路，假如点击事件是一个难题，这个难题最终被上级领导分给了一个程序员去处理（这是事件分发过程），结果这个程序员搞不定(onTouchEvent返回false)，现在该怎么办呢？难题必须要解决，那只能交给水平更高的上级解决（上级的onTouchEvent被调用)，如果上级再搞不定，那只能交给上级的上级去解决，就这样将难题一层层地向上抛，这是公司内部一种很常见的处理问题的过程。从这个角度来看，View的事件传递过程还是很贴近现实的，毕竟程序员也生活在现实中。

![638171-a06b6ec93ad30382.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1579602873538-8f3e1d6e-1029-4642-a736-5df94d645afc.png#align=left&display=inline&height=1041&name=638171-a06b6ec93ad30382.png&originHeight=1041&originWidth=611&size=54277&status=done&style=none&width=611)
关于事件传递的机制，这里给出一些结论，根据这些结论可以更好地理解整个传递机制，如下所示：
(1) 同一个事件序列是指从手指接触屏幕的那一刻起，到手指离开屏慕的那一刻结束，在这个过程中所产生的一系列事件，这个事件序列以down事件开始，中间含有数量不定的move事件，最后以up结束
(2) 正常情况下，一个事件序列只能被一个Visw拦截且消耗。这一条的原因可以参考（3），因为一旦一个元素拦截了某此事件，那么同一个事件序列内的所有事件都会直接交给它处理，因此同一个事件序列中的事件不能分别由两个View同时处理，但是通过特殊手段可以做到，比如一个Vew将本该自己处理的事件通过onTouchEvent强行传递给其他View处理。
(3) 某个View一旦决定拦截，那么这一个事件序列都只能由它来处理（如果事件序列能够传递给它的话)，并且它的onInterceprTouchEvent不会再被调用。这条也很好理解，就是说当一个View决定拦截一个事件后，那么系统会把同一个事件序列内的其他方法都直接交给它来处理，因此就不用再调用这个View的onInterceptTouchEvent去询问它是否要拦截了。
(4) 某个View一旦开始处理事件，如果它不消耗ACTON_DOWN事件(onTouchEvent返回了false)，那么同一事件序列中的其他事件都不会再交给它来处理，并且事件将重新交由它的父元素去处理，即父元素的onTouchEvent会被调用。意思就是事件一旦交给一个View处理，那么它就必须消耗掉，否则同一事件序列中剩下的事件就不再交给它来处理了，这就好比上级交给程序员一件事，如果这件事没有处理好，短期内上级就不敢再把事情交给这个程序员做了，二者是类似的道理。
(5) 如果View不消耗除ACTION_DOWN以外的其他事件，那么这个点击事件会消失，此时父元素的onTouchEvent并不会被调用，并且当前View可以持续收到后续的事件，最终这些消失的点击事件会传递给Activity处理。
(6) ViewGroup默认不拦截任何事件。Android源码中ViewGroup的onInterceptTouchEvent方法默认返回false
(7) View没有onInterceptTouchEvent方法，一旦有点击事件传递给它，那么它的onTouchEvent方法就会被调用。
(8) view的onTouchEvent默认都会消耗事件（返回true)，除非它是不可点击的(clickable和longClickable同时为false)，View的longClickable属性默认都为false,clickable属性要分情况，比如Button的clickable属性默认为true，而TextView 的clickable属性默认为false
(9) view 的enable.属性不影响onTouchEvent的默认返回值。哪怕一个View是disable状态的，只要它的clickable或者longclickable有一个为true，那么它的onTouchEvent就返会true。
(10) onclick会发生的前提实际当前的View是可点击的，并且他收到了down和up的事件
(11) 事件传递过程是由外到内的，理解就是事件总是先传递给父元素，然后再由父元素分发给子View，通过requestDisallowInterptTouchEvent方法可以再子元素中干预元素的事件分发过程，但是ACTION_DOWN除外。
## View的滑动冲突
### 1.常见的滑动冲突场景
常见的滑动一般有三个方面：

- 外部滑动方向和内部滑动方向不一致
- 外部滑动方向和内部滑动方向一致
- 上面两种情况的嵌套
### 2.滑动冲突的处理规则
场景1，它的处理规则是：当用户左右滑动时，需要让外部的View拦截点击事件，当用户上下滑动时，需要让内部View拦截点击事件。这个时候我们就可以根据它们的特征来解决滑动冲突，具体来说是：根据滑动是水平滑动还是竖直滑动来判断到底由谁来拦截事件，根据滑动过程中两个点之间的坐标就可以得出到到底由谁来拦截事行；如何根据坐标来获取滑动的方向呢？这个很简单，有很多可以参考，比如可以依据滑动路径和水平方向做形成的夹角，也可以依据水平方向和竖直方向上的距离差来判断，某些特殊时候还可以依据水平和竖直方向的速度差来做判断。这里我们可以通过水平和竖直方向的距离差来判断，比如竖直方向滑动的距离大就判断为竖直滑动。否则判断为水平滑动。根据这个规则就可以进行下一步的解决方法制定了。

对于场景2来说，比较特殊，它无法根据滑动的角度、距离差以及速度差来做判断，但是这个时候一般都能在业务上找到突破点，比如业务上有规定：当处于某种状态时需要外部View响应用户的滑动，而处于另外一种状态时则需要内部View来响应View的滑动，根据这种业务上的需求我们也能得出相应的处理规则，有了处理规则同样可以进行下一步处理。

场景3来说，它的滑动规则就更复杂了，和场景2一样，它也无法直接根据滑动的角度、距离差以及速度差来做判断，同样还是只能从业务上找到突破点，具体方法和场景2一样，都是从业务的需求上得出相应的处理规则。

### 3.滑动冲突的解决方式
#### 1.外部拦截法
所谓的外部拦截费是指点击事件都先经过父容器的拦截处理，如果父容器需要这个事件就给给他，这里我们还得重写我们的onIterceptTouchEvent。

```
		@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercepted = false;
        int x = (int) ev.getX();
        int y = (int) ev.getY();
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                intercepted = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if("父容器需要当前点击事件") { // 比如：Math.abs(deltaX) > Math.abs(deltaY)
                    intercepted = true;
                }else {
                    intercepted = false;
                }
                break;
            case MotionEvent.ACTION_UP:
                intercepted = false;
                break;
        }
        mLastXIntercept = x;
        mLastYIntercept = x;
        return intercepted;
    }
    
    上述代码是外部拦截法的典型逻辑，针对不同的滑动冲突，只需要修改父容器需要当前点击事件这个条件即可，
其他均不需做修改并且也不能修改。这里对上述代码再描述一下，在onInterceptTouchEvent方法中，
首先是ACTION_DOWN这个事件，父容器必须返回false，即不拦截ACTION_DOWN事件，
这是因为一旦父容器拦截了ACTION_DOWN，那么后续的ACTION_MOVE和ACTION_UP事件都会直接交由父容器处理，
这个时候事件没法再传递给子元素了；其次是ACTION_MOVE事件，这个事件可以根据需要来决定是否拦截，
如果父容器需要拦截就返回true，否则返回false；最后是ACTION_UP事件，这里必须要返回false，
因为ACTION_UP事件本身没有太多意义考虑一种情况，假设事件交由子元素处理，如果父容器在ACTION_UP时返回了true，
会导致子元素无法接收到ACTION_UP事件，这个时候子元素中的onClick事件就无法触发，但是父容器比较特殊，
一旦它开始拦截任何一个事件，那么后续的事件都会交给它处理，而ACTION_UP作为最后一个事件
也必定可以传递给父容器，即便父容器的onInterceptTouchEvent方法在ACTION_UP时返回了false。
```
#### 2.内部拦截法
内部拦截法是指父容器不拦截任何事件，所有的事件都传递给子元素，如果子元素要消耗此事件就直接消耗掉，否则就交由父容器进行处理，这种方法和Android中的事件分发机制不一致，需要配合requestDisallowInterceptTouchEvent方法才能正常工作，使用起来较外部拦截法稍显复杂。

```
		@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX =  x - mLastX;
                int deltaY =  x - mLastY;
                if("父容器的点击事件") { // 比如：Math.abs(deltaX) > Math.abs(deltaY)
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                break;
            case MotionEvent.ACTION_UP:

                break;
        }
        mLastX = x;
        mLastY = y;
        return super.dispatchTouchEvent(event);
    }
    
    上述代码就是内部拦截法的典型代码，当面对不同的滑动策略只需要修改里面的条件即可，
其他不需要做改动，除了子元素需要处理之外，父元素默认也要拦截除ACTION_DOWN之外的其他事件，
这样当子元素调用getParent().requestDisallowInterceptTouchEvent(false)方法时，父元素才能继续拦截所需要的事件。

为什么父容器不能拦截ACTION_DOWN事件呢？那是因为ACTION_DOWN事件并接受FLAG_DISALLOW_DOWN这个标记位的
控制，所以一旦父容器拦截，那么所有的事件都无法传递到子元素中，这样内部拦截就无法起作用了，父元素要做如下修改：
		@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            return false;
        }else {
            return true;
        }
    }
```
**注：**父布局的onInterceptTouchEvent()方法会比该次事件下子View的onTouchEvent()方法要更早进行（如果该事件会分发给子View的话），所以若子View设置getParent().requestDisallowInterceptTouchEvent(true)代码的那次父布局事件的onInterceptTouchEvent()返回的是true，那么父布局依然会拦截事件，子View的这次禁用父布局拦截事件请求将会失败。若父布局的ACTION_MOVE事件某些条件下返回true而ACTION_DOWN事件返回false，而此时其子布局在其ACTION_DOWN事件下，设置getParent().requestDisallowInterceptTouchEvent(true)禁止父布局拦截的事件，则在发生ACTION_MOVE事件时则不会响应父布局的事件，可禁止成功。原因：在设置禁止父布局拦截为true方法中，实际为上是设置了其成员变量**mGroupFlags**为**FLAG_DISALLOW_INTERCEPT**状态，而由于父布局的onInterceptTouchEvent方法比子布局的所有事件分发方法都早，因此设置禁止父布局拦截事件时会在下一次事件发生才生效，直到ACTION_UP事件发生时父布局调用resetTouchState()方法重置触摸状态。
