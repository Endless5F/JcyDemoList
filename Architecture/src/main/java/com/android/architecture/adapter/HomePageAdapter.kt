package com.android.architecture.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.architecture.R
import com.android.architecture.data.HomeData

class HomePageAdapter(private val mContext: Context, private val mItemData: List<HomeData.ItemView>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mHeaderViews = SparseArrayCompat<Int>()

    private val headersCount: Int
        get() = mHeaderViews.size()

    val isHaveHeaderView: Boolean
        get() = mHeaderViews.size() > 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (mHeaderViews.get(viewType) != null) {
            val v = LayoutInflater.from(mContext).inflate(mHeaderViews.get(viewType)!!, parent,
                    false)
            return HeadViewHolder(v)
        } else if (ITEM_TYPE_TITLE == viewType) {
            val v = LayoutInflater.from(mContext).inflate(R.layout.activity_home_page_title,
                    parent, false)
            return TitleViewHolder(v)
        } else {
            val v = LayoutInflater.from(mContext).inflate(R.layout.activity_home_page_item,
                    parent, false)
            return ItemViewHolder(v)
        }
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        var position = position
        val viewType = getItemViewType(position)
        if (mHeaderViews.get(viewType) != null) {
            val headViewHolder = viewHolder as HeadViewHolder
            headViewHolder.scan.setOnClickListener { }
            return
        } else {
            position -= headersCount
        }
        if (viewType == ITEM_TYPE_TITLE) {
            val titleViewHolder = viewHolder as TitleViewHolder
            titleViewHolder.title.text = mItemData[position].desc
        } else {
            val itemViewHolder = viewHolder as ItemViewHolder
            itemViewHolder.name.text = mItemData[position].desc
            //            itemViewHolder.icon.setText(mItemData.get(position).icon);
            val pos = position
            itemViewHolder.name.setOnClickListener {
                when (pos) {
                    1 -> {

                    }
                    2 -> {

                    }
                    3 -> {

                    }
                    4 -> {

                    }
                    5 -> {

                    }
                    6 -> {

                    }
                    7 -> {

                    }
                    8 -> {

                    }
                    9 -> {

                    }
                    10 -> {

                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isHeaderViewPos(position)) {
            mHeaderViews.keyAt(position)
        } else if (isTitle(position)) {
            ITEM_TYPE_TITLE
        } else {
            ITEM_TYPE_SECOND
        }
    }

    override fun getItemCount(): Int {
        return mItemData.size + headersCount
    }

    private fun isHeaderViewPos(position: Int): Boolean {
        return position < headersCount
    }

    fun addHeaderView(view: Int) {
        mHeaderViews.put(mHeaderViews.size() + ITEM_TYPE_HEADER, view)
    }

    fun removeHeaderView() {
        mHeaderViews.clear()
    }

    private fun isTitle(position: Int): Boolean {
        return "" == mItemData[position - headersCount].icon
    }

    internal inner class ItemViewHolder @SuppressLint("WrongViewCast")
    constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var icon: TextView

        init {
            name = itemView.findViewById(R.id.tv_item_name)
            icon = itemView.findViewById(R.id.tv_item_icon)
        }
    }

    internal inner class TitleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var title: TextView

        init {
            title = itemView.findViewById(R.id.tv_title)
        }
    }

    internal inner class HeadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var scan: TextView

        init {
            scan = itemView.findViewById(R.id.tv_scan)
        }
    }

    companion object {
        private val ITEM_TYPE_HEADER = 100000
        private val ITEM_TYPE_TITLE = 111110
        private val ITEM_TYPE_SECOND = 111111
    }
}
