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

package com.linecorp.clay.graphic

import android.graphics.PointF

/**
 * Calculates the sum of two points
 *
 * @param pointB the addend point
 * @return new point
 */
infix fun PointF.add(pointB: PointF) = PointF(this.x + pointB.x, this.y + pointB.y)

/**
 * Calculate the difference of two points
 *
 * @param pointB the subtrahend point
 * @return new point
 */
infix fun PointF.sub(pointB: PointF): PointF {
    return PointF(this.x - pointB.x, this.y - pointB.y)
}

/**
 * Calculates the dot product of two points
 *
 * @param pointB the multiplier point
 * @return new point
 */
infix fun PointF.mul(scale: Float): PointF {
    return PointF(this.x * scale, this.y * scale)
}
