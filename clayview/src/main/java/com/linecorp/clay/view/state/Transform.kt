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
