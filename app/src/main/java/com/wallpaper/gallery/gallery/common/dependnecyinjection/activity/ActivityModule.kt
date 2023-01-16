package com.wallpaper.gallery.gallery.common.dependnecyinjection.activity

import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import dagger.Module
import dagger.Provides

@Module
abstract class ActivityModule {

    companion object {
        @Provides
        fun layoutInflater(activity: AppCompatActivity) = LayoutInflater.from(activity)

        @Provides
        fun bitmapUtil() = BitmapUtils()

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager
    }
}