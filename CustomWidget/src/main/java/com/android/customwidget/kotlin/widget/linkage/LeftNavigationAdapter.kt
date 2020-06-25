package com.android.customwidget.kotlin.widget.linkage

import android.content.Context
import android.graphics.Color
import android.support.design.internal.FlowLayout
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.ext.sp
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.padding

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
        val param = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        text.layoutParams = param
        return ViewHolder(text)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        val text = (p0.itemView as TextView)
        text.text = mDatas[p1].name

        text.textSize = 18f
        text.setPadding(2.dp, 8.dp, 2.dp, 8.dp)

        if (mDatas[p1].isChoose) {
            text.backgroundColor = Color.RED
        } else {
            text.backgroundColor = Color.BLUE
        }
        p0.itemView.setOnClickListener {
            mItemClickListener?.invoke(p1)
        }
    }

    fun setOnItemClickListener(function: (position: Int) -> Unit) {
        mItemClickListener = function
    }
}
