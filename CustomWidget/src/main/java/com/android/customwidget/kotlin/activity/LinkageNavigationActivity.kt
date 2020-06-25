package com.android.customwidget.kotlin.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.customwidget.R
import com.android.customwidget.kotlin.fragment.NavigationFragment

class LinkageNavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linkage_navigation)

        val transaction = supportFragmentManager.beginTransaction()
        val fragment = NavigationFragment()
        transaction.add(R.id.rootView, fragment)
        transaction.show(fragment)
        transaction.commitAllowingStateLoss()
    }
}
