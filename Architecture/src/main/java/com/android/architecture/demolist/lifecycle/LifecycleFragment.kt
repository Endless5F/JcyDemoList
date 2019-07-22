package com.android.architecture.demolist.lifecycle

import android.support.v4.app.Fragment

class LifecycleFragment : Fragment() {
    init {
        lifecycle.addObserver(StudyLifecyleObserver(context!!))
    }



}