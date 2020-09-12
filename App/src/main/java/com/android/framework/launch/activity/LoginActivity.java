package com.android.framework.launch.activity;

import android.os.Bundle;

import com.android.baselibrary.base.BaseCompatActivity;
import com.android.baselibrary.util.ScreenUtils;
import com.android.framework.R;

public class LoginActivity extends BaseCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //适配全尺寸屏幕
        //在serContentView之前调用,资源放在 xxhdpi，那么我们宽度转换为 dp 就是 1080 / 3 = 360dp
        if (ScreenUtils.isPortrait()) {
            ScreenUtils.adaptScreen4VerticalSlide(this, 360);
        } else {
            ScreenUtils.adaptScreen4HorizontalSlide(this, 360);
        }
        setContentView(R.layout.login_and_register);

    }
}
