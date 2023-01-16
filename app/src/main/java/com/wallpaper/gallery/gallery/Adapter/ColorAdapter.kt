package com.wallpaper.gallery.gallery.adapter

import android.content.Context
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.BuildConfig
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.SaveBitmapColorDataClass
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.gallery.gallery.utils.Constant
import com.wallpaper.gallery.gallery.viewHolder.ColorViewHolder

class ColorAdapter(val context: Context,
                   val activity: AppCompatActivity,
                   val bitmapUtils: BitmapUtils,
                   private val onSaveCb: (SaveBitmapColorDataClass) -> Unit) : RecyclerView.Adapter<ColorViewHolder>() {

    private val mDisplayMetrics: DisplayMetrics = DisplayMetrics()
    private val COLOR_ARRAY = Constant.COLORS_ARRAY
    private var mColorItemList = ArrayList<ColorItem>()

    init {
        //setUpData
        setUpColorList()
        activity.windowManager.defaultDisplay.getMetrics(mDisplayMetrics)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val layouts = if (BuildConfig.UNISTYLE) R.layout.view_holder_color_uni_style else R.layout.view_holder_color
        val view = LayoutInflater.from(context).inflate(layouts, parent, false)


        return ColorViewHolder(view, context, bitmapUtils) { saveCallback ->
            val pos = saveCallback.position
            println("ColorAdapter position: $pos")
            mColorItemList.mapIndexed { index, colorItem ->
                colorItem.isSelect = index == pos
                mColorItemList.set(index, colorItem)
            }
            notifyDataSetChanged()
            onSaveCb.invoke(saveCallback)
        }
    }

    override fun getItemCount(): Int {
        return when (mColorItemList.size) {
            0 -> {
                COLOR_ARRAY.size
            }
            else -> {
                mColorItemList.size
            }
        }
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val viewHolder = holder as ColorViewHolder?
        val colorItem = mColorItemList[position]
        val isHaveTwoColors = colorItem.isHaveTwoColors
        val color1 = colorItem.color
        val color2 = colorItem.colorTwo
        val isSelect = colorItem.isSelect
        if (viewHolder != null) {
            if (isHaveTwoColors) {
                if (color1 != null && color2 != null) {
                    viewHolder.setUI(color1, color2)
                }
            } else {
                if (color1 != null) {
                    viewHolder.setUI(color1)
                }
            }
            println("ColorAdapter onBindViewHolder pos: $position , isSelect : $isSelect")
            viewHolder.setItemSelectBackground(isSelect)
        }
    }

    private fun setUpColorList() {
        //SetUp ColorItemList First
        for (color in COLOR_ARRAY) {
            val colorItem = ColorItem()
            if (!color.contains("-")) {
                val colorValue = Color.parseColor(color)
                with(colorItem) {
                    this.color = colorValue
                    colorTwo = null
                    isHaveTwoColors = false
                    isSelect = false
                }
            } else {
                val colorStrings = color.split("-".toRegex()).toTypedArray()
                val color1 = Color.parseColor(colorStrings[0])
                val color2 = Color.parseColor(colorStrings[1])
                with(colorItem) {
                    this.color = color1
                    colorTwo = color2
                    isHaveTwoColors = true
                    isSelect = false
                }
            }
            mColorItemList.add(colorItem)
        }
    }

    fun unSelectAllColorItems() {
        mColorItemList.forEach {
            it.isSelect = false
        }
        notifyDataSetChanged()
    }

    class ColorItem {
        var color: Int? = null
        var colorTwo: Int? = null
        var isSelect: Boolean = false
        var isHaveTwoColors = false
    }
}