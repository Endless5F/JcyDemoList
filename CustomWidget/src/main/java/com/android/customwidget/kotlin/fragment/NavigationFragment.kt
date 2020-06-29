package com.android.customwidget.kotlin.fragment

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
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

class NavigationFragment : Fragment() {

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
        rvLeft.layoutManager = LinearLayoutManager(context)
        rvRight.layoutManager = LinearLayoutManager(context)
        val manager = rvRight.layoutManager as LinearLayoutManager
        //左边联动右边
        leftNavigationAdapter.setOnItemClickListener { position ->
            leftNavigationAdapter.setChoose(position)
            manager.scrollToPositionWithOffset(position, 0)
//            TopLinearSmoothScroller(context as Activity).apply {
//                targetPosition = position
//                manager.startSmoothScroll(this)
//            }
        }

        //右边联动左边
        rvRight.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItemPosition = manager.findFirstVisibleItemPosition()
                if (firstItemPosition != -1) {
                    rvLeft.smoothScrollToPosition(firstItemPosition)
                    leftNavigationAdapter.setChoose(firstItemPosition)
                }
            }

        })
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
                rightNavigationAdapter.setDataList(nav.data)
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