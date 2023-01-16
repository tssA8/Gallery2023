package com.wallpaper.gallery.gallery.viewHolder

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.SaveBitmapColorDataClass
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.gallery.gallery.utils.Constant
import com.makeramen.roundedimageview.RoundedImageView

class ColorViewHolder(itemView: View,
                      private val context: Context,
                      private val bitmapUtils: BitmapUtils,
                      private val onSaveCb: (SaveBitmapColorDataClass) -> Unit) : RecyclerView.ViewHolder(itemView) {
    private val mImageView: RoundedImageView = itemView.findViewById(R.id.image)
    private val mImageViewContainer: FrameLayout = itemView.findViewById(R.id.fl_color_container)
    fun setUI(color: Int) {
        val bitmap = bitmapUtils.createColorBitmap(color, 50, 50)
        with(mImageView) {
            setImageBitmap(bitmap)
            borderColor = context.resources.getColor(R.color.purpleGrey)
            borderWidth = 1.0f
        }
    }

    fun setUI(color1: Int, color2: Int) {
        val bitmap = bitmapUtils.createColorBitmap(color1, color2, 50, 50)
        with(mImageView) {
            setImageBitmap(bitmap)
            borderColor = context.resources.getColor(R.color.purpleGrey)
            borderWidth = 1.0f
        }
    }

    fun setItemSelectBackground(isSelected: Boolean) {
        if (isSelected) {
            mImageView.borderColor = context.resources.getColor(R.color.lightBlue)
            mImageView.borderWidth = 3.0f
        } else {
            mImageView.borderColor = context.resources.getColor(R.color.purpleGrey)
            mImageView.borderWidth = 1.0f
        }
    }


    init {
        mImageViewContainer.setOnClickListener {
            println("Color_click")
            onSaveCb.invoke(SaveBitmapColorDataClass(EditImageType.IMAGE_TYPE_COLOR.value,
                    adapterPosition.toString() + "",
                    Constant.isOverrideBitmap,
                    context,
                    adapterPosition))
        }
        mImageViewContainer.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            println("Color_focus")
            if (hasFocus) {
                v.performClick()
            }
        }
    }
}