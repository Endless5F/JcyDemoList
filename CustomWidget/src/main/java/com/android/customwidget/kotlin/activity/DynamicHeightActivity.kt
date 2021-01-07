package com.android.customwidget.kotlin.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.widget.dynamicheightvp.ComplicatedDynamicHeightViewPagerItemView
import kotlinx.android.synthetic.main.activity_dynamic_height.*

class DynamicHeightActivity : BaseActivity() {

    private val imageIds =
            intArrayOf(
                    R.drawable.image01,
                    R.drawable.image02,
                    R.drawable.image03,
                    R.drawable.image04,
                    R.drawable.image05,
                    R.drawable.image06,
                    R.drawable.image07,
                    R.drawable.image08,
                    R.drawable.image09,
                    R.drawable.image10,
                    R.drawable.image11,
                    R.drawable.image12
            )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_height)
        val dynamicHeightItemViews = imageIds.map { imageId ->
            createComplicatedDynamicHeightViewPagerItemView(imageId)
        }
        dynamicHeightViewPager.adapter = object : PagerAdapter() {
            override fun isViewFromObject(p0: View, p1: Any): Boolean {
                return p0 == p1
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val  dynamicHeightItemView = dynamicHeightItemViews[position]
                container.addView(dynamicHeightItemView.getItemView())
                return dynamicHeightItemView.getItemView()
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                dynamicHeightItemViews[position].removeViewFromParent(container)
            }

            override fun getCount(): Int = dynamicHeightItemViews.size
        }
        dynamicHeightViewPager.init(dynamicHeightItemViews = dynamicHeightItemViews)
        dynamicHeightViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {

            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(p0: Int) {
                textView.text = "${p0+1}/${imageIds.size}"
            }
        })
        textView.text = "0/${imageIds.size}"
    }


    private fun createComplicatedDynamicHeightViewPagerItemView(@DrawableRes imageId: Int) =
            ComplicatedDynamicHeightViewPagerItemView(this){
                setImageResource(imageId)
            }

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DynamicHeightActivity::class.java))
        }
    }
}