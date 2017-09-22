/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import com.linecorp.clay.Style.Color.Companion.OPACITY_100
import com.linecorp.clay.Style.Color.Companion.OPACITY_50
import com.linecorp.clay.Style.Companion.DEFAULT_BORDER_WIDTH
import com.linecorp.clay.Style.Companion.DEFAULT_STROKE_WIDTH_FOR_POINT

internal class PointIndicatorDrawable() : Drawable() {
    private val solidPaint = Paint()
    private val strokePaint = Paint()
    private val strokeBorderPaint = Paint()

    var point: PointF? = null

    var radius: Float
        set(value) {
            outerRadius = Math.max(value, 1f)
        }
        get() = outerRadius

    var color: Int
        set(value) {
            solidPaint.color = value
            solidPaint.alpha = OPACITY_50
            strokePaint.color = value
            strokePaint.alpha = OPACITY_100
        }
        get() = strokePaint.color

    var borderColor: Int
        set(value) {
            strokeBorderPaint.color = value
        }
        get() = strokeBorderPaint.color

    var strokeBorderWidth: Float
        set(value) {
            _strokeBorderWidth = value
            strokeBorderPaint.strokeWidth = strokeWidth + 2 * _strokeBorderWidth
        }
        get() = _strokeBorderWidth

    var strokeWidth: Float
        set(value) {
            strokePaint.strokeWidth = value
            strokeBorderPaint.strokeWidth = value + 2 * strokeBorderWidth
        }
        get() = strokePaint.strokeWidth

    private var outerRadius: Float = 1f
    private var _strokeBorderWidth: Float = 1f

    init {
        solidPaint.color = Color.WHITE
        solidPaint.alpha = OPACITY_50
        strokePaint.style = Paint.Style.STROKE
        strokePaint.color = Color.WHITE
        strokePaint.alpha = OPACITY_100

        strokeBorderPaint.style = Paint.Style.STROKE
        strokeBorderPaint.color = Color.BLACK

        strokeWidth = DEFAULT_STROKE_WIDTH_FOR_POINT
        strokeBorderWidth = DEFAULT_BORDER_WIDTH
    }

    constructor(point: PointF) : this() {
        this.point = point
    }

    override fun draw(canvas: Canvas?) {
        point?.let { point ->
            canvas?.drawCircle(point.x, point.y, outerRadius, solidPaint)
            canvas?.drawCircle(point.x, point.y, outerRadius, strokeBorderPaint)
            canvas?.drawCircle(point.x, point.y, outerRadius, strokePaint)
        }
    }

    override fun setAlpha(alpha: Int) {
        solidPaint.alpha = alpha
        strokePaint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return solidPaint.alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //do nothing
    }
}
