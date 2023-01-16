package com.wallpaper.gallery.gallery.dialogs.promptdialog

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.mvc.BaseViewMvc
import com.wallpaper.gallery.gallery.utils.BitmapUtils

class PromptViewMvcImpl(layoutInflater: LayoutInflater,
                        parent: ViewGroup?,
                        activity: AppCompatActivity,
                        bitmapUtils: BitmapUtils)
    : BaseViewMvc<PromptViewMvc.Listener?>(
        layoutInflater,
        parent,
        R.layout.dialog_prompt,
        activity,
        null,
        bitmapUtils
    ) {
    private val mTxtTitle: TextView = findViewById(R.id.txt_title)
    private val mTxtMessage: TextView = findViewById(R.id.txt_message)
    private val mBtnPositive: AppCompatButton = findViewById(R.id.btn_positive)
    private val mBtnNegative: AppCompatButton = findViewById(R.id.btn_negative)

    init {
        mBtnPositive.setOnClickListener {
            for (listener in listeners) {
                listener?.onPositiveButtonClicked()
            }
        }
        mBtnNegative.setOnClickListener {
            for (listener in listeners) {
                listener?.onNegativeButtonClicked()
            }
        }
    }

    fun setTitle(title: String) {
        mTxtTitle.text = title
    }

    fun setMessage(message: String) {
        mTxtMessage.text = message
    }

    fun setPositiveButtonCaption(caption: String) {
        mBtnPositive.text = caption
    }

    fun setNegativeButtonCaption(caption: String) {
        mBtnNegative.text = caption
    }

}