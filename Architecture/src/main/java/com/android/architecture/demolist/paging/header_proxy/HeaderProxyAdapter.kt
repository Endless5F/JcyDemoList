package com.android.architecture.demolist.paging.header_proxy

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.android.architecture.demolist.paging.db.Student
import com.android.architecture.demolist.paging.viewholder.FooterViewHolder
import com.android.architecture.demolist.paging.viewholder.HeaderViewHolder
import com.android.architecture.demolist.paging.viewholder.StudentViewHolder

class HeaderProxyAdapter : PagedListAdapter<Student, RecyclerView.ViewHolder>(diffCallback) {

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_TYPE_HEADER
            itemCount - 1 -> ITEM_TYPE_FOOTER
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_HEADER -> HeaderViewHolder(parent)
            ITEM_TYPE_FOOTER -> FooterViewHolder(parent)
            else -> StudentViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bindsHeader()
            is FooterViewHolder -> holder.bindsFooter()
            is StudentViewHolder -> holder.bindTo(getStudentItem(position))
        }
    }

    private fun getStudentItem(position: Int): Student? {
        return getItem(position - 1)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 2
    }

    // 注册监听数据被刷新
    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        // 数据被刷新时通知RecycleView有一个头布局，即开始position需要改变
        super.registerAdapterDataObserver(AdapterDataObserverProxy(observer, 1))
    }

    // 移除监听数据被刷新
    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer);
    }


    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean =
                    oldItem == newItem
        }

        private const val ITEM_TYPE_HEADER = 99
        private const val ITEM_TYPE_FOOTER = 100
    }
}