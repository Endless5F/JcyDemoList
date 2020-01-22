# View的工作原理

## MeasureSpec
MeasureSpec代表一个32位int值，高两位代表SpecMode，低30位代表SpecSize，SpecMode是指测量模式，而SpecSize是指在某个测量模式下的规格大小。
### SpecMode

- UNSPECIFIED

父容器不对View有任何的限制，要多大给多大，这种情况一般用于系统内部，表示一种测量的状态。

- EXACTLY

父容器已经检测出View所需要的精度大小，这个时候View的最终大小就是SpecSize所指定的值，它对应于LayoutParams中的match_parent,和具体的数值这两种模式。

- AT_MOST

父容器指定了一个可用大小，即SpecSize，View的大小不能大于这个值，具体是什么值要看不同view的具体实现，它对应于LayoutParams中wrap_content。
### LayouParams

- LayouParams.MATCH_PARENT：精确模式，大小就是窗口的大小
- LayouParams.WRAP_CONTENT：最大模式，大小不定，但是不能超出屏幕的大小
- 固定大小（比如100dp）：精确模式，大小为LayoutParams中指定的大小
### 子元素的MesureSpec的创建
子元素的MesureSpec的创建和父容器的MesureSpec和子元素的LayoutParams有关。

```
public static final int FILL_PARENT = -1;
public static final int MATCH_PARENT = -1;
public static final int WRAP_CONTENT = -2;

// Android-23以下，sUseZeroUnspecifiedMeasureSpec为true
sUseZeroUnspecifiedMeasureSpec = targetSdkVersion < Build.VERSION_CODES.M;

public static int getChildMeasureSpec(int spec, int padding, int childDimension) {
    int specMode = MeasureSpec.getMode(spec);
    int specSize = MeasureSpec.getSize(spec);

    int size = Math.max(0, specSize - padding);

    int resultSize = 0;
    int resultMode = 0;

    switch (specMode) {
        // Parent has imposed an exact size on us
        case MeasureSpec.EXACTLY: // 父布局有具体大小
            if (childDimension >= 0) {
            		// 若子布局设置具体数值，则直接设置具体数值(以开发者需求为主)，测量模式依旧为EXACTLY
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // 若子布局设置MATCH_PARENT，则直接设置父布局允许的最大值，测量模式依旧为EXACTLY
                resultSize = size;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // 若子布局设置WRAP_CONTENT，则直接设置父布局允许的最大值，但测量模式更改为AT_MOST
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;
        case MeasureSpec.AT_MOST:	// 父布局不确定大小，但此时大小为父布局的父布局所允许的最大值
            if (childDimension >= 0) {
                // 若子布局设置具体数值，则直接设置具体数值(以开发者需求为主)，
                // 由于子布局大小已确定，以开发者设置为主，因此此时测量模式更改为EXACTLY
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // 若子布局设置MATCH_PARENT，则直接设置父布局的父布局允许的最大值(即当前父布局的值)，
                // 由于父布局大小不定，而子布局需要填满全部，而子布局具体大小也未知，因此当前测量模式依旧为AT_MOST
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // 若子布局设置WRAP_CONTENT，则直接设置父布局的父布局允许的最大值(即当前父布局的值)，
                // 由于父布局大小不定，而子布局需要包裹自己本身大小因此具体大小未知，所以当前测量模式也为AT_MOST
                resultSize = size;
                resultMode = MeasureSpec.AT_MOST;
            }
            break;
        case MeasureSpec.UNSPECIFIED:	// 父布局想多大多大
            if (childDimension >= 0) {
                // 若子布局设置具体数值，则直接设置具体数值(以开发者需求为主)，
                // 由于子布局大小已确定，以开发者设置为主，因此此时测量模式更改为EXACTLY
                resultSize = childDimension;
                resultMode = MeasureSpec.EXACTLY;
            } else if (childDimension == LayoutParams.MATCH_PARENT) {
                // 若子布局设置MATCH_PARENT，Android-23以下直接设置为0，否则为父布局的大小
                // 若此时Android版本为Android-23以下，size则为0，否则size为父布局的父布局...
                // 直到某一父布局的测量模式不为UNSPECIFIED，则此时的size就为此级别父布局的大小
                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                resultMode = MeasureSpec.UNSPECIFIED;
            } else if (childDimension == LayoutParams.WRAP_CONTENT) {
                // 同上
                resultSize = View.sUseZeroUnspecifiedMeasureSpec ? 0 : size;
                resultMode = MeasureSpec.UNSPECIFIED;
            }
            break;
    }
    return MeasureSpec.makeMeasureSpec(resultSize, resultMode);
}
```
**总结：**

