package com.wallpaper.gallery.gallery.common.service

import android.app.Service
import com.wallpaper.gallery.gallery.MyApplication
import com.wallpaper.gallery.gallery.common.dependnecyinjection.service.ServiceModule

abstract class BaseService : Service() {

    private val appComponent get() = (application as MyApplication).appComponent

    private val serviceComponent by lazy {
        appComponent.newServiceComponent(ServiceModule(this, application))
    }

    protected val injector get() = serviceComponent
}