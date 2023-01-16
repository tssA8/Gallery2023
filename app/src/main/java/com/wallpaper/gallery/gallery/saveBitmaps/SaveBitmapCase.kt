package com.wallpaper.gallery.gallery.saveBitmaps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.wallpaper.gallery.gallery.BuildConfig
import com.wallpaper.gallery.gallery.MainActivity
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.common.TestUtils.printCoroutineScopeInfo
import com.wallpaper.gallery.gallery.utils.*
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.*
import java.io.*

class SaveBitmapCase(private val saveBitmapBase: SaveBitmapBase, bitmapUtils: BitmapUtils) : SaveBitmapBase(bitmapUtils) {
    private val BUILD_SELECTOR = BuildConfig.VAR
    private val WALLPAPER_WIDTH = 1920
    private val WALLPAPER_HEIGHT = 1080
    private val FULL_HD_STRING = "FULL_HD"

    suspend fun saveBitmapByFileStreams(browsePicPath: String): Result {
        return withContext(BackgroundDispatcher) {
            this.printCoroutineScopeInfo()
            withContext(NonCancellable) {
                this.printCoroutineScopeInfo()
                Log.d("SaveBitmapCase", "saveBitmapByFileStreams")
                val source = File(browsePicPath)
                val target = File(MainActivity.RETURN_URL)
                copyFileUsingFileStreams(source, target)
            }
        }
    }

    suspend fun saveBitmapByFile(source: File?, dest: File?, context: Context): Result {
        return withContext(BackgroundDispatcher) {
            Log.d("SaveBitmapCase", "saveBitmapByFile, copyFileUsingFileStreams")
            if (source != null && source.exists()) {
                val bitmap = BitmapFactory.decodeFile(source.absolutePath)
                val size = getMemorySize(bitmap, SizeType.MB)
                Log.d("SaveBitmapCase", "saveBitmapByFile, size : $size")
                if (size >= 10) {
                    Log.d("SaveBitmapCase", "saveBitmapByFile, scaleAndSaveWallpaper ,EditImageType.IMAGE_TYPE_FILL ")
                    scaleAndSaveWallpaper(EditImageType.IMAGE_TYPE_FILL.value, context, bitmap)
                } else {
                    copyFileUsingFileStreams(source, dest)
                }
            } else {
                copyFileUsingFileStreams(source, dest)
            }
        }
    }

