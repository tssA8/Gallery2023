package com.wallpaper.gallery.gallery.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.wallpaper.gallery.gallery.sendAppVersion.SendAppVersion
import com.wallpaper.gallery.gallery.common.service.BaseService
import javax.inject.Inject

class MyService : BaseService() {
    @Inject lateinit var sendAppVersion: SendAppVersion

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        injector.inject(this)
        val filter = IntentFilter()
        filter.addAction(LAUNCHER_GET_APP_VERSION)
        this.registerReceiver(myReceiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(myReceiver)
    }


    private val myReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive(), action:" + intent.action)
            when (intent.action) {
                LAUNCHER_GET_APP_VERSION -> sendAppVersion.sendAppInfo(packageManager, packageName, context)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        throw UnsupportedOperationException("Not yet implemented")
    }

    companion object {
        private val TAG = MyService::class.java.simpleName
        private const val LAUNCHER_GET_APP_VERSION = "com.benq.launchercs.action.GET_APP_VERSION"

        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val myIntent = Intent(context, MyService.javaClass)
                context.startForegroundService(myIntent)
            } else {
                val myIntent = Intent(context, MyService.javaClass)
                context.startService(myIntent)
            }
        }
    }
}