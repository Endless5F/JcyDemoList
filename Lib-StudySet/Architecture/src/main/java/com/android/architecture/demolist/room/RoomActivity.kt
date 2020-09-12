package com.android.architecture.demolist.room

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.architecture.R
import com.android.architecture.toast
import kotlinx.android.synthetic.main.activity_room.*

class RoomActivity : AppCompatActivity() {

    private val userDao: UserDao by lazy { AppDataBase.getInstance(applicationContext).userDao() }

    private val mFirstList = listOf("张", "李", "王", "赵", "孙", "周", "钱", "吴", "郑", "诸葛", "令狐", "上官")
    private val mSecondList = listOf("一", "二", "三", "四", "五", "六", "七", "八", "九", "十")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)
        // 不使用LiveData和RxJava的前提下，Room的操作是不可以放在主线程中的
        bt_add.setOnClickListener {
            Thread {
                userDao.insertOne(
                        UserBean(
                                firstName = mFirstList.random(),
                                lastName = mSecondList.random()
                        )
                )
            }.start()
        }

        bt_delete.setOnClickListener {
            Thread {
                if (userDao.all.isNotEmpty()) {
                    userDao.delete(userDao.all.first())
                } else {
                    runOnUiThread {
                        toast("userList = []")
                    }
                }
            }.start()
        }

        bt_update.setOnClickListener {
            Thread {
                if (userDao.all.isNotEmpty()) {
                    val user = userDao.all.first()
                    user.firstName = "令狐一"
                    userDao.updateOne(user)
                } else {
                    runOnUiThread {
                        toast("userList = []")
                    }
                }
            }.start()
        }

        bt_query.setOnClickListener {
            Thread {
                val users = userDao.all
                runOnUiThread {
                    toast("userList = $users")
                }
            }.start()
        }
    }
}
