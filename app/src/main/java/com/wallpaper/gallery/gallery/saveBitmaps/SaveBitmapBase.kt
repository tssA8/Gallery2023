package com.wallpaper.gallery.gallery.saveBitmaps

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.WindowManager
import com.wallpaper.gallery.gallery.common.TestUtils.printCoroutineScopeInfo
import com.wallpaper.gallery.gallery.utils.BitmapUtils
import com.wallpaper.ifp.unilauncher.util.BackgroundDispatcher
import kotlinx.coroutines.withContext
import java.io.*


open class SaveBitmapBase(private val bitmapUtils: BitmapUtils) {
    private val colors = arrayOf("#000000", "#3C6B70", "#3C4670", "#632E8B", "#00C4BF-#004CA4", "#7EB6FF-#551AB7", "#72A6FF-#10069D", "#B237EC-#3B30B0")
    private val WALLPAPER_WIDTH = 1920
    private val WALLPAPER_HEIGHT = 1080
    private val FOUR_K_WIDTH = 3840
    private val FOUR_K_HEIGHT = 2160

    sealed class Result {
        class Success(val bitmap: Bitmap?, val path: String?) : Result()
        object Fail : Result()
        object Error_ImageSizeToBigNotSupport : Result()
    }

    suspend fun setColorWallpaper(path: String, context: Context): Result {
        return withContext(BackgroundDispatcher) {
            val position = path.toInt()
            if (!colors[position].contains("-")) {
                val color = Color.parseColor(colors[position])
                val returnBitmap = bitmapUtils.createColorBitmap(color, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                return@withContext setWallpaperByBitmap(context, returnBitmap)
            } else {
                val colorStrings = colors[position].split("-").toTypedArray()
                val color1 = Color.parseColor(colorStrings[0])
                val color2 = Color.parseColor(colorStrings[1])
                val returnBitmap = bitmapUtils.createColorBitmap(color1, color2, WALLPAPER_WIDTH, WALLPAPER_HEIGHT)
                return@withContext setWallpaperByBitmap(context, returnBitmap)
            }
        }
    }

    suspend fun setTypeCancelAndLeave(): Result {
        return withContext(BackgroundDispatcher) {
            Result.Success(null, "")
        }
    }

    suspend fun setNoTypeWallpaper(context: Context, path: String?): Result {
        return withContext(BackgroundDispatcher) {
            return@withContext setWallpaperStreams(context, path, false)
        }
    }

//    suspend fun setNoTypeWallpaper(context: Context, bitmap: Bitmap??): Result {
//        return withContext(BackgroundDispatcher) {
//            return@withContext setWallpaperStreams(context, path, false)
//        }
//    }

    suspend fun setWallpaperStreams(context: Context, path: String?, isSetBitmap: Boolean): Result {
        return withContext(BackgroundDispatcher) {
            this.printCoroutineScopeInfo()
            try {
                val inp: InputStream = BufferedInputStream(FileInputStream(path))
                inp.let {
                    val myWallpaperManager = WallpaperManager.getInstance(context)
                    try {
                        setWallpaperSize(context, myWallpaperManager)
                        myWallpaperManager.setStream(it)
                        val bitmap = BitmapFactory.decodeFile(path)
                        if (isSetBitmap) Result.Success(bitmap, path)
                        else Result.Success(null, path)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Result.Fail
                    } finally {
                        it.close()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Result.Fail
            }
        }
    }

    fun isImage4k(uri: String?): Boolean {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(uri, options)
        return options.outWidth == FOUR_K_WIDTH && options.outHeight == FOUR_K_HEIGHT
    }

    private fun setWallpaperSize(ctx: Context, wallpaperManager: WallpaperManager) {
        val size = Point()
        val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getRealSize(size)
        wallpaperManager.suggestDesiredDimensions(size.x, size.y)
    }

    suspend fun setWallpaperByBitmap(context: Context, bitmap: Bitmap?): Result {
        return withContext(BackgroundDispatcher) {
            this.printCoroutineScopeInfo()
            val myWallpaperManager = WallpaperManager.getInstance(context)
            try {
                setWallpaperSize(context, myWallpaperManager)
                myWallpaperManager.setBitmap(bitmap)
                Result.Success(bitmap, "")
            } catch (e: IOException) {
                e.printStackTrace()
                Result.Fail
            }
        }
    }

    // 从sd卡上加载图片
    fun decodeSampledBitmapFromFd(pathName: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        val src = BitmapFactory.decodeFile(pathName, options)
        return createScaleBitmap(src, reqWidth, reqHeight)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize > reqHeight
                    && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    suspend fun saveBitmapFile(bitmap: Bitmap?, filePath: String?): Result {
        return withContext(BackgroundDispatcher) {
            val file = File(filePath) //将要保存图片的路径
            try {
                val bos = BufferedOutputStream(FileOutputStream(file))
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, bos)
                bos.flush()
                bos.close()
                Result.Success(bitmap, "")
            } catch (e: IOException) {
                e.printStackTrace()
                Result.Fail
            }
        }
    }

    // Fill
    fun createFillBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleWidth: Int
        val scaleHeight: Int
        return if (width.toFloat() / bitmap.width.toFloat() > height.toFloat() / bitmap.height.toFloat()) {
            scaleWidth = width
            scaleHeight = (scaleWidth.toFloat() / bitmap.width.toFloat() * bitmap.height.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            Bitmap.createBitmap(returnBitmap, 0, (returnBitmap.height - height) / 2, width, height, null, false)
        } else {
            scaleHeight = height
            scaleWidth = (scaleHeight.toFloat() / bitmap.height.toFloat() * bitmap.width.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            Bitmap.createBitmap(returnBitmap, (returnBitmap.width - width) / 2, 0, width, height, null, false)
        }
    }

    // Fill
    fun createFillBitmap(path: String?, width: Int, height: Int): Bitmap? {
        Log.d("SaveBitmapBase", "AAA_createFillBitmap path : $path")
        val bitmap = BitmapFactory.decodeFile(path)
        Log.d("SaveBitmapBase", "AAA_createFillBitmap bitmap : $bitmap")
        if (bitmap != null) {
            val scaleWidth: Int
            val scaleHeight: Int
            return if (width.toFloat() / bitmap.width.toFloat() > height.toFloat() / bitmap.height.toFloat()) {
                Log.d("SaveBitmapBase", "AAA_createFillBitmap 111 bitmap : $bitmap")
                scaleWidth = width
                scaleHeight = (scaleWidth.toFloat() / bitmap.width.toFloat() * bitmap.height.toFloat()).toInt()
                val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
                Bitmap.createBitmap(returnBitmap, 0, (returnBitmap.height - height) / 2, width, height, null, false)
            } else {
                Log.d("SaveBitmapBase", "AAA_createFillBitmap 222 bitmap : $bitmap")
                scaleHeight = height
                scaleWidth = (scaleHeight.toFloat() / bitmap.height.toFloat() * bitmap.width.toFloat()).toInt()
                val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
                Bitmap.createBitmap(returnBitmap, (returnBitmap.width - width) / 2, 0, width, height, null, false)
            }
        } else {
            return null
        }
    }

    fun createScaleBitmap(src: Bitmap, dstWidth: Int, dstHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false)
    }


    //Fit
    fun createFitBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(resultBitmap)
        val color = Color.BLACK
        canvas.drawColor(color)
        val scaleWidth: Int
        val scaleHeight: Int
        if (width.toFloat() / bitmap.width.toFloat() > height.toFloat() / bitmap.height.toFloat()) {
            scaleHeight = height
            scaleWidth = (scaleHeight.toFloat() / bitmap.height.toFloat() * bitmap.width.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            canvas.drawBitmap(returnBitmap, ((width - returnBitmap.width) / 2).toFloat(), 0f, null)
        } else {
            scaleWidth = width
            scaleHeight = (scaleWidth.toFloat() / bitmap.width.toFloat() * bitmap.height.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            canvas.drawBitmap(returnBitmap, 0f, ((height - returnBitmap.height) / 2).toFloat(), null)
        }
        return resultBitmap
    }


    //Fit
    fun createFitBitmap(path: String?, width: Int, height: Int): Bitmap? {
        val bitmap = BitmapFactory.decodeFile(path) ?: return null
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(resultBitmap)
        val color = Color.BLACK
        canvas.drawColor(color)
        val scaleWidth: Int
        val scaleHeight: Int
        if (width.toFloat() / bitmap.width.toFloat() > height.toFloat() / bitmap.height.toFloat()) {
            scaleHeight = height
            scaleWidth = (scaleHeight.toFloat() / bitmap.height.toFloat() * bitmap.width.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            canvas.drawBitmap(returnBitmap, ((width - returnBitmap.width) / 2).toFloat(), 0f, null)
        } else {
            scaleWidth = width
            scaleHeight = (scaleWidth.toFloat() / bitmap.width.toFloat() * bitmap.height.toFloat()).toInt()
            val returnBitmap = createScaleBitmap(bitmap, scaleWidth, scaleHeight)
            canvas.drawBitmap(returnBitmap, 0f, ((height - returnBitmap.height) / 2).toFloat(), null)
        }
        return resultBitmap
    }

}