package com.android.baselibrary.base.adapter.kotlin

import android.support.annotation.NonNull
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.view.ViewGroup
import java.util.*

abstract class RecyclerAdapter: RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {

    var fromType: String? = null

    companion object {
        val HEADER_OFFSET = 100000
        val FOOTER_OFFSET = 200000
    }

    private val mHeaderViews = mutableListOf<View>() // 头视图列表

    private val mFooterViews = mutableListOf<View>() // 尾视图列表

    private val mHeaderViewTypes = mutableListOf<Int>()

    private val mFooterViewTypes = mutableListOf<Int>()

    abstract fun getInnerItemCount(): Int

    @NonNull
    protected abstract fun onCreateInnerViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder

    protected abstract fun onBindInnerViewHolder(holder: RecyclerViewHolder, position: Int)

    protected abstract fun getInnerViewType(position: Int): Int

    open fun onBindInnerViewHolder(holder: RecyclerViewHolder, position: Int, payloads: List<Any>) {
        onBindInnerViewHolder(holder, position)
    }

    /**
     * 添加头部
     * 如果多次添加，按先后顺序依次展示
     */
    fun addHeaderView(view: View) {
        if (!mHeaderViews.contains(view)) {
            mHeaderViews.add(view)
            notifyItemInserted(getHeaderCount() - 1)
        }
    }

    /**
     * 移除头部
     * @param view
     */
    fun removeHeaderView(view: View) {
        if (mHeaderViews.contains(view)) {
            val position = mHeaderViews.indexOf(view)
            mHeaderViews.remove(view)
            notifyItemRemoved(position)
        }
    }

    /**
     * 添加尾部
     * 如果多次添加，按先后顺序依次展示
     */
    fun addFooterView(view: View) {
        mFooterViews.add(view)
        notifyItemInserted(itemCount - 1)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        if (mHeaderViewTypes.contains(viewType)) {
            val index = viewType - HEADER_OFFSET
            val headerView = mHeaderViews[index]
            return HeaderHolder(headerView)
        }

        if (mFooterViewTypes.contains(viewType)) {
            val index = viewType - FOOTER_OFFSET
            val footerView = mFooterViews[index]
            return FooterHolder(footerView)
        }
        return onCreateInnerViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (isHeaderAt(position) || isFooterAt(position)) {
            return
        }

        onBindInnerViewHolder(holder, getInnerPosition(position))
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int, payloads: List<Any>) {
        if (isHeaderAt(position) || isFooterAt(position)) {
            return
        }

        onBindInnerViewHolder(holder, getInnerPosition(position), payloads)
    }

    fun isHeaderAt(position: Int): Boolean {
        if (getHeaderCount() > 0 && position < getHeaderCount()) {
            return true
        }

        return false
    }

    fun isFooterAt(position: Int): Boolean {
        if (getFooterCount() > 0 && position >= getInnerItemCount() + getHeaderCount()) {
            return true
        }

        return false
    }

    fun isInnerType(position: Int): Boolean {
        if (getHeaderCount() > 0 && position < getHeaderCount()) {
            return false
        }
        return !(getFooterCount() > 0 && position >= getInnerItemCount() + getHeaderCount())
    }

    /**
     * 获取Item在里层（去除头和尾）的实际位置
     *
     */
    protected fun getInnerPosition(position: Int): Int {
        return position - getHeaderCount()
    }

    /**
     * 获取Header序号
     * 如果不是Header，返回-1
     */
    protected fun getHeaderIndex(position: Int): Int {
        return if (getHeaderCount() > 0 && position < getHeaderCount()) {
            position
        } else {
            -1
        }
    }

    /**
     * 获取Footer序号
     * 如果不是Footer，返回-1
     */
    protected fun getFooterIndex(position: Int): Int {
        return if (getFooterCount() > 0 && position >= getInnerItemCount() + getHeaderCount()) {
            position - getInnerItemCount() - getHeaderCount()
        } else {
            -1
        }
    }

    /**
     * 获取Header总数
     * 如果没有，返回0
     */
    fun getHeaderCount(): Int {
        return if (mHeaderViews == null || mHeaderViews.size <= 0) {
            0
        } else {
            mHeaderViews.size
        }
    }

    /**
     * 获取Footer总数
     * 如果没有，返回0
     */
    fun getFooterCount(): Int {
        return if (mFooterViews == null || mFooterViews.size <= 0) {
            0
        } else {
            mFooterViews.size
        }
    }

    fun getFooterViews(): MutableList<View> {
        return mFooterViews
    }

    override fun getItemCount(): Int {
        return getInnerItemCount() + getHeaderCount() + getFooterCount()
    }

    /**
     * 获取列表数据
     * @return
     */
    open fun getItemList(): ArrayList<*>? {
        return null
    }

    /**
     * 获取第一个Header View
     */
    fun getFirstHead(): View? {
        return mHeaderViews.firstOrNull()
    }

    override fun getItemViewType(position: Int): Int {
        if (getHeaderCount() > 0 && position < getHeaderCount()) {
            val headerIndex = getHeaderIndex(position)
            mHeaderViewTypes.add(headerIndex + HEADER_OFFSET)
            if (headerIndex + HEADER_OFFSET >= FOOTER_OFFSET) {
                throw IllegalStateException("Header ViewType is out of range, must smaller than footer ViewType " + "range")
            }
            return headerIndex + HEADER_OFFSET
        }

        if (getFooterCount() > 0 && position >= getInnerItemCount() + getHeaderCount()) {
            val footerIndex = getFooterIndex(position)
            mFooterViewTypes.add(footerIndex + FOOTER_OFFSET)
            return footerIndex + FOOTER_OFFSET
        }

        val innerViewType = getInnerViewType(getInnerPosition(position))
        if (innerViewType >= HEADER_OFFSET) {
            throw IllegalStateException("Inner ViewType is out of range, must smaller than header ViewType range")
        }
        return innerViewType
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        val layoutManager = recyclerView.layoutManager
        if (layoutManager is GridLayoutManager) {
            val spanSizeLookup = layoutManager.spanSizeLookup
            /**
             * 配置网格列表时，Header和Footer需要独占一行
             */
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (getHeaderIndex(position) != -1) {
                        return layoutManager.spanCount
                    } else if (getFooterIndex(position) != -1) {
                        return layoutManager.spanCount
                    }
                    spanSizeLookup?.getSpanSize(position)
                    return 1
                }
            }
            layoutManager.spanCount = layoutManager.spanCount
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        if (lp is StaggeredGridLayoutManager.LayoutParams) {
            lp.isFullSpan = holder is HeaderHolder || holder is FooterHolder
        }
    }

    internal inner class HeaderHolder(itemView: View) : RecyclerViewHolder(itemView)

    internal inner class FooterHolder(itemView: View) : RecyclerViewHolder(itemView)

    open class RecyclerViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}