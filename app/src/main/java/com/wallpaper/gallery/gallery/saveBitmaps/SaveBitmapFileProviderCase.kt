package com.wallpaper.gallery.gallery.saveBitmaps

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.WindowManager
import com.wallpaper.gallery.gallery.common.EditImageType
import com.wallpaper.gallery.gallery.utils.*
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.*
import java.io.*

class SaveBitmapFileProviderCase(private val saveBitmapBase: SaveBitmapBase, bitmapUtils: BitmapUtils) : SaveBitmapBase(bitmapUtils) {

    suspend fun saveBitmap(inputStream: InputStream, path: String): Result {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                try {
                    try {
                        return@withContext saveFileFromUri(inputStream, path)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Result.Fail
                    }
                } catch (t: Throwable) {
                    if (t !is CancellationException) {
                        Result.Fail
                    } else {
                        Result.Fail
                        throw t
                    }
                }
            }
        }
    }

    suspend fun setWallpaperFilter(cropType: Int, context: Context, fileProviderBitmap: Bitmap?, preloadPicPath: String?): Result {
        return withContext(BackgroundDispatcher) {
            when (cropType) {
                EditImageType.IMAGE_TYPE_NO.value -> {
                    return@withContext saveBitmapBase.setNoTypeWallpaper(context, preloadPicPath)
                }
                EditImageType.IMAGE_TYPE_FIT.value -> {
                    var resized: Bitmap?
                    fileProviderBitmap?.let { bt ->
                        resized = saveBitmapBase.createFitBitmap(bt, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                        setWallpapers(context, resized)
                        Result.Success(resized, "")
                    } ?: run {
                        Result.Fail
                    }
                }
                EditImageType.IMAGE_TYPE_FILL.value -> {
                    var resized: Bitmap?
                    fileProviderBitmap?.let { bt ->
                        resized = saveBitmapBase.createFillBitmap(bt, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                        setWallpapers(context, resized)
                        Result.Success(resized, "")
                    } ?: run {
                        Result.Fail
                    }
                }
                EditImageType.IMAGE_TYPE_STRETCH.value -> {
                    var resized: Bitmap?
                    fileProviderBitmap?.let { bt ->
                        resized = saveBitmapBase.createScaleBitmap(bt, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                        setWallpapers(context, resized)
                        Result.Success(resized, "")
                    } ?: run {
                        Result.Fail
                    }
                }
                EditImageType.IMAGE_TYPE_COLOR.value -> {
                    return@withContext saveBitmapBase.setColorWallpaper("", context)
                }
                EditImageType.IMAGE_TYPE_CANCEL_AND_LEAVE.value -> {
                    return@withContext saveBitmapBase.setTypeCancelAndLeave()
                }
                else -> Result.Success(fileProviderBitmap, "")
            }
        }
    }

    private fun setWallpapers(context: Context, bitmap: Bitmap?) {
        val myWallpaperManager = WallpaperManager.getInstance(context)
        try {
            setWallpaperSize(context, myWallpaperManager)
            myWallpaperManager.setBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun setWallpaperSize(ctx: Context, wallpaperManager: WallpaperManager) {
        val size = Point()
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(size)
        wallpaperManager.suggestDesiredDimensions(size.x, size.y)
    }

    private suspend fun saveFileFromUri(inputStream: InputStream, path: String): Result {
        return withContext(BackgroundDispatcher) {
            withContext(NonCancellable) {
                Log.d("AAA", "saveFileFromUri")
                try {
                    val image = File(path)
                    val outputStream: OutputStream = FileOutputStream(image)
                    val buf = ByteArray(1024)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) {
                        outputStream.write(buf, 0, len)
                    }
                    outputStream.close()
                    inputStream.close()
                } catch (e: IOException) {
                    Result.Fail
                    e.printStackTrace()
                } finally {
                    Result.Success(null, "")
                }
                Result.Success(null, "")
            }
        }
    }


    companion object {
        private const val WALLPAPER_WIDTH = 1920
        private const val WALLPAPER_HEIGHT = 1080
    }

}