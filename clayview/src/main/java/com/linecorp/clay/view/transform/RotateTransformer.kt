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
import android.graphics.RectF
import com.linecorp.clay.graphic.calculateAngleForRotation
import com.linecorp.clay.graphic.centerPoint

internal class RotateTransformer(initPoint1: PointF,
                        initPoint2: PointF?,
                        val viewPort: RectF) : Transformer<Matrix> {

    //use for translate
    var lastPoint1: PointF
    var lastPoint2: PointF? = null
    var imageRect: RectF
    var lastAngle: Double? = null

    init {
        lastPoint1 = PointF(initPoint1.x, initPoint1.y)
        imageRect = RectF()
        initPoint2?.let { point2 ->
            lastPoint2 = PointF(point2.x, point2.y)
        }
    }

    override fun transform(target: Matrix, points: List<PointF>) {
        if (points.count() < 2) {
            return
        }

        val point1 = points[0]
        val point2 = points[1]

        val pointForAngle = point1
        val rotatePivot = centerPoint(point1, point2)
        target.mapRect(imageRect, viewPort)

        //angel is -90 to 90
        val newAngle = calculateAngleForRotation(pointForAngle, rotatePivot)
        if (lastAngle == null) {
            lastAngle = newAngle
        }

        //if the new angle is in different quadrants, flip the angleDelta
        var angleDelta = newAngle - lastAngle!!
        if (Math.abs(angleDelta) >= 90) {
            if (angleDelta < 0) {
                angleDelta += 180
            } else {
                angleDelta -= 180
            }
        }

        val rotate = angleDelta * RATATE_RADIO

        if (!rotate.isNaN()) {
            target.postRotate(rotate.toFloat(), imageRect.centerX(), imageRect.centerY())
        }

        lastPoint1 = point1
        lastPoint2 = point2

        lastAngle = newAngle
    }

    companion object {
        private const val RATATE_RADIO = 1.1f
    }
}
