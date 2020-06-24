package com.android.customwidget.kotlin.widget.linkage

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean

class LeftNavigationAdapter(val context: Context) : RecyclerView.Adapter<LeftNavigationAdapter.ViewHolder>() {

    class ViewHolder(item: View) : RecyclerView.ViewHolder(item)

    var mDatas = mutableListOf<NavigationBean>()
    var mItemClickListener: ((Int) -> Unit)? = null

    fun setDataList(datas: MutableList<NavigationBean>) {
        mDatas = datas
        notifyDataSetChanged()
    }

    fun setChoose(position: Int) {
        mDatas.forEach {
            it.isChoose = false
        }
        mDatas[position].isChoose = true
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val text = TextView(context)
        return ViewHolder(text)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        (p0.itemView as TextView).text = mDatas[p1].name

        mItemClickListener?.invoke(p1)
    }

    fun setOnItemClickListener(function: (position: Int) -> Unit) {
        mItemClickListener = function
    }
}