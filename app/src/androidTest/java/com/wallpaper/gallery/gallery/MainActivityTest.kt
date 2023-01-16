package com.wallpaper.gallery.gallery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test

class MainActivityTest {
    lateinit var instrumentationContext: Context

    @Before
    fun setup() {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun createFillBitmap_Insert_bitmap() = runBlocking  {
        withContext(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeResource(instrumentationContext.resources, com.wallpaper.gallery.gallery.R.drawable.testwallpapers)
            bitmap?.let {
                val result = createFillBitmap(it, 1920, 1080)
                println("createFillBitmap result.width: $result.width")
                MatcherAssert.assertThat(result.width, CoreMatchers.`is`(1920))
                MatcherAssert.assertThat(result.height, CoreMatchers.`is`(1080))
            }
            println("createFillBitmap completed")
        }
    }

    @Test
    fun createFitBitmap_Insert_bitmap() = runBlocking  {
        withContext(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeResource(instrumentationContext.resources, com.wallpaper.gallery.gallery.R.drawable.testwallpapers)
            bitmap?.let {
                val result = createFitBitmap(it, 1920, 1080)
                println("createFitBitmap result.width: $result.width")
                MatcherAssert.assertThat(result.width, CoreMatchers.`is`(1920))
                MatcherAssert.assertThat(result.height, CoreMatchers.`is`(1080))
            }
            println("createFitBitmap completed")
        }
    }


    @Test
    fun createStretchBitmap_Insert_bitmap() = runBlocking  {
        withContext(Dispatchers.Default) {
            val bitmap = BitmapFactory.decodeResource(instrumentationContext.resources, com.wallpaper.gallery.gallery.R.drawable.testwallpapers)
            bitmap?.let {
                val result = createScaleBitmap(it, 1920, 1080)
                println("createStretchBitmap result.width: $result.width")
                MatcherAssert.assertThat(result.width, CoreMatchers.`is`(1920))
                MatcherAssert.assertThat(result.height, CoreMatchers.`is`(1080))
            }
            println("createStretchBitmap completed")
        }
    }

    @Test
    fun createColorBitmap_create_bitmap() = runBlocking  {
        withContext(Dispatchers.Default) {
            val color = Color.parseColor("#3C6B70")
            val result = createColorBitmap(color, 1920, 1080)
            println("createColor result.width: $result.width")
            MatcherAssert.assertThat(result.width, CoreMatchers.`is`(1920))
            MatcherAssert.assertThat(result.height, CoreMatchers.`is`(1080))
            println("createColor completed")
        }
    }


    private fun createFillBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        val scaleWidth: Int
        val scaleHeight: Int
        return if (width.toFloat() / bitmap.width.toFloat() > height.toFloat() / bitmap.height.toFloat()) {
            scaleWidth = width
            scaleHeight = (scaleWidth.toFloat() / bitmap.width.toFloat() * bitmap.height.toFloat()).toInt()
            val returnBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false)
            Bitmap.createBitmap(returnBitmap, 0, (returnBitmap.height - height) / 2, width, height, null, false)
        } else {
            scaleHeight = height
            scaleWidth = (scaleHeight.toFloat() / bitmap.height.toFloat() * bitmap.width.toFloat()).toInt()
            val returnBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, false)
            Bitmap.createBitmap(returnBitmap, (returnBitmap.width - width) / 2, 0, width, height, null, false)
        }
    }


    private fun createFitBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
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

    private fun createScaleBitmap(src: Bitmap, dstWidth: Int, dstHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false)
    }

    //生成一个纯色的bitmap
    private fun createColorBitmap(color: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

//    @Test
//    fun correctUseOfCoroutinesVariant2() = runBlocking {
//        withContext(Dispatchers.Default) {
//            val totalIterations = (1..5).toList().map { duration ->
//                async {
//                    val startTimeNano = System.nanoTime()
//                    var iterations = 0
//                    while (System.nanoTime() < startTimeNano + (duration * 10f.pow(9) )) {
//                        iterations++
//                    }
//                    iterations
//                }
//            }.awaitAll().sum()
//
//            println("total iterations: $totalIterations")
//        }
//    }
}