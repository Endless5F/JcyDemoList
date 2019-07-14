package com.android.architecture.activity

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.android.architecture.data.HomeData

import com.android.architecture.R
import com.android.architecture.adapter.HomePageAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rl_demo_list = findViewById<RecyclerView>(R.id.rl_demo_list)
        rl_demo_list.layoutManager = LinearLayoutManager(this)//线性布局
        val homePageAdapter = HomePageAdapter(this, HomeData.addDevTotalRes)
        //        homePageAdapter.addHeaderView(R.layout.activity_home_page_header);
        rl_demo_list.adapter = homePageAdapter


        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            Snackbar.make(fab, "Replace with your own action",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
}
