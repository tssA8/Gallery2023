package com.wallpaper.gallery.gallery.dialogs.promptdialog

import android.app.Dialog
import android.os.Bundle
import com.wallpaper.gallery.gallery.dialogs.BaseDialog
import com.wallpaper.gallery.gallery.dialogs.DialogsEventBus
import javax.inject.Inject

class PromptDialog(private var mViewMvc: PromptViewMvc) : BaseDialog(), PromptViewMvc.Listener {
    @Inject lateinit var mDialogsEventBus: DialogsEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        checkNotNull(arguments) { "arguments mustn't be null" }
        mViewMvc.setTitle(arguments!!.getString(ARG_TITLE))
        mViewMvc.setMessage(arguments!!.getString(ARG_MESSAGE))
        mViewMvc.setPositiveButtonCaption(arguments!!.getString(ARG_POSITIVE_BUTTON_CAPTION))
        mViewMvc.setNegativeButtonCaption(arguments!!.getString(ARG_NEGATIVE_BUTTON_CAPTION))
        val dialog = Dialog(requireContext())
        dialog.setContentView(mViewMvc.rootView)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        mViewMvc.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        mViewMvc.unregisterListener(this)
    }

    override fun onPositiveButtonClicked() {
        dismiss()
        mDialogsEventBus.postEvent(PromptDialogEvent(PromptDialogEvent.Button.POSITIVE))
    }

    override fun onNegativeButtonClicked() {
        dismiss()
        mDialogsEventBus.postEvent(PromptDialogEvent(PromptDialogEvent.Button.NEGATIVE))
    }

    companion object {
        protected const val ARG_TITLE = "ARG_TITLE"
        protected const val ARG_MESSAGE = "ARG_MESSAGE"
        protected const val ARG_POSITIVE_BUTTON_CAPTION = "ARG_POSITIVE_BUTTON_CAPTION"
        protected const val ARG_NEGATIVE_BUTTON_CAPTION = "ARG_NEGATIVE_BUTTON_CAPTION"
        fun newPromptDialog(title: String?,
                            message: String?,
                            positiveButtonCaption: String?,
                            negativeButtonCaption: String?,
                            promptViewMvc: PromptViewMvc): PromptDialog {
            val promptDialog = PromptDialog(promptViewMvc)
            val args = Bundle(4)
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            args.putString(ARG_POSITIVE_BUTTON_CAPTION, positiveButtonCaption)
            args.putString(ARG_NEGATIVE_BUTTON_CAPTION, negativeButtonCaption)
            promptDialog.arguments = args
            return promptDialog
        }
    }
}