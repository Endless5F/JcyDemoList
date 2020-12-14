package com.android.baselibrary.base.adapter.kotlin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 多布局Adapter
 * @author jiaochengyun
 *
 * T：泛型传入具体类型则为单布局需要传入mLayoutId，否则若为Any类型则为多布局，需要通过mMultiTypeSupport返回layoutId
 */
abstract class MultiTypeRecyclerAdapter<T>() : GenericRecyclerAdapter<T>(arrayListOf<T>()) {

    private lateinit var mContext: Context

    // 单种布局
    private var mLayoutId: Int = -1

    // 多布局(每种类型)返回布局id(当作viewType)
    private var mMultiTypeSupport: ((item: T, position: Int) -> Int)? = null

    // 单布局
    constructor(context: Context, layoutId: Int) : this() {
        mContext = context
        mLayoutId = layoutId
    }

    /**
     * 多布局支持
     */
    constructor(context: Context, multiTypeSupport: (item: T, position: Int) -> Int) : this() {
        mContext = context
        mMultiTypeSupport = multiTypeSupport
    }

    /**
     * 根据当前位置获取不同的viewType
     */
    override fun getInnerViewType(position: Int): Int {
        // 多布局支持
        return if (mMultiTypeSupport != null) {
            mMultiTypeSupport!!.invoke(getInnerAll()[getInnerPosition(position)], position)
        } else super.getItemViewType(position)
    }

    override fun onCreateInnerViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        // 多布局支持
        if (mMultiTypeSupport != null) {
            mLayoutId = viewType
        }

        // 先inflate数据
        val itemView: View = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false)
        // 返回ViewHolder
        return RecyclerViewHolder(itemView)
    }

    override fun innerViewTypeCheck(innerViewType: Int) {
        // 多布局viewType为布局Id，因此必定大于 HEADER_OFFSET
        if (innerViewType <= HEADER_OFFSET) {
            throw IllegalStateException("Inner ViewType is out of range, must more than header ViewType range")
        }
    }

    override fun onBindInnerViewHolder(viewHolder: RecyclerViewHolder, position: Int) {
        val itemData = getInnerAll()[getInnerPosition(position)]
        // 交给子类Adapter处理
        convert(viewHolder, itemData, position)
    }

    /**
     * 利用抽象方法回传出去，每个不一样的Adapter去设置
     *
     * @param itemData 当前的数据
     */
    abstract fun convert(holder: RecyclerViewHolder, itemData: T, position: Int)

    fun addData(item: T?, notify: Boolean = true) {
        val startIndex = getInnerItemCount() + getHeaderCount()
        item?.let {
            getInnerAll().add(item)
            if (notify) {
                notifyItemRangeChanged(startIndex, getInnerAll().size)
            }
        }
    }

    fun addAllData(list: List<T>?, notify: Boolean = true) {
        val startIndex = getInnerItemCount() + getHeaderCount()
        list?.let {
            getInnerAll().addAll(it)
            if (notify) {
                notifyItemRangeChanged(startIndex, getInnerAll().size)
            }
        }
    }
}