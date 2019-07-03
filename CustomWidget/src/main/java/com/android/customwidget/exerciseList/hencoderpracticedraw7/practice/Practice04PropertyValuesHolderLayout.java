package com.android.customwidget.exerciseList.hencoderpracticedraw7.practice;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.customwidget.R;


public class Practice04PropertyValuesHolderLayout extends RelativeLayout {
    View view;
    Button animateBt;

    public Practice04PropertyValuesHolderLayout(Context context) {
        super(context);
    }

    public Practice04PropertyValuesHolderLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Practice04PropertyValuesHolderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        view = findViewById(R.id.objectAnimatorView);
        animateBt = (Button) findViewById(R.id.animateBt);

        animateBt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // 使用 PropertyValuesHolder.ofFloat() 来创建不同属性的动画值方案
                // 第一个： scaleX 从 0 到 1
                PropertyValuesHolder valuesHolder1 = PropertyValuesHolder.ofFloat("scaleX",1);
                // 第二个： scaleY 从 0 到 1
                PropertyValuesHolder valuesHolder2 = PropertyValuesHolder.ofFloat("scaleY",1);
                // 第三个： alpha 从 0 到 1
                PropertyValuesHolder valuesHolder3 = PropertyValuesHolder.ofFloat("alpha",1);

                // 然后，用 ObjectAnimator.ofPropertyValuesHolder() 把三个属性合并，创建 Animator 然后执行
                ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(view, valuesHolder1, valuesHolder2, valuesHolder3);
                animator.start();
            }
        });
    }
}
