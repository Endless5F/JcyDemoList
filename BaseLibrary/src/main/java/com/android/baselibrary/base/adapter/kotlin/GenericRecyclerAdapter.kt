package com.android.baselibrary.base.adapter.kotlin

import android.view.View

abstract class GenericRecyclerAdapter<T>(private val mDataList: MutableList<T>): RecyclerAdapter() {

    private var mOnItemClickListener: ((position: Int, data: T) -> Unit)? = null

    private var mOnItemLongClickListener: ((position: Int, data: T) -> Boolean)? = null

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        if (!isHeaderAt(position) && !isFooterAt(position)) {
            registerItemClickListener(holder.itemView, getInnerPosition(position))
        }
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, position, payloads)

        if (!isHeaderAt(position) && !isFooterAt(position)) {
            registerItemClickListener(holder.itemView, getInnerPosition(position))
            registerItemLongClickListener(holder.itemView, getInnerPosition(position))
        }
    }

    /**
     * 设置点击事件
     */
    private fun registerItemClickListener(itemView: View, position: Int) {
        if (mOnItemClickListener != null) {
            itemView.setOnClickListener {
                val data = mDataList.elementAtOrNull(position) ?: return@setOnClickListener
                mOnItemClickListener?.invoke(position, data)
            }
        }
    }

    /**
     * 设置长按事件
     */
    private fun registerItemLongClickListener(itemView: View, position: Int) {
        if (mOnItemLongClickListener != null) {
            itemView.setOnLongClickListener {
                val data = mDataList.elementAtOrNull(position) ?: return@setOnLongClickListener false
                mOnItemLongClickListener!!.invoke(position, data)
            }
        }
    }

    /**
     * 设置item点击事件
     */
    fun setOnItemClickListener(li: (position: Int, data: T) -> Unit) {
        mOnItemClickListener = li
    }

    /**
     * 设置item点击事件
     */
    fun setOnItemLongClickListener(li: (position: Int, data: T) -> Boolean) {
        mOnItemLongClickListener = li
    }

    override fun getInnerItemCount(): Int {
        return mDataList.size
    }

    override fun getInnerViewType(position: Int): Int {
        return 0
    }

    open fun addAll(list: MutableList<T>, notify: Boolean = false) {
        mDataList.addAll(list)
        if (notify) {
            notifyDataSetChanged()
        }
    }

    open fun clear(notify: Boolean = false) {
        mDataList.clear()
        if (notify) {
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}