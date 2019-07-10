package com.android.customwidget.kotlin.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import kotlinx.android.synthetic.main.time_item.view.*

class TimeRvAdapter(private val mContext : Context, private val dataList : List<String>) : RecyclerView.Adapter<TimeRvAdapter.TimeViewHodler>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHodler {
        val view = LayoutInflater.from(mContext).inflate(R.layout.time_item, parent, false)
        return TimeViewHodler(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(viewHodler: TimeViewHodler, position: Int) {
        viewHodler.itemView.tv_time.text = dataList[position]
    }

    class TimeViewHodler constructor(itemView: View) : RecyclerView.ViewHolder(itemView)
}