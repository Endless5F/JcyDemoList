package com.android.architecture.demolist.paging

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.architecture.R
import com.android.architecture.demolist.paging.basic.BasicUsageActivity
import com.android.architecture.demolist.paging.header_proxy.HeaderProxyActivity
import com.android.architecture.demolist.paging.header_simple.HeaderSimpleActivity
import kotlinx.android.synthetic.main.activity_paging.*

class PagingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paging)

        mBtnBasicUsage.setOnClickListener {
            startActivity(Intent(this, BasicUsageActivity::class.java))
        }
        mBtnHeaderMultiType.setOnClickListener {
            startActivity(Intent(this, HeaderSimpleActivity::class.java))
        }
        mBtnHeaderProxy.setOnClickListener {
            startActivity(Intent(this, HeaderProxyActivity::class.java))
        }
    }
}
