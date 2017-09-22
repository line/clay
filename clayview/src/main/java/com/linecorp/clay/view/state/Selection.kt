/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.state

import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import com.linecorp.clay.view.DrawingPath

/**
 * Selection state, it manipulates drawing path
 */
internal class Selection(drawingPath: DrawingPath,
                private var touchedPoint: PointF,
                val pointId: Int,
                val validRect: RectF) :
        EditState<DrawingPath>(drawingPath) {

    //for the continuous trimming, generally it would have some delta distance after the second steps
    private val delta = PointF(0f, 0f)

    init {
        //first touch point might be outside of the image.
        setupFirstValidPoint(touchedPoint.x, touchedPoint.y)
    }

    override fun doOnTouchMove(event: MotionEvent) {
        if (editingObject.isEmpty) {
            setupFirstValidPoint(touchedPoint.x, touchedPoint.y)
            touchedPoint.set(event.x, event.y)
            return
        }

        val x = (event.x - delta.x).coerceIn(validRect.left, validRect.right)
        val y = (event.y - delta.y).coerceIn(validRect.top, validRect.bottom)
        Log.v(TAG, "x: $x, y: $y, touchedPoint.x: ${touchedPoint.x}, " +
                 "touchedPoint.y: ${touchedPoint.y}, valid rect: $validRect")
        val dx = x - touchedPoint.x
        val dy = y - touchedPoint.y
        if (Math.abs(dx) >= TOUCH_TOLERANCE || Math.abs(dy) >= TOUCH_TOLERANCE) {
            editingObject.smoothTo(x, y, checkMinDistance = true)
            touchedPoint.set(x, y)
        }
    }

    override fun doOnTouchEnd(event: MotionEvent) {
        if (editingObject.length < TOUCH_TOLERANCE) {
            editingObject.reset()
            return
        }

        val x = (event.x - delta.x).coerceIn(validRect.left, validRect.right)
        val y = (event.y - delta.y).coerceIn(validRect.top, validRect.bottom)
        editingObject.smoothTo(x, y, checkMinDistance = false)
    }

    private fun setupFirstValidPoint(x: Float, y: Float) {
        if (editingObject.isEmpty) {
            if (validRect.contains(x, y)) {
                Log.d(TAG, "setupFirstValidPoint, ($x, $y)")
                editingObject.reset()
                editingObject.moveTo(x, y)
            }
        } else {
            editingObject.endPoint?.let { endPoint ->
                val newX = Math.min(Math.max(x, validRect.left), validRect.right)
                val newY = Math.min(Math.max(y, validRect.top), validRect.bottom)
                delta.x = newX - endPoint.x
                delta.y = newY - endPoint.y
                Log.d(TAG, "continuous from last point, (${endPoint.x}, ${endPoint.y}), " +
                         "delta = (${delta.x}, ${delta.y})")
                touchedPoint.set(endPoint.x, endPoint.y)
            }
        }
    }

    companion object {
        private const val TAG = "Selection"
        private const val TOUCH_TOLERANCE = 4f
    }
}
