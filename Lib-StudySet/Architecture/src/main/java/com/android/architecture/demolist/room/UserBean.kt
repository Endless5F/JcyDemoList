package com.android.architecture.demolist.room

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey

/**
 *
 *  注解              说明
 *  @Entity         声明这是一个表（实体），主要参数：tableName-表名、foreignKeys-外键、indices-索引。
 *  @ColumnInfo     主要用来修改在数据库中的字段名。
 *  @PrimaryKey     声明该字段主键并可以声明是否自动创建。
 *  @Ignore         声明某个字段只是临时用，不存储在数据库中。
 *  @Embedded        类似于关联表，通过注解符@Embedded内嵌一个Java对象。
 *                  内嵌对象本身也是一个Android Room的@Entity。也有自己的列名和主键等完整的Android Room数据表要素
 * */

/**
 * 有时，确切的字段和组字段必须是独一无二的。你可以强加这个独一无二的特性通过设置一个@Index注解的unique属性为true。
 * 如下代码阻止了表拥有两行包含同样的firstName和last列的值集合。
 *  @Entity(indices = {@Index(value = {"first_name", "last_name"}, unique = true)})
 * */
@Entity(tableName = "User")
data class UserBean constructor(
        // 修改列名用法：@ColumnInfo(name = "first_name")
        @ColumnInfo var firstName: String,
        @ColumnInfo(name = "last_name") var lastName: String
) {
    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    // 临时字段不可放在构造方法中
    @Ignore val isWork: Boolean = false
}