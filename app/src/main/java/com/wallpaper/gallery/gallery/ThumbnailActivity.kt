package com.wallpaper.gallery.gallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.VectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.wallpaper.gallery.gallery.activities.BaseActivity
import com.wallpaper.gallery.gallery.common.IntentGallery
import com.wallpaper.gallery.gallery.common.MessagesDisplayer
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapBase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapCase
import com.wallpaper.gallery.gallery.saveBitmaps.SaveBitmapFileProviderCase
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.gallery.gallery.utils.Constant
import com.wallpaper.gallery.gallery.utils.Utils
import com.wallpaper.gallery.gallery.viewMvc.ThumbnailViewMvc
import com.wallpaper.gallery.gallery.viewMvc.ViewMvcFactory
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.*
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URL
import java.net.URLDecoder
import javax.inject.Inject

class ThumbnailActivity : BaseActivity(), ThumbnailViewMvc.Listener {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var viewMvc: ThumbnailViewMvc

    @Inject lateinit var saveBitmapCase: SaveBitmapCase
    @Inject lateinit var viewMvcFactory: ViewMvcFactory
    @Inject lateinit var imageIntentWithAMS: IntentGallery
    @Inject lateinit var context: Context
    @Inject lateinit var bitmapUtils: BitmapUtils
    @Inject lateinit var saveBitmapFileProviderCase:SaveBitmapFileProviderCase
    @Inject lateinit var messagesDisplayer: MessagesDisplayer

    private var browsePicPath = ""
    private var mGuest: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        injector.inject(this)
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate")
        viewMvc = viewMvcFactory.newThumbnailViewMvc(null, this)

        nowBitmap = null
        RETURN_URL = intent.getStringExtra(Utils.RESULT_FILE)
//        ORIGINAL_THUMBNAIL_BITMAP = intent.getParcelableExtra(Utils.ORIGINAL_THUMBNAIL_BITMAP)
        ORIGINAL_THUMBNAIL_URL = intent.getStringExtra(Utils.ORIGINAL_THUMBNAIL_URL)
        ORIGINAL_THUMBNAIL_PATH = intent.getStringExtra(Utils.ORIGINAL_THUMBNAIL_PATH)
        mUserType = intent.getIntExtra(USER_TYPE, USER_TYPE_GUEST)
        mUserName = intent.getStringExtra(USER_NAME)

        setContentView(viewMvc.rootView)

        coroutineScope.launch {
            mGuest = viewMvc.getBitmap(R.drawable.ic_thumbnail_guest)
            Log.d(TAG, "onCreate: mGuest= ${mGuest?.byteCount}")
        }

        Log.d(TAG, "onCreate: ORIGINAL_THUMBNAIL_URL= $ORIGINAL_THUMBNAIL_URL")
        Log.d(TAG, "onCreate: mUserType= $mUserType,mUserName= $mUserName")

