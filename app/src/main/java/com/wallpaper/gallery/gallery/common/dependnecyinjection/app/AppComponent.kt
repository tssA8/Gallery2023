package com.wallpaper.gallery.gallery.common.dependnecyinjection.app

import com.wallpaper.gallery.gallery.common.dependnecyinjection.activity.ActivityComponent
import com.wallpaper.gallery.gallery.common.dependnecyinjection.service.ServiceComponent
import com.wallpaper.gallery.gallery.common.dependnecyinjection.service.ServiceModule
import dagger.Component

@AppScope
@Component(modules = [AppModule::class])
interface AppComponent {

    fun newActivityComponentBuilder(): ActivityComponent.Builder

    fun newServiceComponent(serviceModule: ServiceModule): ServiceComponent
}