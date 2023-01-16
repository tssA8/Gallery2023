package com.wallpaper.gallery.gallery.sendAppVersion

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.wallpaper.gallery.gallery.common.dependnecyinjection.service.ServiceScope
import javax.inject.Inject

@ServiceScope
class SendAppVersion @Inject constructor() {

    fun sendAppInfo(packageManager: PackageManager,
                    packageName: String,
                    context: Context) {
        println("$TAG sendAppInfo")
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = pInfo.versionName
            val it = Intent(LAUNCHER_RETURN_APP_VERSION)
            it.putExtra(EXTRA_PACKAGE_NAME, packageName)
            it.putExtra(EXTRA_VERSION_NO, versionName)
            it.putExtra(EXTRA_IS_STAGE, false)
            context.sendBroadcast(it)
            Log.d(TAG, "package name: $packageName, version: $versionName")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TAG = "SendAppVersion"
        private const val EXTRA_IS_STAGE = "is_stage"
        private const val EXTRA_VERSION_NO = "version_no"
        private const val EXTRA_PACKAGE_NAME = "package_name"
        private const val LAUNCHER_RETURN_APP_VERSION = "com.benq.launchercs.action.APP_VERSION"
    }
}