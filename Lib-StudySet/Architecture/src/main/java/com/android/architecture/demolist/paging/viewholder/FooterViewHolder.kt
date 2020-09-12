package com.android.architecture.demolist.paging.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.architecture.R

class FooterViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_paging_footer, parent, false)) {

    fun bindsFooter() {
        // empty implementation
    }
}