本文是学完HenCoder课程后的内容整理和心得体会（https://hencoder.com/）
## 自定义View分类
* 继承系统控件View，例如：TextView、ImageView等
* 继承系统ViewGroup，例如LinearLayout、FrameLayout等（一般指自定义组合控件）
* 继承View，重写onMeasure()、onDraw()等，纯自己测量绘制
* 继承ViewGroup，重写onMeasure()、onLayout()，纯自己测量布局

## 自定义View绘制
```
* 自定义绘制的方式是重写绘制方法，其中最常用的是 onDraw()
* 绘制的关键是 Canvas 的使用 
    * Canvas 的绘制类方法： drawXXX() （关键参数：Paint）
    * Canvas 的辅助类方法：范围裁切(clipXXX()等)和几何变换(Matrix)
* 可以使用不同的绘制方法来控制遮盖关系(绘制顺序：背景、主体、子View、滑动边缘渐变和滑动条、前景)
```
自定义绘制的四个级别(来源于HenCoder自定义View 1-1 绘制基础)：

1. Canvas 的 drawXXX() 系列方法及 Paint 最常见的使用

    Canvas.drawXXX() 是自定义绘制最基本的操作。掌握了这些方法，你才知道怎么绘制内容，例如怎么画圆、怎么画方、怎么画图像和文字。组合绘制这些内容，再配合上 Paint 的一些常见方法来对绘制内容的颜色和风格进行简单的配置，就能够应付大部分的绘制需求了。
2. Paint 的完全攻略

    Paint 可以做的事，不只是设置颜色，也不只是我在视频里讲的实心空心、线条粗细、有没有阴影，它可以做的风格设置真的是非常多、非常细。
3. Canvas 对绘制的辅助——范围裁切和几何变换。

    大多数时候，它们并不会被用到，但一旦用到，通常都是很炫酷的效果。范围裁切和几何变换都是用于辅助的，它们本身并不酷，让它们变酷的是设计师们的想象力与创造力。而你要做的，是把他们的想象力与创造力变成现实。
4. 使用不同的绘制方法来控制绘制顺序
    
    控制绘制顺序解决的并不是「做不到」的问题，而是性能问题。同样的一种效果，你不用绘制顺序的控制往往也能做到，但需要用多个 View 甚至是多层 View 才能拼凑出来，因此代价是 UI 的性能；而使用绘制顺序的控制的话，一个 View 就全部搞定了。

接下来总结一下上面四部分应用的api（来源于HenCoder自定义View 1-1 -- 1-5）：

一大波知识点将要来临：

