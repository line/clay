/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.transform

import android.graphics.Matrix
import android.graphics.PointF
import android.util.Log
import com.linecorp.clay.graphic.centerPoint
import com.linecorp.clay.graphic.distance

internal class ZoomTransformer(initPoint1: PointF, initPoint2: PointF, val maxScale: Float = 4f) : Transformer<Matrix> {
    val matrixPoints = FloatArray(9)
    val tempMatrix = Matrix()

    var scalePivot: PointF
    var lastPoint1: PointF
    var lastPoint2: PointF
    var lastDistance: Float

    init {
        lastPoint1 = PointF(initPoint1.x, initPoint1.y)
        lastPoint2 = PointF(initPoint2.x, initPoint2.y)
        scalePivot = centerPoint(lastPoint1, lastPoint2)
        lastDistance = distance(lastPoint1, lastPoint2)
    }
    override fun transform(target: Matrix, points: List<PointF>) {

        if (points.isEmpty()) {
            return
        }
        val point1 = points[0]
        var point2: PointF? = null

        if (points.count() > 1) {
            point2 = points[1]
        }

        val newDistance: Float
        if (point2 != null) {
            newDistance = distance(point1, point2)
        } else {
            newDistance = lastDistance
        }

        val scale = newDistance / lastDistance

        tempMatrix.set(target)
        target.postScale(scale, scale, scalePivot.x, scalePivot.y)
        target.getValues(matrixPoints)
        val scaleX = matrixPoints[Matrix.MSCALE_X]
        val scaleY = matrixPoints[Matrix.MSCALE_Y]
        if (scaleX > maxScale || scaleY > maxScale) {
            Log.w("ZoomTransformer", "scale exceeds ${maxScale}x, use origin matrix")
            target.set(tempMatrix)
        }
        lastPoint1 = point1

        point2?.let { lastPoint2 = it }

        lastDistance = newDistance
    }
}
