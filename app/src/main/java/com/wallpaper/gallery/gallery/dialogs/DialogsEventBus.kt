package com.wallpaper.gallery.gallery.dialogs

import com.wallpaper.gallery.gallery.common.BaseObservable
import javax.inject.Inject

class DialogsEventBus @Inject constructor() : BaseObservable<DialogsEventBus.Listener?>() {
    interface Listener {
        fun onDialogEvent(event: Any?)
    }

    fun postEvent(event: Any?) {
        for (listener in listeners) {
            listener?.onDialogEvent(event)
        }
    }
}