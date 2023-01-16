package com.wallpaper.gallery.gallery.viewHolder

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.ChangeViewDataClass
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.utils.Constant
import java.io.File


class PictureViewHolder(itemView: View,
                        pictureList: List<String?>,
                        private val imageLoader: ImageLoader,
                        private val clickPictureCb: (ChangeViewDataClass) -> Unit) : RecyclerView.ViewHolder(itemView) {

    private val mImageView: ImageView = itemView.findViewById(R.id.image)
    private val mImageContainer: FrameLayout = itemView.findViewById(R.id.ll_image_container)
    private val mImageFocusView: ImageView = itemView.findViewById(R.id.iv_image_focus)

    fun setUI(path: String?) {
        val file = File(path)
        imageLoader.loadImage(file, mImageView)
    }

    init {
        mImageContainer.setOnClickListener {
            println("PictureViewHolder onclick")
            val picPath = pictureList[adapterPosition]
            picPath?.let {
                Constant.preloadBitmapPath = it
                clickPictureCb.invoke(ChangeViewDataClass(true, it))
                imageLoader.loadImage(File(it), mImageView)
            }
//            setItemSelectBackground(true)
        }

        mImageContainer.setOnFocusChangeListener { v: View?, hasFocus: Boolean ->
            println("PictureViewHolder hasFocus: $hasFocus")
            if (hasFocus) {
                v?.performClick()
            }
            var fid = v?.nextFocusDownId
            println("PictureViewHolder fid: $fid")
        }
    }

//    fun setItemSelectBackground(isSelected: Boolean) {
//        if (isSelected) {
//            imageLoader.loadImageResource(R.drawable.picture_focus_border, mImageFocusView)
//        } else {
//            imageLoader.loadImageResource(R.drawable.picture_normal_border, mImageFocusView)
//        }
//    }
}