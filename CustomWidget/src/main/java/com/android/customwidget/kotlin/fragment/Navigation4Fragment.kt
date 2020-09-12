package com.android.customwidget.kotlin.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dispatchMainLoopWork
import com.android.customwidget.kotlin.ext.dispatchSerialWork
import com.android.customwidget.kotlin.ext.getAssetsFileJson
import com.android.customwidget.kotlin.widget.linkage.CustomLinearLayoutManager
import com.android.customwidget.kotlin.widget.linkage.adapter.LeftNavigationAdapter
import com.android.customwidget.kotlin.widget.linkage.adapter.RightNavigation4Adapter
import com.android.customwidget.kotlin.widget.linkage.bean.Navigation
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import com.google.gson.Gson
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.fragment_navigation.*


class Navigation4Fragment : androidx.fragment.app.Fragment() {

    var commonList = mutableListOf<NavigationBean>()

    //初始化左侧recyclerview的adapter
    private val leftNavigationAdapter: LeftNavigationAdapter by lazy {
        LeftNavigationAdapter(context as Activity)
    }

    //初始化右侧recyclerview的adapter
    private val rightNavigationAdapter: RightNavigation4Adapter by lazy {
        RightNavigation4Adapter(context as Activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNavi()
        return inflater.inflate(R.layout.fragment_navigation, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        recyclerViewLinkage()
        super.onActivityCreated(savedInstanceState)
    }

    protected var mRefreshView: SmartRefreshLayout? = null

    /**
     * 左右两个RecyclerView联动
     */
    private fun recyclerViewLinkage() {
        rvLeft.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val layoutManager = CustomLinearLayoutManager(context)
        layoutManager.setScrollEnabled(false)
        rvRight.layoutManager = layoutManager

        rvLeft.adapter = leftNavigationAdapter
        rvRight.adapter = rightNavigationAdapter

        //左边联动右边
        val manager = rvRight.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
        //左边联动右边
        leftNavigationAdapter.setOnItemClickListener { position ->
            leftNavigationAdapter.setChoose(position)
            rvRight.smoothScrollToPosition(position)
//            manager.scrollToPositionWithOffset(position, 0)
        }

        rightNavigationAdapter.addRefreshListener { position ->
            if (position >= 0) {
                leftNavigationAdapter.setChoose(position)
                rvRight.smoothScrollToPosition(position)
            }
        }

        rightNavigationAdapter.addLoadMoreListener { position ->
            if (position < commonList.size) {
                leftNavigationAdapter.setChoose(position)
                manager.scrollToPositionWithOffset(position, 0)
            }
        }
    }

    /**
     * 获取导航数据
     */
    private fun getNavi() {
        dispatchSerialWork {
            val data = context?.getAssetsFileJson("Navigation")
            val nav = Gson().fromJson(data, Navigation::class.java)

            dispatchMainLoopWork {
                commonList = nav.data
                //分别给左右两个adapter填充数据
                leftNavigationAdapter.setDataList(commonList)
                rightNavigationAdapter.setDataList(commonList)
            }
        }
    }
}