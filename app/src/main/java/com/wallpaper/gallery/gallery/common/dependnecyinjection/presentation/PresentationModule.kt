package com.wallpaper.gallery.gallery.common.dependnecyinjection.presentation

import androidx.savedstate.SavedStateRegistryOwner
import com.wallpaper.gallery.gallery.common.IntentGallery
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapBase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapCase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapFileProviderCase
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import dagger.Module
import dagger.Provides

@Module
class PresentationModule(private val savedStateRegistryOwner: SavedStateRegistryOwner) {

    @Provides
    fun saveBitmapBase(bitmapUtils: BitmapUtils) = SaveBitmapBase(bitmapUtils)

    @Provides
    fun saveBitmapCase(saveBitmapBase: SaveBitmapBase, bitmapUtils: BitmapUtils) = SaveBitmapCase(saveBitmapBase, bitmapUtils)

    @Provides
    fun saveBitmapFileProviderCase(saveBitmapBase: SaveBitmapBase, bitmapUtils: BitmapUtils) = SaveBitmapFileProviderCase(saveBitmapBase, bitmapUtils)

    @Provides
    fun intentWithAMS() = IntentGallery()

    @Provides
    fun savedStateRegistryOwner() = savedStateRegistryOwner

}