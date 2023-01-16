package com.wallpaper.gallery.gallery.common

import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class BaseObservable<LISTENER_CLASS> {
    // thread-safe set of listeners
    private val mListeners = Collections.newSetFromMap(
            ConcurrentHashMap<LISTENER_CLASS, Boolean>(1))

    fun registerListener(listener: LISTENER_CLASS) {
        mListeners.add(listener)
    }

    fun unregisterListener(listener: LISTENER_CLASS) {
        mListeners.remove(listener)
    }

    protected val listeners: Set<LISTENER_CLASS>
        protected get() = Collections.unmodifiableSet(mListeners)
}