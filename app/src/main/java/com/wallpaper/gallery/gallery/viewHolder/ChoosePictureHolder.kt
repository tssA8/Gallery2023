package com.wallpaper.gallery.gallery.viewHolder

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.ChoosePictureDataClass
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.utils.Constant

class ChoosePictureHolder(itemView: View,
                          private val imageLoader: ImageLoader,
                          pictureList: ArrayList<Int>,
                          private val context: Context,
                          private val choosePictureCb: (ChoosePictureDataClass) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private val mImageView: ImageView = itemView.findViewById(R.id.image)
    private val mImageContainer: FrameLayout = itemView.findViewById(R.id.ll_image_container)
    private val mImageFocusView: ImageView = itemView.findViewById(R.id.iv_image_focus)

    fun setUI(pictureId: Int) {
        imageLoader.loadImageResource(pictureId, mImageView)
    }

    init {
        mImageContainer.setOnClickListener {
            println("ChoosePictureHolder onclick")
            val picPath = pictureList[adapterPosition]
            choosePictureCb.invoke(ChoosePictureDataClass(picPath))
        }
    }

}