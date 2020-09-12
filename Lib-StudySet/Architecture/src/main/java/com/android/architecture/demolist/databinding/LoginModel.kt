package com.android.architecture.demolist.databinding

import android.content.Context
import android.content.Intent
import android.databinding.ObservableField
import android.text.Editable
import android.util.Log
import android.widget.Toast
import com.android.architecture.activity.MainActivity
import com.joe.jetpackdemo.common.listener.SimpleWatcher
import kotlin.math.log

/**
 * DataBinding ObserVable 双向绑定
 *  Data Binding中如果我们直接修改Model实体对象（也就是POJO(普通的JavaBean)）中的数据，这些数据并不能直接更新到UI，
 *  所以Data Binding给了我们一套很好的通知机制，分别有三类： Observable objects, observable fields, and observable collections，
 *  分别表示观察对象、观察字段、观察集合，若相应的对象、字段、集合中数据变化时候，那么UI将会自动更新数据。
 * 1.Observable objects：（详细代码参见：ObservableObjects类）
 *  因为Observable是个接口，Google为我们提供了一个BaseObservable类，我们只要把Model类继承自它就获得了通知UI更新数据的能力了，
 *  再getter方法上添加Bindable注解，在setter方法中使用notifying（notifyPropertyChanged(BR.xxx);）提醒UI更新数据。
 * 2.ObservableFields（即本例子形式）
 *  第一种步骤繁琐，于是，Google推出ObservableFields类，使用它我们可以简化我们的Model类
 *  当然ObservableField<T>中传入的泛型可以是Java中的基本类型，当然我们还可以使用 ：
 *      ObservableBoolean, ObservableByte, ObservableChar, ObservableShort,
 *      ObservableInt, ObservableLong, ObservableFloat, ObservableDouble,
 *      ObservableParcelable等具体的类型，效果也和ObservableField<T>是一样的
 * 3.Observable Collections
 *  Google也为我们提供了一些通知类型的集合，有这三种：
 *      ObservableArrayList<T>、ObservableArrayMap<K,V>、ObservableMap<K,V>，
 *      它和平场使用的List、Map用法一样，但是多了通知功能。我们在layout中的<data>区域导入包后就可以直接用它了，
 *      当它内部的数据发生改变时就自动会通知UI界面更新。
 * */
class LoginModel constructor(name: String, pwd: String, context: Context) {
    // 构造函数，设置可观察的域
    val n = ObservableField<String>(name)
    val p = ObservableField<String>(pwd)
    var context: Context = context

    /**
     * 用户名改变回调的函数
     */
    fun onNameChanged(s: CharSequence) {
        n.set(s.toString())
    }

    /**
     * 密码改变的回调函数
     */
    fun onPwdChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        p.set(s.toString()) // 设置可观察的域，设置成功之后，会通知UI控件进行更新
    }

    fun login() {
        // get() 方法：获取可观察的域的内容，可以使用UI控件监测它的值
        if (n.get().equals("databinding") && p.get().equals("123456")) {
            Toast.makeText(context, "账号密码正确", Toast.LENGTH_SHORT).show()
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

    // SimpleWatcher 是简化了的TextWatcher
    val nameWatcher = object : SimpleWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            Log.d("nameWatcher","nameWatcher == ${s.toString()}")
            n.set(s.toString())
        }
    }

    val pwdWatcher = object : SimpleWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)

            p.set(s.toString())
        }
    }
}