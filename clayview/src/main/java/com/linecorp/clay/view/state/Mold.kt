/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.state

import android.graphics.PointF
import android.graphics.RectF
import android.view.MotionEvent
import com.linecorp.clay.view.DrawingPath

/**
 * Mold state, it molds the closed path
 */
internal class Mold(drawingPath: DrawingPath, private var touchedPoint: PointF, val pointIndex: Int, val validRect: RectF) :
        EditState<DrawingPath>(drawingPath) {

    override fun doOnTouchMove(event: MotionEvent) {
        if (pointIndex < 0) {
            return
        }

        val x = event.x.coerceIn(validRect.left, validRect.right)
        val y = event.y.coerceIn(validRect.top, validRect.bottom)
        editingObject.updateControlPoint(pointIndex, x, y)

        touchedPoint.set(event.x, event.y)
    }
}
