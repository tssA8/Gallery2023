package com.wallpaper.gallery.gallery.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.ChoosePictureDataClass
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.viewHolder.ChoosePictureHolder

class ChoosePictureAdapter(val activity: AppCompatActivity,
                           private val imageLoader: ImageLoader,
                           private val defaultWallpaper: ArrayList<Int>,
                           private val choosePictureCallback: (ChoosePictureDataClass) -> Unit)
    : RecyclerView.Adapter<ChoosePictureHolder>() {

    private lateinit var imageContainer: FrameLayout

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoosePictureHolder {
        val v = R.layout.choose_wallpaper_layout
        val view = LayoutInflater.from(parent.context).inflate(v, parent, false)
        imageContainer = view.findViewById<FrameLayout>(R.id.ll_image_container)


        return ChoosePictureHolder(view,
                imageLoader,
                defaultWallpaper,
                parent.context) { choosePictureDataClass ->
            choosePictureCallback.invoke(choosePictureDataClass)
        }
    }

    override fun onBindViewHolder(holder: ChoosePictureHolder, position: Int) {
        val viewHolder = holder as ChoosePictureHolder
        viewHolder!!.setUI(defaultWallpaper[position])

    }

    override fun getItemCount(): Int {
        return defaultWallpaper.size
    }

}