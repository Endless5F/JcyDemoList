package com.android.customwidget.kotlin.fragment

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dispatchMainLoopWork
import com.android.customwidget.kotlin.ext.dispatchSerialWork
import com.android.customwidget.kotlin.ext.getAssetsFileJson
import com.android.customwidget.kotlin.widget.linkage.adapter.LeftNavigationAdapter
import com.android.customwidget.kotlin.widget.linkage.adapter.RightNavigationAdapter
import com.android.customwidget.kotlin.widget.linkage.TopItemDecoration
import com.android.customwidget.kotlin.widget.linkage.bean.Navigation
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_navigation.*


class Navigation2Fragment : androidx.fragment.app.Fragment() {

    val mTransitioner = LayoutTransition()
    var leftList = mutableListOf<NavigationBean>()

    //初始化左侧recyclerview的adapter
    private val leftNavigationAdapter: LeftNavigationAdapter by lazy {
        LeftNavigationAdapter(context as Activity)
    }

    //初始化右侧recyclerview的adapter
    private val rightNavigationAdapter: RightNavigationAdapter by lazy {
        RightNavigationAdapter(context as Activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNavi()
        return inflater.inflate(R.layout.fragment_navigation, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        rvLeft.adapter = leftNavigationAdapter
        rvRight.adapter = rightNavigationAdapter
        recyclerViewLinkage()
        super.onActivityCreated(savedInstanceState)
    }

    /**
     * 左右两个RecyclerView联动
     */
    private fun recyclerViewLinkage() {
        rvLeft.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rvRight.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val manager = rvRight.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
        //左边联动右边
        leftNavigationAdapter.setOnItemClickListener { position ->
            leftNavigationAdapter.setChoose(position)
//            manager.scrollToPositionWithOffset(position, 0)
            val singleList = mutableListOf<NavigationBean>()
            singleList.add(leftList[position])
            rightNavigationAdapter.setDataList(singleList)
        }


        rvRight.layoutTransition = mTransitioner

        setTransition()

        //右边联动左边
        rvRight.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItemPosition = manager.findFirstVisibleItemPosition()
                if (firstItemPosition != -1) {
                    rvLeft.smoothScrollToPosition(firstItemPosition)
                    leftNavigationAdapter.setChoose(firstItemPosition)
                }
            }

        })
    }

    @SuppressLint("ObjectAnimatorBinding")
    private fun setTransition() {
        /**
         * 添加View时过渡动画效果
         */
        val addAnimator = ObjectAnimator.ofFloat(null, "rotationY", 0f, 90f, 0f).setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING))
        mTransitioner.setAnimator(LayoutTransition.APPEARING, addAnimator)
        /**
         * 移除View时过渡动画效果
         */
        val removeAnimator = ObjectAnimator.ofFloat(null, "rotationX", 0f, -90f, 0f).setDuration(mTransitioner.getDuration(LayoutTransition.DISAPPEARING))
        mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, removeAnimator)
        /**
         * view 动画改变时，布局中的每个子view动画的时间间隔
         */
        mTransitioner.setStagger(LayoutTransition.CHANGE_APPEARING, 30)
        mTransitioner.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30)
        /**
         * LayoutTransition.CHANGE_APPEARING和LayoutTransition.CHANGE_DISAPPEARING的过渡动画效果
         * 必须使用PropertyValuesHolder所构造的动画才会有效果，不然无效！使用ObjectAnimator是行不通的,
         * 发现这点时真特么恶心,但没想到更恶心的在后面,在测试效果时发现在构造动画时，”left”、”top”、”bottom”、”right”属性的
         * 变动是必须设置的,至少设置两个,不然动画无效,问题是我们即使这些属性不想变动!!!也得设置!!!
         * 我就问您恶不恶心!,因为这里不想变动,所以设置为(0,0)
         *
         */
        val pvhLeft = PropertyValuesHolder.ofInt("left", 0, 0)
        val pvhTop = PropertyValuesHolder.ofInt("top", 0, 0)
        val pvhRight = PropertyValuesHolder.ofInt("right", 0, 0)
        val pvhBottom = PropertyValuesHolder.ofInt("bottom", 0, 0)

        /**
         * view被添加时,其他子View的过渡动画效果
         */
        val animator = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f, 1f)
        val changeIn = ObjectAnimator.ofPropertyValuesHolder(
                this, pvhLeft, pvhBottom, animator).setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_APPEARING))
        mTransitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn)
        /**
         * view移除时，其他子View的过渡动画
         */
        val pvhRotation = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.5f, 1f)
        val changeOut = ObjectAnimator.ofPropertyValuesHolder(
                this, pvhLeft, pvhBottom, pvhRotation).setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_DISAPPEARING))
        mTransitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut)
    }

    /**
     * 获取导航数据
     */
    private fun getNavi() {
        dispatchSerialWork {
            val data = context?.getAssetsFileJson("Navigation")
            val nav = Gson().fromJson(data, Navigation::class.java)

            dispatchMainLoopWork {
                leftList = nav.data
                //默认左侧第一个为选中状态
                leftList[0].isChoose = true
                //分别给左右两个adapter填充数据
                leftNavigationAdapter.setDataList(leftList)
                val singleList = mutableListOf<NavigationBean>()
                singleList.add(nav.data[0])
                rightNavigationAdapter.setDataList(singleList)
                //右侧recyclerview悬浮置顶效果
                val top = TopItemDecoration(context as Activity).apply {
                    this.tagListener = {
                        leftList[it].name
                    }
                }
                rvRight.addItemDecoration(top)
            }
        }
    }
}