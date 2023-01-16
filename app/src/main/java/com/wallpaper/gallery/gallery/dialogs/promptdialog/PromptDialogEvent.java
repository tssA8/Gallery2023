package com.wallpaper.gallery.gallery.dialogs.promptdialog;

public class PromptDialogEvent {

    public enum Button {
        POSITIVE, NEGATIVE
    }

    private final Button mClickedButton;

    public PromptDialogEvent(Button clickedButton) {
        mClickedButton = clickedButton;
    }

    public Button getClickedButton() {
        return mClickedButton;
    }
}
