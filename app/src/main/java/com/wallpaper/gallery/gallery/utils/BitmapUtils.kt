package com.wallpaper.gallery.gallery.utils

import android.graphics.*
import javax.inject.Inject

open class BitmapUtils @Inject constructor() {
    // 图片裁剪为stretch
    // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响
    fun createScaleBitmap(src: Bitmap, dstWidth: Int, dstHeight: Int): Bitmap {
        val dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false)
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle() // 释放Bitmap的native像素数组
        }
        return dst
    }

    //生成一个纯色的bitmap
    fun createColorBitmap(color: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        canvas.drawColor(color)
        return bitmap
    }

    //生成一个渐变的bitmap
    fun createColorBitmap(color1: Int, color2: Int, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint() //定义一个Paint
        val mShader: Shader = LinearGradient((width / 2).toFloat(), height.toFloat(), (width / 2).toFloat(), 0f, intArrayOf(color1, color2), null, Shader.TileMode.REPEAT)
        paint.shader = mShader
        canvas.drawPaint(paint)
        return bitmap
    }


    /**
     * @param bitmapColor  圆的颜色
     * @param backColor    图片背景颜色
     * @param txtColor     文字的颜色
     * @param text         文字
     * @param circleWidth  图片宽度
     * @param circleHeight 图片高度
     * @return
     */
    fun drawDefaultImg(bitmapColor: Int, backColor: Int, txtColor: Int, text: String?, circleWidth: Int, circleHeight: Int): Bitmap {
        var text = text
        var circleWidth = circleWidth
        var circleHeight = circleHeight
        if (circleHeight <= 0) { //做判断，如果传入的高度为0，则默认为60
            circleHeight = 60
        }
        if (circleWidth <= 0) { //做判断，如果传入的宽度为0，则默认为60
            circleWidth = 60
        }
        if (text == null) { //如果传入的文字为空，则设置为空格
            text = " "
        }
        val bitmap = Bitmap.createBitmap(circleWidth, circleHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(bitmap)
        canvas.drawColor(bitmapColor)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = backColor
        val raduis = (bitmap.width / 2).coerceAtMost(bitmap.height / 2)
        canvas.drawCircle((bitmap.width / 2).toFloat(), (bitmap.height / 2).toFloat(), raduis.toFloat(), paint)
        val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        txtPaint.textSize = (bitmap.height / 3).toFloat()
        txtPaint.color = txtColor
        txtPaint.strokeWidth = 10f
        txtPaint.textSize = (2 * bitmap.height / 3).toFloat()
        val txtWid = txtPaint.measureText(text)
        if (text.length == 1) {
//            canvas.drawText(text,(bitmap.getWidth()-txtWid)/2, bitmap.getHeight()/2+bitmap.getHeight()/3/2, txtPaint);
            canvas.drawText(text, (bitmap.width - txtWid) / 2, (bitmap.height / 2).toFloat(), txtPaint)
        } else {
//            canvas.drawText(text,(bitmap.getWidth()-txtWid)/text.length(), bitmap.getHeight()/2+bitmap.getHeight()/3/2, txtPaint);
            canvas.drawText(text, (bitmap.width - txtWid) / text.length, (3 * bitmap.height / 4).toFloat(), txtPaint)
        }
        //        canvas.save(Canvas.ALL_SAVE_FLAG);
//        canvas.restore();
        return bitmap
    }


}