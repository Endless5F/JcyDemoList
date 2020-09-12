package com.android.architecture.demolist.paging.basic

import android.arch.paging.PagedListAdapter
import android.support.v7.util.DiffUtil
import android.view.ViewGroup
import com.android.architecture.demolist.paging.db.Student
import com.android.architecture.demolist.paging.viewholder.StudentViewHolder

/**
 * PagedListAdapter：一种RecyclerView的适配器
 *  paging 提供了一个新的 PagedListAdapter, 在实例化这个 Adapter 的时候，
 *  我们需要提供一个自己实现的 DiffUtil.ItemCallback 或者 AsyncDifferConfig
 * */
class BasicStudentAdapter() : PagedListAdapter<Student, StudentViewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder = StudentViewHolder(parent)

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Student>() {
            override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean =
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean =
                    oldItem == newItem
        }
    }
}