1. 父布局测量模式为MeasureSpec.EXACTLY：

若子布局设置WRAP_CONTENT，则子布局的测量模式为AT_MOST，否则为EXACTLY。

2. 父布局测量模式为MeasureSpec.AT_MOST：

若子布局设置固定值，则子布局的测量模式为EXACTLY，否则为AT_MOST。

3. 父布局测量模式为MeasureSpec.UNSPECIFIED：

若子布局设置固定值，则子布局的测量模式为EXACTLY，否则为UNSPECIFIED。
## View的工作流程
### measure
#### View的measure过程
View 的 measure过程由其measure方法来完成，measure方法是一个final类型的方法，这就意味着子类不能重写此方法，在View的measure方法中去调用View的onMesure方法，因此只需要看onMeasure的实现即可。
#### ViewGroup的measure过程
对于ViewGroup来说，除了完成自己的measure过程以外，还会遍历去调用所有子元素的measure方法，各个子元素再通归去执行这个过程。和View不同的是，ViewGroup是一个抽象类，其测量过程的onMeasure方法需要各个子类去实现，因此它没有重写View的onMeasure方法，但是它提供了一系列measureChildXXX(measureChildren、measureChild、measureChildWithMargins)，以供子布局去测量。
#### Activity的onCreate、onStart、onResume生命周期内获取View的宽高方法

1. Activity/View#onWindowFocusChanged：

onWindowFocusChanged这个方法的含义是：View已经初始化完毕了，宽/高已经准备好了，这个时候去获取宽/高是没问题的。需要注意的是，onWindowFocusChanged会被调用多次，当Activity的窗口得到焦点和失去焦点时均会被调用一次。具体来说，当Activity继续执行和暂停执行时，onWindowFocusChanged均会被调用，如果频繁地进行onResume和onPause，那么onWindowFocusChanged也会被频繁地调用。

2. View.post(runnable)

通过post可以将一个runnable投递到消息队列，然后等到Lopper调用runnable的时候，View也就初始化好了

3. ViewTreeObserver

使用ViewTreeObserver的众多回调可以完成这个功能，比如使用OnGlobalLayoutListener这个接口，当View树的状态发生改变或者View树内部的View的可见性发生改变，onGlobalLayout方法就会回调，因此这是获取View的宽高一个很好的例子，需要注意的是，伴随着View树状态的改变，这个方法也会被调用多次
```
ViewTreeObserver observer = mTextView.getViewTreeObserver();
    observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
    @Override
    public void onGlobalLayout(){
        mTextView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        int width=mTextView.getMeasuredWidth();
        int height=mTextView.getMeasuredHeight();
    }
});
```

4. View.measure(int widthMeasureSpec , int heightMeasureSpec)

通过手动测量View的宽高，这种方法比较复杂，这里要分情况来处理，根据View的LayoutParams来处理
#### 小结：
View的onMeasure是三大流程中最复杂的一个，measure完成以后，通过getMeasureWidth/Height就可以正确地获取到View的测量宽/高。需要注意的是，在某些极端情况下measure才能确定最终的测量宽/高，在这种情形下，系统可能要多次调用measure方法进行测量，在这种情况下，在onMeasure方法中拿到的测量值很可能是不准确的。一个比较好的习惯是在onLayout方法中去获取View的测量宽/高或者最终宽/高。
### layout
#### View的layout过程
Layout的作用是ViewGroup用来确定子元素的作用的，当ViewGroup的位置被确认之后，他的layout就会去遍历所有子元素并且调用onLayout方法，在layout方法中onLayou又被调用，layout的过程和measure过程相比就要简单很多了，layout方法确定了View本身的位置，而onLayout方法则会确定所有子元素的位置。

