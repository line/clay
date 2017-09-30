package com.linecorp.clay.drawable

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt

interface PathDrawableProperties {
    var strokeWidth: Float
    var strokeBorderWidth: Float
    var strokeBorderColor: Int
    var strokeColor: Int
}

internal class PathDrawable(val path: Path) : Drawable(), PathDrawableProperties {

    private val pathPaint = createDefaultStrokePaint(Color.WHITE)
    private val pathBorderPaint = createDefaultStrokePaint(Color.BLACK)

    override var strokeWidth: Float
        set(value) {
            pathPaint.strokeWidth = value
            pathBorderPaint.strokeWidth = value + 2 * strokeBorderWidth
            pathPaint.pathEffect = CornerPathEffect(value)
        }
        get() = pathPaint.strokeWidth

    private var _strokeBorderWidth = 1f

    override var strokeBorderWidth: Float
        set(value) {
            _strokeBorderWidth = value
            pathBorderPaint.strokeWidth = strokeWidth + 2 * _strokeBorderWidth
        }
        get() = _strokeBorderWidth

    override var strokeBorderColor: Int
        set(@ColorInt value) {
            pathBorderPaint.color = value
        }
        get() = pathBorderPaint.color

    override var strokeColor: Int
        set(@ColorInt value) {
            pathPaint.color = value
        }
        get() = pathPaint.color

    init {
        strokeWidth = 10f
        strokeBorderWidth = 1f
    }

    override fun draw(canvas: Canvas?) {
        canvas?.drawPath(path, pathBorderPaint)
        canvas?.drawPath(path, pathPaint)
    }

    private fun createDefaultStrokePaint(@ColorInt color: Int): Paint {
        return Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    override fun setAlpha(alpha: Int) {
        //not use
    }

    override fun getOpacity() = 0xFF

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //not use
    }
}
