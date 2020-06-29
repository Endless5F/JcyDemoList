package com.android.customwidget.kotlin.widget.linkage.adapter

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.addRefresh
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.android.synthetic.main.navigation_item_right2.view.*


class RightNavigation4Adapter(val context: Context) : RecyclerView.Adapter<RightNavigation4Adapter.ViewHolder>() {

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        var mRefreshView: SmartRefreshLayout? = null
        init {
            item.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)

            item.item_category.apply {
                if (mRefreshView == null) {
                    mRefreshView = addRefresh(this)
                    mRefreshView?.setEnableAutoLoadMore(false)
                    mRefreshView?.setRefreshHeader(ClassicsHeader(context))
                    mRefreshView?.setRefreshFooter(ClassicsFooter(context))
                }
            }
        }
    }


    var mDatas = mutableListOf<NavigationBean>()

    fun setDataList(datas: MutableList<NavigationBean>) {
        mDatas = datas
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val root = LayoutInflater.from(context).inflate(R.layout.navigation_item_right2, null, false)
        return ViewHolder(root)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    private var mRefreshListener: ((position: Int) -> Unit)? = null
    private var mLoadMoreListener: ((position: Int) -> Unit)? = null

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.itemView.item_category.apply {
            layoutManager = LinearLayoutManager(context)
            val navigationAdapter = RightNavigationAdapter(context)
            adapter = navigationAdapter
            setItemData(p1, navigationAdapter)

            p0.mRefreshView?.apply {
                setOnRefreshListener {
                    Log.e("mRefreshView", "setOnRefreshListener $p1")
                    finishRefresh(true)
                    mRefreshListener?.invoke(p1 - 1)
                    return@setOnRefreshListener
                }

                setOnLoadMoreListener {
                    Log.e("mRefreshView", "setOnLoadMoreListener  $p1")
                    finishLoadMore(true)
                    mLoadMoreListener?.invoke(p1 + 1)
                    return@setOnLoadMoreListener
                }
            }
        }
    }

    private fun setItemData(position: Int, adapter: RightNavigationAdapter) {
        val singleList = mutableListOf<NavigationBean>()
        singleList.add(mDatas[position])
        singleList.add(mDatas[position])
        singleList.add(mDatas[position])
        singleList.add(mDatas[position])
        singleList.add(mDatas[position])
        adapter.setDataList(singleList)
//        Log.e("mRefreshView", "setItemData $position ${singleList.toString()}")
    }

    fun addRefreshListener(callback: (position: Int) -> Unit) {
        mRefreshListener = callback
    }

    fun addLoadMoreListener(callback: (position: Int) -> Unit) {
        mLoadMoreListener = callback
    }
}