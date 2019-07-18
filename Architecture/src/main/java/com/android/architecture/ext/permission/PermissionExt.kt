package luyao.util.ktx.ext.permission

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat

/**
 * Created by luyao
 * on 2019/6/18 15:47
 */

const val TAG ="ktx"

fun FragmentActivity.request(vararg permissions: String) {
    ActivityCompat.requestPermissions(this, permissions, 0XFF)
}

fun FragmentActivity.request(vararg permissions: String, callbacks: PermissionsCallbackDSL.() -> Unit) {

    val permissionsCallback = PermissionsCallbackDSL().apply { callbacks() }
    val requestCode = PermissionsMap.put(permissionsCallback)

    val needRequestPermissions = permissions.filter { !isGranted(it) }

    if (needRequestPermissions.isEmpty()) {
        permissionsCallback.onGranted()
    } else {
        val shouldShowRationalePermissions = mutableListOf<String>()
        val shouldNotShowRationalePermissions = mutableListOf<String>()
        for (permission in needRequestPermissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                shouldShowRationalePermissions.add(permission)
            else
                shouldNotShowRationalePermissions.add(permission)
        }

        if (shouldShowRationalePermissions.isNotEmpty()) {
            permissionsCallback.onShowRationale(
                PermissionRequest(
                    getKtxPermissionFragment(this),
                    shouldShowRationalePermissions,
                    requestCode
                )
            )
        }


        if (shouldNotShowRationalePermissions.isNotEmpty()) {
            getKtxPermissionFragment(this).requestPermissionsByFragment(
                shouldNotShowRationalePermissions.toTypedArray(),
                requestCode
            )
        }
    }
}

private fun getKtxPermissionFragment(activity: FragmentActivity): KtxPermissionFragment {
    var fragment = activity.supportFragmentManager.findFragmentByTag(TAG)
    if (fragment == null) {
        fragment = KtxPermissionFragment()
        activity.supportFragmentManager.beginTransaction().add(fragment, TAG).commitNow()
    }
    return fragment as KtxPermissionFragment
}


fun Activity.isGranted(permission: String): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}