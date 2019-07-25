package com.android.architecture.demolist.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Dao
interface UserDao {
    @Insert
    fun insertOne(vararg user: UserBean)

    @Insert
    fun insertAll(users: List<UserBean>)

    @Delete
    fun delete(vararg user: UserBean)

    @Delete
    fun deleteAll(user: List<UserBean>)

    @Update
    fun updateOne(vararg user: UserBean)

    @Update
    fun updateShoes(user: List<UserBean>)

    @get:Query("SELECT * FROM User")
    val all: List<UserBean>

    @Query("SELECT * FROM User WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserBean>

    @Query("SELECT * FROM User WHERE firstName LIKE :first AND " + "last_name LIKE :last LIMIT 1")
    fun findByName(first: String, last: String): UserBean

    // 配合LiveData 通过Id查询用户
    @Query("SELECT * FROM User WHERE uid=:id")
    fun findShoeByIdLD(id: Long): LiveData<UserBean>

    // 配合RxJava 通过Id查询单款鞋子
//    @Query("SELECT * FROM UserBean WHERE uid=:id")
//    fun findShoeByIdRx(id: Long): Flowable<Shoe>
}