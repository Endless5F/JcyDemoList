package com.android.architecture.demolist.room

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * 数据库文件
 *
 * @Database注解声明当前是一个数据库文件，注解中entities变量声明数据库中的表（实体），以及其他的例如版本等变量。
 * 同时，获取的Dao也必须在数据库类中。完成之后，点击build目录下的make project，系统就会自动帮我创建AppDataBase和xxxDao的实现类。
 */
@Database(entities = [UserBean::class], version = 1, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    // 得到UserDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var instance: AppDataBase? = null

        fun getInstance(context: Context): AppDataBase {
            return instance ?: synchronized(this) {
                instance ?: buildDataBase(context)
                        .also {
                            instance = it
                        }
            }
        }

        private fun buildDataBase(context: Context): AppDataBase {
            return Room.databaseBuilder(context, AppDataBase::class.java, "room-database")
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 创建完成回调
                        }
                    })
                    .build()
        }
    }
}