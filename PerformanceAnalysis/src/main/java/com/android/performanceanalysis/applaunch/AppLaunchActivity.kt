package com.android.performanceanalysis.applaunch

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.performanceanalysis.R

class AppLaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_launch)
    }
}
