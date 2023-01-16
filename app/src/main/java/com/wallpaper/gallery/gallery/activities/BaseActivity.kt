package com.wallpaper.gallery.gallery.activities

import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.MyApplication
import com.wallpaper.gallery.gallery.common.dependnecyinjection.presentation.PresentationModule

open class BaseActivity : AppCompatActivity() {

    private val appComponent get() = (application as MyApplication).appComponent

    val activityComponent by lazy {
        appComponent.newActivityComponentBuilder()
                .activity(this)
                .build()
    }

    private val presentationComponent by lazy {
        activityComponent.newPresentationComponent(PresentationModule(this))
    }

    protected val injector get() = presentationComponent
}