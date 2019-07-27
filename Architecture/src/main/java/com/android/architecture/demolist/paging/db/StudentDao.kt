package com.android.architecture.demolist.paging.db

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
/**
 * DataSource或DataSource.Factory ：数据源，DataSource将数据转变成PagedList，DataSource.Factory则用来创建DataSource
 *
 * 创建数据源：
 *  1.非Room数据库：如果没有使用Room数据库，我们需要自定义实现DataSource，通常实现DataSource有三种方式，分别继承三种抽象类。
 *      它们分别是：
 *          PageKeyedDataSource<Key, Value>     分页请求数据的场景
 *          ItemKeyedDataSource<Key, Value>     以表的某个列为key，加载其后的N个数据（个人理解以某个字段进行排序，然后分段加载数据）
 *          PositionalDataSource<T>             当数据源总数特定，根据指定位置请求数据的场景
 *
 *  2.Room数据库：fun getAllStudent(): DataSource.Factory<Int, Student>
 * */
@Dao
interface StudentDao {

    @Query("SELECT * FROM Student ORDER BY name COLLATE NOCASE ASC")
    fun getAllStudent(): DataSource.Factory<Int, Student>

    @Insert
    fun insert(students: List<Student>)

    @Insert
    fun insert(student: Student)
}