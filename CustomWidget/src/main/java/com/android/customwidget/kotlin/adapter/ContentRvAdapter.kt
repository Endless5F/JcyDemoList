package com.android.customwidget.kotlin.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.customwidget.R
import com.android.customwidget.kotlin.bean.Program
import kotlinx.android.synthetic.main.content_item.view.*

class ContentRvAdapter(private val mContext: Context
                       , private val dataList: List<Program>) : androidx.recyclerview.widget.RecyclerView.Adapter<ContentRvAdapter.ContentViewHodler>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentViewHodler {
        val view = LayoutInflater.from(mContext).inflate(R.layout.content_item, parent, false)
        return ContentViewHodler(view)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(viewHodler: ContentViewHodler, position: Int) {
        viewHodler.itemView.tv_program_name.text = dataList[position].name
        viewHodler.itemView.tv_program_num.text = "${dataList[position].num}"
        viewHodler.itemView.tv_program_desc.text = dataList[position].desc

        viewHodler.itemView.setOnClickListener {
            Log.d("setOnClickListener", "setOnClickListener $position")
            Toast.makeText(mContext, "$position", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
    }

    class ContentViewHodler constructor(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView)
}