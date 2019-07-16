package com.android.architecture.demolist.lifecycle

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.architecture.R

class LifecycleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle)

        lifecycle.addObserver(StudyLifecyleObserver())
    }
}