![](https://user-gold-cdn.xitu.io/2019/6/29/16ba16516ff524dd?w=240&h=240&f=png&s=40532)
```
前景提要：View的坐标系
    在 Android 里，每个 View 都有一个自己的坐标系，彼此之间是不影响的。
    这个坐标系的原点是 View 左上角的那个点；水平方向是 x 轴，右正左负；
    竖直方向是 y 轴，下正上负（注意，是下正上负，不是上正下负，和上学时候学的坐标系方向不一样）。
1.Canvas 的 drawXXX() 系列方法及 Paint 最常见的使用
    Canvas 类下的所有 draw- 打头的方法，例如 drawCircle() drawBitmap()。
        Canvas.drawColor(@ColorInt int color) 颜色填充
        drawCircle(float centerX, float centerY, float radius, Paint paint) 画圆
            前两个参数 centerX centerY 是圆心的坐标，第三个参数 radius 是圆的半径，单位都是像素，
            它们共同构成了这个圆的基本信息（即用这几个信息可以构建出一个确定的圆）；
            第四个参数 paint我在视频里面已经说过了，它提供基本信息之外的所有风格信息，例如颜色、线条粗细、阴影等。
        drawRect(float left, float top, float right, float bottom, Paint paint) 画矩形
            left, top, right, bottom 是矩形四条边的坐标。
        drawPoint(float x, float y, Paint paint) 画点
            x 和 y 是点的坐标。点的大小可以通过 paint.setStrokeWidth(width) 来设置；
            点的形状可以通过  paint.setStrokeCap(cap) 来设置：ROUND画出来是圆形的点，SQUARE 或 BUTT 画出来是方形的点。
        drawOval(float left, float top, float right, float bottom, Paint paint) 画椭圆
            只能绘制横着的或者竖着的椭圆，不能绘制斜的（斜的倒是也可以，但不是直接使用 drawOval()，而是配合几何变换，后面会讲到）。
            left, top, right, bottom 是这个椭圆的左、上、右、下四个边界点的坐标。
        drawLine(float startX, float startY, float stopX, float stopY, Paint paint) 画线
            startX, startY, stopX, stopY 分别是线的起点和终点坐标。
        drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) 画圆角矩形
            left, top, right, bottom 是四条边的坐标，rx 和 ry 是圆角的横向半径和纵向半径。
        drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) 绘制弧形或扇形
            drawArc() 是使用一个椭圆来描述弧形的。left, top, right, bottom 描述的是这个弧形所在的椭圆；
            startAngle 是弧形的起始角度（x 轴的正向，即正右的方向，是 0 度的位置；顺时针为正角度，逆时针为负角度），sweepAngle 是弧形划过的角度；
            useCenter 表示是否连接到圆心，如果不连接到圆心，就是弧形，如果连接到圆心，就是扇形。
        drawPath(Path path, Paint paint) 画自定义图形
            Path 可以描述直线、二次曲线、三次曲线、圆、椭圆、弧形、矩形、圆角矩形。把这些图形结合起来，就可以描述出很多复杂的图形。
            Path 方法第一类：直接描述路径。addXxx()——添加子图形 或者 xxxTo()——画线（直线或曲线）
                addCircle(float x, float y, float radius, Direction dir) 添加圆
                    x, y, radius 这三个参数是圆的基本信息，最后一个参数 dir 是画圆的路径的方向。
                addOval(float left, float top, float right, float bottom, Direction dir) / addOval(RectF oval, Direction dir) 添加椭圆
                addRect(float left, float top, float right, float bottom, Direction dir) / addRect(RectF rect, Direction dir) 添加矩形
                addRoundRect(RectF rect, float rx, float ry, Direction dir) / addRoundRect(float left, float top, float right, float bottom, float rx, float ry, Direction dir) / addRoundRect(RectF rect, float[] radii, Direction dir) / addRoundRect(float left, float top, float right, float bottom, float[] radii, Direction dir) 添加圆角矩形
                addPath(Path path) 添加另一个 Path
                lineTo(float x, float y) / rLineTo(float x, float y) 画直线
                quadTo(float x1, float y1, float x2, float y2) / rQuadTo(float dx1, float dy1, float dx2, float dy2) 画二次贝塞尔曲线
                cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) / rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) 画三次贝塞尔曲线
                moveTo(float x, float y) / rMoveTo(float x, float y) 移动到目标位置
                arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) / arcTo(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) / arcTo(RectF oval, float startAngle, float sweepAngle) 画弧形
                close() 封闭当前子图形
            Path 方法第二类：辅助的设置或计算，这类方法的使用场景比较少，例如：Path.setFillType(Path.FillType ft) 设置填充方式
        drawBitmap(Bitmap bitmap, float left, float top, Paint paint) 画 Bitmap
            绘制 Bitmap 对象，也就是把这个 Bitmap 中的像素内容贴过来。其中 left 和 top 是要把 bitmap 绘制到的位置坐标。
        drawText(String text, float x, float y, Paint paint) 绘制文字
            界面里所有的显示内容，都是绘制出来的，包括文字。 drawText() 这个方法就是用来绘制文字的。参数  text 是用来绘制的字符串，x 和 y 是绘制的起点坐标。
    Paint 类的几个最常用的方法。具体是： 
        Paint.setStyle(Style style) 设置绘制模式
        Paint.setColor(int color) 设置颜色
        Paint.setStrokeWidth(float width) 设置线条宽度
        Paint.setTextSize(float textSize) 设置文字大小
        Paint.setAntiAlias(boolean aa) 设置抗锯齿开关
2. Paint 的完全攻略：Paint 的 API 大致可以分为 4 类：
    颜色：
        直接设置颜色：
            setColor(int color)
            setARGB(int a, int r, int g, int b)
        设置 Shader：Paint.setShader(Shader shader) 
            LinearGradient 线性渐变
            RadialGradient 辐射渐变
            SweepGradient 扫描渐变
            BitmapShader 用 Bitmap 来着色
                跟 Canvas.drawBitmap() 一样的效果。如果你想绘制圆形的 Bitmap，
                就别用 drawBitmap() 了，改用 drawCircle() + BitmapShader就可以了（其他形状同理）。
            ComposeShader 混合着色器
        设置颜色过滤：Paint.setColorFilter(ColorFilter filter)
            LightingColorFilter 用来模拟简单的光照效果的（改变R、G、B颜色值的比例多少）
            PorterDuffColorFilter 是使用一个指定的颜色和一种指定的 PorterDuff.Mode 来与绘制对象进行合成。
            ColorMatrixColorFilter 使用一个 ColorMatrix 来对颜色进行处理。 ColorMatrix 这个类，内部是一个 4x5 的矩阵，ColorMatrix 可以把要绘制的像素进行转换。
        Paint.setXfermode(Xfermode xfermode)：Xfermode 指的是你要绘制的内容和 Canvas 的目标位置的内容应该怎样结合计算出最终的颜色。
          但通俗地说，其实就是要你以绘制的内容作为源图像，以 View中已有的内容作为目标图像，选取一个  PorterDuff.Mode 作为绘制内容的颜色处理方案。
            PorterDuffXfermode Xfermode只有这一个子类
            Xfermode 注意事项：
                1. 使用离屏缓冲（Off-screen Buffer）
                    Canvas.saveLayer() 没有特殊要求使用此方法，性能高
                    View.setLayerType()
    效果：
        setAntiAlias (boolean aa) 设置抗锯齿
        setStyle(Paint.Style style)
        线条形状：
            setStrokeWidth(float width) 单位为像素，默认值是 0，0和1的区别在于几何变换
            setStrokeCap(Paint.Cap cap) 设置线头的形状。线头形状有三种：BUTT 平头、ROUND 圆头、SQUARE 方头。默认为 BUTT。
            setStrokeJoin(Paint.Join join) 设置拐角的形状。有三个值可以选择：MITER 尖角、 BEVEL 平角和 ROUND 圆角。默认为 MITER。
            setStrokeMiter(float miter) 这个方法是对于 setStrokeJoin() 的一个补充，它用于设置 MITER 型拐角的延长线的最大值。
        色彩优化：
            setDither(boolean dither) 设置图像的抖动。
            setFilterBitmap(boolean filter) 设置是否使用双线性过滤来绘制 Bitmap 。
            setPathEffect(PathEffect effect) 使用 PathEffect 来给图形的轮廓设置效果。对 Canvas 所有的图形绘制有效
                CornerPathEffect 把所有拐角变成圆角。
                DiscretePathEffect 把线条进行随机的偏离，让轮廓变得乱七八糟。乱七八糟的方式和程度由参数决定。
                DashPathEffect 使用虚线来绘制线条。
                PathDashPathEffect 这个方法比 DashPathEffect 多一个前缀 Path ，所以顾名思义，它是使用一个 Path 来绘制「虚线」
                SumPathEffect 这是一个组合效果类的 PathEffect 。它的行为特别简单，就是分别按照两种 PathEffect 分别对目标进行绘制。
                ComposePathEffect 这也是一个组合效果类的 PathEffect 。不过它是先对目标 Path 使用一个 PathEffect，然后再对这个改变后的 Path 使用另一个 PathEffect。
            setShadowLayer(float radius, float dx, float dy, int shadowColor) 在之后的绘制内容下面加一层阴影。
            setMaskFilter(MaskFilter maskfilter) 为之后的绘制设置 MaskFilter。
              上一个方法 setShadowLayer() 是设置的在绘制层下方的附加效果；而这个  MaskFilter 和它相反，设置的是在绘制层上方的附加效果。
                BlurMaskFilter(float radius, BlurMaskFilter.Blur style) 模糊效果的 MaskFilter。
                    NORMAL: 内外都模糊绘制
                    SOLID: 内部正常绘制，外部模糊
                    INNER: 内部模糊，外部不绘制
                    OUTER: 内部不绘制，外部模糊
                EmbossMaskFilter 浮雕效果的 MaskFilter。
            获取绘制的 Path，例如：getFillPath(Path src, Path dst)
    drawText() 相关 ，Paint 有些设置是文字绘制相关的，即和 drawText() 相关的。
        drawText(String text, float x, float y, Paint paint)
        drawTextOnPath() 沿着一条 Path 来绘制文字。
        StaticLayout 绘制多行的文字，StaticLayout 并不是一个 View 或者 ViewGroup ，而是 android.text.Layout 的子类，它是纯粹用来绘制文字的。 
          StaticLayout 支持换行，它既可以为文字设置宽度上限来让文字自动换行，也会在 \n 处主动换行。
            StaticLayout(CharSequence source, TextPaint paint, int width, Layout.Alignment align, float spacingmult, float spacingadd, boolean includepad)
                width 是文字区域的宽度，文字到达这个宽度后就会自动换行； 
                align 是文字的对齐方向； 
                spacingmult 是行间距的倍数，通常情况下填 1 就好； 
                spacingadd 是行间距的额外增加值，通常情况下填 0 就好； 
                includepad 是指是否在文字上下添加额外的空间，来避免某些过高的字符的绘制出现越界。
        Paint 对文字绘制的辅助
            设置显示效果类：
                setTextSize(float textSize) 设置文字大小。
                setTypeface(Typeface typeface) 设置字体。
                setFakeBoldText(boolean fakeBoldText) 是否使用伪粗体。
                setStrikeThruText(boolean strikeThruText) 是否加删除线。
                setUnderlineText(boolean underlineText) 是否加下划线。
                setTextSkewX(float skewX) 设置文字横向错切角度，就是文字倾斜度。
                setTextScaleX(float scaleX) 设置文字横向放缩。也就是文字变胖变瘦。
                setLetterSpacing(float letterSpacing) 设置字符间距。默认值是 0。
                setFontFeatureSettings(String settings) 用 CSS 的 font-feature-settings 的方式来设置文字。
                setTextAlign(Paint.Align align) 设置文字的对齐方式。一共有三个值：LEFT CETNER 和 RIGHT。默认值为 LEFT。
                setTextLocale(Locale locale) / setTextLocales(LocaleList locales) 设置绘制所使用的 Locale。
                setHinting(int mode) 设置是否启用字体的 hinting （字体微调）。
                setSubpixelText(boolean subpixelText) 是否开启次像素级的抗锯齿（ sub-pixel anti-aliasing ）。
            测量文字尺寸类：
                float getFontSpacing() 获取推荐的行距。
                FontMetircs getFontMetrics() 获取 Paint 的 FontMetrics。类似于英文的三格四线，字体在Android系统上会有5条线
                    FontMetrics是个相对专业的工具类，它提供了几个文字排印方面的数值：ascent, descent, top, bottom,  leading。
                    另外，ascent 和 descent 这两个值还可以通过 Paint.ascent() 和 Paint.descent() 来快捷获取。
                    字体的高度可通过 descent - ascent 来获取
                getTextBounds(String text, int start, int end, Rect bounds) 获取文字的显示范围。
                    参数里，text 是要测量的文字，start 和 end 分别是文字的起始和结束位置，bounds 是存储文字显示范围的对象，方法在测算完成之后会把结果写进 bounds。
                float measureText(String text) 测量文字的宽度并返回。
                getTextWidths(String text, float[] widths) 获取字符串中每个字符的宽度，并把结果填入参数 widths。
            光标相关：
                getRunAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, int offset) 
                    对于一段文字，计算出某个字符处光标的 x 坐标。 start end 是文字的起始和结束坐标；contextStart contextEnd 是上下文的起始和结束坐标；isRtl 是文字的方向；offset 是字数的偏移，即计算第几个字符处的光标。
                getOffsetForAdvance(CharSequence text, int start, int end, int contextStart, int contextEnd, boolean isRtl, float advance)
                    给出一个位置的像素值，计算出文字中最接近这个位置的字符偏移量（即第几个字符最接近这个坐标）。
                    方法的参数很简单： text 是要测量的文字；start end 是文字的起始和结束坐标；contextStart contextEnd 是上下文的起始和结束坐标；isRtl 是文字方向；advance 是给出的位置的像素值。填入参数，对应的字符偏移量将作为返回值返回。
                    getOffsetForAdvance() 配合上 getRunAdvance() 一起使用，就可以实现「获取用户点击处的文字坐标」的需求。
                hasGlyph(String string) 检查指定的字符串中是否是一个单独的字形 (glyph）。最简单的情况是，string 只有一个字母（比如  a）。
    初始化：
        reset() 重置 Paint 的所有属性为默认值。相当于重新 new 一个，不过性能当然高一些啦。
        set(Paint src) 把 src 的所有属性全部复制过来。相当于调用 src 所有的 get 方法，然后调用这个 Paint 的对应的 set 方法来设置它们。
        setFlags(int flags) 批量设置 flags。相当于依次调用它们的 set 方法。
            paint.setFlags(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);  
            等价于 paint.setAntiAlias(true);  和 paint.setDither(true);
3. Canvas 对绘制的辅助——范围裁切和几何变换。
    前景提要：
        Canvas.save() 和 Canvas.restore() 及时保存和恢复绘制范围，分别用在范围裁切和几何变换前后
    范围裁切：
        Canvas.clipRect(left, top, right, bottom);  
        Canvas.clipPath()
    几何变换：
        使用 Canvas 来做常见的二维变换（多个变换，该种方式是倒着执行的，Canvas 的几何变换顺序是反的）
            Canvas.translate(float dx, float dy) 平移 
                参数里的 dx 和 dy 表示横向和纵向的位移。
            Canvas.rotate(float degrees, float px, float py) 旋转
                参数里的 degrees 是旋转角度，单位是度（也就是一周有 360° 的那个单位），方向是顺时针为正向； px 和 py 是轴心的位置。
            Canvas.scale(float sx, float sy, float px, float py) 放缩
                参数里的 sx sy 是横向和纵向的放缩倍数； px py 是放缩的轴心。
            Canvas.skew(float sx, float sy) 错切
                参数里的 sx 和 sy 是 x 方向和 y 方向的错切系数。
        使用 Matrix 来做变换
            使用 Matrix 来做常见变换（多个变换，顺序执行）
                创建 Matrix 对象；
                调用 Matrix 的 pre/postTranslate/Rotate/Scale/Skew() 方法来设置几何变换；
                使用 Canvas.setMatrix(matrix) 或 Canvas.concat(matrix) 来把几何变换应用到 Canvas。
            使用 Matrix 来做自定义变换
                Matrix.setPolyToPoly(float[] src, int srcIndex, float[] dst, int dstIndex, int pointCount) 用点对点映射的方式设置变换
                    setPolyToPoly() 的作用是通过多点的映射的方式来直接设置变换。「多点映射」的意思就是把指定的点移动到给出的位置，从而发生形变。
                    例如：(0, 0) -> (100, 100) 表示把 (0, 0) 位置的像素移动到 (100, 100) 的位置，这个是单点的映射，单点映射可以实现平移。而多点的映射，就可以让绘制内容任意地扭曲。
            使用 Camera 来做三维变换
                Camera.rotate*() 三维旋转
                    Camera.rotate*() 一共有四个方法： rotateX(deg) rotateY(deg) rotateZ(deg) rotate(x, y, z)
                Camera.translate(float x, float y, float z) 移动
                Camera.setLocation(x, y, z) 设置虚拟相机的位置
                    它的参数的单位不是像素，而是 inch，英寸。
                    Camera.setLocation(x, y, z) 的 x 和 y 参数一般不会改变，直接填 0 就好。
                    z值变大，相机后移，可使几何变换变大的图标变小
4. 使用不同的绘制方法来控制绘制顺序
    drawBackground() 绘制背景，不允许重写
    onDraw() 绘制主体
        写在 super.onDraw() 的上面
            如果把绘制代码写在 super.onDraw() 的上面，由于绘制代码会执行在原有内容的绘制之前，所以绘制的内容会被控件的原内容盖住。
    dispatchDraw()：绘制子 View 的方法
        写在 super.dispatchDraw() 的下面
            只要重写 dispatchDraw()，并在 super.dispatchDraw() 的下面写上你的绘制代码，这段绘制代码就会发生在子 View 的绘制之后，从而让绘制内容盖住子 View 了。
        写在 super.dispatchDraw() 的上面
            把绘制代码写在 super.dispatchDraw() 的上面，这段绘制就会在 onDraw() 之后、  super.dispatchDraw() 之前发生，也就是绘制内容会出现在主体内容和子 View 之间。
    onDrawForeground() API 23 才引入的，会依次绘制滑动边缘渐变、滑动条和前景。
        写在 super.onDrawForeground() 的下面
            如果你把绘制代码写在了 super.onDrawForeground() 的下面，绘制代码会在滑动边缘渐变、滑动条和前景之后被执行，那么绘制内容将会盖住滑动边缘渐变、滑动条和前景。
        写在 super.onDrawForeground() 的上面
            如果你把绘制代码写在了 super.onDrawForeground() 的上面，绘制内容就会在 dispatchDraw() 和  super.onDrawForeground() 之间执行，那么绘制内容会盖住子 View，但被滑动边缘渐变、滑动条以及前景盖住
    draw() 总调度方法
        写在 super.draw() 的下面
            由于 draw() 是总调度方法，所以如果把绘制代码写在 super.draw() 的下面，那么这段代码会在其他所有绘制完成之后再执行，也就是说，它的绘制内容会盖住其他的所有绘制内容。
        写在 super.draw() 的上面
            同理，由于 draw() 是总调度方法，所以如果把绘制代码写在 super.draw() 的上面，那么这段代码会在其他所有绘制之前被执行，所以这部分绘制内容会被其他所有的内容盖住，包括背景。是的，背景也会盖住它。
注意
关于绘制方法，有两点需要注意一下：
1.出于效率的考虑，ViewGroup 默认会绕过 draw() 方法，换而直接执行 dispatchDraw()，以此来简化绘制流程。所以如果你自定义了某个 ViewGroup 的子类（比如 LinearLayout）并且需要在它的除  dispatchDraw() 以外的任何一个绘制方法内绘制内容，你可能会需要调用 View.setWillNotDraw(false) 这行代码来切换到完整的绘制流程（是「可能」而不是「必须」的原因是，有些 ViewGroup 是已经调用过 setWillNotDraw(false) 了的，例如 ScrollView）。
2.有的时候，一段绘制代码写在不同的绘制方法中效果是一样的，这时你可以选一个自己喜欢或者习惯的绘制方法来重写。但有一个例外：如果绘制代码既可以写在 onDraw() 里，也可以写在其他绘制方法里，那么优先写在 onDraw() ，因为 Android 有相关的优化，可以在不需要重绘的时候自动跳过  onDraw() 的重复执行，以提升开发效率。享受这种优化的只有 onDraw() 一个方法。
```
具体效果和详细描述，建议查看HenCoder 自定义View 1-1 到 1-5 节。
## 自定义布局流程
两个阶段：测量阶段和布局阶段。

测量阶段：从上到下递归地调用每个 View 或者 ViewGroup 的 measure()方法，测量他们的尺寸并计算它们的位置； 

布局阶段：从上到下递归地调用每个 View 或者 ViewGroup 的 layout() 方法，把测得的它们的尺寸和位置赋值给它们。

**View 或 ViewGroup 的布局过程**(来源于HenCoder自定义布局 2-1 到 2-3)

1. 测量阶段，measure() 方法被父 View 调用，在 measure() 中做一些准备和优化工作后，调用  onMeasure() 来进行实际的自我测量。 onMeasure() 做的事，View 和 ViewGroup 不一样：
    1. View：View 在 onMeasure() 中会计算出自己的尺寸然后保存；
    2. ViewGroup：ViewGroup 在 onMeasure() 中会调用所有子 View 的 measure() 让它们进行自我测量，并根据子 View 计算出的期望尺寸来计算出它们的实际尺寸和位置（实际上 99.99% 的父 View 都会使用子 View 给出的期望尺寸来作为实际尺寸）然后保存。同时，它也会根据子 View 的尺寸和位置来计算出自己的尺寸然后保存；
2. 布局阶段，layout() 方法被父 View 调用，在 layout() 中它会保存父 View 传进来的自己的位置和尺寸，并且调用 onLayout() 来进行实际的内部布局。onLayout() 做的事， View 和 ViewGroup 也不一样：
    1. View：由于没有子 View，所以 View 的 onLayout() 什么也不做。
    2. ViewGroup：ViewGroup 在 onLayout() 中会调用自己的所有子 View 的 layout() 方法，把它们的尺寸和位置传给它们，让它们完成自我的内部布局。

**布局分类：**

* 重写 onMeasure() 来修改已有的 View 的尺寸；

    1. 重写 onMeasure() 方法，并在里面调用 super.onMeasure()，触发原有的自我测量；
    2. 在 super.onMeasure() 的下面用 getMeasuredWidth() 和 getMeasuredHeight() 来获取到之前的测量结果，并使用自己的算法，根据测量结果计算出新的结果；
    3. 调用 setMeasuredDimension() 来保存新的结果。
* 重写 onMeasure() 来全新定制自定义 View 的尺寸；

    重写 onMeasure() 方法，不调用 super.onMeasure()，完全自己测量
    1. 重新 onMeasure()，并计算出 View 的尺寸；
    2. 使用 resolveSize() 来让子 View 的计算结果符合父 View 的限制（当然，如果你想用自己的方式来满足父 View 的限制也行）。
* 重写 onMeasure() 和 onLayout() 来全新定制自定义 ViewGroup 的内部布局。

    重写 onMeasure() 的三个步骤：
    
        1. 调用每个子 View 的 measure() 来计算子 View 的尺寸
        2.计算子 View 的位置并保存子 View 的位置和尺寸
        3.计算自己的尺寸并用 setMeasuredDimension() 保存
    重写 onLayout() 的方式：
        
        在 onLayout() 里调用每个子 View 的 layout() ，让它们保存自己的位置和尺寸。
具体效果和详细描述，建议查看HenCoder 自定义布局 2-1 到 2-3 节，有视频讲解的哦。

## View的生命周期
    onFinishInflate() 当View中所有的子控件均被映射成xml后触发 
    onMeasure( int ,  int ) 确定所有子元素的大小 
    onLayout( boolean ,  int ,  int ,  int ,  int ) 当View分配所有的子元素的大小和位置时触发     
    onSizeChanged( int ,  int ,  int ,  int ) 当view的大小发生变化时触发  
    onDraw(Canvas) view渲染内容的细节  
    onKeyDown( int , KeyEvent) 有按键按下后触发  
    onKeyUp( int , KeyEvent) 有按键按下后弹起时触发  
    onTrackballEvent(MotionEvent) 轨迹球事件  
    onTouchEvent(MotionEvent) 触屏事件  
    onFocusChanged( boolean ,  int , Rect) 当View获取或失去焦点时触发   
    onWindowFocusChanged( boolean ) 当窗口包含的view获取或失去焦点时触发  
    onAttachedToWindow() 当view被附着到一个窗口时触发  
    onDetachedFromWindow() 当view离开附着的窗口时触发和onAttachedToWindow() 是相反的。  
    onWindowVisibilityChanged( int ) 当窗口中包含的可见的view发生变化时触发
    
![](https://user-gold-cdn.xitu.io/2019/6/29/16b9eec6b786b697?w=960&h=1266&f=png&s=221253)
## 案例
通过学习HenCoder的自定义View和布局，也看了看HenCoder「仿写酷界面」活动——征稿中的几个综合练习，自己看懂了3个半，原谅我就看了一半的“小米运动首页顶部的运动记录界面”仿写代码。最后为了更好的掌握和理解，自己尝试着实现了一下“即刻的点赞效果”，自己仿写的不太好，不过绘制和布局还是可以讲一讲我自己的看法：
```
// 感兴趣的同学可以看一看，请忽略动画部分，哈哈哈，文字的动画还没做，缩放的动画做的也不太好
package com.android.customwidget.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.android.customwidget.R;

public class ThumbUpView extends View implements View.OnClickListener {

    private Bitmap selected;
    private Bitmap unselected;
    private Bitmap shining;
    private Paint paintIcon;
    private Paint paintText;
    private Paint paintCircle;

    // 点赞数量
    private int likeNumber;
    // 图标和文字间距
    private int widthSpace;
    private int textHeight;
    // 文字的绘制是基于baseline的，而高度则是通过descent - ascent获取的
    private int textDescentAndBaselineSpace;
    // 火花和点赞图标之间的间距，此值为负
    private int shinAndThubSpace;

    private Path mClipPath = new Path();

    private float SCALE_MIN = 0.9f;
    private float SCALE_MAX = 1f;
    private float mScale = SCALE_MIN;
    private float mUnScale = SCALE_MAX;

    private int alpha;
    private int alphaStart = 64;
    private int alphaEnd = 0;
    private float radius = 24;
    private float radiusStart = 0;
    private float radiusEnd;

    // 是否是喜爱
    private boolean isLike = false;

    public ThumbUpView(Context context) {
        super(context);
        init();
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThumbUpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        likeNumber = 0;
        widthSpace = dip2px(5);
        shinAndThubSpace = -dip2px(8);

        paintIcon = new Paint();
        paintIcon.setStyle(Paint.Style.STROKE);

        paintText = new Paint();
        paintText.setAntiAlias(true);
        paintText.setStyle(Paint.Style.STROKE);
        paintText.setTextSize(dip2px(14));
        paintText.setColor(getResources().getColor(R.color.comm_main_color));

        paintCircle = new Paint();
        paintCircle.setColor(Color.RED);
        paintCircle.setAntiAlias(true);
        paintCircle.setStyle(Paint.Style.STROKE);
        paintCircle.setStrokeWidth(5);

        // 获取文字高度
//        textHeight = (int) (paintText.descent() - paintText.ascent());
        Paint.FontMetrics fontMetrics = paintText.getFontMetrics();
        Paint.FontMetricsInt fm = paintText.getFontMetricsInt();
        float ascent = fontMetrics.ascent;
        float descent = fontMetrics.descent;
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        float leading = fontMetrics.leading;
        textHeight = (int) (descent - ascent);
        textDescentAndBaselineSpace = (int) (descent - leading);
        selected = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_selected);
        unselected = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_unselected);
        shining = BitmapFactory.decodeResource(getResources(), R.drawable.ic_messages_like_selected_shining);

        setOnClickListener(this);
    }

    /**
     * 大家都说继承自View的自定义控件需要重写onMeasure方法，为什么？
     *  其实如果我们不重写onMeasure方法，则父布局就不知道你到底多大，
     *  就会将其剩余的所有空间都给你，此时如果还需要别的控件添加进父布局，
     *  则会出现没有空间显示该多余出的控件，因此我们需要自己测量我们到底有多大
     * 
     * 此处实际上需要根据widthMeasureSpec和heightMeasureSpec中的mode去分别设置宽高
     * 不过自定义View的测量可以根据自己的期望来设置
     * 测量无非就是布局中设置的padding值+你自己设定的间距+图标宽高+文字宽高...
     * */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = getMeasureWidth();
        int measureHeight = getMeasureHeight();
        int i = resolveSize(measureWidth, widthMeasureSpec);
        int j = resolveSize(measureHeight, heightMeasureSpec);
        setMeasuredDimension(i, j);
    }

    // 根据mode和实际测量设置宽，本View未采用
    private int getWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        // 可用空间
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getMeasureWidth();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                /**
                 * 此处可以不比较大小，因为用户的需要大于一切，
                 * 那么我们会说如果我们自己测量的宽大于上面的result(specSize)怎么办？那当然是出现Bug啦，
                 * 因此若加了 Math.max(getMeasureWidth(), result) 处理则会避免由用户设置的过大而导致的Bug
                 * 不过虽然可以避免用户设置导致的Bug，但是可能需要开发此View的人依旧需要做相应的处理
                 * */
                result = Math.max(getMeasureWidth(), result);
                break;
        }
        return result;
    }

    // 根据mode和实际测量设置高，本View未采用
    private int getHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                result = specSize;
                break;
            case MeasureSpec.AT_MOST:
                result = getMeasureHeight();
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                // 同理getWidth方法
                result = Math.max(getMeasureHeight(), result);
                break;
        }
        return result;
    }

    // 获取测量的宽
    private int getMeasureWidth() {
        int widthResult = 0;
        // 3 * widthSpace : 图标左侧、图标与文字中间、文字右侧都设置 5dp 间距
        widthResult += selected.getWidth() + 3 * widthSpace + paintText.measureText(likeNumber + "");
        // 一定不要忘记累加padding值
        widthResult += getPaddingLeft() + getPaddingRight();
        return widthResult;
    }

    // 获取测量的高
    private int getMeasureHeight() {
        int heightResult = 0;

        // 获取点赞图标以及点赞火花图标组合后的高度
        // , shinAndThubSpace 的原因是两图标组合并非是上下并列，而是火花会靠近点赞图标
        int iconHeight = selected.getHeight() + shining.getHeight() + shinAndThubSpace;
        heightResult = Math.max(textHeight, iconHeight);
        heightResult += getPaddingTop() + getPaddingBottom();
        return heightResult;
    }

    // 周期函数--View大小发生改变时回调
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.e("onSizeChanged", "onSizeChanged ： width == " + w + "  height == " + h + " oldw == " + oldw + " oldh == " + oldh);
        radiusEnd = getCircleData()[2] + 3;
    }

    /**
     * 绘制的位置，一般在测量时已经确定好其位置
     * 根据padding和自己设置的间距(没有则为0)以及想画的位置确定坐标
     * */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIcon(canvas);
        drawNumber(canvas);
    }

    // 画图标
    private void drawIcon(Canvas canvas) {
        float left = widthSpace + getPaddingLeft();
        float top = shining.getHeight() + getPaddingTop() + shinAndThubSpace;
        Log.e("getMeasureWidth", "getMeasure == " + getMeasureWidth());
        if (isLike) {
            float shinLeft = left + selected.getWidth() / 2 - shining.getWidth() / 2;
            float shinTop = getPaddingTop();
            canvas.drawBitmap(shining, shinLeft, shinTop, paintIcon);

            canvas.save();
            canvas.scale(mScale, mScale);
            canvas.drawBitmap(selected, left, top, paintIcon);
            canvas.restore();

            float[] circleData = getCircleData();
            paintCircle.setAlpha(alpha);
            canvas.drawCircle(circleData[0], circleData[1], radius, paintCircle);
        } else {
            canvas.save();
            canvas.scale(mUnScale, mUnScale);
            canvas.drawBitmap(unselected, left, top, paintIcon);
            canvas.restore();
        }
    }

    // 画数字
    private void drawNumber(Canvas canvas) {
        Log.e("getMeasureHeight", "getMeasure == " + getMeasureHeight() + "  " + textDescentAndBaselineSpace);
        float left = selected.getWidth() + 2 * widthSpace + getPaddingLeft();
        float top = shining.getHeight() + getPaddingTop() + shinAndThubSpace + selected.getHeight() / 2 + textHeight / 2 - textDescentAndBaselineSpace;
        canvas.drawText(likeNumber + "", left, top, paintText);
    }

    // 获取圆的信息-- 圆中心位置(坐标)、和半径
    private float[] getCircleData() {
        // 此圆最大要完全包裹点赞图标和火花图标，因此其圆心Y坐标要在点赞和火花图标整体的中心
        float centerX = getPaddingLeft() + widthSpace + selected.getWidth() / 2;
        float iconHeight = shining.getHeight() + selected.getHeight() + shinAndThubSpace;
        float centerY = getPaddingTop() + iconHeight / 2;
        float iconWidthMax = Math.max(shining.getWidth(), selected.getWidth());
        float radius = Math.max(iconWidthMax, iconHeight) / 2;
        return new float[]{centerX, centerY,radius};
    }
    
    // --------------------------------Animate Start-------------------------------------

    @Override
    public void onClick(View v) {
        if (isLike) {
            likeNumber--;
            showThumbDownAnim();
        } else {
            likeNumber++;
            showThumbUpAnim();
        }
    }

    private float getCircleRadiusAnim() {
        return radius;
    }

    // 圆半径大小动画
    public void setCircleRadiusAnim(float rudiusAnim) {
        radius = rudiusAnim;
        /**
         * invalidate方法和postInvalidate方法都是用于进行View的刷新。
         * invalidate方法应用在UI线程中，而postInvalidate方法应用在非UI线程中，
         * 用于将线程切换到UI线程，postInvalidate方法最后调用的也是invalidate方法。
         */
        invalidate(); // postInvalidate();
    }

    private int getCircleColorAnim() {
        return alpha;
    }

    // 透明度动画
    public void setCircleColorAnim(int alphaAnim) {
        alpha = alphaAnim;
        invalidate();
    }

    public float getUnSelectAnim() {
        return mUnScale;
    }

    // 取消点赞图标缩放动画
    public void setUnSelectAnim(float scaleSize) {
        mUnScale = scaleSize;
        invalidate();
    }

    public float getSelectAnim() {
        return mScale;
    }

    // 点赞图标缩放动画
    public void setSelectAnim(float scaleSize) {
        mScale = scaleSize;
        invalidate();
    }

    /**
     * 展示点赞动画
     * */
    public void showThumbUpAnim() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "selectAnim", SCALE_MIN, SCALE_MAX);
        animator1.setDuration(150);
        animator1.setInterpolator(new OvershootInterpolator());

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "unSelectAnim", SCALE_MAX, SCALE_MIN);
        animator2.setDuration(150);
        animator2.addListener(new ClickAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isLike = true;
            }
        });

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator animator3 = ObjectAnimator.ofInt(this, "circleColorAnim", alphaStart, alphaEnd);
        animator3.setInterpolator(new DecelerateInterpolator());

        @SuppressLint("ObjectAnimatorBinding")
        ObjectAnimator animator4 = ObjectAnimator.ofFloat(this, "circleRadiusAnim", radiusStart, radiusEnd);
        animator4.setDuration(150);

        AnimatorSet set = new AnimatorSet();
        set.play(animator1).with(animator3).with(animator4);
        set.play(animator1).after(animator2);
        set.start();
    }

    /**
     * 展示取消点赞动画
     * */
    public void showThumbDownAnim() {
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(this, "selectAnim", SCALE_MAX, SCALE_MIN);
        animator1.setDuration(150);
        animator1.addListener(new ClickAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isLike = false;
            }
        });

        ObjectAnimator animator2 = ObjectAnimator.ofFloat(this, "unSelectAnim", SCALE_MIN, SCALE_MAX);
        animator2.setDuration(150);
        animator2.setInterpolator(new OvershootInterpolator());

        AnimatorSet set = new AnimatorSet();
        set.play(animator2).before(animator1);
        set.start();
    }

    /**
     * 动画监听
     * */
    private abstract class ClickAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);

        }

        @Override
        public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);

        }
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    // --------------------------------Animate End---------------------------------------
}
```
## 扩展之属性动画
**ViewPropertyAnimator**

使用方式：View.animate() 后跟 translationX() 等方法，动画会自动执行。具体可以跟的方法以及方法所对应的 View 中的实际操作的方法如下图所示：

![](https://user-gold-cdn.xitu.io/2019/6/29/16ba0cb8d20a5c35?w=1000&h=706&f=png&s=266807)

**ObjectAnimator**
使用方式：

    1. 如果是自定义控件，需要添加 setter / getter 方法；
    2. 用 ObjectAnimator.ofXXX() 创建 ObjectAnimator 对象；
    3. 用 start() 方法执行动画。
示例：
```
public class SportsView extends View {  
    float progress = 0;
    ......

    // 创建 getter 方法
    public float getProgress() {
        return progress;
    }

    // 创建 setter 方法
    public void setProgress(float progress) {
        this.progress = progress;
        // setter 方法记得加 invalidate()刷新绘制哦
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ......
        canvas.drawArc(arcRectF, 135, progress * 2.7f, false, paint);
        ......
    }
}
......

// 创建 ObjectAnimator 对象
ObjectAnimator animator = ObjectAnimator.ofFloat(view, "progress", 0, 65);  
// 执行动画
animator.start();  
```
**通用功能**

1. setDuration(int duration) 设置动画时长  单位是毫秒。
2. setInterpolator(Interpolator interpolator) 设置 Interpolator，Interpolator 其实就是速度设置器。你在参数里填入不同的 Interpolator ，动画就会以不同的速度模型来执行。

    * AccelerateDecelerateInterpolator 先加速再减速。这是默认的 Interpolator
    * LinearInterpolator 匀速。
    * AccelerateInterpolator 持续加速。在整个动画过程中，一直在加速，直到动画结束的一瞬间，直接停止。
    * DecelerateInterpolator 持续减速直到 0。动画开始的时候是最高速度，然后在动画过程中逐渐减速，直到动画结束的时候恰好减速到 0。
    * AnticipateInterpolator 先回拉一下再进行正常动画轨迹。效果看起来有点像投掷物体或跳跃等动作前的蓄力。
    * OvershootInterpolator 动画会超过目标值一些，然后再弹回来。效果看起来有点像你一屁股坐在沙发上后又被弹起来一点的感觉。
    * AnticipateOvershootInterpolator 上面这两个的结合版：开始前回拉，最后超过一些然后回弹。
    * BounceInterpolator 在目标值处弹跳。有点像玻璃球掉在地板上的效果。
    * CycleInterpolator 这个也是一个正弦 / 余弦曲线，不过它和 AccelerateDecelerateInterpolator 的区别是，它可以自定义曲线的周期，所以动画可以不到终点就结束，也可以到达终点后回弹，回弹的次数由曲线的周期决定，曲线的周期由 CycleInterpolator() 构造方法的参数决定。
    * PathInterpolator 自定义动画完成度 / 时间完成度曲线。用这个 Interpolator 你可以定制出任何你想要的速度模型。定制的方式是使用一个 Path 对象来绘制出你要的动画完成度 / 时间完成度曲线。
    * FastOutLinearInInterpolator 加速运动。这个 Interpolator 的作用你不能看它的名字，一会儿 fast 一会儿 linear 的，完全看不懂。其实它和  AccelerateInterpolator 一样，都是一个持续加速的运动路线。只不过 FastOutLinearInInterpolator 的曲线公式是用的贝塞尔曲线，而 AccelerateInterpolator 用的是指数曲线。具体来说，它俩最主要的区别是  FastOutLinearInInterpolator 的初始阶段加速度比 AccelerateInterpolator 要快一些。
    * FastOutSlowInInterpolator 先加速再减速。同样也是先加速再减速的还有前面说过的 AccelerateDecelerateInterpolator，不过它们的效果是明显不一样的。FastOutSlowInInterpolator 用的是贝塞尔曲线，AccelerateDecelerateInterpolator 用的是正弦 / 余弦曲线。具体来讲， FastOutSlowInInterpolator 的前期加速度要快得多。
    * LinearOutSlowInInterpolator 持续减速。它和 DecelerateInterpolator 比起来，同为减速曲线，主要区别在于 LinearOutSlowInInterpolator 的初始速度更高。对于人眼的实际感觉，区别其实也不大，不过还是能看出来一些的。

**设置监听器**

1. ViewPropertyAnimator.setListener() / ObjectAnimator.addListener()

    * onAnimationStart(Animator animation) 当动画开始执行时，这个方法被调用。
    * onAnimationEnd(Animator animation) 当动画结束时，这个方法被调用。
    * onAnimationCancel(Animator animation) 当动画被通过 cancel() 方法取消时，这个方法被调用。需要说明一下的是，就算动画被取消，onAnimationEnd() 也会被调用。所以当动画被取消时，如果设置了  AnimatorListener，那么 onAnimationCancel() 和 onAnimationEnd() 都会被调用。onAnimationCancel() 会先于 onAnimationEnd() 被调用。
    * onAnimationRepeat(Animator animation) 当动画通过 setRepeatMode() / setRepeatCount() 或 repeat() 方法重复执行时，这个方法被调用。由于 ViewPropertyAnimator 不支持重复，所以这个方法对 ViewPropertyAnimator 相当于无效。
2. ViewPropertyAnimator.setUpdateListener() / ObjectAnimator.addUpdateListener()

    * onAnimationUpdate(ValueAnimator animation)
当动画的属性更新时（不严谨的说，即每过 10 毫秒，动画的完成度更新时），这个方法被调用。
方法的参数是一个 ValueAnimator，ValueAnimator 是 ObjectAnimator 的父类，也是 ViewPropertyAnimator 的内部实现，所以这个参数其实就是 ViewPropertyAnimator 内部的那个 ValueAnimator，或者对于  ObjectAnimator 来说就是它自己本身。
3. ObjectAnimator.addPauseListener()
4. ViewPropertyAnimator.withStartAction/EndAction()

    * withStartAction() / withEndAction() 是一次性的，在动画执行结束后就自动弃掉了，就算之后再重用  ViewPropertyAnimator 来做别的动画，用它们设置的回调也不会再被调用。而 set/addListener() 所设置的 AnimatorListener 是持续有效的，当动画重复执行时，回调总会被调用。
    * withEndAction() 设置的回调只有在动画正常结束时才会被调用，而在动画被取消时不会被执行。这点和 AnimatorListener.onAnimationEnd() 的行为是不一致的。

**TypeEvaluator**

    关于 ObjectAnimator，上期讲到可以用 ofInt() 来做整数的属性动画和用 ofFloat() 来做小数的属性动画。
    这两种属性类型是属性动画最常用的两种，不过在实际的开发中，可以做属性动画的类型还是有其他的一些类型。
    当需要对其他类型来做属性动画的时候，就需要用到 TypeEvaluator 了。
1. ArgbEvaluator 颜色渐变的动画。 
2. 自定义 Evaluator 如果你对 ArgbEvaluator 的效果不满意，或者你由于别的什么原因希望写一个自定义的 TypeEvaluator，你可以这样写：
```
// 自定义 HslEvaluator
private class HsvEvaluator implements TypeEvaluator<Integer> {  
   float[] startHsv = new float[3];
   float[] endHsv = new float[3];
   float[] outHsv = new float[3];

   @Override
   public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
       // 把 ARGB 转换成 HSV
       Color.colorToHSV(startValue, startHsv);
       Color.colorToHSV(endValue, endHsv);

       // 计算当前动画完成度（fraction）所对应的颜色值
       if (endHsv[0] - startHsv[0] > 180) {
           endHsv[0] -= 360;
       } else if (endHsv[0] - startHsv[0] < -180) {
           endHsv[0] += 360;
       }
       outHsv[0] = startHsv[0] + (endHsv[0] - startHsv[0]) * fraction;
       if (outHsv[0] > 360) {
           outHsv[0] -= 360;
       } else if (outHsv[0] < 0) {
           outHsv[0] += 360;
       }
       outHsv[1] = startHsv[1] + (endHsv[1] - startHsv[1]) * fraction;
       outHsv[2] = startHsv[2] + (endHsv[2] - startHsv[2]) * fraction;

       // 计算当前动画完成度（fraction）所对应的透明度
       int alpha = startValue >> 24 + (int) ((endValue >> 24 - startValue >> 24) * fraction);

       // 把 HSV 转换回 ARGB 返回
       return Color.HSVToColor(alpha, outHsv);
   }
}

ObjectAnimator animator = ObjectAnimator.ofInt(view, "color", 0xff00ff00);  
// 使用自定义的 HslEvaluator
animator.setEvaluator(new HsvEvaluator());  
animator.start();  
```
3. ofObject()借助于 TypeEvaluator，属性动画就可以通过 ofObject() 来对不限定类型的属性做动画了。
    
    1. 为目标属性写一个自定义的 TypeEvaluator
    2. 使用 ofObject() 来创建 Animator，并把自定义的 TypeEvaluator 作为参数填入
```
// 例如：
private class PointFEvaluator implements TypeEvaluator<PointF> {  
   PointF newPoint = new PointF();

   @Override
   public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
       float x = startValue.x + (fraction * (endValue.x - startValue.x));
       float y = startValue.y + (fraction * (endValue.y - startValue.y));

       newPoint.set(x, y);

       return newPoint;
   }
}

ObjectAnimator animator = ObjectAnimator.ofObject(view, "position",  
        new PointFEvaluator(), new PointF(0, 0), new PointF(1, 1));
animator.start();  
```
**PropertyValuesHolder 同一个动画中改变多个属性**

很多时候，你在同一个动画中会需要改变多个属性，例如在改变透明度的同时改变尺寸。如果使用  ViewPropertyAnimator，你可以直接用连写的方式来在一个动画中同时改变多个属性，而对于 ObjectAnimator，是不能这么用的。不过你可以使用 PropertyValuesHolder 来同时在一个动画中改变多个属性。
```
PropertyValuesHolder holder1 = PropertyValuesHolder.ofFloat("scaleX", 1);  
PropertyValuesHolder holder2 = PropertyValuesHolder.ofFloat("scaleY", 1);  
PropertyValuesHolder holder3 = PropertyValuesHolder.ofFloat("alpha", 1);

ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, holder1, holder2, holder3)  
animator.start();  
```
PropertyValuesHolder 的意思从名字可以看出来，它是一个属性值的批量存放地。所以你如果有多个属性需要修改，可以把它们放在不同的 PropertyValuesHolder 中，然后使用 ofPropertyValuesHolder() 统一放进  Animator。这样你就不用为每个属性单独创建一个 Animator 分别执行了。

**AnimatorSet 多个动画配合执行**

有的时候，你不止需要在一个动画中改变多个属性，还会需要多个动画配合工作，比如，在内容的大小从 0 放大到 100% 大小后开始移动。这种情况使用 PropertyValuesHolder 是不行的，因为这些属性如果放在同一个动画中，需要共享动画的开始时间、结束时间、Interpolator 等等一系列的设定，这样就不能有先后次序地执行动画了。这就需要用到 AnimatorSet 了。
```
ObjectAnimator animator1 = ObjectAnimator.ofFloat(...);  
animator1.setInterpolator(new LinearInterpolator());  
ObjectAnimator animator2 = ObjectAnimator.ofInt(...);  
animator2.setInterpolator(new DecelerateInterpolator());

AnimatorSet animatorSet = new AnimatorSet();  
// 两个动画依次执行
animatorSet.playSequentially(animator1, animator2);  
animatorSet.start();  
```
使用 playSequentially()，就可以让两个动画依次播放，而不用为它们设置监听器来手动为他们监管协作。

AnimatorSet 还可以这么用：
```
// 两个动画同时执行
animatorSet.playTogether(animator1, animator2);  
animatorSet.start();  

// 使用 AnimatorSet.play(animatorA).with/before/after(animatorB)
// 的方式来精确配置各个 Animator 之间的关系
animatorSet.play(animator1).with(animator2);  
animatorSet.play(animator1).before(animator2);  
animatorSet.play(animator1).after(animator2);  
animatorSet.start(); 
```
**PropertyValuesHolders.ofKeyframe() 把同一个属性拆分**
除了合并多个属性和调配多个动画，你还可以在 PropertyValuesHolder 的基础上更进一步，通过设置  Keyframe （关键帧），把同一个动画属性拆分成多个阶段。例如，你可以让一个进度增加到 100% 后再「反弹」回来。
```
// 在 0% 处开始
Keyframe keyframe1 = Keyframe.ofFloat(0, 0);  
// 时间经过 50% 的时候，动画完成度 100%
Keyframe keyframe2 = Keyframe.ofFloat(0.5f, 100);  
// 时间见过 100% 的时候，动画完成度倒退到 80%，即反弹 20%
Keyframe keyframe3 = Keyframe.ofFloat(1, 80);  
PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe("progress", keyframe1, keyframe2, keyframe3);

ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, holder);  
animator.start();  
```
****
**ValueAnimator 最基本的轮子**

很多时候，你用不到它，只是在你使用一些第三方库的控件，而你想要做动画的属性却没有 setter / getter 方法的时候，会需要用到它。想具体了解的，可以查看<a href="https://www.jianshu.com/p/2412d00a0ce4">Android 属性动画：这是一篇很详细的 属性动画 总结&攻略</a>
    
## 扩展之滑动Scroller和VelocityTracker
自定义控件中，难免会遇到需要滑动的场景。而Canvas提供的scrollTo和scrollBy方法只能达到移动的效果，需要达到真正的滑动便需要两把基础利器Scroller和VelocityTracker。我个人并没有在项目中实战过，不过看的demo中会出现这两个类，因此就了解了一下其基础的API。

不过在了解Scroller和VelocityTracker之前我们需要先了解几点基础：

* getScrollX()、getScrollY()

    View X轴的偏移量，是相对自己初始位置的滑动偏移距离，只有当有scroll事件发生时，这两个方法才能有值，否则getScrollX()、getScrollY()都是初始时的值0。
    

![](https://user-gold-cdn.xitu.io/2019/6/29/16ba10ff4f470d8d?w=818&h=470&f=jpeg&s=59631)

* scrollTo()

    是绝对滚动(以view的内容的中心为原点，如果x为负值，则向右滚，y为负值向下滚)，基于开始位置滚动，如果已经滚动到了指定位置，重复调用不起作用。
* scrollBy()

    是相对滚动，内部调用了scrollTo，它是基于当前位置的相对滑动；不基于开始位置，就意味着可重复调用有效果。

**Scroller API：** (滚动的一个封装类)

* 构造方法

    (1) Scroller(Context context) 创建一个 Scroller 实例。
    
        参数解析：
        第一个参数 context： 上下文；
    (2) Scroller(Context context, Interpolator interpolator) 创建一个 Scroller 实例。
    
        参数解析：
        第一个参数 context： 上下文；
        第二个参数 interpolator： 插值器，用于在 computeScrollOffset 方法中，并且是在 SCROLL_MODE 模式下，根据时间的推移计算位置。为null时，使用默认 ViscousFluidInterpolator 插值器。
    (3) Scroller(Context context, Interpolator interpolator, boolean flywheel)创建一个 Scroller 实例。

        参数解析：
        第一个参数 context： 上下文；
        第二个参数 interpolator： 插值器，用于在 computeScrollOffset 方法中，并且是在 SCROLL_MODE 模式下，根据时间的推移计算位置。为null时，使用默认 ViscousFluidInterpolator 插值器。
        第三个参数 flywheel： 支持渐进式行为，该参数只作用于 FLING_MODE 模式下。
* 常用公有方法

    (1) setFriction(float friction) 用于设置在 FLING_MODE 模式下的摩擦系数

        参数解析：
        第一个参数 friction： 摩擦系数
    (2) isFinished() 滚动是否已结束，用于判断 Scroller 
    
        在滚动过程的状态，我们可以做一些终止或继续运行的逻辑分支。
    (3) forceFinished(boolean finished) 强制的让滚动状态置为我们所设置的参数值 finished 。
    
    (4)  getDuration() 返回 Scroller 将持续的时间（以毫秒为单位）。

    (5) getCurrX() 返回滚动中的当前X相对于原点的偏移量。

    (6) getCurrY() 返回滚动中的当前Y相对于原点的偏移量。
    
    (7) getCurrVelocity() 获取当前速度。
    
    (8) computeScrollOffset() 计算滚动中的新坐标，会配合着 getCurrX 和 getCurrY 
    
        方法使用，达到滚动效果。值得注意的是，如果返回true，说明动画还未完成。
        相反，返回false，说明动画已经完成或是被终止了。
    (9) startScroll public void startScroll(int startX, int startY, int dx, int dy) 通过提供起点，行程距离和滚动持续时间，进行滚动的一种方式，即 SCROLL_MODE。该方法可以用于实现像ViewPager的滑动效果。

        参数解析：
        第一个参数 startX： 开始点的x坐标
        第二个参数 startY： 开始点的y坐标
        第三个参数 dx： 水平方向的偏移量，正数会将内容向左滚动。
        第四个参数 dy： 垂直方向的偏移量，正数会将内容向上滚动。
        第五个参数 duration： 滚动的时长
    (10) fling public void fling(int startX, int startY, int velocityX, int velocityY,int minX, int maxX, int minY, int maxY) 用于带速度的滑动，行进的距离将取决于投掷的初始速度。可以用于实现类似 RecycleView 的滑动效果。

        参数解析：
        第一个参数 startX： 开始滑动点的x坐标
        第二个参数 startY： 开始滑动点的y坐标
        第三个参数 velocityX： 水平方向的初始速度，单位为每秒多少像素（px/s）
        第四个参数 velocityY： 垂直方向的初始速度，单位为每秒多少像素（px/s）
        第五个参数 minX： x坐标最小的值，最后的结果不会低于这个值；
        第六个参数 maxX： x坐标最大的值，最后的结果不会超过这个值；
        第七个参数 minY： y坐标最小的值，最后的结果不会低于这个值；
        第八个参数 maxY： y坐标最大的值，最后的结果不会超过这个值；

        值得一说：
        minX <= 终止值的x坐标 <= maxX
        minY <= 终止值的y坐标 <= maxY
    (11) abortAnimation()
public void abortAnimation()  停止动画，值得注意的是，此时如果调用 getCurrX() 和 getCurrY() 移动到的是最终的坐标，这一点和通过  forceFinished 直接将动画停止是不相同的。

<font size="3" color="red">特别说明：getCurrX()和getCurrY()</font> ，我们从源码看起
```
public class Scroller {
    private int mStartX;//水平方向，滑动时的起点偏移坐标
    private int mStartY;//垂直方向，滑动时的起点偏移坐标
    private int mFinalX;//滑动完成后的偏移坐标，水平方向
    private int mFinalY;//滑动完成后的偏移坐标，垂直方向

    private int mCurrX;//滑动过程中，根据消耗的时间计算出的当前的滑动偏移距离，水平方向
    private int mCurrY;//滑动过程中，根据消耗的时间计算出的当前的滑动偏移距离，垂直方向
    private int mDuration; //本次滑动的动画时间
    private float mDeltaX;//滑动过程中，在达到mFinalX前还需要滑动的距离，水平方向
    private float mDeltaY;//滑动过程中，在达到mFinalX前还需要滑动的距离，垂直方向
    ......
    public void startScroll(int startX, int startY, int dx, int dy) {
        startScroll(startX, startY, dx, dy, DEFAULT_DURATION);
    }

    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        mMode = SCROLL_MODE;
        mFinished = false;
        mDuration = duration;
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;
        mFinalX = startX + dx;
        mFinalY = startY + dy;
        mDeltaX = dx;
        mDeltaY = dy;
        mDurationReciprocal = 1.0f / (float) mDuration;
    }
    
    public void fling(int startX, int startY, int velocityX, int velocityY,
            int minX, int maxX, int minY, int maxY) {
        // Continue a scroll or fling in progress
        if (mFlywheel && !mFinished) {
            float oldVel = getCurrVelocity();

            float dx = (float) (mFinalX - mStartX);
            float dy = (float) (mFinalY - mStartY);
            float hyp = (float) Math.hypot(dx, dy);

            float ndx = dx / hyp;
            float ndy = dy / hyp;

            float oldVelocityX = ndx * oldVel;
            float oldVelocityY = ndy * oldVel;
            if (Math.signum(velocityX) == Math.signum(oldVelocityX) &&
                    Math.signum(velocityY) == Math.signum(oldVelocityY)) {
                velocityX += oldVelocityX;
                velocityY += oldVelocityY;
            }
        }

        mMode = FLING_MODE;
        mFinished = false;

        float velocity = (float) Math.hypot(velocityX, velocityY);
     
        mVelocity = velocity;
        mDuration = getSplineFlingDuration(velocity);
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartX = startX;
        mStartY = startY;

        float coeffX = velocity == 0 ? 1.0f : velocityX / velocity;
        float coeffY = velocity == 0 ? 1.0f : velocityY / velocity;

        double totalDistance = getSplineFlingDistance(velocity);
        mDistance = (int) (totalDistance * Math.signum(velocity));
        
        mMinX = minX;
        mMaxX = maxX;
        mMinY = minY;
        mMaxY = maxY;

        mFinalX = startX + (int) Math.round(totalDistance * coeffX);
        // Pin to mMinX <= mFinalX <= mMaxX
        mFinalX = Math.min(mFinalX, mMaxX);
        mFinalX = Math.max(mFinalX, mMinX);
        
        mFinalY = startY + (int) Math.round(totalDistance * coeffY);
        // Pin to mMinY <= mFinalY <= mMaxY
        mFinalY = Math.min(mFinalY, mMaxY);
        mFinalY = Math.max(mFinalY, mMinY);
    }
    
    public boolean computeScrollOffset() {
        if (mFinished) {
            return false;
        }

        int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    
        if (timePassed < mDuration) {
            switch (mMode) {
            case SCROLL_MODE:
                final float x = mInterpolator.getInterpolation(timePassed * mDurationReciprocal);
                // 通过运算来计算mCurrX和mCurrY值
                mCurrX = mStartX + Math.round(x * mDeltaX);
                mCurrY = mStartY + Math.round(x * mDeltaY);
                break;
            case FLING_MODE:
                final float t = (float) timePassed / mDuration;
                final int index = (int) (NB_SAMPLES * t);
                float distanceCoef = 1.f;
                float velocityCoef = 0.f;
                if (index < NB_SAMPLES) {
                    final float t_inf = (float) index / NB_SAMPLES;
                    final float t_sup = (float) (index + 1) / NB_SAMPLES;
                    final float d_inf = SPLINE_POSITION[index];
                    final float d_sup = SPLINE_POSITION[index + 1];
                    velocityCoef = (d_sup - d_inf) / (t_sup - t_inf);
                    distanceCoef = d_inf + (t - t_inf) * velocityCoef;
                }

                mCurrVelocity = velocityCoef * mDistance / mDuration * 1000.0f;
                // 通过运算来计算mCurrX和mCurrY值
                mCurrX = mStartX + Math.round(distanceCoef * (mFinalX - mStartX));
                // Pin to mMinX <= mCurrX <= mMaxX
                mCurrX = Math.min(mCurrX, mMaxX);
                mCurrX = Math.max(mCurrX, mMinX);
                
                mCurrY = mStartY + Math.round(distanceCoef * (mFinalY - mStartY));
                // Pin to mMinY <= mCurrY <= mMaxY
                mCurrY = Math.min(mCurrY, mMaxY);
                mCurrY = Math.max(mCurrY, mMinY);

                if (mCurrX == mFinalX && mCurrY == mFinalY) {
                    mFinished = true;
                }

                break;
            }
        }
        else {
            mCurrX = mFinalX;
            mCurrY = mFinalY;
            mFinished = true;
        }
        return true;
    }
    ......
```
从源码中可以看出getCurrX()和getCurrY()即mCurrX和mCurrY的值跟(mStartX、mStartY)和(mDeltaX、mDeltaY)/(velocityX、velocityY)有关：
    
    左、上滑(mDeltaX、mDeltaY)/(velocityX、velocityY)值为负，mCurrX和mCurrY从mStartX、mStartY初始值一点点减小
        
    右、下滑(mDeltaX、mDeltaY)/(velocityX、velocityY)值为正，mCurrX和mCurrY从mStartX、mStartY初始值一点点增大
由此可以引申出滑动中实时的偏移量的多少：
    
    diffX = mStartX - mScroller.getCurrX();
    diffY = mStartY - mScroller.getCurrY();

**VelocityTracker API：**(滑动速度跟踪器VelocityTracker, 用来监听手指移动改变的速度;)
    
(1) obtain() 获取一个 VelocityTracker 对象。VelocityTracker的构造函数是私有的，也就是不能通过new来创建。

(2) recycle() 回收 VelocityTracker 实例。

(3) clear() 重置 VelocityTracker 回其初始状态。

(4) addMovement(MotionEvent event) 为 VelocityTracker 传入触摸事件（包括ACTION_DOWN、ACTION_MOVE、ACTION_UP等），这样 VelocityTracker 才能在调用了 computeCurrentVelocity 方法后，正确的获得当前的速度。

(5)  computeCurrentVelocity(int units) 根据已经传入的触摸事件计算出当前的速度，可以通过getXVelocity 或 getYVelocity进行获取对应方向上的速度。值得注意的是，计算出的速度值不超过Float.MAX_VALUE。

    参数解析：
    参数 units： 速度的单位。值为1表示每毫秒像素数，1000表示每秒像素数。
(6) computeCurrentVelocity(int units, float maxVelocity) 根据已经传入的触摸事件计算出当前的速度，可以通过getXVelocity 或 getYVelocity进行获取对应方向上的速度。值得注意的是，计算出的速度值不超过maxVelocity。

    参数解析：
    第一个参数 units： 速度的单位。值为1表示每毫秒像素数，1000表示每秒像素数。
    第二个参数 maxVelocity： 最大的速度，计算出的速度不会超过这个值。值得注意的是，这个参数必须是正数，且其单位就是我们在第一参数设置的单位。
(7) getXVelocity() 获取最后计算的水平方向速度，使用此方法前需要记得先调用computeCurrentVelocity

(8) getYVelocity() 获取最后计算的垂直方向速度，使用此方法前需要记得先调用computeCurrentVelocity

(9) getXVelocity(int id) 获取对应的手指id最后计算的水平方向速度，使用此方法前需要记得先调用computeCurrentVelocity

    参数解析：
    参数 id： 触碰的手指的id

(10) getYVelocity(int id) 获取对应的手指id最后计算的垂直方向速度，使用此方法前需要记得先调用computeCurrentVelocity

    参数解析：
    参数 id： 触碰的手指的id
    
小结:

VelocityTracker 的 API 简单明了，我们可以用记住一个套路。

* 在触摸事件为 ACTION_DOWN 或是进入 onTouchEvent 方法时，通过 obtain 获取一个 VelocityTracker ；
* 在触摸事件为 ACTION_UP 时，调用 recycle 进行释放 VelocityTracker；
* 在进入 onTouchEvent 方法或将 ACTION_DOWN、ACTION_MOVE、ACTION_UP 的事件通过 addMovement 方法添加进 VelocityTracker；
* 在需要获取速度的地方，先调用 computeCurrentVelocity 方法，然后通过 getXVelocity、getYVelocity 获取对应方向的速度；

**ViewConfiguration API：**

获取实例： ViewConfiguration viewConfiguration = ViewConfiguration.get(Context);

常用对象方法：（主要关注前3个方法）

    //  获取touchSlop （系统 滑动距离的最小值，大于该值可以认为滑动）
    int touchSlop = viewConfiguration.getScaledTouchSlop();
    //  获得允许执行fling （抛）的最小速度值
    int minimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    //  获得允许执行fling （抛）的最大速度值
    int maximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
    //  Report if the device has a permanent menu key available to the user 
    //  （报告设备是否有用户可找到的永久的菜单按键）
    //  即判断设备是否有返回、主页、菜单键等实体按键（非虚拟按键）
    boolean hasPermanentMenuKey = viewConfiguration.hasPermanentMenuKey();  
常用静态方法：
    
    //  获得敲击超时时间，如果在此时间内没有移动，则认为是一次点击
    int tapTimeout =  ViewConfiguration.getTapTimeout();
    //  双击间隔时间，在该时间内被认为是双击
    int doubleTapTimeout = ViewConfiguration.getDoubleTapTimeout();
    //  长按时间，超过此时间就认为是长按
    int longPressTimeout = ViewConfiguration.getLongPressTimeout();
    //  重复按键间隔时间
    int repeatTimeout = ViewConfiguration.getKeyRepeatTimeout();
    
**引申：OverScroll简介：**

    在Android手机上，当我们滚动屏幕内容到达内容边界时，如果再滚动就会有一个发光效果。
    而且界面会进行滚动一小段距离之后再回复原位，这些效果是如何实现的呢？
    我们需要使用Scroller和scrollTo的升级版OverScroller和overScrollBy了，还有发光的EdgeEffect类。
由于我此前并没有研究过OverScroll和EdgeEffect类，所以就不列举其API进行阐述啦，感兴趣的小伙伴自己了解下吧。

## 扩展之触摸反馈
自定义触摸反馈的关键：

    1. 重写 onTouchEvent()，在里面写上你的触摸反馈算法，并返回 true（关键是 ACTION_DOWN 事件时返回  true）。
    2. 如果是会发生触摸冲突的 ViewGroup，还需要重写 onInterceptTouchEvent()，
        在事件流开始时返回  false，并在确认接管事件流时返回一次 true，以实现对事件的拦截。
    3. 当子 View 临时需要组织父 View 拦截事件流时，可以调用父 View 的  requestDisallowInterceptTouchEvent() ，
        通知父 View 在当前事件流中不再尝试通过  onInterceptTouchEvent() 来拦截。

<font size="3" color="red">特别说明：</font> 本文大部分直接复制于HenCoder中的内容(算是用于整合自定义控件知识点)，少部分自己的理解和实战。

<font size="3" color="red">心得体会：</font>由于最近在项目中遇到了平常控件无法实现，而且github上也没搜索到的效果，因此引起了我重新捡起曾经研究过多次，但都放弃了的自定义View，从新跟着凯哥(扔物线：朱凯大佬)的HenCoder走了一遍知识点，并且也做了其练习项目，但是当这些都做完后，感觉自己还是只知其一不知其二，这才引发我自己练习一个自定义控件的想法。开始着手去仿写“即刻的点赞效果”，当然一开始是很头疼的，毕竟从来没有自己动手写过(只改过现成的)，一开始在计算该View的大小时就出错不断，导致画的位置也不对，但是当我静下心来一点一点想自己究竟需要什么样子，并且通过固定一个图标的位置，来确定其它图标和文字的位置时，才感觉豁然开朗，就这样一点点调试终于画出了类似于原View的效果(当然画的不是很好，还有一些细节已经考虑大小改变之类的，没有动手尝试，动画做的也不是很好，不过最重要的还是真正知道了到底如何测量和绘制)。所以，建议跟我之前一样的小伙伴，一定要动手尝试的计算绘制一个自己的View，收获肯定会有的。哈哈哈，啰嗦的够多啦，本文到此结束啦，我还是个小菜鸟，继续努力啦。

![](https://user-gold-cdn.xitu.io/2019/6/29/16ba1668b2c995ef?w=450&h=461&f=png&s=245977)

## 参考链接：

https://hencoder.com/

https://juejin.im/post/5c7f4f0351882562ed516ab6

......

<font color=#ff0000>（注：若有什么地方阐述有误，敬请指正。）</font>