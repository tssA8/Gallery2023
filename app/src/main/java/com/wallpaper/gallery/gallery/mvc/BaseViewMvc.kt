package com.wallpaper.gallery.gallery.mvc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.MutableList

open class BaseViewMvc<LISTENER_TYPE>(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        @LayoutRes private val layoutId: Int,
        private val activities: AppCompatActivity,
        private val pictureList: MutableList<String>? = ArrayList(),
        private val bitmapUtils: BitmapUtils
) {

    val rootView: View = layoutInflater.inflate(layoutId, parent, false)

    protected val context: Context get() = rootView.context

    protected val activity: AppCompatActivity get() = activities

    protected val listeners = HashSet<LISTENER_TYPE>()

    protected val bitmapUtil :BitmapUtils get() = bitmapUtils

    protected val pictures: MutableList<String>? get() = pictureList

    fun registerListener(listener: LISTENER_TYPE) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: LISTENER_TYPE) {
        listeners.remove(listener)
    }

    protected fun <T : View?> findViewById(@IdRes id: Int): T {
        return rootView.findViewById<T>(id)
    }

}