package com.android.customwidget.kotlin.widget.linkage.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dp
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import com.zhy.view.flowlayout.TagView
import kotlinx.android.synthetic.main.navigation_item_right.view.*
import org.jetbrains.anko.padding

class RightNavigationAdapter(val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<RightNavigationAdapter.ViewHolder>() {

    class ViewHolder(item: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(item)

    var mDatas = mutableListOf<NavigationBean>()

    fun setDataList(datas: MutableList<NavigationBean>) {
        mDatas = datas
//        notifyDataSetChanged()
        notifyItemRangeChanged(0,mDatas.size)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val root = LayoutInflater.from(context).inflate(R.layout.navigation_item_right, null, false)
        return ViewHolder(root)
    }

    override fun getItemCount(): Int {
        return mDatas.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.itemView.sub_title.text = mDatas[p1].name
        p0.itemView.flow_content.removeAllViews()
        mDatas[p1].articles.forEach { data ->
            p0.itemView.flow_content.apply {
                val tag = TagView(context)
                val text = TextView(context)
                text.text = data.title
                text.textSize = 14f
                text.padding = 3.dp
                text.setBackgroundResource(R.drawable.common_bg_rect)
                tag.addView(text)
                addView(tag)
            }
        }
    }
}