    private suspend fun copyFileUsingFileStreams(source: File?, dest: File?): Result {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                var input: InputStream? = null
                var output: OutputStream? = null
                try {
                    input = FileInputStream(source)
                    output = FileOutputStream(dest)
                    val buf = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        output.write(buf, 0, bytesRead)
                    }
                    Result.Success(null, "")
                } catch (e: IOException) {
                    Result.Fail
                } finally {
                    input!!.close()
                    output!!.close()
                }
            }
        }
    }

    suspend fun saveBitmap(bitmap: Bitmap?, path: String): Result {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                var compressBitmap = bitmap
                bitmap?.let {
                    val size = getMemorySize(it)
                    println("compressBitmap size : $size")
                }
                saveBitmapBase.saveBitmapFile(compressBitmap, path)
            }
        }
    }

    suspend fun saveBitmapByPath(filePath: String, bitmap: Bitmap): Result {
        return withContext(Dispatchers.IO) {
            this.printCoroutineScopeInfo()
            withContext(NonCancellable) {
                this.printCoroutineScopeInfo()
                val file = File(filePath)
                try {
                    val bos = BufferedOutputStream(FileOutputStream(file))
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos)
                    bos.flush()
                    bos.close()
                    println("withContext done")
                    Result.Success(bitmap, "")
                } catch (e: IOException) {
                    e.printStackTrace()
                    println("withContext fail")
                    Result.Fail
                }
            }
        }
    }

    suspend fun scaleAndSaveWallpaper(cropType: Int, context: Context, bitmap: Bitmap): Result {
        return withContext(BackgroundDispatcher) {
            when (cropType) {
                EditImageType.IMAGE_TYPE_NO.value -> {
                    return@withContext saveBitmapBase.setNoTypeWallpaper(context, "")
                }
                EditImageType.IMAGE_TYPE_FIT.value -> {
                    val returnBitmap = saveBitmapBase.createFitBitmap(bitmap, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                    saveBitmapByPath(MainActivity.RETURN_URL, returnBitmap)
                }
                EditImageType.IMAGE_TYPE_FILL.value -> {
                    val returnBitmap = saveBitmapBase.createFillBitmap(bitmap, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                    saveBitmapByPath(MainActivity.RETURN_URL, returnBitmap)
                }
                EditImageType.IMAGE_TYPE_STRETCH.value -> {
                    val returnBitmap = saveBitmapBase.createScaleBitmap(bitmap, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                    saveBitmapByPath(MainActivity.RETURN_URL, returnBitmap)
                }
                else -> {
                    Result.Success(null, "")
                }
            }
        }
    }


    suspend fun setWallpaperFilter(cropType: Int, path: String, context: Context, browsePicPath: String, fileProviderBitmap: Bitmap?, preloadPicPath: String?): Result {
        return withContext(BackgroundDispatcher) {
            this.printCoroutineScopeInfo()
            when (cropType) {
                EditImageType.IMAGE_TYPE_NO.value -> {
                    return@withContext saveBitmapBase.setNoTypeWallpaper(context, preloadPicPath)
                }
                EditImageType.IMAGE_TYPE_FIT.value -> {
                    browsePicPath.let {
                        val is4K = isImage4k(it)
                        Log.d("AAA", "setWallpaperFilter , is4K : $is4K"+" BUILD_SELECTOR : $BUILD_SELECTOR")
                        if (BUILD_SELECTOR == FULL_HD_STRING && is4K) {
                            return@withContext Result.Error_ImageSizeToBigNotSupport
                        } else if (is4K) {
                            return@withContext saveBitmapBase.setWallpaperStreams(context, it, true)
                        }
                        val returnBitmap = saveBitmapBase.createFitBitmap(it, WALLPAPER_WIDTH, MainActivity.WALLPAPER_HEIGHT)
                        if (returnBitmap != null) {
                            return@withContext saveBitmapBase.setWallpaperByBitmap(context, returnBitmap)
                        } else {
                            return@withContext Result.Error_ImageSizeToBigNotSupport
                        }
                    }
                }
                EditImageType.IMAGE_TYPE_FILL.value -> {
                    browsePicPath.let {
                        val is4K = isImage4k(it)
                        Log.d("AAA", "setWallpaperFilter , is4K : $is4K"+" BUILD_SELECTOR : $BUILD_SELECTOR")
                        if (BUILD_SELECTOR == FULL_HD_STRING && is4K) {
                            return@withContext Result.Error_ImageSizeToBigNotSupport
                        } else if (is4K) {
                            return@withContext saveBitmapBase.setWallpaperStreams(context, it, true)
                        }
                        val returnBitmap = saveBitmapBase.createFillBitmap(it, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                        Log.d("SaveBitmapCase", "AAA_returnBitmap : " + returnBitmap)
                        if (returnBitmap != null) {
                            return@withContext saveBitmapBase.setWallpaperByBitmap(context, returnBitmap)
                        } else {
                            return@withContext Result.Error_ImageSizeToBigNotSupport
                        }
                    }
                }
                EditImageType.IMAGE_TYPE_STRETCH.value -> {
                    browsePicPath.let {
                        val is4K = isImage4k(it)
                        Log.d("AAA", "setWallpaperFilter , is4K : $is4K"+" BUILD_SELECTOR : $BUILD_SELECTOR")
                        if (BUILD_SELECTOR == FULL_HD_STRING && is4K) {
                            return@withContext Result.Error_ImageSizeToBigNotSupport
                        } else if (is4K) {
                            return@withContext saveBitmapBase.setWallpaperStreams(context, it, true)
                        }
                        val returnBitmap = decodeSampledBitmapFromFd(browsePicPath, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                        if (returnBitmap != null) {
                            return@withContext saveBitmapBase.setWallpaperByBitmap(context, returnBitmap)
                        } else {
                            return@withContext Result.Fail
                        }
                    }
                }
                EditImageType.IMAGE_TYPE_COLOR.value -> {
                    return@withContext setColorWallpaper(path, context)
                }
                EditImageType.IMAGE_TYPE_CANCEL_AND_LEAVE.value -> {
                    return@withContext saveBitmapBase.setTypeCancelAndLeave()
                }
                else -> {
                    return@withContext Result.Success(null, browsePicPath)
                }
            }
        }
    }


//    bitmap占用的内存大小等于bitmapWidth*bitmapHeight*bitmap像素格式，常用的像素格式有下面两种
//    ARGB_8888:4byte
//    RGB_565:2byte
//
//    在实际开发中，bitmap占用的内存大小还和图片存放的目录和设备的密度有关，举个例子
//    一张3840*2160的PNG 图片，我把它放到 drawable-xxhdpi
//    其中 density 对应 xxhdpi 为480，targetDensity 对应IFP 4K的DPI = 480
//     3840/480 * 480 * 2160/480 *480 * 4 = 33177600

    fun getMemorySize(bitmap: Bitmap, sizeType: SizeType = SizeType.KB): Int {
        val bytes = bitmap.allocationByteCount
        return when (sizeType) {
            SizeType.B -> bytes
            SizeType.KB -> bytes / 1024
            SizeType.MB -> bytes / 1024 / 1024
            SizeType.GB -> bytes / 1024 / 1024 / 1024
        }
    }

    enum class SizeType {
        B,
        KB,
        MB,
        GB
    }
}