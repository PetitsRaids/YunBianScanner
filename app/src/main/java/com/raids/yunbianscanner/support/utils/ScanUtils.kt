package com.raids.yunbianscanner.support.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc

object ScanUtils {

    init {
        System.loadLibrary("identify-lib")
    }

    public fun identifyProcess(bitmap: Bitmap): String? {
        val temp =
            findBarcodeArea(
                bitmap
            )
        if (temp != null) {
            val mScannable =
                prepossessing(
                    temp
                )
            return identifyBarcode(
                mScannable
            )
        }
        return null
    }

    public fun identifyProcessByPicture(context: Context, uri: Uri): String? {
        val bitmap =
            loadPicture(
                context,
                uri
            )
        return if (bitmap != null) {
            identifyProcess(
                bitmap
            )
        } else {
            null
        }
    }

    private external fun identifyBarcode(bitmap: Bitmap?): String?

    private fun prepossessing(srcMat: Mat): Bitmap {
        val temp = Mat()
        // 灰度化
        Imgproc.cvtColor(srcMat, temp, Imgproc.COLOR_BGRA2GRAY)
        // otsu二值化
        Imgproc.threshold(temp, srcMat, 30.0, 255.0, Imgproc.THRESH_OTSU)
        val tempBitmap = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(srcMat, tempBitmap)
        return tempBitmap
    }

    private fun findBarcodeArea(srcBitmap: Bitmap): Mat? {
        var isFirst = true
        val src = Mat()
        val dst = Mat()
        Utils.bitmapToMat(srcBitmap, src)

        val gradX = Mat()
        val gradY = Mat()
        // 灰度化
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGRA2GRAY)
        val temp = Mat()
        var maxArea = 0.0
        var max = 0
        var count = 0
        val rect: Rect
        val contours = mutableListOf<MatOfPoint>()
        // 建立图像在X和Y方向上的梯度幅值
        fun realFindBarcodeArea(): Boolean {
            if (isFirst) {
                Imgproc.Sobel(dst, gradX, 5, 1, 0, 3)
                Imgproc.Sobel(dst, gradY, 5, 0, 1, 3)
            } else {
                Imgproc.Scharr(dst, gradX, 5, 1, 0)
                Imgproc.Scharr(dst, gradY, 5, 0, 1)
            }
            Core.subtract(gradX, gradY, temp)
            Core.convertScaleAbs(temp, temp)
            // 模糊化，消除部分噪点
            Imgproc.blur(temp, temp, Size(9.0, 9.0))
            // 固定阈值二值化
            Imgproc.threshold(temp, temp, 180.0, 255.0, Imgproc.THRESH_BINARY)
            // 用长方形做闭运算，填充条形码中间空隙
            var element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(21.0, 7.0))
            Imgproc.morphologyEx(temp, temp, Imgproc.MORPH_CLOSE, element, Point(-1.0, -1.0), 4)
            // 空隙填充完毕之后进行腐蚀和膨胀操作
            element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
            // 腐蚀
            Imgproc.erode(temp, temp, element, Point(-1.0, -1.0), 4)
            // 膨胀
            Imgproc.dilate(temp, temp, element, Point(-1.0, -1.0), 4)

            val hierarchy = Mat()
            Imgproc.findContours(
                temp,
                contours,
                hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_SIMPLE
            )
            Log.d("NDK", "contours.size is ${contours.size}")

            contours.iterator().forEach {
                if (Imgproc.contourArea(it) > maxArea) {
                    maxArea = Imgproc.contourArea(it)
                    max = count
                }
                count++
            }
            return !(count == 0 || Imgproc.contourArea(contours[max]) < 6000)
        }
        if (!realFindBarcodeArea()) {
            isFirst = false
            if (!realFindBarcodeArea())
                return null
        }
        rect = Imgproc.boundingRect(contours[max])
        rect.x = rect.x - 10
        rect.width = rect.width + 20
        Log.d("NDK", "rect.size() is ${rect.size()}")
        Imgproc.rectangle(src, rect, Scalar(255.0, 255.0), 9)
        return Mat(src, rect)
    }

    private fun loadPicture(context: Context, uri: Uri): Bitmap? {
        val options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inJustDecodeBounds = true // 只加载图片信息，不读取像素
        val exifInterface = context.contentResolver.openInputStream(uri)?.let { ExifInterface(it) }
        val orientation = exifInterface?.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        val matrix = Matrix()
        when (orientation) {
            6 -> {
                matrix.postRotate(90.0F)
            }
            3 -> {
                matrix.postRotate(180.0F)
            }
            8 -> {
                matrix.postRotate(270.0F)
            }
        }
        Log.d("NDK", "orientation is $orientation.")
        BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(uri),
            android.graphics.Rect(),
            options
        )
        options.inSampleSize =
            calculateScaleRate(
                options
            )
        options.inJustDecodeBounds = false
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri), null, options)
        return if (bitmap != null) {
            Log.d("NDK", "FUCK")
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false).also {
                bitmap.recycle()
            }
        } else {
            null
        }
    }

    private fun calculateScaleRate(options: BitmapFactory.Options): Int {
        val scaleRate:Int
        var width = options.outWidth
        var height = options.outHeight
        val longerWidth = if (width > height) {
            width
        } else {
            height
        }
        when (longerWidth) {
            in 0..1000 -> {
                scaleRate = 1
            }
            in 1000..2000 -> {
                scaleRate = 2
                width /= 2
                height /= 2
            }
            in 2000..4000 -> {
                scaleRate = 4
                width /= 4
                height /= 4
            }
            in 4000..8000 -> {
                scaleRate = 8
                width /= 8
                height /= 8
            }
            in 8000..16000 -> {
                scaleRate = 16
                width /= 16
                height /= 16
            }
            else -> {
                return 0
            }
        }
        return scaleRate
    }

}