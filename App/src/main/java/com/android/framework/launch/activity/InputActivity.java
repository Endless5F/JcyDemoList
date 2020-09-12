package com.android.framework.launch.activity;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.framework.R;
import com.android.framework.launch.adapter.VpInputAdapter;
import com.android.framework.launch.fragment.InputFragmentCustomized;
import com.android.framework.launch.fragment.InputFragmentCustomized2;

import java.util.ArrayList;
import java.util.List;

public class InputActivity extends BaseToolbarCompatActivity {

    private TabLayout tabInput;
    private ViewPager vpInput;
    private List<String> dataList = new ArrayList<>();
    private List<Fragment> fragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        setMiddleTitle("自定义键盘");
        initView();
        initData();
        initLisenter();
    }

    private void initView() {
        tabInput = findViewById(R.id.tab_input);
        vpInput = findViewById(R.id.vp_input);
    }

    private void initData() {
        dataList.add("自定义键盘键值不换行");
        dataList.add("自定义键盘键值换行");
        fragmentList.add(InputFragmentCustomized.getInstance());
        fragmentList.add(InputFragmentCustomized2.getInstance());
    }

    private void initLisenter() {
        vpInput.setAdapter(new VpInputAdapter(this, getSupportFragmentManager(), fragmentList,
                dataList));
        tabInput.setupWithViewPager(vpInput);
        tabInput.getTabAt(0).select();//设置第一个为选中
    }
}
