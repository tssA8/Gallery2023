package com.wallpaper.gallery.gallery.viewMvc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.dialogs.promptdialog.PromptViewMvcImpl
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import java.util.*
import javax.inject.Inject
import javax.inject.Provider

class ViewMvcFactory @Inject constructor(
        private val layoutInflater: LayoutInflater,
        private val imageLoaderProvider: Provider<ImageLoader>,
        private val bitmapUtils: BitmapUtils
) {

    fun newMainViewMvc(parent: ViewGroup?, pictures: MutableList<String> = ArrayList(), activity: AppCompatActivity): MainViewMvc {
        return MainViewMvc(layoutInflater, parent, pictures, imageLoaderProvider.get(), activity, bitmapUtils)
    }

    fun newThumbnailViewMvc(parent: ViewGroup?, activity: AppCompatActivity): ThumbnailViewMvc {
        return ThumbnailViewMvc(layoutInflater, parent, activity, bitmapUtils)
    }

    fun PromptViewMvc(parent: ViewGroup?, activity: AppCompatActivity): PromptViewMvcImpl {
        return PromptViewMvcImpl(layoutInflater, parent, activity, bitmapUtils)
    }
}