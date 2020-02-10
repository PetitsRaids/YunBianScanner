package com.raids.yunbianscanner.weight

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class RectScanView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var mWidth = 0
    private var mHeight = 0
    private var rectX = 0
    private var rectY = 0
    private var rectWidth = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = MeasureSpec.getSize(widthMeasureSpec)
        mHeight = MeasureSpec.getSize(heightMeasureSpec)
        var centerX = mWidth / 2
        var centerY = mHeight / 2
        rectX = centerX - rectWidth
        rectY = centerY - rectWidth
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
//        canvas.drawRoundRect()
    }
}