package com.wallpaper.gallery.gallery.common.dependnecyinjection.app

import android.app.Application
import dagger.Module
import dagger.Provides

@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    fun application() = application

    @Provides
    @AppScope
    fun context() = application.applicationContext

}