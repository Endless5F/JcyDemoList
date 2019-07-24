package com.android.architecture.demolist.databinding

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class RegisterModel {

    val n = MutableLiveData<String>()
    val p = MutableLiveData<String>()
    val mail = MutableLiveData<String>()

    /**
     * 用户名改变回调的函数
     */
    fun onNameChanged(s: CharSequence) {
        //n.set(s.toString())
        n.value = s.toString()
    }

    /**
     * 邮箱改变的时候
     */
    fun onEmailChanged(s: CharSequence) {
        //n.set(s.toString())
        mail.value = s.toString()
    }

    /**
     * 密码改变的回调函数
     */
    fun onPwdChanged(s: CharSequence) {
        //p.set(s.toString())
        p.value = s.toString()
    }

    fun register() {

    }
}