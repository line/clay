/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.transform

import android.graphics.Matrix
import android.graphics.PointF

internal class OneFingerPanTransformer(initPoint1: PointF) : Transformer<Matrix> {

    var lastPoint: PointF

    init {
        lastPoint = PointF(initPoint1.x, initPoint1.y)
    }

    override fun transform(target: Matrix, points: List<PointF>) {
        if (points.isEmpty()) {
            return
        }

        val newPoint = points[0]
        target.postTranslate(newPoint.x - lastPoint.x, newPoint.y - lastPoint.y)
        lastPoint = newPoint
    }
}
