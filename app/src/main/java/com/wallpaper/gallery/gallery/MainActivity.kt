package com.wallpaper.gallery.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.wallpaper.gallery.gallery.activities.BaseActivity
import com.wallpaper.gallery.gallery.common.ChooseMode
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.common.IntentGallery
import com.wallpaper.gallery.gallery.common.MessagesDisplayer
import com.wallpaper.gallery.gallery.common.TestUtils.printCoroutineScopeInfo
import com.wallpaper.gallery.gallery.common.permossions.PermissionsHelper
import com.wallpaper.gallery.gallery.dialogs.DialogsNavigators
import com.wallpaper.gallery.gallery.dialogs.promptdialog.PromptViewMvcImpl
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapBase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapCase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapCase.SizeType
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapFileProviderCase
import com.wallpaper.gallery.gallery.service.MyService
import com.wallpaper.gallery.gallery.utils.Constant
import com.wallpaper.gallery.gallery.viewMvc.MainViewMvc
import com.wallpaper.gallery.gallery.viewMvc.ViewMvcFactory
import com.wallpaper.gallery.gallery.viewmodels.MyViewModel
import com.wallpaper.gallery.gallery.viewmodels.ViewModelFactory
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.*
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import javax.inject.Inject
import kotlin.collections.ArrayList


class MainActivity : BaseActivity(), MainViewMvc.Listener, PermissionsHelper.Listener {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Inject lateinit var dialogsNavigators: DialogsNavigators
    @Inject lateinit var saveBitmapCase: SaveBitmapCase
    @Inject lateinit var saveBitmapFileProviderCase: SaveBitmapFileProviderCase
    @Inject lateinit var viewMvcFactory: ViewMvcFactory
    @Inject lateinit var intentGallery: IntentGallery
    @Inject lateinit var context: Context
    @Inject lateinit var messagesDisplayer: MessagesDisplayer
    @Inject lateinit var myViewModelFactory: ViewModelFactory
    @Inject lateinit var permissionsHelper: PermissionsHelper

    private lateinit var viewMvc: MainViewMvc
    private lateinit var myViewModel: MyViewModel
    private lateinit var promptViewMvc: PromptViewMvcImpl

    //自选图片地址
    private val FAKE_CONTENT_URI = "content://com.benq.ifp.ams.provider/root/storage/B5E9-0410/FHD%20Photo/11.jpg"
    private val DEBUG_IMAGE_PATH_BIG_IMAGE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + "cyberpunk2.jpg"
    private val DEBUG_IMAGE_PATH_SMALL_IMAGE = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + "apple_silicon.jpg"
    private val pictureList: MutableList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "AAA onCreate")
        //addView
        viewMvc = viewMvcFactory.newMainViewMvc(
                null,
                getPrePictures(directory),
                this)

        promptViewMvc = viewMvcFactory.PromptViewMvc(
                null,
                this)
