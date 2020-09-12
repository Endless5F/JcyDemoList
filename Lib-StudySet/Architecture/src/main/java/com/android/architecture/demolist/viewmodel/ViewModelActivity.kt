package com.android.architecture.demolist.viewmodel

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.android.architecture.R
import kotlinx.android.synthetic.main.activity_view_model.*

class ViewModelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_model)
        // 无参构造
        val viewModel = ViewModelProviders.of(this).get(NumberViewModel::class.java)
        // 有参构造
        val viewModel2 = ViewModelProviders.of(this, NumberViewModelFactory(5)).get(NumberViewModel::class.java)
        textView2.text = "" + viewModel.number
        button.setOnClickListener {
            viewModel.number++
            textView2.text = "" + viewModel.number
        }

        button2.setOnClickListener {
            viewModel.number += 2
            textView2.text = "" + viewModel.number
        }
    }
}