        mUserShortName = when {
            mUserName.length < 2 -> {
                " "
            }
            !mUserName.contains(" ") -> {
                mUserName.substring(0, 2).toUpperCase()
            }
            else -> {
                (mUserName[0].toString() + "" + mUserName[mUserName.indexOf(" ") + 1]).toUpperCase()
            }
        }

//        if (BuildConfig.ISDEVELOP) ORIGINAL_THUMBNAIL_URL = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + File.separator + "superman.jpg"
        if (ORIGINAL_THUMBNAIL_URL != null) {
            coroutineScope.launch(BackgroundDispatcher) {
                val bm = bitmapFromURL
                println("getBitmapFromURL get bitmap $bm")
                setNowBitmap(bm)
                withContext(Dispatchers.Main) {
                    println("getBitmapFromURL showPreviewPage")
                    viewMvc.showPreviewPage(false)
                }
            }
        } else {
            coroutineScope.launch(BackgroundDispatcher) {
                val file = File(ORIGINAL_THUMBNAIL_PATH)
                val isFileExists = file.exists()
                when {
                    isFileExists -> {
                        if (!file.isDirectory &&
                                (file.absolutePath.endsWith("jpg")
                                        || file.absolutePath.endsWith("jpeg")
                                        || file.absolutePath.endsWith("png"))) {
                            Log.d(TAG, "getBitmapFromURL is work")
                            returnBitmap = BitmapFactory.decodeFile(file.absolutePath)
                            Log.d(TAG, "getBitmapFromURL is work returnBitmap : $returnBitmap")
                            withContext(Dispatchers.Main) {
                                setNowBitmap(returnBitmap)
                                viewMvc.showPreviewPage(false)
                            }
                        } else {
                            throw RuntimeException("Bitmap path is wrong ! $ORIGINAL_THUMBNAIL_PATH")
                        }
                    }
                    else -> {
                        Log.e(TAG, "getBitmapFromURL isFileExists : $isFileExists")
                        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_thumbnail_guest)
                        drawable?.let {
                            returnBitmap =  viewMvc.getVectorDrawable(it as VectorDrawable)
                            saveBitmapToFile(returnBitmap)
                            Log.d(TAG, "getBitmapFromURL is work returnBitmap : $returnBitmap")
                            withContext(Dispatchers.Main) {
                                setNowBitmap(returnBitmap)
                                viewMvc.showPreviewPage(false)
                            }
                        }

        //                    throw RuntimeException("Bitmap is notFound ! $ORIGINAL_THUMBNAIL_PATH")
                    }
                }
            }
        }
        registerReceiver(mAccountReceiver, IntentFilter(ACTION_AMS_LOGIN))
    }

    override fun onStart() {
        super.onStart()
        viewMvc.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        coroutineScope.coroutineContext.cancelChildren()
        viewMvc.unregisterListener(this)
    }


    override fun onDestroy() {
        viewMvc.unregisterListener(this)
        unregisterReceiver(mAccountReceiver)
        super.onDestroy()
    }

    private val mAccountReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "Receive change account")
            finish() // close window when change account
        }
    }

    private val bitmapFromURL: Bitmap?
        get() {
            try {
                val url = URL(ORIGINAL_THUMBNAIL_URL)
                val connection = url.openConnection()
                connection.connect()
                val input = connection.getInputStream()
                returnBitmap = BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                Log.e(TAG, "getBitmapFromURL Exception:$e")
                e.printStackTrace()
                returnBitmap = when (mUserType) {
                    USER_TYPE_LOCAL_PUBLIC -> mGuest
                    USER_TYPE_GUEST -> mGuest
                    USER_TYPE_USER -> bitmapUtils.drawDefaultImg(Color.GRAY, Color.GRAY, Color.WHITE, mUserShortName, BIG_HEAD_SIZE, BIG_HEAD_SIZE)
                    else -> mGuest
                }
            }
            return returnBitmap
        }

    @UiThread
    private fun showToast() {
        coroutineScope.launch {
            messagesDisplayer.showUseCaseError()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && data != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == PICK_FROM_GALLERY) {
                    val uri = data.data
                    if (Constant.isAndroidR) {
                        saveBitmap(uri)
                    } else {
                        coroutineScope.launch(BackgroundDispatcher + NonCancellable) {
                            val file = File(uri.path)
                            if (file.exists() && file.length() > 0) {
                                try {
                                    browsePicPath = URLDecoder.decode(uri.toString().substring(uri.toString().indexOf("/")), "UTF-8")
                                    launch(Dispatchers.Main.immediate) {
                                        viewMvc.showCrop(browsePicPath)
                                    }
                                } catch (e: UnsupportedEncodingException) {
                                    e.printStackTrace()
                                    showToast()
                                }
                            } else {
                                showToast()
                            }
                        }
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                viewMvc.showPreviewPage(false)
            }
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
            if (nowPage == CROP_PAGE) {
                IntentGallery()
            } else if (nowPage == PREVIEW_PAGE2) {
                viewMvc.showCrop(browsePicPath)
                return false
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        private const val TAG = "ThumbnailActivity"
        private const val PICK_FROM_GALLERY = 1

        const val BIG_HEAD_SIZE = 400
        var RETURN_URL = Environment.getExternalStorageDirectory().absolutePath + "/thumbnail.jpg"
        private var ORIGINAL_THUMBNAIL_URL: String? = null

        //        private var ORIGINAL_THUMBNAIL_BITMAP: Bitmap? = null
        private var ORIGINAL_THUMBNAIL_PATH: String? = null

        //    private static final String RETURN_URL = "/mnt/sdcard/benq/result/thumbnail.jpg";
        //临时用来预览的Bitmap
        private var nowBitmap: Bitmap? = null

        //返回的Bitmap
        private var returnBitmap: Bitmap? = null

        //旋转的次数
        var rotateNum = 0

        //记录当前在哪个页面
        private var nowPage = 0
        private const val PREVIEW_PAGE = 1
        private const val PREVIEW_PAGE2 = 2
        private const val CROP_PAGE = 3

        private const val AMS_SETRESULT_VALUE = 1

        //用户类型和用户名
        private const val USER_TYPE = "userType"
        private const val USER_TYPE_LOCAL_PUBLIC = 0
        private const val USER_TYPE_GUEST = 1
        private const val USER_TYPE_USER = 2
        private const val USER_NAME = "userName"
        private var mUserType = 0
        private var mUserName = ""
        var mUserShortName = ""
        private const val ACTION_AMS_LOGIN = "com.benq.action.ams.AMS_LOGIN"
    }

    override fun getNowPageSate(): ThumbnailViewMvc.NowPage {
        return when (nowPage) {
            PREVIEW_PAGE -> {
                ThumbnailViewMvc.NowPage.PREVIEW_PAGE
            }
            PREVIEW_PAGE2 -> {
                ThumbnailViewMvc.NowPage.PREVIEW_PAGE2
            }
            CROP_PAGE -> {
                ThumbnailViewMvc.NowPage.CROP_PAGE
            }
            else -> {
                ThumbnailViewMvc.NowPage.PREVIEW_PAGE
            }
        }
    }

    override fun setNowPageState(page: ThumbnailViewMvc.NowPage) {
        nowPage = when (page) {
            ThumbnailViewMvc.NowPage.PREVIEW_PAGE -> {
                PREVIEW_PAGE
            }
            ThumbnailViewMvc.NowPage.PREVIEW_PAGE2 -> {
                PREVIEW_PAGE2
            }
            ThumbnailViewMvc.NowPage.CROP_PAGE -> {
                CROP_PAGE
            }
        }
    }

    override fun getUserType(): ThumbnailViewMvc.UserType {
        return when (mUserType) {
            USER_TYPE_LOCAL_PUBLIC -> ThumbnailViewMvc.UserType.USER_TYPE_LOCAL_PUBLIC
            USER_TYPE_GUEST -> ThumbnailViewMvc.UserType.USER_TYPE_GUEST
            USER_TYPE_USER -> ThumbnailViewMvc.UserType.USER_TYPE_USER
            else -> ThumbnailViewMvc.UserType.USER_TYPE_LOCAL_PUBLIC
        }
    }

    override suspend fun saveBitmapToFile(bm: Bitmap?) {
        withContext(BackgroundDispatcher) {
            saveBitmapCase.saveBitmap(bm, RETURN_URL)
        }
    }

    override fun intentAms() {
        Log.d(TAG, "intentAms")
        startActivityForResult(imageIntentWithAMS.getIntent(), AMS_SETRESULT_VALUE)
    }

    override fun getNowBitmap(): Bitmap? {
        return nowBitmap
    }

    override fun setNowBitmap(bm: Bitmap?) {
        nowBitmap = bm
    }

    override fun getBrowsePath(): String {
        return browsePicPath
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
                            launch(Dispatchers.Main.immediate) {
                                viewMvc.getFileProviderAndroidR(uri)
                                viewMvc.showCrop()
                            }
                        }
                        is SaveBitmapBase.Result.Fail -> {
                            //may be show a toast
                            Log.e(TAG, "Save image fail")
                        }
                        is SaveBitmapBase.Result.Error_ImageSizeToBigNotSupport -> {
                            Log.e(TAG, "Save image Error_ImageSizeToBigNotSupport")
                        }
                    }
                }
            }
        }
    }

}