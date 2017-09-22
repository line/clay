/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.drawable

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import com.linecorp.clay.Style
import com.linecorp.clay.Style.Companion.DEFAULT_BORDER_WIDTH
import com.linecorp.clay.Style.Companion.DEFAULT_STROKE_WIDTH
import com.linecorp.clay.Style.Companion.DEFAULT_STROKE_WIDTH_FOR_POINT
import com.linecorp.clay.view.DrawingPath

/**
 * Path selection drawable, if highlight, it draws clear inside the path and draw mask outside
 * If not highlight, it just draw the path
 */
internal class PathSelectDrawable(var drawingPath: DrawingPath, @ColorInt maskColor: Int) : Drawable() {

    private val selectedPathPaint = createDefaultStrokePaint(Color.WHITE)
    private val selectedPathBorderPaint = createDefaultStrokePaint(Color.BLACK)
    private val pathInsidePaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        color = Color.TRANSPARENT
    }

    private val moldControlPointDrawable: PointIndicatorDrawable = PointIndicatorDrawable().apply {
        strokeWidth = DEFAULT_STROKE_WIDTH_FOR_POINT
        color = Style.Color.LINE_GREEN
        strokeBorderWidth = 0f
    }

    private var mask = ColorDrawable(maskColor)

    var maskBounds: Rect
        set(value) {
            mask.bounds = value
        }
        get() = mask.bounds

    var strokeWidth: Float
        set(value) {
            selectedPathPaint.strokeWidth = value
            selectedPathBorderPaint.strokeWidth = value + 2 * strokeBorderWidth
            selectedPathPaint.pathEffect = CornerPathEffect(value)

            moldControlPointDrawable.radius = value
        }
        get() = selectedPathPaint.strokeWidth

    private var _strokeBorderWidth = 1f

    var strokeBorderWidth: Float
        set(value) {
            _strokeBorderWidth = value
            selectedPathBorderPaint.strokeWidth = strokeWidth + 2 * _strokeBorderWidth
        }
        get() = _strokeBorderWidth

    var strokeBorderColor: Int
        set(@ColorInt value) {
            selectedPathBorderPaint.color = value
        }
        get() = selectedPathBorderPaint.color

    var strokeColor: Int
        set(@ColorInt value) {
            selectedPathPaint.color = value
        }
        get() = selectedPathPaint.color

    var controlPointColor: Int
        set(@ColorInt value) {
            moldControlPointDrawable.color = value
        }
        get() = moldControlPointDrawable.color

    init {
        strokeWidth = DEFAULT_STROKE_WIDTH
        strokeBorderWidth = DEFAULT_BORDER_WIDTH
    }

    private fun drawMaskLayer(canvas: Canvas?) {
        mask.draw(canvas)
        canvas?.drawPath(drawingPath.path, pathInsidePaint)
    }

    override fun draw(canvas: Canvas?) {
        if (drawingPath.isClosed) {
            drawMaskLayer(canvas)
        }
        //on some device, it might draw a point on (0, 0) with the selectedPathPaint even the path is empty....
        if (!drawingPath.isEmpty) {
            canvas?.drawPath(drawingPath.path, selectedPathBorderPaint)
            canvas?.drawPath(drawingPath.path, selectedPathPaint)
            if (drawingPath.isClosed) {
                drawingPath.controlPoints.forEach { point ->
                    moldControlPointDrawable.point = point
                    moldControlPointDrawable.draw(canvas)
                }
            }
        }
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

    override fun getOpacity(): Int {
        return 0xFF
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        //not use
    }
}
