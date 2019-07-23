package com.android.architecture.demolist.livedata

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class LiveDataFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 同一Activity下的fragment可通过此方式共享数据
        val viewModel = ViewModelProviders.of(activity!!).get(LiveDataTimerViewModel::class.java!!)
        viewModel.elapsedTime.observe(this, object : Observer<Long> {
            override fun onChanged(t: Long?) {
                // ......
            }

        })
        return super.onCreateView(inflater, container, savedInstanceState)
    }
}