package com.wallpaper.gallery.gallery.viewMvc

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.wallpaper.gallery.gallery.BuildConfig
import com.wallpaper.gallery.gallery.R
import com.wallpaper.gallery.gallery.ThumbnailActivity
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.gallery.gallery.utils.LikeQQCropViewTwo
import com.wallpaper.gallery.gallery.mvc.BaseViewMvc
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import com.makeramen.roundedimageview.RoundedImageView
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


class ThumbnailViewMvc(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        activities: AppCompatActivity,
        bitmapUtils: BitmapUtils
) : BaseViewMvc<ThumbnailViewMvc.Listener>(
        layoutInflater,
        parent,
        if (BuildConfig.UNISTYLE) R.layout.activity_thumbnail_uni_style else R.layout.activity_thumbnail,
        activities,
        null,
        bitmapUtils
) {


    interface Listener {
        fun intentAms()
        fun getNowBitmap(): Bitmap?
        fun setNowBitmap(bm: Bitmap?)
        fun getBrowsePath(): String?
        fun getNowPageSate(): NowPage
        fun setNowPageState(page: NowPage)
        fun getUserType(): UserType
        suspend fun saveBitmapToFile(bm: Bitmap?)
    }

    sealed class NowPage {
        object PREVIEW_PAGE : NowPage()
        object PREVIEW_PAGE2 : NowPage()
        object CROP_PAGE : NowPage()
    }

    sealed class UserType {
        object USER_TYPE_LOCAL_PUBLIC : UserType()
        object USER_TYPE_GUEST : UserType()
        object USER_TYPE_USER : UserType()
    }

    private val TAG = "ThumbnailViewMvc"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var browsePicBitmapAndroidR: Bitmap? = null
    private var uriFromAms: Uri? = null
    private var returnBitmap: Bitmap? = null
    private var guestBitmap: Bitmap? = null

    //view
    private lateinit var tvReset: TextView
    private lateinit var btnResetUniStyle: Button
    private var btnBack: ImageView = findViewById(R.id.iv_back)
    private var btnCancel: ImageView = findViewById(R.id.iv_cancel)
    private var rvThumbnail: RoundedImageView = findViewById(R.id.thumbnail)
    private var btnSave: Button = findViewById(R.id.btn_save)
    private var btnBrowse: Button = findViewById(R.id.btn_browse)
    private var previewView: LinearLayout = findViewById(R.id.preview_view)
    private var cropView: LinearLayout = findViewById(R.id.crop_view)
    private var clipImageLayout: LikeQQCropViewTwo = findViewById(R.id.clipImageLayout)
    private var btnRotate: ImageView = findViewById(R.id.btn_rotate)
    private var btnNext: Button = findViewById(R.id.btn_next)


    init {

        if (BuildConfig.UNISTYLE) {
            btnResetUniStyle = findViewById(R.id.reset)
            btnSave.background = activity.getDrawable(R.drawable.button_unistyle_selector)
            btnResetUniStyle.background = activity.getDrawable(R.drawable.button_unistyle_selector)
            btnBrowse.background = activity.getDrawable(R.drawable.button_unistyle_selector)
        } else {
            tvReset = findViewById(R.id.reset)
        }

        btnBack.setOnClickListener {
            for (listener in listeners) {
                when (listener.getNowPageSate()) {
                    NowPage.PREVIEW_PAGE -> {
                        val path = listener.getBrowsePath()
                        path?.let {
                            showCrop(it)
                        }
                    }
                    NowPage.PREVIEW_PAGE2 -> {
                        val path = listener.getBrowsePath()
                        path?.let {
                            showCrop(it)
                        }
                    }
                    NowPage.CROP_PAGE -> {
                        listener.intentAms()
                    }
                }
            }
        }
        btnCancel.setOnClickListener {
            val resultIntent = Intent()
            activity.setResult(Activity.RESULT_CANCELED, resultIntent)
            activity.finish()
        }
        btnSave.setOnClickListener {
            coroutineScope.launch {
                for (listener in listeners) {
                    val bm = listener.getNowBitmap()
                    bm?.let {
                        withContext(BackgroundDispatcher) {
                            val resultIntent = Intent()
                            returnBitmap = it
                            for (listener in listeners) {
                                listener.saveBitmapToFile(it)
                            }
                            activity.setResult(Activity.RESULT_OK, resultIntent)
                        }
                        activity.finish()
                    } ?: run {
                        val resultIntent = Intent()
                        activity.setResult(RESULT_CANCELED, resultIntent)
                        activity.finish()
                    }
                }
            }
        }
        btnBrowse.setOnClickListener {
            println(TAG + " btnBrowse")
            for (listener in listeners) {
                listener.intentAms()
            }
        }

        if (BuildConfig.UNISTYLE) {
            btnResetUniStyle.setOnClickListener {
                resetBitmap()
            }
        } else {
            tvReset.setOnClickListener {
                resetBitmap()
            }
        }
        btnNext.setOnClickListener {
            val bm = bitmapUtils.createScaleBitmap(clipImageLayout.clip(), ThumbnailActivity.BIG_HEAD_SIZE, ThumbnailActivity.BIG_HEAD_SIZE)
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    for (listener in listeners) {
                        listener.setNowBitmap(bm)
                    }
                    showPreviewPage(true)
                }
            }

        }
        btnRotate.setOnClickListener {
            ThumbnailActivity.rotateNum = (ThumbnailActivity.rotateNum + 1) % 4
            val angle = (0 - ThumbnailActivity.rotateNum) * 90
            for (listener in listeners) {
                var path = listener.getBrowsePath()
                if (TextUtils.isEmpty(path)) path = ThumbnailActivity.RETURN_URL
                clipImageLayout.setBitmapForWidth(path, 1920, angle)
            }
        }

        btnRotate.setOnFocusChangeListener { _, hasFocus ->
            when (hasFocus) {
                true -> {
                    btnRotate.setImageResource(R.drawable.rotate_active)
                }
                false -> {
                    btnRotate.setImageResource(R.drawable.ic_thumbnail_edit_rotate)
                }
            }
        }

        if (!BuildConfig.UNISTYLE) {
            tvReset.setOnFocusChangeListener { _, isClick ->
                if (isClick) {
                    tvReset.setTextColor(activity.getResources().getColor(R.color.lightBlue))
                } else {
                    tvReset.setTextColor(activity.resources.getColor(R.color.littleWhite))
                }
            }
        }

        btnBrowse.requestFocus()
    }


    suspend fun getBitmap(drawableId: Int): Bitmap? {
        return withContext(BackgroundDispatcher) {
            when (val drawable = ContextCompat.getDrawable(activity, drawableId)) {
                is BitmapDrawable -> {
                    guestBitmap = BitmapFactory.decodeResource(activity.resources, drawableId)
                    guestBitmap
                }
                is VectorDrawable -> {
                    guestBitmap = getVectorDrawable(drawable)
                    guestBitmap
                }
                else -> {
                    guestBitmap = null
                    guestBitmap
                    throw IllegalArgumentException("unsupported drawable type")
                }
            }
        }
    }

    fun getVectorDrawable(vectorDrawable: VectorDrawable): Bitmap? {
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)
        Log.e(TAG, "getVectorDrawable done")
        return bitmap
    }

    @UiThread
    fun showCrop(browsePicPath: String) {
        ThumbnailActivity.rotateNum = 0
        previewView.visibility = View.GONE
        cropView.visibility = View.VISIBLE
        btnBack.visibility = View.VISIBLE
        clipImageLayout.setBitmapForWidth(browsePicPath, 1920)
        clipImageLayout.radius = 420F
        clipImageLayout.borderColor = activity.resources.getColor(R.color.cropMaskColor)
        clipImageLayout.maskColor = activity.resources.getColor(R.color.cropMaskColor)
        for (listener in listeners) {
            listener.setNowPageState(NowPage.CROP_PAGE)
        }
    }

    @UiThread
    fun showCrop() {
        ThumbnailActivity.rotateNum = 0
        previewView.visibility = View.GONE
        cropView.visibility = View.VISIBLE
        btnBack.visibility = View.VISIBLE
        clipImageLayout.setBitmapForWidth(browsePicBitmapAndroidR, 1920)
        clipImageLayout.radius = 420F
        clipImageLayout.borderColor = ContextCompat.getColor(activity, R.color.cropMaskColor)
        clipImageLayout.maskColor = ContextCompat.getColor(activity, R.color.cropMaskColor)
        for (listener in listeners) {
            listener.setNowPageState(NowPage.CROP_PAGE)
        }
    }

    fun showPreviewPage(hasBack: Boolean) {
        previewView.visibility = View.VISIBLE
        cropView.visibility = View.GONE
        var bm: Bitmap? = null
        for (listener in listeners) {
            bm = listener.getNowBitmap()
        }
        when (hasBack) {
            true -> {
                btnBack.visibility = View.VISIBLE
                bm?.apply {
                    rvThumbnail.setImageBitmap(this)
                }
                for (listener in listeners) {
                    listener.setNowPageState(NowPage.PREVIEW_PAGE2)
                }
            }
            false -> {
                btnBack.visibility = View.INVISIBLE
                bm?.apply {
                    Log.d(TAG, "AAA_showPreviewPage, setImageBitmap")
                    rvThumbnail.setImageBitmap(this)
                } ?: run {
                    Log.d(TAG, "AAA_showPreviewPage, bm is null , let`s set returnBitmap")
                    if (returnBitmap != null) rvThumbnail.setImageBitmap(returnBitmap)
                }
                for (listener in listeners) {
                    listener.setNowPageState(NowPage.PREVIEW_PAGE)
                }
            }
        }
    }

    private fun resetBitmap() {
        for (listener in listeners) {
            when (listener.getUserType()) {
                is UserType.USER_TYPE_LOCAL_PUBLIC -> {
                    guestBitmap?.let {
                        listener.setNowBitmap(it)
                        rvThumbnail.setImageBitmap(it)
                    } ?: run {
                        coroutineScope.launch {
                            val bm = getBitmap(R.drawable.ic_thumbnail_guest)
                            listener.setNowBitmap(bm)
                            rvThumbnail.setImageBitmap(bm)
                        }
                    }
                }
                is UserType.USER_TYPE_GUEST -> {
                    guestBitmap?.let {
                        listener.setNowBitmap(it)
                        rvThumbnail.setImageBitmap(it)
                    } ?: run {
                        coroutineScope.launch {
                            val bm = getBitmap(R.drawable.ic_thumbnail_guest)
                            listener.setNowBitmap(bm)
                            rvThumbnail.setImageBitmap(bm)
                        }
                    }
                }
                is UserType.USER_TYPE_USER -> {
                    coroutineScope.launch {
                        val bm = bitmapUtil.drawDefaultImg(Color.GRAY,
                                Color.GRAY, Color.WHITE,
                                ThumbnailActivity.mUserShortName,
                                ThumbnailActivity.BIG_HEAD_SIZE,
                                ThumbnailActivity.BIG_HEAD_SIZE)
                        listener.setNowBitmap(bm)
                        rvThumbnail.setImageBitmap(bm)
                    }
                }
            }
        }
    }

    suspend fun getFileProviderAndroidR(uri: Uri?): Bitmap? {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                val file = File(ThumbnailActivity.RETURN_URL)
                when {
                    file.exists() -> {
                        Log.d(TAG, "AAA_File is exist")
                        browsePicBitmapAndroidR = BitmapFactory.decodeFile(file.absolutePath)
                        browsePicBitmapAndroidR
                    }
                    else -> {
                        Log.d(TAG, "AAA_File is not exist")
                        browsePicBitmapAndroidR = getBitmap(uri)
                        browsePicBitmapAndroidR
                    }
                }
            }
        }
    }


    private suspend fun getBitmap(uri: Uri?): Bitmap? {
        return withContext(BackgroundDispatcher) {
            var bitmap: Bitmap? = null
            uriFromAms = uri
            if (uri == null) {
                Log.e(TAG, "Get uri : $uri fail")
            } else {
                val contentResolver = activity.contentResolver
                if (contentResolver == null) {
                    Log.e(TAG, "Get contentResolver : $contentResolver fail")
                } else {
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "rw")
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                    if (fileDescriptor == null) {
                        Log.e(TAG, "Get fileDescriptor : $fileDescriptor fail")
                    } else {
                        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                        try {
                            parcelFileDescriptor.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            bitmap
        }
    }


}