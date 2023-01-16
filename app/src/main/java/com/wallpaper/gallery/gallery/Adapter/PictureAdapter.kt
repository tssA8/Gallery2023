package com.wallpaper.gallery.gallery.adapter

import android.util.DisplayMetrics
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.BuildConfig
import com.wallpaper.gallery.gallery.ChangeViewDataClass
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.viewHolder.PictureViewHolder
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.viewMvc.MainViewMvc

class PictureAdapter(val activity: AppCompatActivity,
                     private val pictureList: List<String>,
                     private val imageLoader: ImageLoader,
                     private val changeWallpapersCallback: (ChangeViewDataClass) -> Unit) : RecyclerView.Adapter<PictureViewHolder>() {
    private val displayMetrics: DisplayMetrics = DisplayMetrics()

    private lateinit var imageContainer: FrameLayout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val v = when (BuildConfig.UNISTYLE) {
            true -> {
                R.layout.view_holder_uni_picture
            }
            else -> R.layout.view_holder_picture
        }
        val view = LayoutInflater.from(parent.context).inflate(v, parent, false)
        imageContainer = view.findViewById<FrameLayout>(R.id.ll_image_container)
        imageContainer.setOnKeyListener { view, i, keyEvent ->
            var keyCode = keyEvent.keyCode
            Log.d(MainViewMvc.TAG, " imageContainer keyCode: $keyCode")
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                view.nextFocusDownId = R.id.btn_save
            }
            false
        }

        return PictureViewHolder(view, pictureList, imageLoader) { clickImageCallback ->
            val path = clickImageCallback.path
            println("PictureViewHolder path: $path")
            changeWallpapersCallback.invoke(clickImageCallback)
        }
    }

    override fun getItemCount(): Int {
        return pictureList.size
    }

    init {
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        val viewHolder = holder as PictureViewHolder?
        viewHolder!!.setUI(pictureList[position])
//        viewHolder.setItemSelectBackground(true)
        changeWallpapersCallback.invoke(ChangeViewDataClass(true, pictureList[position]))
    }

    fun setWallpaperFocused() {
        if (::imageContainer.isInitialized) imageContainer.requestFocus()
    }
}