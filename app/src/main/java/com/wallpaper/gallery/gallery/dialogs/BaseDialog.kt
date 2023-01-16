package com.wallpaper.gallery.gallery.dialogs

import androidx.fragment.app.DialogFragment
import com.wallpaper.gallery.gallery.activities.BaseActivity
import com.wallpaper.gallery.gallery.common.dependnecyinjection.presentation.PresentationModule
open class BaseDialog: DialogFragment() {

    private val presentationComponent by lazy {
        (requireActivity() as BaseActivity).activityComponent.newPresentationComponent(PresentationModule(this))
    }

    protected val injector get() = presentationComponent
}