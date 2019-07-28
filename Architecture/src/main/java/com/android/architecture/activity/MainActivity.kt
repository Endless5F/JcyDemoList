package com.android.architecture.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.android.architecture.data.HomeData

import com.android.architecture.R
import com.android.architecture.adapter.HomePageAdapter
import com.android.architecture.toast
import java.util.*

class MainActivity : AppCompatActivity() {

    // 获取权限
    private val REQUEST_CODE_PERMISSIONS = 101

    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2

    private val permissions = Arrays.asList(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    )

    private var permissionRequestCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rl_demo_list = findViewById<RecyclerView>(R.id.rl_demo_list)
        rl_demo_list.layoutManager = LinearLayoutManager(this)//线性布局
        val homePageAdapter = HomePageAdapter(this, HomeData.addDevTotalRes)
        //        homePageAdapter.addHeaderView(R.layout.activity_home_page_header);
        rl_demo_list.adapter = homePageAdapter


        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            Snackbar.make(fab, "Replace with your own action",
                    Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        // 获取权限
        requestPermissionsIfNecessary()
    }

    /**
     * Request permissions twice - if the user denies twice then show a toast about how to update
     * the permission for storage. Also disable the button if we don't have access to pictures on
     * the device.
     */
    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                        this,
                        permissions.toTypedArray(),
                        REQUEST_CODE_PERMISSIONS
                )
            } else {
                toast("转到设置 - >应用和通知 - > WorkManager演示 - >应用权限并授予对存储的访问权限。")
                // TODO 解决权限问题
            }
        }
    }

    /** Permission Checking  */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }
}
