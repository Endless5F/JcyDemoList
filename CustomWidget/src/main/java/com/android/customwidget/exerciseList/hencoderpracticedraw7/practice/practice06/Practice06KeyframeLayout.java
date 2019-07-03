package com.android.customwidget.exerciseList.hencoderpracticedraw7.practice.practice06;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.customwidget.R;


public class Practice06KeyframeLayout extends RelativeLayout {
    Practice06KeyframeView view;
    Button animateBt;

    public Practice06KeyframeLayout(Context context) {
        super(context);
    }

    public Practice06KeyframeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice06KeyframeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        view = (Practice06KeyframeView) findViewById(R.id.objectAnimatorView);
        animateBt = (Button) findViewById(R.id.animateBt);

        animateBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用 Keyframe.ofFloat() 来为 view 的 progress 属性创建关键帧
                // 初始帧：progress 为 0
                Keyframe keyframe1 = Keyframe.ofFloat(0,0);
                // 时间进行到一半：progress 为 100
                Keyframe keyframe2 = Keyframe.ofFloat(0.5f,100);
                // 结束帧：progress 回落到 80
                Keyframe keyframe3 = Keyframe.ofFloat(1f,75);
                // 使用 PropertyValuesHolder.ofKeyframe() 来把关键帧拼接成一个完整的属性动画方案
                PropertyValuesHolder holder = PropertyValuesHolder.ofKeyframe("progress",keyframe1,keyframe2,keyframe3);
                // 使用 ObjectAnimator.ofPropertyValuesHolder() 来创建动画
                ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view,holder);
                animator.setInterpolator(new OvershootInterpolator());
                animator.start();
            }
        });
    }
}
