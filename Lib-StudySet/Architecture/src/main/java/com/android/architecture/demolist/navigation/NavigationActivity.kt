package com.android.architecture.demolist.navigation

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.android.architecture.R

class NavigationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)
        /**
         * Navigation.findNavController(View) 返回了一个 NavController ,
         * 它是整个 Navigation 架构中 最重要的核心类，我们所有的导航行为都由 NavController 处理，这个我们后面再讲。
         * 我们通过获取 NavController，然后调用  NavController.navigate()方法进行导航。
         * 我们更多情况下通过传入ActionId，指定对应的 导航行为 ；同时可以通过传入Bundle以 数据传递；
         * 或者是再传入一个 NavOptions配置更多（比如 转场动画，它也可以通过这种方式进行代码的动态配置）。
         * NavController.navigate()方法更多时候应用在 向下导航 或者 指定向上导航（比如Page3 直接返回 Page1，跳过返回Page2的这一步）；
         * 如果我们处理back事件，我们应该使用 NavController.navigateUp()。
         * */
        val navController = Navigation.findNavController(this, R.id.fragment)
        // 用于在ActionBar上显示fragment的label属性，例如：fragment_page1，该属性设置于navigation资源文件夹下
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    /**
     * 配合 app:defaultNavHost="true" 属性，在点击系统Back键时返回fragment，而非activity
     * */
    override fun onSupportNavigateUp(): Boolean {
        val navController = Navigation.findNavController(this, R.id.fragment)
        return navController.navigateUp()
//        return super.onSupportNavigateUp()
    }
}
