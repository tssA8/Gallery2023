package com.wallpaper.gallery.gallery

import android.app.Application
import com.wallpaper.gallery.gallery.common.dependnecyinjection.app.AppComponent
import com.wallpaper.gallery.gallery.common.dependnecyinjection.app.AppModule
import com.wallpaper.gallery.gallery.common.dependnecyinjection.app.DaggerAppComponent

class MyApplication : Application() {

    public val appComponent: AppComponent by lazy {
        DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
    }

}