# Window和WindowManager

Window表示的是一个窗口的概念，在日常生活中使用的并不是很多，但是某些特殊的需求还是需要的，比如悬浮窗之类的，他的具体实现是PhoneWindow,创建一个Window很简单，只需要WindowManager去实现，WindowManager是外界访问Window的入口，Window的具体实现是在WindowManagerService中，他们两个的交互是一个IPC的过程，Android中的所有视图都是通过Windowl来实现的，无论是Activity,Dialog还是Toast,他们的视图都是直接附加在Window上的，因此Window是View的直接管理者，在之前的事件分发中我们说到，View的事件是通过WIndow传递给DecorView，然后DecorView传递给我们的View，就连Activity的setContentView,都是由Window传递的。
## Window和WindowManager的使用
```
Button btn = new Button(this);
btn.setText("我是窗口");
WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
WindowManager.LayoutParams layout = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT
        , WindowManager.LayoutParams.WRAP_CONTENT, 0, 0, PixelFormat.TRANSLUCENT);
layout.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
layout.gravity = Gravity.CENTER;
layout.type = WindowManager.LayoutParams.TYPE_PHONE;
layout.x = 300;
layout.y = 100;
wm.addView(btn, layout);
```
### Flag参数(Window的属性)

- FLAG_NOT_FOCUSABLE：表示窗口不需要获取焦点，也不需要接收各种事件，这属性会同时启动FLAG_NOT_TOUCH_MODAL，最终的事件会传递给下层的具体焦点的window
- FLAG_NOT_TOUCH_MODAL：在此模式下，系统会将当前window区域以外的单击事件传递给底层的Window，此前的Window区域以内的单机事件自己处理，这个标记很重要，一般来说都需要开启，否则其他window将无法获取单击事件
- FLAG_SHOW_WHEN_LOCKED：开启这个属性可以让window显示在锁屏上
### Type参数(Window的类型)
> Window有三种类型，分别是应用，子，系统，应用Window对应一个Activity,子Window不能单独存在，需要依赖一个父Window，比如常见的Dialog都是子Window,系统Window需要声明权限，比如系统的状态栏。

Window是分层的，每个Window对应着z-ordered,层级大的会覆盖在层级小的Window上面，这和HTML中的z-index的概念是一致的，在这三类中，应用是层级范围是1-99，子Window的层级是1000-1999，系统的层级是2000-2999。这些范围对应着type参数，如果想要Window在最顶层，那么层级范围设置大一点就好了，很显然系统的值要大一些，系统的值很多，我们一般会选择TYPE_SYSTEM_OVERLAY和TYPE_SYSTEM_ERROR。
系统Window需要声明权限：
<**uses-permission** android:name="android.permission.SYSTEM_ALERT_WINDOW"/>
WindowManager所提供的功能很简单，常用的有三个方法，添加View,更新View,删除View,这三个方法定义在ViewManager中，而WindowManager继承自ViewManager。
```
public interface ViewManager {
    public void addView(View view, ViewGroup.LayoutParams params);

    public void updateViewLayout(View view, ViewGroup.LayoutParams params);

    public void removeView(View view);
}
```
## Activity，Window，WindowManager有什么联系
> Window表示一个窗口的概念,在日常开发中直接接触Window的机会并不多,但是在某些特殊时候我们需要在桌面上显示一个类似悬浮窗的东西,那么这种效果就需要用到Window来实现。Window的具体实现位于WindowManagerService中, WindowManager和WindowManagerService的交互是一个IPC过程。Android中所的视图都是通过Window来呈现的,不管是 Activity,Dialog还是Toast,它们的视图实际上都是附加在Window上的,因此Window实际是View的直接管理者。从View的事件分发机制也可以知道，单击事件由Window传递给DecorView,然后再DecorView传递给我们的View,连Activity的设置视图的方法setContentView在底也是通过Window来完成的。

![v2-38312edbf8cf648a5a1d993afb762e3d_r.jpg](https://cdn.nlark.com/yuque/0/2020/jpeg/754789/1581411524679-8246e692-7e63-47c7-a7c5-968b99ae5620.jpeg#align=left&display=inline&height=644&name=v2-38312edbf8cf648a5a1d993afb762e3d_r.jpg&originHeight=644&originWidth=1024&size=95143&status=done&style=none&width=1024)通过查看Activity，Dialog的源码发现，他们所依赖的WindowManager，是通过context.getSystemService()方法获取的，该方法底层又是从SystemServiceRegistry获取服务，而这些服务都是在SystemServiceRegistry的静态代码块中初始化的，所以不管有多少Activity，Dialog，WindowManager的实例只有一个。

WindowManagerImpl中依赖的WindowManagerGlobal也是单例模式创建的，所以app范围内，WindowManagerGlobal实例也只有一个，其内部的mViews,mRoots,mParams域维护了Activity（或Dialog等）的根View和ViewRootImpl以及布局参数之间的对应关系。
## Toast显示
![Toast显示流程图.png](https://cdn.nlark.com/yuque/0/2020/png/754789/1581411890684-489ebb7d-8538-4155-98b8-4864c36a7776.png#align=left&display=inline&height=705&name=Toast%E6%98%BE%E7%A4%BA%E6%B5%81%E7%A8%8B%E5%9B%BE.png&originHeight=705&originWidth=821&size=57372&status=done&style=none&width=821)
### 图片引用自[Android对话框Dialog，PopupWindow，Toast的实现机制](https://blog.csdn.net/feiduclear_up/article/details/49080587)
## [Andorid 任意界面悬浮窗](https://github.com/yhaolpz/FloatWindow)
