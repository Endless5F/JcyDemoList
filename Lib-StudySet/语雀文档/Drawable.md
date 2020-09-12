# Drawable

## 一、Drawable的简介
Drawable 是 “所有可绘制东西” 的一个抽象，大多数时候，我们只需要把各种不同类型的资源作为转化为 drawable，然后 View 会帮我们把它渲染到屏幕上。Drawable 类提供了一个通用 API，用于解析转化各种可视资源到 Canvas，跟 View 不一样，**Drawable 不能接受任何事件以及用户交互**。

在Android中，Drawable可不是单单的图片那么简单，他说直观点可以理解为一种图片的抽象概念，通过颜色也可以定义出各式各样的图片，在实际开发中，Drawable常常被作为一个View的背景，一般分XML和图片两种，当然，我们也可以用图片来实现，不过这种方式就比较复杂了，Drawable作为一个抽象类，他衍生出了很多的子类，我们可以看下他的结构图：
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1583419396549-e92c89c0-efcb-42b3-bb76-8fb51108512f.png#align=left&display=inline&height=488&originHeight=488&originWidth=754&size=0&status=done&style=none&width=754)
Drawable的内部宽高参数很重要，通过getIntrinsicWidth和getIntrinsicHeight这两个方法可以获取到他们，但是比并不是所有的Drawable都有内部宽高，比如一张图片所形成的Drawable，他就有，但是如果你是颜色所形成的的，那就自然是没有的，而且要注意的是，内部宽高不等于他的大小，因为当View是背景会被拉伸至View的等同大小。
## 二、Drawable的分类
### 1. BitmapDrawable
BitmapDrawable是比较简单的一张图片的Drawable，我们在实际开发中，可以直接设置为View的背景，也可以通过XML的形式来描述BitmapDrawable展示更多的效果，如下所示：
```
<?xml version="1.0" encoding="utf-8"?>
<bitmap xmlns:android="http://schemas.android.com/apk/res/android"
    android:antialias="true"
    android:dither="true"
    android:filter="true"
    android:gravity="center"
    android:mipMap="true"
    android:src="@color/colorPrimaryDark"
    android:tileMode="clamp">

</bitmap>
```

- **android:src**：这个是资源，可以是图片也可以是颜色
- **android:antialias**：是否开启图片抗锯齿，开启后图片会变得平滑一点，同时也会在一定程度上降低清晰度，不过这个降低我们完全可以无视，所以这个可以开启
- **android:dither**：是否开启抖动效果，当图片的像素配置和手机不一致的时候，开启这个选项可以让高质量的图片在低分辨率的屏幕上保持比较好的显示效果，比如图片的色彩模式ARGB8888，但是设备只支持RGB555，这个时候开启抖动模式可以让图片不会过于失真，在Android中创建的Bitmap一般会使用ARGB8888这个模式，即ARGB四个通道各占8位，在这个色彩下，一个像素所占为4个字节，一个像素的位数综合越高，图片越逼真，抖动也应该开启
- **android:filter**：是否开启过滤效果，当图片尺寸被拉伸或者压缩时，开启过滤效果会保持比较好的显示效果，所以这个也可开启
- **android:gravity**：这个就不用说，位置方向，上下左右，可以用“|”符号来实现左上，右下等效果，可以具体看下他的属性，如图：