layout的方法的大致流程如下，首先会通过一个setFrame方法来设定View的四个顶点的位置，即初始化mLeft,mTop,mRight,mBottom这四个值，View的四个顶点一旦确定，那么View在父容器的位置也就确定了，接下来会调用onLayout方法，这个方法的用途是调用父容器确定子元素的位置，和onMeasure类似，onLayout的具体位置实现同样和具体布局有关，所有View和ViewGroup均没有真正的实现onLayout方法。
### draw

- 对View的背景进行绘制
- 保存当前的图层信息(可跳过)
- 绘制View的内容
- 对View的子View进行绘制(如果有子View)
- 绘制View的褪色的边缘，类似于阴影效果(可跳过)
- 绘制View的装饰（例如：滚动条）
源码中提示，其中第2步和第5步是可以跳过的，是常见的情况。

使用不同的绘制方法，以及在重写的时候把绘制代码放在 super.绘制方法() 的上面或下面不同的位置，以此来实现需要的遮盖关系。表格总结：
![006tKfTcly1fii5jk7l19j30q70e0di5.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1579701569580-b639b508-eb2d-4796-984f-2b2e2e12488a.jpeg#align=left&display=inline&height=504&name=006tKfTcly1fii5jk7l19j30q70e0di5.jpg&originHeight=504&originWidth=943&size=86548&status=done&style=none&width=943)
## 自定义View
### 自定义View的分类

1. 继承View重写onDraw方法

重写了绘制，一般就是想自己实现某些图形了，因为原生控件已经满足不了你了，很显然这需要绘制的方式来完成，采用这个方式需要自身支=warp_content,并且pading也要自己处理，比较考验你的功底了

2. 继承ViewGroup派生出来的Layout

这个相当于重写容器了，当某些效果看起来像是View的组合的时候，就是他上场的时候了，不过这个很复杂，需要合理的使用测量和布局这两个过程，还要兼顾子元素的这两个过程

3. 继承特定的View

比如TextView，就是重写原生的View嘛，比如你想让TextView默认有颜色之类的，有一些小改动，这个就可以用它的，他相对来说比较简单，这个就不需要自己支持包裹内容和pading了

4. 继承特定的ViewGroup

这个和上述一样，只不过是重写容器而已，这个也比较常见，事件分发的时候用的也多。
### 自定义View的须知

1. 让View支持warp_content

这个在之前将测量的时候说过，如果你不特殊处理一下是达不到满意的效果的，这里就不重复了

2. 如果有有必要，让你的View支持padding

这是因为如果你不处理下的话，那么该属性是不会生效的，在ViewGroup也是一样

3. 尽量不要在View中使用Handler

为什么不能用，是因为没有必要，View本身就有一系列的post方法，当然，你想用也没人拦着你，我倒是觉得handler写起来代码简洁很多

4. View中如果有线程或者动画，需要及时停止，参考View#onDetachedFromWindow

这个问题那就更好理解了，你要是不停止这个线程或者动画，容易导致内存溢出的，所以你要在一个合适的机会销毁这些资源，在Activity有生命周期，而在View中，当View被remove的时候，onDetachedFromWindow会被调用，和此方法对应的是onAttachedToWindow

5. View带有滑动嵌套时，需要处理好滑动冲突
## 参考
源码分析可参考：[View绘制流程源码分析](https://juejin.im/post/5cb438695188257a9e31263a)
自定义View可参考：[View之自定义全解析(入门)](https://juejin.im/post/5d160a4ce51d45773d46865a)
