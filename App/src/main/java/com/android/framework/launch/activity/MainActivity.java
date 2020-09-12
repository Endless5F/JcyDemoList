package com.android.framework.launch.activity;

import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.android.baselibrary.base.BaseToolbarCompatActivity;
import com.android.framework.R;
import com.android.framework.launch.adapter.HomePageAdapter;
import com.android.framework.launch.data.HomeData;

public class MainActivity extends BaseToolbarCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setMiddleTitle("首页");

        RecyclerView rl_demo_list = findViewById(R.id.rl_demo_list);
        rl_demo_list.setLayoutManager(new LinearLayoutManager(this));//线性布局
        HomePageAdapter homePageAdapter = new HomePageAdapter(this, HomeData.addDevTotalRes);
        homePageAdapter.addHeaderView(R.layout.activity_home_page_header);
        rl_demo_list.setAdapter(homePageAdapter);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action",
                Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    @Override
    public boolean setFactory2() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
