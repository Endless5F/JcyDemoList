package com.android.framework.launch.activity;

import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.framework.R;
import com.android.framework.launch.adapter.RecycleAdapter;
import com.android.framework.launch.adapter.VpAdapter;

import java.util.ArrayList;
import java.util.List;

public class VpAndRecycleActivity extends BaseToolbarCompatActivity {

    private List<String> picList;
    private ViewPager viewPager;
    private RecyclerView recyclerView;
    private RecycleAdapter recycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_vp);
        setMiddleTitle("RecycleView+ViewPager联动");

        initData();
        initView();
        initLisenter();
    }

    private void initData() {
        picList = new ArrayList<>();
        picList.add("http://old.bz55.com/uploads/allimg/140416/1-140416100A3.jpg");
        picList.add("http://pic1.win4000.com/wallpaper/3/58ae554aa36f2.jpg");
        picList.add("http://img.ivsky.com/img/bizhi/pre/201601/27/february_2016-003.jpg");
        picList.add("http://img.ivsky.com/img/bizhi/pre/201601/27/february_2016-004.jpg");
        picList.add("http://img.ivsky.com/img/tupian/pre/201511/16/chongwugou.jpg");
        picList.add("http://p0.so.qhimgs1.com/bdr/_240_/t01b5c4bd41707c827a.jpg");
        picList.add("http://p2.so.qhimgs1.com/bdr/_240_/t01fafa8a942238b06e.jpg");
    }

    private void initView() {
        viewPager = findViewById(R.id.viewPager);
        recyclerView = findViewById(R.id.recyclerView);

        viewPager.setAdapter(new VpAdapter(this, picList));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        recycleAdapter = new RecycleAdapter(this, picList);
        recyclerView.setAdapter(recycleAdapter);
    }

    private void initLisenter() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                recyclerView.smoothScrollToPosition(position);
                recycleAdapter.setBackground(position);
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        recycleAdapter.setOnClick(new RecycleAdapter.OnClickItemLisenter() {
            @Override
            public void onClick(int position) {
                viewPager.setCurrentItem(position);
            }
        });
    }
}
