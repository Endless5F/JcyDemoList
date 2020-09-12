package com.android.architecture.demolist.lifecycle

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import com.android.architecture.loge
import com.android.architecture.ext.toast

class StudyLifecyleObserver(val context : Context) : DefaultLifecycleObserver {
    val Tag = "StudyLifecyleObserver"

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        loge(Tag, "onCreate")
        toast(context, "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        loge(Tag, "onStart")
        toast(context, "onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        loge(Tag, "onResume")
        toast(context, "onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        loge(Tag, "onPause")
        toast(context, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        loge(Tag, "onStop")
        toast(context, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        loge(Tag, "onDestroy")
        toast(context, "onDestroy")
    }
}