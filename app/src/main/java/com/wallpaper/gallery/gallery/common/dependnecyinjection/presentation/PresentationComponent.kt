package com.wallpaper.gallery.gallery.common.dependnecyinjection.presentation

import com.wallpaper.gallery.gallery.MainActivity
import com.wallpaper.gallery.gallery.ThumbnailActivity
import dagger.Subcomponent

@PresentationScope
@Subcomponent(modules = [PresentationModule::class, ViewModelModule::class])
interface PresentationComponent {
    fun inject(activity: MainActivity)
    fun inject(activity: ThumbnailActivity)
}