package com.android.customwidget.kotlin.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.android.customwidget.BaseActivity
import com.android.customwidget.R
import com.android.customwidget.kotlin.widget.dynamicheightvp.ComplicatedDynamicHeightViewPagerItem
import com.android.customwidget.kotlin.widget.dynamicheightvp.DynamicHeightViewPagerItemInterface
import com.android.customwidget.kotlin.widget.dynamicheightvp.DynamicHeightViewPagerView
import kotlinx.android.synthetic.main.activity_dynamic_height.*
import kotlin.random.Random

class DynamicHeight2Activity : BaseActivity() {

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
        val dynamicHeightItemViews = imageIds.mapIndexed { index, imageId ->
            createThirdItemView(index, imageId)
        }
        dynamicHeightViewPager.adapter = object : PagerAdapter() {
            override fun isViewFromObject(p0: View, p1: Any): Boolean {
                return p0 == p1
            }

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val dynamicHeightItemView = dynamicHeightItemViews[position]
                container.addView(dynamicHeightItemView.getItemView())
                return dynamicHeightItemView.getItemView()
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                dynamicHeightItemViews[position].removeFromParent(container)
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
                textView.text = "${p0 + 1}/${imageIds.size}"
            }
        })
        textView.text = "0/${imageIds.size}"
    }

    private fun createThirdItemView(position: Int, @DrawableRes imageId: Int) =
            ThirdActivityItem(this, position, imageId)

    companion object {
        fun start(activity: Activity) {
            activity.startActivity(Intent(activity, DynamicHeight2Activity::class.java))
        }
    }
}


private val random = Random(System.currentTimeMillis())

class ThirdActivityItem(private val context: Context, private val position: Int, @DrawableRes private val imageId: Int) : ComplicatedDynamicHeightViewPagerItem {

    private var thirdActivityItemView: ActivityItemView? = null

    private fun initThirdActivityItemView() {
        if (thirdActivityItemView == null) {
            thirdActivityItemView = ActivityItemView(context, this)
            thirdActivityItemView!!.init(position){
                setImageResource(imageId)
            }
        }
    }

    override fun getOriginContentHeight(): Int {
        initThirdActivityItemView()
        return thirdActivityItemView!!.getOriginContentHeight()
    }

    override fun getResizeView(): View {
        initThirdActivityItemView()
        return thirdActivityItemView!!.getResizeView()
    }

    fun getItemView(): View {
        initThirdActivityItemView()
        return thirdActivityItemView!!
    }

    fun removeFromParent(parent: ViewGroup){
        parent.removeView(thirdActivityItemView)
        thirdActivityItemView = null
    }

    override fun onScaleChanged(scale: Float) {

    }
}

private class ActivityItemView(
        context: Context,
        private val dynamicHeightViewPagerItemInterface: DynamicHeightViewPagerItemInterface
) : FrameLayout(context), DynamicHeightViewPagerView {

    private val frameLayout = FrameLayout(context)
    private val imageView = ImageView(context)
    private val textView = TextView(context)
    private val contentHeight: Int

    init {
        frameLayout.setBackgroundResource(R.color.primary_dark_material_dark)
        textView.gravity = Gravity.CENTER
        frameLayout.addView(
                imageView,
                LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT,
                        Gravity.TOP or Gravity.CENTER_HORIZONTAL
                )
        )
        frameLayout.addView(
                textView,
                LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                )
        )

        contentHeight = random.nextInt(300, 2000)
        addView(
                frameLayout,
                LayoutParams(LayoutParams.MATCH_PARENT, contentHeight)
        )
    }

    fun init(index: Int, invoke: ImageView.() -> Unit) {
        imageView.invoke()
        textView.text = "当前是第${index + 1}张图片"
    }

    fun getOriginContentHeight(): Int = contentHeight

    fun getResizeView(): View = frameLayout

    override fun getDynamicHeightViewPagerItemInterface() = dynamicHeightViewPagerItemInterface
}