![](https://cdn.nlark.com/yuque/0/2020/png/754789/1583419584614-4d4ca6bf-766c-4381-ac11-a93bafc2da5e.png#align=left&display=inline&height=817&originHeight=817&originWidth=1330&size=0&status=done&style=none&width=1330)

- **android:mipMap**：这是一种图片相关的处理技术，也叫纹理映射，比较抽象，默认为false，不常用
- **android:tileMode**：平铺模式，这个选项有几个值： disabled | clamp | repeat | mirror 其中disabled 是关闭平铺模式，这个也是默认值，开启后，gravity属性会无效，先说下其余三个属性的区别，三种都表示平铺模式
- **repeat**：表示简单的水平和竖直方向上平铺效果
- **mirror**：表示一种在水平和竖直方向上的镜面投影效果
- **clamp**：表示四周像素扩散效果

接下来说一下NinePatchDrawable，说白了就是用代码实现的.9图片而已：
```
<?xml version="1.0" encoding="utf-8"?>
<nine-patch xmlns:android="http://schemas.android.com/apk/res/android"
   android:dither="true"
   android:src="@drawable/ic_launcher">
</nine-patch>
```
### 2. ShapeDrawable
ShapeDrawable是一种很常见的Drawable,可以理解为色彩构造的图片，他即是纯色的图形，也可以具有渐变的图形，不过语法要多很多，而且繁杂，示例：
```
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="line">

    <corners
        android:bottomLeftRadius="10dp"
        android:bottomRightRadius="10dp"
        android:radius="10dp"
        android:topLeftRadius="10dp"
        android:topRightRadius="10dp" />

    <gradient
        android:angle="10"
        android:centerColor="@color/colorPrimary"
        android:centerX="10"
        android:centerY="10"
        android:endColor="@color/colorPrimary"
        android:gradientRadius="10dp"
        android:startColor="@color/colorAccent"
        android:type="linear"
        android:useLevel="true" />

    <padding
        android:bottom="10dp"
        android:left="10dp"
        android:right="10dp"
        android:top="10dp" />

    <size
        android:width="100dp"
        android:height="100dp" />

    <solid android:color="@color/colorAccent" />

    <stroke
        android:width="100dp"
        android:color="@color/colorPrimaryDark"
        android:dashGap="@dimen/activity_horizontal_margin"
        android:dashWidth="10dp" />

</shape>
```
需要注意的是，shape标签创建的Drawable,实际上是GradientDrawable，我们还是先来分析下他们的属性区别：

- **android:shape**：表示图片的形状，有四个选项，line（横线），oval(椭圆)，rectangle(矩形)，ring（圆环），他的默认值是矩形，而且line（横线）和ring（圆环）都必须通过stroke标签来指定宽高，颜色等信息，否则无法达到预期的效果。针对ring的形状，有五个特殊的属性，android:radius，android:tickness，android:innerRadiusRatio，android:ticknessRatio和android:userLevel，具体如下：

![](https://cdn.nlark.com/yuque/0/2020/png/754789/1583419907114-1b3f719e-6393-4cf7-a0a7-9e5a19fdbf26.png#align=left&display=inline&height=773&originHeight=773&originWidth=1324&size=0&status=done&style=none&width=1324)

- **corners**：表示shape的四个角度，它只是用于矩形的shape，这里的角度是指圆角的成都，用px来表示，他有五个属性：
  - android:radius：为四个角同时设置相同的角度，优先级较低，会被其他四个覆盖
  - android:topLeftRadius：设置最上角的角度
  - android:topRightRadius：设置右上角的角度
  - android:bottomLeftRadius：设置最下角的角度
  - android:bottomRightRadius：设置右下角的角度
- **gradient**：它与solid标签是互相排斥的，因为solid表示纯色，而它表示渐变，属性如下：
  - android:angle：渐变的角度，默认为0，其值必须为45的倍数，0表示从左往右，90表示从上到下
  - android:centerColor：渐变的中心颜色
  - android:centerX：渐变中心点的横坐标
  - android:centerY：渐变中心点的纵坐标
  - android:endColor：渐变的结束颜色
  - android:gradientRadius：渐变半径，只有当type = radial 时才有效
  - android:startColor：渐变的开始颜色
  - android:type：渐变的类型，
  - android:useLevel：false
- **solid**：这个标签表示纯色填充，通过android:color来表示填充颜色
- **stroke**：shape的描边
  - android:width:宽度
  - android:color:颜色
  - android:dashwidth:虚线线段的宽度
  - android:dashGap:组成虚线的线段之间的间隔，间隔越大虚线看起来空隙越大
  - 注意，如果android:dashGap和android:dashwidth有任何一个为0的话，那么虚线就不能生效了
- **pading**：这个表示空白，但是他表示的不是shape的空白，而是包含他view的空白，而且有四个属性，左上右下
- **size**：shape的大小，有两个属性，width/height ，分别表示的是shape的宽高，也可以理解为shape的股友大小，但是一般来说，他并不是最终的大小，这个有点抽象，但是我们要明白，对于shape来说并没有宽高这个概念，作为view的背景他会适应view的宽高，size标签虽然是设置股友大小，但是还是会被拉伸
### 3. LayerDrawable
LayerDrawable对应的xml是< layer-list>，他可以理解为图层，通过不同的view达到叠加的效果。
一个layer-list中可以包含多个item，每个item表示一个Drawable。Item的结构也比较见到那，比较常用的属性有android:top、android:bottom、android:left、android:right，它们分别表示Drawable相对于View的上下左右偏移量，单位为像素。另外可以通过android:drawable属性来直接引用一个已有的Drawable资源，也可以在item中自定义Drawable。默认情况下，layer-list中所有的Drawable都会被缩放至View的大小，对于bitmap来说，需要使用android:grawity属性才能控制图片更好的显示效果。Layer-list有层次的概念，下面的item会覆盖上面的item，通过合理的分层，可以实现一些特殊的叠加效果。
```
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">

    <item>
        <shape android:shape="rectangle">
            <solid android:color="#0ac39e" />
        </shape>

    </item>

    <item android:bottom="6dp">
        <shape android:shape="rectangle">
            <solid android:color="#ffffff" />
        </shape>
    </item>

    <item
        android:bottom="1dp"
        android:left="1dp"
        android:right="1dp">
        <shape android:shape="rectangle">
            <solid android:color="#ffffff" />
        </shape>

    </item>

</layer-list>
```
### 4. StateListDrawable
StateListDrawable对应的是< selector>标签，它会根据view的状态来选择出现的drawable。
**

- **android:constantSize**：StateListDrawable的股友大小是不随其状态的改变发生改变的，因为状态的改变会导致他切换不同的drawable,而不同的drawable具有不同的drawable,true表示StateListDrawable的固有大小不变，这时他的固有大小就是内部所有drawable的固有大小的最大值，false则是跟随状态改变，默认false
- **android:dither：**是否开启抖动效果，这个在之前就已经提过，开启此选项可以让图片在低质量的屏幕上显示较好的效果，默认为true
- **android:variablePadding：**StateListDrawable的pading是跟随其状态发生改变的而改变，fasle为最大值，跟constantSize类似，不建议开启，默认false

<item>标签标示的是一个具体的Drawable，它的结构也比较简单，其中drawable是已有资源的id，剩下的就是各种状态，状态如图：
![](https://cdn.nlark.com/yuque/0/2020/png/754789/1583421174800-c3dbd7b1-602d-48a3-8f87-eda4ec1ac9ee.png#align=left&display=inline&height=404&originHeight=404&originWidth=1319&size=0&status=done&style=none&width=1319)
系统会根据View的状态从selector中选择对应的item，每一个对应着一个drawable,系统会按照从上到下来查找，直到查找到第一天匹配的item。一般来说，默认的item都应该放在selector的最后一条并且不附带任何的状态，这样当上面的item都无法匹配View当前状态时，系统就会选择默认的item。
### 5. LevenlListDrawable
LevenlListDrawable对应着标签，同样表示一个drawable的集合。集合中的每一个Drawable都有一个等级，根据不同的等级切换不同的Item。
```
<?xml version="1.0" encoding="utf-8"?>
<level-list xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:drawable="@drawable/ic_launcher"
        android:maxLevel="10"
        android:minLevel="1" />

</level-list>
```
标签中的每个Item各表示一个Drawable，并有与之对应的等级，而等级则是由android:maxLevel和android:minLevel所决定的，其等级范围是0-10000，最小为0，默认值，最大则为10000。
### 6. TransitionDrawable
TransitionDrawable对应的是用于实现两个Drawable的淡入淡出。

```
<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item
        android:bottom="@dimen/activity_horizontal_margin"
        android:drawable="@color/colorAccent"
        android:left="@dimen/activity_horizontal_margin"
        android:right="@dimen/activity_horizontal_margin"
        android:top="@dimen/activity_horizontal_margin" />
</transition>

或

<?xml version="1.0" encoding="utf-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@color/colorAccent" />
    <item android:drawable="@color/colorPrimary" />
</transition>


// 接着将上面的设置为View的背景，最后通过startTransition和resetTransition来操作
TransitionDrawable drawable = (TransitionDrawable) textView.getBackground();
drawable.startTransition(1000);
```
### 7. InsetDrawable
InsetDrawable对应的是，它可以将其他的Drawable内嵌到自己当中，并且可以在四周留下一定的距离，当一个View想他的背景比实际的距离小的时候就可以用，通过LayerDrawable也可以实现。
```
<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/ic_launcher"
    android:insetBottom="@dimen/activity_horizontal_margin"
    android:insetLeft="@dimen/activity_horizontal_margin"
    android:insetRight="@dimen/activity_horizontal_margin"
    android:insetTop="@dimen/activity_horizontal_margin">

</inset>

<?xml version="1.0" encoding="utf-8"?>
<inset xmlns:android="http://schemas.android.com/apk/res/android"
    android:insetBottom="15dp"
    android:insetLeft="15dp"
    android:insetRight="15dp"
    android:insetTop="15dp">

    <shape>
        <solid android:color="@color/colorAccent" />
    </shape>

</inset>

```
### [8. ScaleDrawable](https://www.jianshu.com/p/6038ad7aac50)
#### 什么是ScaleDrawable
ScaleDrawable对应标签<scale>,它可以根据自己的等级(level)将指定的Drawable缩放到一定比例。
**什么是等级(level)？**
等级对ScaleDrawable的影响：
等级0表示ScaleDrawable不可见，这是默认值。
_若想ScaleDrawable可见，则需要等级不为0。_
_等级为10000时就没有缩放效果了_
**_级别越大Drawable显示得越大，应该设置为0-10000_**
**_XML设置的缩放比例越大，Drawable显示的越小_**
#### 11.2 ScaleDrawable语法/属性

```
<?xml version="1.0" encoding="utf-8"?>
<scale
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/drawable_resource"
    //android:gravity 当图片小于容器尺寸时，设置此选项可以对图片经典定位，
    //这个属性比较多，不同选项可以使用‘|’来组合使用。
    //top   将图片放在容器顶部，不改变图片大小
    //bottom    将图片放在容器底部，不改变图片大小
    //left  将图片放在容器左侧，不改变图片大小
    //right 将图片放在容器右侧，不改变图片大小
    //center_vertical   图片竖直居中，不改变图片大小
    //fill_vertical 图片竖直方向填充容器
    //center_horizontal 图片水平居中，不改变图片大小
    //fill_horizontal   图片水平方向填充容器
    //center    使图片在水平方向和竖直方向同时居中，不改变图片大小
    //fill  图片填充容器，默认值
    //clip_vertical 竖直方向剪切，很少使用
    //clip_horizontal   水平方向剪切，很少使用
    android:scaleGravity=["top" | "bottom" | "left" | "right" | "center_vertical" |
                          "fill_vertical" | "center_horizontal" | "fill_horizontal" |
                          "center" | "fill" | "clip_vertical" | "clip_horizontal"]
    //android:scaleHeight表示Drawable的高的缩放比例，值越大，内部Drawable的高度显示得越小，  
    //例如android:scaleHeight=”70%”,那么显示时Drawable的高度只有原来的30%。
    android:scaleHeight="percentage"  
    //android:scaleWidth表示Drawable的宽的缩放比例，值越大，内部Drawable的宽显示得越小，
    //例如android:scaleWidth=”70%”,那么显示时Drawable的宽度只有原来的30%。
    android:scaleWidth="percentage" />
```
#### 11.3 ScaleDrawable使用案例
大致效果
![](//upload-images.jianshu.io/upload_images/2851519-efed785294b55bb9.png?imageMogr2/auto-orient/strip|imageView2/2/w/1200/format/webp#align=left&display=inline&height=942&originHeight=942&originWidth=1200&status=done&style=none&width=1200)
第一步：xml文件定义

```
<?xml version="1.0" encoding="utf-8"?>
<scale xmlns:android="http://schemas.android.com/apk/res/android"
    android:drawable="@drawable/shape_oval"
    android:scaleGravity="center"
    android:scaleHeight="20%"
    android:scaleWidth="20%"></scale>
```
第二步：代码定义
当设置为imageview的src属性

```
ScaleDrawable drawable = (ScaleDrawable) ivShow.getDrawable();
drawable.setLevel(1);
```
当设置为textview的background属性

```
ScaleDrawable scale= (ScaleDrawable) scaleImage.getBackground();
scale.setLevel(1);
```
### 9. ClipDrawable
ClipDrawable对应的是，它可以根据自己的等级裁剪另一个Drawable，裁剪的方法可以通过android:clipOrientation
```
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:clipOrientation="vertical"
    android:drawable="@drawable/ic_launcher"
    android:gravity="center">

</clip>

```
其中clipOrientation标示裁剪方向，有水平和竖直，gravity比较复杂，需要和clipOrientation才有作用，具体如图：![](https://cdn.nlark.com/yuque/0/2020/png/754789/1583422225371-0ee4b2a9-ac90-4edd-b800-fba6e8971d8a.png#align=left&display=inline&height=1080&originHeight=1080&originWidth=1920&size=0&status=done&style=none&width=1920)
示例：来实现一张图片从上往下进行裁剪的效果

```
<?xml version="1.0" encoding="utf-8"?>
<clip xmlns:android="http://schemas.android.com/apk/res/android"
    android:clipOrientation="vertical"
    android:drawable="@drawable/ic_launcher"
    android:gravity="bottom">

</clip>

// 使用
clipDrawable = (ClipDrawable) imageView.getBackground();
clipDrawable.setLevel(5000);
```
## 三、自定义Drawable
说起自定义View，我们一般都会重写View或者ViewGroup来实现我们的需求，实际上有时候用自定义Drawable也可以实现我们的需求。自定义Drawable不需要OnLayout()和OnMeasure()，这样会省去很多的重新测量和布局。

自定义Drawable需要继承Drawable类，并且重写draw()，setAlpha()，setColorFilter，getOpacity()这四个方法。

1. draw ：draw方法我们已经非常熟悉了。再需要绘制图像时，会调用这个方法。Canvas是画布的意思，所以的绘制操作都会由它来完成。
1. setAlpha：这是一个设置透明度的方法。如果设置了透明度，那么可以传递给画笔Paint，0（0x00）表示完全透明，255（0xFF）表示完全不透明。
1. setColorFilter：如果一个Drawable设置了一个颜色过滤器，那么在绘制出来之前，被绘制内容的每一个像素都会被颜色过滤器改变。ColorFilter是一个抽象类，它有一个比较好用的子类ColorMatrixColorFilter，我们可以通过设置颜色矩阵来改变最终的显示效果。
1. getOpacity：这个方法的意思是获得不透明度。 有几个值：PixelFormat：UNKNOWN，TRANSLUCENT，TRANSPARENT，或者 OPAQUE。
  - ~OPAQUE：完全不透明，遮盖在他下面的所有内容
  - ~TRANSPARENT：透明，完全不显示任何东西
  - ~TRANSLUCENT：只有绘制的地方才覆盖底下的内容。

这个值，可以根据setAlpha中设置的值进行调整。比如，alpha == 0时设置为PixelFormat.TRANSPARENT。在alpha == 255时设置为PixelFormat.OPAQUE。在其他时候设置为PixelFormat.TRANSLUCENT。

