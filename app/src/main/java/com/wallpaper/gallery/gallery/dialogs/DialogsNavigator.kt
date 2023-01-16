package com.wallpaper.gallery.gallery.dialogs

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.dialogs.infodialog.InfoDialog
import com.wallpaper.gallery.gallery.dialogs.promptdialog.PromptDialog
import com.wallpaper.gallery.gallery.dialogs.promptdialog.PromptViewMvc
import javax.inject.Inject

class DialogsNavigators @Inject constructor(private val fragmentManager: FragmentManager) {
    private var isInit = false
    fun showLoadingDialog() {
        if (isInit) return
        print("showLoadingDialog")
        fragmentManager.beginTransaction()
                .add(DialogFragment.newInstance(), null)
                .commitAllowingStateLoss()
        isInit = true
    }

    fun closeIt() {
        if (!isInit) return
        DialogFragment.getInstance().closeIt()
        isInit = false
    }

    //Permission
    fun showUseCaseErrorDialog(activity: AppCompatActivity,
                               tag: String,
                               promptViewMvc: PromptViewMvc) {
        val dialogFragment: PromptDialog = PromptDialog.newPromptDialog(
                activity.getString(R.string.error_network_call_failed_title),
                activity.getString(R.string.error_network_call_failed_message),
                activity.getString(R.string.error_network_call_failed_positive_button_caption),
                activity.getString(R.string.error_network_call_failed_negative_button_caption),
                promptViewMvc
        )
        dialogFragment.show(fragmentManager, tag)
    }

    fun showPermissionGrantedDialog(activity: AppCompatActivity,
                                    tag: String?) {
        val dialogFragment: InfoDialog = InfoDialog.newInfoDialog(
                activity.getString(R.string.permission_dialog_title),
                activity.getString(R.string.permission_dialog_granted_message),
                activity.getString(R.string.permission_dialog_button_caption)
        )
        dialogFragment.show(fragmentManager, tag)
    }

    fun showPermissionDeclinedCantAskMoreDialog(activity: AppCompatActivity,
                                                tag: String?) {
        val dialogFragment: InfoDialog = InfoDialog.newInfoDialog(
                activity.getString(R.string.permission_dialog_title),
                activity.getString(R.string.permission_dialog_cant_ask_more),
                activity.getString(R.string.permission_dialog_button_caption)
        )
        dialogFragment.show(fragmentManager, tag)
    }

    fun showDeclinedDialog(activity: AppCompatActivity, tag: String?) {
        val dialogFragment: InfoDialog = InfoDialog.newInfoDialog(
                activity.getString(R.string.permission_dialog_title),
                activity.getString(R.string.permission_dialog_user_declined),
                activity.getString(R.string.permission_dialog_button_caption)
        )
        dialogFragment.show(fragmentManager, tag)
    }


}