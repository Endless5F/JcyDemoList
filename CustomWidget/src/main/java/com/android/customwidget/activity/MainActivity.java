package com.android.customwidget.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.customwidget.BaseActivity;
import com.android.customwidget.R;
import com.android.customwidget.adapter.HomePageAdapter;
import com.android.customwidget.data.HomeData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import static com.android.customwidget.kotlin.ext.ViewExtKt.setPaddingStatusBarHeight;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView title = findViewById(R.id.title);
        setPaddingStatusBarHeight(title);

        RecyclerView rl_demo_list = findViewById(R.id.rl_demo_list);
        rl_demo_list.setLayoutManager(new LinearLayoutManager(this));//线性布局
        HomePageAdapter homePageAdapter = new HomePageAdapter(this, HomeData.addDevTotalRes);
//        homePageAdapter.addHeaderView(R.layout.activity_home_page_header);
        rl_demo_list.setAdapter(homePageAdapter);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(fab, "Replace with your own action",
                        Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
}
