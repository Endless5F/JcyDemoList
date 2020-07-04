package com.android.customwidget.kotlin.widget.lineargrid

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.customwidget.R
import kotlinx.android.synthetic.main.linear_gird_item.view.*

class GridItemAdapter : RecyclerView.Adapter<GridItemAdapter.ViewHolder> {

    var mContext: Context? = null
    var mDataList: MutableList<DataEntity> = mutableListOf()

    constructor(context: Context) {
        this.mContext = context
    }

    fun setData(list: List<DataEntity>) {
        mDataList.clear()
        mDataList.addAll(list)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.linear_gird_item, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.tv_hot_part_item.text = mDataList[position].display_name
    }
}