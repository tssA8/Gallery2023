package com.wallpaper.gallery.gallery.utils

import android.os.Build

object Constant {

    const val ACTION_AMS_LOGIN = "com.wallpaper.action.ams.AMS_LOGIN"
    var isOverrideBitmap = false
    var preloadBitmapPath = "" //which is getting from PictureViewHolder`s List
    val COLORS_ARRAY = arrayOf("#000000", "#3C6B70", "#3C4670", "#632E8B", "#00C4BF-#004CA4", "#7EB6FF-#551AB7", "#72A6FF-#10069D", "#B237EC-#3B30B0")
    var isAndroidR = Build.VERSION.SDK_INT >= 30
    var CHOOSE_PICTURE_ACTION = "choose_picture_action"
    var CHOOSE_PICTURE_EXTRA = "choose_picture_extra"
}