//        RETURN_URL = intent.getStringExtra(Utils.RESULT_FILE)
        Log.d(TAG, "!!! onCreate: RETURN_URL= $RETURN_URL")
        //setView
        setContentView(viewMvc.rootView)
        //set default wallpaper
        viewMvc.showChoosePrePic()
        registerReceiver(mAccountReceiver, IntentFilter(Constant.ACTION_AMS_LOGIN))
        if (BuildConfig.ISDEVELOP) MyService.start(this)

        val permissions = ArrayList<String>()
        permissions.apply {
            this.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            this.add(Manifest.permission.INTERNET)
            this.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            this.add(Manifest.permission.SET_WALLPAPER)
            this.add(Manifest.permission.SET_WALLPAPER_HINTS)
        }
        if (permissionsHelper.hasPermissions(permissions)) {
            dialogsNavigators.showPermissionGrantedDialog(this, null)
        } else {
            permissionsHelper.requestPermissions(permissions, REQUEST_CODE)
        }

        coroutineScope.launch {
            Log.d(TAG, "!!! save default bitmap start")
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.cyberpunk2)
            saveBitmapCase.saveBitmap(bitmap, DEBUG_IMAGE_PATH_BIG_IMAGE)
            Log.d(TAG, "!!! save default bitmap end")
        }
        myViewModel = ViewModelProvider(this, myViewModelFactory).get(MyViewModel::class.java)
        myViewModel.resizeBitmapAndSave.observe(this, androidx.lifecycle.Observer { result ->
            closeLoadingDialog()
            viewMvc.enableSaveImageButton()
            saveUseCase(result, "resizeBitmapAndSave")
        })
        myViewModel.saveBitmapByFileStreams.observe(this, androidx.lifecycle.Observer { result ->
            closeLoadingDialog()
            viewMvc.enableSaveImageButton()
            saveUseCase(result, "saveBitmapByFileStreams")
        })
        myViewModel.saveBitmapByPath.observe(this, androidx.lifecycle.Observer { result ->
            println("saveBitmapByPath coroutine done")
            closeLoadingDialog()
            viewMvc.enableSaveImageButton()
            saveUseCase(result, "saveBitmapByPath")
        })
        myViewModel.saveBitmapByFile.observe(this, androidx.lifecycle.Observer { result ->
            closeLoadingDialog()
            viewMvc.enableSaveImageButton()
            saveUseCase(result, "saveBitmapByFile")
        })
        myViewModel.setWallpaperFilter.observe(this, androidx.lifecycle.Observer { result ->
            when (result) {
                is MyViewModel.SetWallpaperResult.Success -> {
                    Log.d(TAG, "MyViewModel.SetWallpaperResult.Success")
                    closeLoadingDialog()
                    val cropType = result.cropType
                    if (Constant.isAndroidR) {
                        setWallpaperResult(cropType, SaveBitmapBase.Result.Success(result.bitmap, result.path))
                    } else {
                        Log.d(TAG, "AAA_setWallpaperFilter, OLD Launcher" + " cropType : " + cropType + " browsePicPath : " + browsePicPath)
                        browsePicPath?.let {
                            setWallpaperResult(cropType, SaveBitmapBase.Result.Success(result.bitmap, result.path))
                        }
                    }
                }
                is MyViewModel.SetWallpaperResult.Fail -> {
                    Log.d(TAG, "MyViewModel.SetWallpaperResult.Fail")
                    closeLoadingDialog()
                    val cropType = result.cropType
                    setWallpaperResult(cropType, SaveBitmapBase.Result.Fail)
                }
                is MyViewModel.SetWallpaperResult.Error_ImageSizeToBigNotSupport -> {
                    Log.d(TAG, "MyViewModel.SetWallpaperResult.Error_ImageSizeToBigNotSupport")
                    closeLoadingDialog()
                    val cropType = result.cropType
                    setWallpaperResult(cropType, SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport)
                }
            }
            isGetWallpaperFromAms = false
        })
    }

    private fun saveUseCase(result: SaveBitmapBase.Result, functionName: String) {
        Log.d(TAG, "saveUseCase : $functionName")
        isGetWallpaperFromAms = false
        when (result) {
            is SaveBitmapBase.Result.Success -> {
                println("saveBitmapByPath coroutine done, saveUseCase")
                setResult(RESULT_OK)
                isSavedDestroy = true
                closeLoadingDialog()
                println("saveBitmapByPath coroutine done, saveUseCase finish")
                finish()
            }
            is SaveBitmapBase.Result.Fail -> {
                setResult(RESULT_CANCELED)
                closeLoadingDialog()
                finish()
            }
            is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                Log.e(TAG, "Save image Error_ImageSizeToBigNotSupport")
            }
        }
    }

    private val mAccountReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Receive change account")
            finish() // close window when change account
        }
    }

    override fun onStart() {
        super.onStart()
        viewMvc.registerListener(this)
        permissionsHelper.registerListener(this)
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
        viewMvc.unregisterListener(this)
        permissionsHelper.unregisterListener(this)
        Log.d(TAG, "onStop")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        getPrePictures(directory)
        viewMvc.notifyDataSetChanged()
    }

    override fun onPause() {
        closeLoadingDialog()
        super.onPause()
    }

    override fun onDestroy() {
        closeLoadingDialog()
        viewMvc.enableSaveImageButton()
        Log.d(TAG, "AAA onDestroy")
        if (isChange && !isSavedDestroy) {
            coroutineScope.launch {
                setWallpaperFilter(EditImageType.IMAGE_TYPE_CANCEL_AND_LEAVE.value, "", null)
            }
            Log.e(TAG, "cropAndSaveWallpaper_ 0 cropType")
            if (IS_SHOW_TOAST) messagesDisplayer.showCropModeError()
            setResult(RESULT_CANCELED)
        }
        unregisterReceiver(mAccountReceiver)
        returnBitmap = null
        viewMvc.clearBitmap()
        Glide.get(this).clearMemory()
        super.onDestroy()
    }


    private fun getPrePictures(path: String): MutableList<String> {
        var file = File(path)
        pictureList.clear()
//        if (file.isDirectory && file.listFiles().isNotEmpty()) {
//            for (picFile in file.listFiles()) {
//                if (!picFile.isDirectory &&
//                        (picFile.absolutePath.endsWith("jpg")
//                                || picFile.absolutePath.endsWith("jpeg")
//                                || picFile.absolutePath.endsWith("png"))) {
//                    pictureList.add(picFile.absolutePath)
//                }
//            }
//        } else {
//            if (BuildConfig.DEBUG) {
//                file = File(DEBUG_IMAGE_PATH_BIG_IMAGE)
//                if (file.exists()) pictureList.add(DEBUG_IMAGE_PATH_BIG_IMAGE)
//            } else {
//                file = File(DEFAULT_WALLPAPER_PATH)
//                if (file.exists()) pictureList.add(DEFAULT_WALLPAPER_PATH)
//            }
//        }

        if (BuildConfig.DEBUG) {
            file = File(DEBUG_IMAGE_PATH_BIG_IMAGE)
            if (file.exists()) pictureList.add(DEBUG_IMAGE_PATH_BIG_IMAGE)
        } else {
            file = File(DEFAULT_WALLPAPER_PATH)
            if (file.exists()) {
                Log.d(TAG, "AAA_isExist DEFAULT_WALLPAPER_PATH")
                pictureList.add(DEFAULT_WALLPAPER_PATH)
            } else {
                Log.d(TAG, "AAA_is not Exist")
                if (file.isDirectory && file.listFiles().isNotEmpty()) {
                    for (picFile in file.listFiles()) {
                        if (!picFile.isDirectory &&
                                (picFile.absolutePath.endsWith("jpg")
                                        || picFile.absolutePath.endsWith("jpeg")
                                        || picFile.absolutePath.endsWith("png"))) {
                            pictureList.add(picFile.absolutePath)
                        }
                    }
                }
            }
        }
        return pictureList
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (viewMvc.chooseModeState == ChooseMode.BROWSE_GALLERY.value) {
                try {
                    intentSystemGallery()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "AAA requestCode= $requestCode, resultCode= $resultCode")
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_FROM_GALLERY) {
                isGetWallpaperFromAms = true
                viewMvc.showChooseCropMode()
                val uriString = data?.data.toString() + ""
                data?.let {
                    try {
                        Log.d(TAG, "AAA uriString= $uriString")
                        if (!TextUtils.isEmpty(uriString)) requestFocus(it, uriString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        isGetWallpaperFromAms = false
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            coroutineScope.launch {
                viewMvc.imageType = EditImageType.IMAGE_TYPE_NO
                viewMvc.showChoosePrePic()
            }
        } else {
            finish()
        }
    }

    private fun requestFocus(data: Intent, uriString: String) {
        Log.d(TAG, "AAA_isAndroidR, Constant.isAndroidR : " + Constant.isAndroidR)
        try {
            if (Constant.isAndroidR) {
                Log.d(TAG, "AAA_isAndroidR, SaveBitmapNow")
                val uri = data.data
                saveBitmap(uri)//save bitmap first
            } else {
                coroutineScope.launch {
                    Log.d(TAG, "AAA_is Old Launcher")
                    withContext(BackgroundDispatcher) {
                        browsePicPath = URLDecoder.decode(uriString.substring(uriString.indexOf("/")), "UTF-8")
                        Log.d(TAG, "AAA_is Old Launcher , browsePicPath : " + browsePicPath)
                    }
                    viewMvc.requestFocusFillMode()
                    Log.d(TAG, "AAA_PICK_FROM_GALLERY clearBitmap , returnBitmap : $returnBitmap")
                    returnBitmap = null
                }
            }
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            if (IS_SHOW_TOAST) messagesDisplayer.showUseCaseError()

        }
    }

    private fun saveBitmap(uri: Uri?) {
        coroutineScope.launch(BackgroundDispatcher) {
            if (uri == null) Log.e(TAG, "uri is null!")
            else {
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.let { ip ->
                    when (saveBitmapFileProviderCase.saveBitmap(ip, RETURN_URL)) {
                        is SaveBitmapBase.Result.Success -> {
                            Log.d(TAG, "Save image success")
//                            setResult(RESULT_OK)
                            launch(Dispatchers.Main) {
                                viewMvc.getFileProviderAndroidR(uri)
                                viewMvc.requestFocusFillMode()
                            }
                            isSavedDestroy = true
                        }
                        is SaveBitmapBase.Result.Fail -> {
                            //may be show a toast
                            Log.e(TAG, "Save image fail")
//                            setResult(RESULT_CANCELED)
//                            launch(Dispatchers.Main) { closeLoadingDialog() }
                        }
                        is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                            Log.e(TAG, "Save image Error_ImageSizeToBigNotSupport")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PICK_FROM_GALLERY = 1
        private const val IS_SHOW_TOAST = false
        const val REQUEST_CODE = 1001
        const val WALLPAPER_HEIGHT = 1080

        //当前在哪个界面
        var browsePicPath = ""

        //预设图片地址
        @SuppressLint("SdCardPath")
        private val directory = "/system/media/benq/wallpaper_preload/"
        private const val DEFAULT_WALLPAPER_PATH = "/system/media/default_wallpaper.jpg"
        private const val AMS_SETRESULT_VALUE = 1

        //返回的url，由于太大的图片传不过去，决定保存一个临时的图片
//      /storage/emulated/0/user/admin/crop_thumb.jpg
        var RETURN_URL = Environment.getExternalStorageDirectory().absolutePath + "/wallpaper.jpg"

        //返回的Bitmap
        var returnBitmap: Bitmap? = null
            get() = field
            set(value) {
                if (value != null) {
                    Log.d(TAG, "AAA_Bitmap w: " + value?.width + " h : " + value?.height)
                }
                field = value
            }
        var returnBitmapPath: String? = null

        var isChange = false

        private var isSavedDestroy = false
        var isGetWallpaperFromAms = false
    }

    override fun updatePictureListData() {
        getPrePictures(directory)
    }

    override fun intentSystemGallery() {
//        startActivity(intentGallery.galleryIntent())
        startActivityForResult(intentGallery.galleryIntent(), 1)
    }

    override fun showLoadingDialog() {
        dialogsNavigators.showLoadingDialog()
    }

    override fun closeLoadingDialog() {
        dialogsNavigators.closeIt()
    }

    override fun resizeBitmapAndSave(cropType: Int) {
        myViewModel.resizeBitmapAndSave(cropType)
    }

    override fun saveBitmapByFileStreams() {
        myViewModel.saveBitmapByFileStreams()
    }

    override fun saveBitmapByPath(bitmap: Bitmap) {
        myViewModel.saveBitmapByPath(bitmap)
    }

    override fun saveBitmapByFile(source: File, dest: File) {
        myViewModel.saveBitmapByFile(source, dest)
    }

    override fun getMemorySize(bitmap: Bitmap): Int {
        return saveBitmapCase.getMemorySize(bitmap, SizeType.MB)
    }

    override fun setResultOK() {
        coroutineScope.launch {
            viewMvc.enableSaveImageButton()
            setResult(RESULT_OK)
            isSavedDestroy = true
            closeLoadingDialog()
            finish()
        }
    }

    override fun finishActivity() {
        coroutineScope.launch {
            closeLoadingDialog()
            finish()
        }
    }

    override suspend fun setWallpaperFilter(cropType: Int, preloadPicPath: String?, bitmap: Bitmap?) {
        withContext(BackgroundDispatcher) {
            if (isGetWallpaperFromAms) {
                if (cropType != EditImageType.IMAGE_TYPE_FILL.value) Log.e(TAG, "ignore now, cropType: $cropType")
                else {
                    this.printCoroutineScopeInfo()
                    myViewModel.setWallpaperFilter(cropType, preloadPicPath, bitmap)
                }
            } else {
                this.printCoroutineScopeInfo()
                myViewModel.setWallpaperFilter(cropType, preloadPicPath, bitmap)
            }
        }
    }

    override fun setColorWallpaper(saveBitmapColorDataClass: SaveBitmapColorDataClass) {
        showLoadingDialog()
        myViewModel.setColorWallpaper(saveBitmapColorDataClass)
    }


    override fun changeWallpaper(changeWallpaper: ChangeViewDataClass) {
        myViewModel.changeWallpaper(changeWallpaper)
    }

    private fun setWallpaperResult(cropType: Int, result: SaveBitmapBase.Result) {
        coroutineScope.launch { closeLoadingDialog() }
        when (cropType) {
            EditImageType.IMAGE_TYPE_NO.value -> {
                when (result) {
                    is SaveBitmapBase.Result.Success -> {
                        Log.d(TAG, "IMAGE_TYPE_NO Success")
                        val bitmapPaths = result.path
                        returnBitmapPath = bitmapPaths
                        returnBitmap = null
                        Constant.isOverrideBitmap = true
                        isChange = true
                    }
                    is SaveBitmapBase.Result.Fail -> {
                        Log.d(TAG, "IMAGE_TYPE_NO Fail")
                    }
                    is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                        coroutineScope.launch {
                            if (Constant.isAndroidR) messagesDisplayer.showImageIsNotSupport()
                            else Log.d(TAG, "Ignore first time")
                            finishActivity()
                        }
                    }
                }
            }
            EditImageType.IMAGE_TYPE_FIT.value -> {
                when (result) {
                    is SaveBitmapBase.Result.Success -> {
                        val bitmap = result.bitmap
                        Log.d(TAG, "IMAGE_TYPE_FIT Success , $bitmap")
                        returnBitmap = bitmap
                    }
                    is SaveBitmapBase.Result.Fail -> {
                        Log.d(TAG, "IMAGE_TYPE_FIT Fail")
                    }
                    is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                        coroutineScope.launch {
                            messagesDisplayer.showImageIsNotSupport()
                            finishActivity()
                        }
                    }
                }
            }
            EditImageType.IMAGE_TYPE_FILL.value -> {
                when (result) {
                    is SaveBitmapBase.Result.Success -> {
                        val bitmap = result.bitmap
                        Log.d(TAG, "IMAGE_TYPE_FILL Success, bitmap : $bitmap")
                        returnBitmap = bitmap
                    }
                    is SaveBitmapBase.Result.Fail -> {
                        Log.d(TAG, "IMAGE_TYPE_FILL Fail")
                    }
                    is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                        coroutineScope.launch {
                            messagesDisplayer.showImageIsNotSupport()
                            finishActivity()
                        }
                    }
                }
            }
            EditImageType.IMAGE_TYPE_STRETCH.value -> {
                when (result) {
                    is SaveBitmapBase.Result.Success -> {
                        Log.d(TAG, "IMAGE_TYPE_STRETCH Success")
                        val bitmap = result.bitmap
                        returnBitmap = bitmap
                    }
                    is SaveBitmapBase.Result.Fail -> {
                        Log.d(TAG, "IMAGE_TYPE_STRETCH Fail")
                    }
                    is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                        coroutineScope.launch {
                            messagesDisplayer.showImageIsNotSupport()
                            finishActivity()
                        }
                    }
                }
            }
            EditImageType.IMAGE_TYPE_COLOR.value -> {
                when (result) {
                    is SaveBitmapBase.Result.Success -> {
                        Log.d(TAG, "IMAGE_TYPE_COLOR Success")
                        val bitmapPaths = result.path
                        val bitmap = result.bitmap
                        returnBitmap = bitmap
                        returnBitmapPath = bitmapPaths
                        isChange = true
                    }
                    is SaveBitmapBase.Result.Fail -> {
                        Log.d(TAG, "IMAGE_TYPE_COLOR Fail")
                    }
                    is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                        coroutineScope.launch {
                            messagesDisplayer.showImageIsNotSupport()
                            finishActivity()
                        }
                    }
                }
            }
            EditImageType.IMAGE_TYPE_CANCEL_AND_LEAVE.value -> {
                isChange = true
                Constant.isOverrideBitmap = true
            }
            else -> {
                Log.d(TAG, "IMAGE_TYPE_UnKnow")
            }
        }
    }

    override fun onPermissionGranted(permission: String?, requestCode: Int) {
        if (requestCode == REQUEST_CODE) {
            dialogsNavigators.showPermissionGrantedDialog(this, null)
        }
    }

    override fun onPermissionDeclined(permission: String?, requestCode: Int) {
        if (requestCode == REQUEST_CODE) {
            dialogsNavigators.showDeclinedDialog(this, null)
        }
    }

    override fun onPermissionDeclinedDontAskAgain(permission: String?, requestCode: Int) {
        if (requestCode == REQUEST_CODE) {
            dialogsNavigators.showPermissionDeclinedCantAskMoreDialog(this, null)
        }
    }

}