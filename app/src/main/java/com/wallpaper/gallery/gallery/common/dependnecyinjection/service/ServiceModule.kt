package com.wallpaper.gallery.gallery.common.dependnecyinjection.service

import android.app.Application
import android.app.Service
import android.content.Context
import dagger.Module
import dagger.Provides

@Module
class ServiceModule(
        val service: Service,
        val application: Application
) {
    @Provides
    fun context(): Context = application.applicationContext
}