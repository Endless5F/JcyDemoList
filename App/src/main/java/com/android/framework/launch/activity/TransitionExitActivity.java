package com.android.framework.launch.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.View;

import com.android.framework.R;
import com.android.framework.launch.fragment.TransitionEnterFragment;

public class TransitionExitActivity extends AppCompatActivity {

    private View imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setupWindowAnimations();
        } else {
//            overridePendingTransition(R.anim.activity_bottom_to_top_enter, R.anim.no_anim);
            overridePendingTransition(R.anim.activity_right_to_left_enter, R.anim.activity_right_to_left_exit);
        }

        imageView = findViewById(R.id.imageView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        /*
         * 1、两个activity之间切换时界面的过渡效果
         * Window.setEnterTransition() 设置进场动画
         * Window.setExitTransition() 设置出场动画
         * Window().setReturnTransition() 设置返回activity时动画
         * Window().setReenterTransition() 设置重新进入时动画
         * <p>
         * Activity A切换到Activity B有三种方式：Explode, Slide 和Fade。
         *  *Explode：从屏幕的中间进入或退出。
         *  *Slide：从屏幕的一边向另一边进入或退出。
         *  *Fade：通过改变透明度来出现或消失。
         * <p>
         * 三种动画有两种实现方式：
         *  *通过xml声明：在res目录下新建transition文件夹在transition文件夹下新建activity_fade.xml文件
         *  *
         */
        // 方式一：
//        Slide slideXml = (Slide) TransitionInflater.from(this).inflateTransition(R.transition.activity_slide);
//        getWindow().setExitTransition(slideXml);
        // 方式二：代码方式实现
//        Fade fade = new Fade();
//        fade.setDuration(1000);
//        getWindow().setEnterTransition(fade);
//        Slide slide = new Slide();
//        slide.setDuration(1000);
//        getWindow().setReturnTransition(slide);

        sharedElementsAnimations();

//        setupWindowFragmentAnimations();

    }

    private void sharedElementsAnimations() {
        /*
         * 2、Shared elements between Activities(共享元素)
         * Shared elements转换确定两个Activity之间共享的视图如何在这两个Activity之间转换。例如，如果两个Activity在不同的位置和大小中具有相同的图像，则通过Shared elements转换会在这两个Activity之间平滑地转换和缩放图像。
         * <p>
         * shared elements转换包括以下几种：
         * changeBounds 改变目标布局中view的边界
         * changeClipBounds 裁剪目标布局中view的边界
         * changeTransform 实现旋转或者缩放动画
         * changeImageTransform 实现目标布局中ImageView的旋转或者缩放动画
         * <p>
         * 步骤：
         * 1. 设置styles.xml文件，允许windowContentTransitions：<item name="android:windowContentTransitions">true</item>
         * 2. 定义一个相同的transition名称：分别在Activity A 和Activity B的布局文件中定义item，这两个item的属性可以不一样，但是android:transitionName必须一样。
         * 3. 在Activity中启动shared element
         * <p>
         * 更多具体参考：https://blog.csdn.net/u010126792/article/details/85786790
         */
        imageView.setOnClickListener(v->{
            Intent i = new Intent(TransitionExitActivity.this, TransitionEnterActivity.class);
            String transitionName = getString(R.string.transform_shared_elements);
            ActivityOptions transitionActivityOptions = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(TransitionExitActivity.this, imageView, transitionName);
                // 多个共享元素开始一项活动
//                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
//                        Pair.create(imageView1, "agreedName1"),
//                        Pair.create(imageView2, "agreedName2"));
            }
            if (transitionActivityOptions == null) {
                startActivity(i);
            } else {
                startActivity(i, transitionActivityOptions.toBundle());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowFragmentAnimations() {
        /*
         * Fragment之间Shared elements
         * 1、允许windowContentTransitions
         * 2、定义一个共同的变换名称
         * 3、使用FragmentTransaction
         */
        TransitionEnterFragment enterFragment = new TransitionEnterFragment();
        // 为所有片段视图定义输入过渡
        Slide slideTransition = new Slide(Gravity.RIGHT);
        slideTransition.setDuration(1000);
        enterFragment.setEnterTransition(slideTransition);
        // 定义仅对共享元素的输入转换
        ChangeBounds changeBoundsTransition = (ChangeBounds) TransitionInflater.from(this).inflateTransition(R.transition.change_bounds);
        enterFragment.setSharedElementEnterTransition(changeBoundsTransition);
        // 防止过渡重叠
//        enterFragment.setAllowEnterTransitionOverlap(true);
//        enterFragment.setAllowReturnTransitionOverlap(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, enterFragment)
                .addSharedElement(imageView, getString(R.string.transform_shared_elements))
                .commit();
    }
}
