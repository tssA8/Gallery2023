package com.wallpaper.gallery.gallery.common

import android.content.Intent
import androidx.core.app.ActivityCompat.startActivityForResult


class IntentGallery {
    private val AMS_PKG_NAME = "com.benq.ifp.ams"
    private val AMS_ACTIVITY_NAME = "${AMS_PKG_NAME}.activityMainDialog"


    private val imageIntent: Intent by lazy {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent
    }

    private val androidGalleryIntent: Intent by lazy {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.type = "image/*"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent
    }

    fun getIntent(): Intent {
//        val intent = imageIntent
//        intent.component = ComponentName(AMS_PKG_NAME, AMS_ACTIVITY_NAME)
        val intent = androidGalleryIntent
        return intent
    }

    fun galleryIntent(): Intent{
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        return intent
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1)
    }

}