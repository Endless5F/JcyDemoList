package com.android.architecture.demolist.paging.db

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey


@Entity
data class Student(@PrimaryKey(autoGenerate = true) val id: Int, val name: String)