package com.wallpaper.gallery.gallery.viewMvc

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.text.TextPaint
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.leanback.widget.HorizontalGridView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wallpaper.gallery.gallery.*
import com.wallpaper.gallery.gallery.adapter.ColorAdapter
import com.wallpaper.gallery.gallery.adapter.PictureAdapter
import com.wallpaper.gallery.gallery.common.ChooseMode
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.common.TestUtils.printCoroutineScopeInfo
import com.wallpaper.gallery.gallery.imageloader.ImageLoader
import com.wallpaper.gallery.gallery.mvc.BaseViewMvc
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.gallery.gallery.utils.Constant
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

class MainViewMvc(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?,
        pictures: MutableList<String> = ArrayList(),
        imageLoader: ImageLoader,
        activity: AppCompatActivity,
        bitmapUtils: BitmapUtils
) : BaseViewMvc<MainViewMvc.Listener>(
        layoutInflater,
        parent,
        if (BuildConfig.UNISTYLE) R.layout.wallpaper_layout_uni_style else R.layout.activity_main,
        activity,
        pictures,
        bitmapUtils
) {

    interface Listener {
        fun intentSystemGallery()
        fun setResultOK()
        fun finishActivity()
        fun showLoadingDialog()
        fun closeLoadingDialog()
        fun updatePictureListData()
        fun changeWallpaper(bindView: ChangeViewDataClass)
        fun getMemorySize(bitmap: Bitmap): Int
        fun resizeBitmapAndSave(cropType: Int)
        fun saveBitmapByFileStreams()
        fun saveBitmapByPath(bitmap: Bitmap)
        fun saveBitmapByFile(source: File, dest: File)
        suspend fun setWallpaperFilter(cropType: Int, preloadPicPath: String?, bitmap: Bitmap?)
        fun setColorWallpaper(saveBitmapColorDataClass: SaveBitmapColorDataClass)
    }

    private val TEN_MB_LIMIT = 10
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val pictureAdapter: PictureAdapter = PictureAdapter(activity, pictures, imageLoader) { changeCallback ->
        for (listener in listeners) {
            listener.changeWallpaper(changeCallback)
        }
    }
    private val colorAdapter: ColorAdapter = ColorAdapter(rootView.context, activity, bitmapUtil) { saveCallback ->
        for (listener in listeners) {
            listener.setColorWallpaper(saveCallback)
        }
        imageType = EditImageType.IMAGE_TYPE_COLOR
    }
    private var llWallpaperContainer: LinearLayout = findViewById(R.id.ll_wallpaper_container)
    private var pictureView: HorizontalGridView = findViewById(R.id.picture_view)
    private var colorView: RecyclerView = findViewById(R.id.color_view)
    private var cropView: LinearLayout
    private var chooseTip: TextView = findViewById(R.id.choose_tip)
    private var pictureTab: TextView = findViewById(R.id.picture)
    private var colorTab: TextView = findViewById(R.id.color)
    private var saveBtn: Button = findViewById(R.id.btn_save)
    private var browseBtn: Button = findViewById(R.id.btn_browse)
    private var backBtn: ImageView = findViewById(R.id.iv_back)
    private var cancelBtn: ImageView = findViewById(R.id.iv_cancel)
    private var cropModeFit: ImageView
    private var cropModeFill: ImageView
    private var cropModeStretch: ImageView
    private var mainLayout: LinearLayout = findViewById(R.id.main_layout)
    private var chooseTab: LinearLayout = findViewById(R.id.choose_tab)
    private lateinit var textPaint: TextPaint
    private var browsePicBitmapAndroidR: Bitmap? = null
    var chooseModeState = ChooseMode.DEFAULT_CHOOSE_IMAGE.value
    private var uriFromAms: Uri? = null
    var imageType: EditImageType = EditImageType.IMAGE_TYPE_NO

    init {
        pictureView.adapter = pictureAdapter
        pictureView.layoutManager = GridLayoutManager(rootView.context, 1, GridLayoutManager.VERTICAL, false)
        colorView.adapter = colorAdapter
        colorView.layoutManager = GridLayoutManager(rootView.context, 4, GridLayoutManager.VERTICAL, false)

        cropView = findViewById(R.id.crop_view)
        cropModeFit = findViewById(R.id.fit_pic)
        cropModeFill = findViewById(R.id.fill_pic)
        cropModeStretch = findViewById(R.id.stretch_pic)
        browseBtn.requestFocus()

        llWallpaperContainer.setOnClickListener {
            Log.d(TAG, "Do nothing")
        }

        pictureTab.setOnClickListener {
            showChoosePrePic()
            val path = pictures[0]
            println("pictureTab OnClick, path : $path")
            path?.let {
                for (listener in listeners) {
                    listener.changeWallpaper(ChangeViewDataClass(true, it))
                }
            }
        }

        colorTab.setOnClickListener {
            showChooseColor()
            colorAdapter.unSelectAllColorItems()
        }

        pictureView.setOnFocusChangeListener { view, isFocused ->
            Log.d(TAG, "pictureView isFocused: $isFocused")
//            if (isFocused) {
//                pictureAdapter.setWallpaperFocused()
//            } else {
//                view.nextFocusUpId = R.id.picture
//                view.nextFocusDownId = R.id.btn_save
//            }
        }

        pictureView.setOnKeyListener { view, i, keyEvent ->
            var keyCode = keyEvent.keyCode
            Log.d(TAG, " pictureView keyCode: $keyCode")
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                saveBtn.requestFocus()
            }
            false
        }

        saveBtn.setOnKeyListener { view, i, keyEvent ->
            var keyCode = keyEvent.keyCode
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                view.nextFocusUpId = R.id.ll_image_container
            }
            false
        }

        saveBtn.setOnClickListener { save ->
            with(save) {
                isFocusable = false
                isEnabled = false
                for (listener in listeners) {
                    listener.showLoadingDialog()
                }
                Log.d("MainViewMvc", " Save image start !!! imageType : $imageType" + " Constant.isAndroidR : ${Constant.isAndroidR}")
                if (Constant.isAndroidR) {
                    Log.d(TAG, "saveBtn is Android R")
                    //UniStyle Launchers
                    val returnBitmap = MainActivity.returnBitmap
                    val imagePath = MainActivity.returnBitmapPath
                    when (imageType) {
                        EditImageType.IMAGE_TYPE_COLOR -> {
                            returnBitmap?.let { bm ->
                                for (listener in listeners) {
                                    listener.saveBitmapByPath(bm)
                                }
                            }
                        }
                        EditImageType.IMAGE_TYPE_NO -> {
                            val source = File(imagePath)
                            val target = File(MainActivity.RETURN_URL)
                            for (listener in listeners) {
                                listener.saveBitmapByFile(source, target)
                            }
                        }
                        EditImageType.IMAGE_TYPE_FIT,
                        EditImageType.IMAGE_TYPE_FILL,
                        EditImageType.IMAGE_TYPE_STRETCH -> {
                            for (listener in listeners) {
                                listener.resizeBitmapAndSave(imageType.value)
                            }
                        }
                        else -> {
                            for (listener in listeners) {
                                listener.finishActivity()
                            }
                        }
                    }
                    if (returnBitmap == null && imagePath == null) {
                        for (listener in listeners) {
                            listener.setResultOK()
                        }
                    }
                } else {
                    Log.d(TAG, "saveBtn is not Android R, Old Launcher imageType : $imageType")
                    //Old Launchers
                    val returnBitmap = MainActivity.returnBitmap
                    val returnBitmapPath = MainActivity.returnBitmapPath
                    Log.d("MainViewMvc", "ZZZ_1")

                    when (imageType) {
                        EditImageType.IMAGE_TYPE_COLOR -> {
                            returnBitmap?.let { bm ->
                                var returnBitmapAllocationMB = 0
                                for (listener in listeners) {
                                    returnBitmapAllocationMB = listener.getMemorySize(bm)
                                }
                                Log.d("MainViewMvc", "ZZZ_2 , returnBitmapAllocationMB: $returnBitmapAllocationMB")
                                if (returnBitmapAllocationMB >= TEN_MB_LIMIT) {
                                    for (listener in listeners) {
                                        listener.saveBitmapByFileStreams()
                                    }
                                } else {
                                    Log.d("MainViewMvc", "AAA_returnBitmap else")
                                    for (listener in listeners) {
                                        listener.saveBitmapByPath(bm)
                                    }
                                }
                            }
                        }
                        EditImageType.IMAGE_TYPE_NO -> {
                            //returnBitmap为空，说明选择的是预设图片
                            val source = File(returnBitmapPath)
                            val target = File(MainActivity.RETURN_URL)
                            for (listener in listeners) {
                                listener.saveBitmapByFile(source, target)
                            }
                        }
                        EditImageType.IMAGE_TYPE_FIT,
                        EditImageType.IMAGE_TYPE_FILL,
                        EditImageType.IMAGE_TYPE_STRETCH -> {
                            returnBitmap?.let { bm ->
                                var returnBitmapAllocationMB = 0
                                for (listener in listeners) {
                                    returnBitmapAllocationMB = listener.getMemorySize(bm)
                                }
                                Log.d("MainViewMvc", "ZZZ_2 , returnBitmapAllocationMB: $returnBitmapAllocationMB")
                                if (returnBitmapAllocationMB >= TEN_MB_LIMIT) {
                                    for (listener in listeners) {
                                        listener.saveBitmapByFileStreams()
                                    }
                                } else {
                                    Log.d("MainViewMvc", "AAA_returnBitmap else")
                                    for (listener in listeners) {
                                        listener.saveBitmapByPath(bm)
                                    }
                                }
                            }
                        }
                        else -> {
                            for (listener in listeners) {
                                listener.finishActivity()
                            }
                        }
                    }
                }
                Log.d("MainViewMvc", "Save image done!!!")
            }
        }

        browseBtn.setOnClickListener {
            for (listener in listeners) {
                listener.intentSystemGallery()
            }
        }

        backBtn.setOnClickListener {
            cropModeFill.clearFocus()
            cropModeFit.onFocusChangeListener = null
            cropModeStretch.onFocusChangeListener = null
            for (listener in listeners) {
                listener.intentSystemGallery()
            }
        }


        cancelBtn.setOnClickListener {
            for (listener in listeners) {
                listener.finishActivity()
            }
        }

        cropModeFit.setOnClickListener { v ->
            coroutineScope.launch {
                for (listener in listeners) {
                    listener.showLoadingDialog()
                    listener.setWallpaperFilter(EditImageType.IMAGE_TYPE_FIT.value, "", browsePicBitmapAndroidR)
                }
                imageType = EditImageType.IMAGE_TYPE_FIT
            }
        }
        cropModeFill.setOnClickListener { v ->
            println("cropModeFill start")
            coroutineScope.launch {
                this.printCoroutineScopeInfo()
                for (listener in listeners) {
                    listener.showLoadingDialog()
                    listener.setWallpaperFilter(EditImageType.IMAGE_TYPE_FILL.value, "", browsePicBitmapAndroidR)
                }
                imageType = EditImageType.IMAGE_TYPE_FILL
            }
        }

        cropModeStretch.setOnClickListener { v ->
            coroutineScope.launch {
                for (listener in listeners) {
                    listener.showLoadingDialog()
                    listener.setWallpaperFilter(EditImageType.IMAGE_TYPE_STRETCH.value, "", browsePicBitmapAndroidR)
                }
                imageType = EditImageType.IMAGE_TYPE_STRETCH
            }
        }


        cropModeFit.setOnFocusChangeListener(setOnFocusListener())
        cropModeFill.setOnFocusChangeListener(setOnFocusListener())
        cropModeStretch.setOnFocusChangeListener(setOnFocusListener())
    }

    private fun setOnFocusListener(): View.OnFocusChangeListener? {
        return View.OnFocusChangeListener { view: View, hasFocus: Boolean ->
            if (hasFocus) {
                Log.d(TAG, "AAA_isFocus : $hasFocus")
                val viewId = view.id
                if (viewId == cropModeFit.getId()) {
                    cropModeFit.performClick()
                } else if (viewId == cropModeFill.getId()) {
                    cropModeFill.performClick()
                } else if (viewId == cropModeStretch.getId()) {
                    cropModeStretch.performClick()
                } else {
                    Log.i(TAG, "mTabWidget unknown view hasFocus : $hasFocus")
                }
            }
        }
    }

    fun notifyDataSetChanged() {
        coroutineScope.launch {
            colorAdapter.notifyDataSetChanged()
            pictureAdapter.notifyDataSetChanged()
        }
    }

    private fun setChooseTipPadding(topValue: Int, Bottomvalue: Int) {
        chooseTip.setPadding(0, topValue, 0, Bottomvalue)
    }

    @UiThread
    fun showChoosePrePic() {
        chooseModeState = ChooseMode.DEFAULT_CHOOSE_IMAGE.value
        if (BuildConfig.UNISTYLE) mainLayout.setBackgroundColor(rootView.context.resources.getColor(R.color.uni_sytle_bg)) else mainLayout.setBackgroundColor(rootView.context.resources.getColor(R.color.littleBlack))
        chooseTip.text = rootView.context.resources.getString(R.string.choose_picture)
        pictureView.visibility = View.VISIBLE
        colorView.visibility = View.GONE
        browseBtn.visibility = View.VISIBLE
        cropView.visibility = View.GONE
        backBtn.visibility = View.INVISIBLE
        pictureTab.setBackgroundResource(R.drawable.tab_bg_active)
        colorTab.setTextColor(rootView.context.resources.getColor(R.color.colorWhite))
        colorTab.setBackgroundResource(R.drawable.tab_bg)
        chooseTab.visibility = View.VISIBLE
        textPaint = pictureTab.paint
        textPaint.isFakeBoldText = true
        textPaint = colorTab.paint
        textPaint.isFakeBoldText = false
        setChooseTipPadding(50, 17)
    }

    @UiThread
    private fun showChooseColor() {
        chooseModeState = ChooseMode.CHOOSE_COLOR.value
        if (BuildConfig.UNISTYLE) mainLayout.setBackgroundColor(rootView.context.resources.getColor(R.color.uni_sytle_bg)) else mainLayout.setBackgroundColor(rootView.context.resources.getColor(R.color.littleBlack))
        chooseTip.text = rootView.context.resources.getString(R.string.choose_color)
        pictureView.visibility = View.GONE
        colorView.visibility = View.VISIBLE
        browseBtn.visibility = View.GONE
        cropView.visibility = View.GONE
        backBtn.visibility = View.INVISIBLE
        colorTab.setBackgroundResource(R.drawable.tab_bg_active)
        pictureTab.setTextColor(rootView.context.resources.getColor(R.color.colorWhite))
        pictureTab.setBackgroundResource(R.drawable.tab_bg)
        chooseTab.visibility = View.VISIBLE
        textPaint = pictureTab.paint
        textPaint.isFakeBoldText = false
        textPaint = colorTab.paint
        textPaint.isFakeBoldText = true
        setChooseTipPadding(0, 0)
    }

    @UiThread
    fun enableSaveImageButton() {
        saveBtn.apply {
            isFocusable = true
            isEnabled = true
        }
    }

    @UiThread
    fun showChooseCropMode() {
        chooseModeState = ChooseMode.BROWSE_GALLERY.value
        mainLayout.setBackgroundColor(Color.TRANSPARENT)
        chooseTip.text = rootView.context.resources.getString(R.string.choose_position)
        pictureView.visibility = View.GONE
        colorView.visibility = View.GONE
        browseBtn.visibility = View.GONE
        cropView.visibility = View.VISIBLE
        backBtn.visibility = View.VISIBLE
        chooseTab.visibility = View.GONE
        setChooseTipPadding(50, 17)
    }

    @UiThread
    fun requestFocusFillMode() {
        cropModeFill.requestFocus()
        cropModeFill.isSelected = true
    }

    fun clearBitmap() {
        browsePicBitmapAndroidR = null
    }


    suspend fun getFileProviderAndroidR(uri: Uri?): Bitmap? {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                val file = File(MainActivity.RETURN_URL)
                when {
                    file.exists() -> {
                        Log.d(TAG, "AAA_File is exist")
                        browsePicBitmapAndroidR = BitmapFactory.decodeFile(file.absolutePath)
                        browsePicBitmapAndroidR
                    }
                    else -> {
                        Log.d(TAG, "AAA_File is not exist")
                        val resultBitmap = getBitmap(uri)
                        browsePicBitmapAndroidR = resultBitmap
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
                Log.e("MainViewMvc", "Get uri : $uri fail")
            } else {
                val contentResolver = activity.contentResolver
                if (contentResolver == null) {
                    Log.e("MainViewMvc", "Get contentResolver : $contentResolver fail")
                } else {
                    var parcelFileDescriptor: ParcelFileDescriptor? = null
                    try {
                        parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "rw")
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                    val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                    if (fileDescriptor == null) {
                        Log.e("MainViewMvc", "Get fileDescriptor : $fileDescriptor fail")
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


    companion object {
        val TAG = "MainViewMvc"
    }

}