/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.state

import android.graphics.Matrix
import android.graphics.PointF
import android.view.MotionEvent
import com.linecorp.clay.view.transform.Transformer
import java.util.ArrayList

/**
 * The transform state, it manipulates matrix
 */
class Transform(matrix: Matrix, val pointIds: List<Int>) : EditState<Matrix>(Matrix(matrix)) {

    private var transformers = ArrayList<Transformer<Matrix>>()

    /**
     * Add another transformer
     *
     * @param transformer The new transformer
     */
    fun addTransformer(transformer: Transformer<Matrix>) {
        transformers.add(transformer)
    }

    /**
     * Clear all transformers
     */
    fun clearTransforms() {
        transformers.clear()
    }

    override fun doOnTouchMove(event: MotionEvent) {
        if (pointIds.isEmpty()) {
            return
        }

        val point1Index = findPointIndex(pointIds[0], event)
        val point1: PointF?
        val points = ArrayList<PointF>()
        if (point1Index >= 0) {
            point1 = PointF(event.getX(point1Index), event.getY(point1Index))
            points.add(point1)
        }

        val point2: PointF?
        if (pointIds.count() >= 2) {
            val point2Index = findPointIndex(pointIds[1], event)
            if (point2Index >= 0) {
                point2 = PointF(event.getX(point2Index), event.getY(point2Index))
                points.add(point2)
            }
        }

        transformers.forEach { transformer ->
            transformer.transform(editingObject, points)
        }
    }

    private fun findPointIndex(id: Int, event: MotionEvent) = event.findPointerIndex(id)
}
