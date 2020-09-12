package com.android.architecture.demolist.paging.viewholder

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.android.architecture.R
import com.android.architecture.demolist.paging.db.Student

class StudentViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_paging_student, parent, false)) {

    private val nameView = itemView.findViewById<TextView>(R.id.name)
    var student: Student? = null

    /**
     * Items might be null if they are not paged in yet. PagedListAdapter will re-bind the
     * ViewHolder when Item is loaded.
     */
    fun bindTo(student: Student?) {
        this.student = student
        nameView.text = student?.name
    }
}