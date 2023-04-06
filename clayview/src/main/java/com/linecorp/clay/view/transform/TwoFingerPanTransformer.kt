/*
 * Copyright 2017 LINE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linecorp.clay.view.transform

import android.graphics.Matrix
import android.graphics.PointF
import com.linecorp.clay.graphic.centerPoint

internal class TwoFingerPanTransformer(initPoint1: PointF, initPoint2: PointF) : Transformer<Matrix> {

    var lastTranslatePivot: PointF
    var lastPoint1: PointF
    var lastPoint2: PointF? = null

    private var stop = false

    init {
        lastTranslatePivot = centerPoint(initPoint1, initPoint2)
        lastPoint1 = PointF(initPoint1.x, initPoint1.y)
        lastPoint2 = PointF(initPoint2.x, initPoint2.y)
    }

    override fun transform(target: Matrix, points: List<PointF>) {
        if (points.isEmpty() || stop) {
            return
        }

        if (points.count() == 1) {
            stop = true
            return
        }

        val point1 = points[0]
        val point2 = points[1]

        val newPivotPoint = centerPoint(point1, point2)

        target.postTranslate(newPivotPoint.x - lastTranslatePivot.x, newPivotPoint.y - lastTranslatePivot.y)

        lastPoint1 = point1
        lastPoint2 = point2

        lastTranslatePivot = newPivotPoint
    }
}
