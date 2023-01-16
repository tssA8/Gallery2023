package com.wallpaper.gallery.gallery.viewmodels

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.wallpaper.gallery.gallery.ChangeViewDataClass
import com.wallpaper.gallery.gallery.MainActivity
import com.wallpaper.gallery.gallery.SaveBitmapColorDataClass
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.common.TestUtils.printCoroutineScopeInfo
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapBase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapCase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapFileProviderCase
import com.wallpaper.gallery.gallery.utils.Constant
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MyViewModel @Inject constructor(
        private val saveBitmapCase: SaveBitmapCase,
        private val saveBitmapFileProviderCase: SaveBitmapFileProviderCase,
        private val context: Context
) : SavedStateViewModel() {

    private lateinit var _resizeBitmapAndSave: MutableLiveData<SaveBitmapBase.Result>
    val resizeBitmapAndSave: LiveData<SaveBitmapBase.Result> get() = _resizeBitmapAndSave

    private lateinit var _saveBitmapByFileStreams: MutableLiveData<SaveBitmapBase.Result>
    val saveBitmapByFileStreams: LiveData<SaveBitmapBase.Result> get() = _saveBitmapByFileStreams

    private lateinit var _saveBitmapByPath: MutableLiveData<SaveBitmapBase.Result>
    val saveBitmapByPath: LiveData<SaveBitmapBase.Result> get() = _saveBitmapByPath

    private lateinit var _saveBitmapByFile: MutableLiveData<SaveBitmapBase.Result>
    val saveBitmapByFile: LiveData<SaveBitmapBase.Result> get() = _saveBitmapByFile

    private lateinit var _setWallpaperFilter: MutableLiveData<SetWallpaperResult>
    val setWallpaperFilter: LiveData<SetWallpaperResult> get() = _setWallpaperFilter

    override fun init(savedStateHandle: SavedStateHandle) {
        _resizeBitmapAndSave = savedStateHandle.getLiveData("resizeBitmapAndSave")
        _saveBitmapByFileStreams = savedStateHandle.getLiveData("saveBitmapByFileStreams")
        _saveBitmapByPath = savedStateHandle.getLiveData("saveBitmapByPath")
        _saveBitmapByFile = savedStateHandle.getLiveData("saveBitmapByFile")
        _setWallpaperFilter = savedStateHandle.getLiveData("setWallpaperFilter")
    }


    fun resizeBitmapAndSave(cropType: Int) {
        viewModelScope.launch(BackgroundDispatcher) {
            val file = File(MainActivity.RETURN_URL)
            if (file.exists()) {
                Log.d("resizeBitmapAndSave", "file is exist")
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                MainActivity.browsePicPath = file.absolutePath
//                Log.d("resizeBitmapAndSave", "file is exist , browsePicPath : ${MainActivity.browsePicPath} cropType : $cropType")
                withContext(Dispatchers.Main) {
                    _resizeBitmapAndSave.value = saveBitmapCase.scaleAndSaveWallpaper(cropType, context, bitmap)
                }
            }
        }
    }


    fun saveBitmapByFileStreams() {
        viewModelScope.launch {
            viewModelScope.printCoroutineScopeInfo()
            _saveBitmapByFileStreams.value = saveBitmapCase.saveBitmapByFileStreams(MainActivity.browsePicPath)
        }
    }


    fun saveBitmapByPath(bitmap: Bitmap) {
        viewModelScope.launch {
            viewModelScope.printCoroutineScopeInfo()
            _saveBitmapByPath.value = saveBitmapCase.saveBitmapByPath(MainActivity.RETURN_URL, bitmap)
        }
    }

    fun saveBitmapByFile(source: File, dest: File) {
        viewModelScope.launch {
            _saveBitmapByFile.value = saveBitmapCase.saveBitmapByFile(source, dest, context)
        }
    }

    suspend fun setWallpaperFilter(cropType: Int, preloadPicPath: String?, bitmap: Bitmap?) {
        Log.d(TAG, "AAA_setWallpaperFilter, isAndroidR: " + Constant.isAndroidR + " cropType : " + cropType)
        withContext(BackgroundDispatcher) {
            this.printCoroutineScopeInfo()
            if (Constant.isAndroidR) {
                val result = saveBitmapFileProviderCase.setWallpaperFilter(cropType, context, bitmap, preloadPicPath)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is SaveBitmapBase.Result.Success -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Success(result.bitmap, result.path, cropType)
                        }
                        is SaveBitmapBase.Result.Fail -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Fail(cropType)
                        }
                        is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Error_ImageSizeToBigNotSupport(cropType)
                        }
                    }
                }
            } else {
                Log.d(TAG, "AAA_setWallpaperFilter, OLD Launcher" + " cropType : " + cropType + " browsePicPath : " + MainActivity.browsePicPath)
                MainActivity.browsePicPath?.let {
                    withContext(Dispatchers.Main) {
                        if (MainActivity.isGetWallpaperFromAms) {
                            if (cropType != EditImageType.IMAGE_TYPE_FILL.value) _setWallpaperFilter.value = SetWallpaperResult.Fail(cropType)
                        }
                        val result = saveBitmapCase.setWallpaperFilter(cropType, "", context, it, bitmap, preloadPicPath)
                        this.printCoroutineScopeInfo()
                        when (result) {
                            is SaveBitmapBase.Result.Success -> {
                                _setWallpaperFilter.value = SetWallpaperResult.Success(result.bitmap, result.path, cropType)
                            }
                            is SaveBitmapBase.Result.Fail -> {
                                _setWallpaperFilter.value = SetWallpaperResult.Fail(cropType)
                            }
                            is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                                _setWallpaperFilter.value = SetWallpaperResult.Error_ImageSizeToBigNotSupport(cropType)
                            }
                        }
                        println("coroutine done")
                    } ?: run {
                        _setWallpaperFilter.value = SetWallpaperResult.Fail(cropType)
                    }
                }
            }
        }
    }

    fun setColorWallpaper(saveBitmapColorDataClass: SaveBitmapColorDataClass) {
        viewModelScope.launch {
            println("${TAG} saveColorWallpaper , start")
            saveBitmapColorDataClass.let { data ->
                val context = data.context
                println("${TAG} saveColorWallpaper saveBitmapByPath ")
                val bm = getNowBitmap(context)
                bm.let {
                    val path = data.path
                    val type = data.cropType
                    val result = saveBitmapCase.setWallpaperFilter(type, path, context, "", it, "")
                    when (result) {
                        is SaveBitmapBase.Result.Success -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Success(result.bitmap, result.path, EditImageType.IMAGE_TYPE_COLOR.value)
                        }
                        is SaveBitmapBase.Result.Fail -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Fail(EditImageType.IMAGE_TYPE_COLOR.value)
                        }
                        is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                            _setWallpaperFilter.value = SetWallpaperResult.Error_ImageSizeToBigNotSupport(EditImageType.IMAGE_TYPE_COLOR.value)
                        }
                    }
                }
            }
        }
    }

    suspend fun getNowBitmap(context: Context): Bitmap {
        return withContext(BackgroundDispatcher) {
            val wallpaperManager = WallpaperManager.getInstance(context) // 获取当前壁纸
            val wallpaperDrawable = wallpaperManager.drawable // 将Drawable,转成Bitmap
            (wallpaperDrawable as BitmapDrawable).bitmap
        }
    }


    fun changeWallpaper(changeWallpaper: ChangeViewDataClass) {
        viewModelScope.launch {
            val state = changeWallpaper.bindViewDone
            val path = changeWallpaper.path
            println("${TAG} mainBindView state : $state+ path : $path")
            setWallpaperFilter(EditImageType.IMAGE_TYPE_NO.value, path, null)
        }
    }


    sealed class SetWallpaperResult: Parcelable {
        @Parcelize class Success(val bitmap: Bitmap?, val path: String?, val cropType: Int) : SetWallpaperResult()
        @Parcelize class Fail(val cropType: Int) : SetWallpaperResult()
        @Parcelize class Error_ImageSizeToBigNotSupport(val cropType: Int) : SetWallpaperResult()
    }

    companion object {
        val TAG = "MyViewModel"
    }

}