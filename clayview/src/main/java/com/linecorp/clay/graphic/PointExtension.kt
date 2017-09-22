/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
