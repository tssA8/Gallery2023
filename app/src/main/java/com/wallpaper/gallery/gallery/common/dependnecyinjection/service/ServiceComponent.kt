package com.wallpaper.gallery.gallery.common.dependnecyinjection.service

import com.wallpaper.gallery.gallery.service.MyService
import dagger.Subcomponent

@ServiceScope
@Subcomponent(modules = [ServiceModule::class])
interface ServiceComponent {
    fun inject(service: MyService)
}