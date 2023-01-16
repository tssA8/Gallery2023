package com.wallpaper.gallery.gallery.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.wallpaper.gallery.gallery.R


class DialogFragment : BaseDialog() {
    private lateinit var alertDialog: AlertDialog

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        closeIt()
        alertDialog = AlertDialog.Builder(activity).let {
            val progressBar = ProgressBar(activity)
            val llPadding = 30
            val ll = LinearLayout(activity)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.setPadding(llPadding, llPadding, llPadding, llPadding)
            ll.gravity = Gravity.CENTER
            var llParam = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            ll.layoutParams = llParam
            val color = activity?.resources?.getColor(R.color.darkGreyBg)
            ll.setBackgroundColor(color ?: Color.parseColor(BACKGROUND_COLOR))
            progressBar.setLayoutParams(llParam)
            progressBar.isIndeterminate = true
            progressBar.setPadding(0, 0, llPadding, 0)
            progressBar.layoutParams = llParam

            llParam = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            it.setCancelable(false)
            it.setView(ll)
            // Creating a TextView inside the layout
            val tvText = TextView(activity)
            tvText.text = activity?.resources?.getText(R.string.loading_dialog_message)
            tvText.setTextColor(Color.parseColor(TEXT_COLOR))
            tvText.textSize = 20f
            tvText.layoutParams = llParam
            ll.addView(progressBar)
            ll.addView(tvText)
            it.create()
        }
        dialog?.setCanceledOnTouchOutside(true)
        return alertDialog
    }

    fun closeIt() {
        if (::alertDialog.isInitialized) {
            alertDialog?.let {
                it.dismiss()
            }
        }
    }

    companion object {
        private const val BACKGROUND_COLOR = "#3b384c"
        private const val TEXT_COLOR = "#80ffffff"
        lateinit var dialog: DialogFragment
        fun newInstance(): DialogFragment {
            dialog = DialogFragment()
            return dialog
        }

        fun getInstance(): DialogFragment {
            return dialog
        }
    }
}