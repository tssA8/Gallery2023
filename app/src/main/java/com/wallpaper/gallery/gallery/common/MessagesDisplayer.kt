package com.wallpaper.gallery.gallery.common

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wallpaper.gallery.gallery.common.dependnecyinjection.activity.ActivityScope
import javax.inject.Inject

@ActivityScope
class MessagesDisplayer @Inject constructor(private val activity: AppCompatActivity) {

    private val errorMsg = "The photo cannot open because the file is invalid."
    private val noCropModeMsg = "onDestroy NO_CROP_MODE"
    private val imageErrorMsg = "ImageSize is 4k , Not Support!"

    fun showUseCaseError() {
        Toast.makeText(activity, errorMsg, Toast.LENGTH_SHORT).show()
    }

    fun showCropModeError() {
        Toast.makeText(activity, noCropModeMsg, Toast.LENGTH_LONG).show()
    }

    fun showImageIsNotSupport() {
        Toast.makeText(activity, imageErrorMsg, Toast.LENGTH_LONG).show()
    }
}