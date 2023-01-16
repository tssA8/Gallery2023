package com.wallpaper.gallery.gallery.dialogs.promptdialog;

import com.wallpaper.gallery.gallery.views.ObservableViewMvc;

public interface PromptViewMvc extends ObservableViewMvc<PromptViewMvc.Listener> {

    public interface Listener {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    void setTitle(String title);
    void setMessage(String message);
    void setPositiveButtonCaption(String caption);
    void setNegativeButtonCaption(String caption);
}

