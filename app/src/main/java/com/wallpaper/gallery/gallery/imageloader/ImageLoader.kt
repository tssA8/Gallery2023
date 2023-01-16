package com.wallpaper.gallery.gallery.imageloader

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.common.dependnecyinjection.activity.ActivityScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.io.File
import javax.inject.Inject

@ActivityScope
class ImageLoader @Inject constructor(private val activity: AppCompatActivity) {

    private val requestOptions = RequestOptions().centerCrop()

    fun loadImage(imageUrl: String, target: ImageView) {
        Glide.with(activity).load(imageUrl).apply(requestOptions).into(target)
    }

    fun loadImage(bm: Bitmap, target: ImageView) {
        Glide.with(activity).load(bm).apply(requestOptions).into(target)
    }

    fun loadImage(f: File, target: ImageView) {
        Glide.with(activity).load(f).apply(RequestOptions.bitmapTransform(RoundedCorners(5)).centerCrop()).into(target)
    }

    fun loadImageResource(imageResource: Int, target: ImageView) {
        Glide.with(activity).load(imageResource).apply(requestOptions).into(target)
    }
}