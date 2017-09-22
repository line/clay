/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic.model

import android.graphics.Bitmap
import android.graphics.Point

/**
 * Calculate the edge with Sobel filter
 * Ref:
 * https://en.wikipedia.org/wiki/Sobel_operator
 *
 * For optimization, this is not full implementation of Sobel operator, but the idea is similar
 * The kernel look like this
 * [ 1  0 -1]
 * [ 0  0  0]
 * [-1  0  1]
 *
 * The complexity is O(n^2), it should be initialized in background thread
 * Limitation (Due to performance reason):
 * This function does not perform smoothing first, may not perform well in noise image
 */
internal class EdgeMap(bitmap: Bitmap) : ImageMap(bitmap.width, bitmap.height) {
    init {
        calculateGradient(bitmap)
    }

    /**
     * Find the nearby pixels that give highest gradient value
     *
     * @param x x coordinate of the center point of searching region
     * @param y y coordinate of the center point of searching region
     * @param radius radius of the search boundary
     *
     * @return point the coordinate with highest value in searching region
     */
    fun maxInRegion(x: Int, y: Int, radius: Int): Point {
        var max = 0
        var maxX = x
        var maxY = y

        for(yOffset in -radius .. radius) {
            for(xOffset in -radius .. radius) {
                val currentX = (x + xOffset).coerceIn(0, width)
                val currentY = (y + yOffset).coerceIn(0, height)
                val gradient = get(x, y)
                if (gradient > max) {
                    maxX = currentX
                    maxY = currentY
                    max = gradient
                }
            }
        }

        return Point(maxX, maxY)
    }

    //TODO: boundary pixel ignored in this testing, should include efficient boundary handling
    private fun calculateGradient(bitmap: Bitmap) {
        // Find the intensity in 2x2 area and compute the gradient (gx, gy)
        val intensityMap = IntensityMap(bitmap.width, bitmap.height)
        for (y in 1..(height).minus(2) step 2) {
            for (x in 1..(width).minus(2) step 2) {
                // calculate the intensity
                intensityMap.setFromRgb(x, y, bitmap.getPixel(x, y))
                intensityMap.setFromRgb(x + 1, y, bitmap.getPixel(x + 1, y))
                intensityMap.setFromRgb(x, y + 1, bitmap.getPixel(x, y + 1))
                intensityMap.setFromRgb(x + 1, y + 1, bitmap.getPixel(x + 1, y + 1))

                // sum the gradient in x, y direction
                val gx = Math.abs(intensityMap.get(x - 1, y) - intensityMap.get(x + 1, y))
                val gy = Math.abs(intensityMap.get(x, y - 1) - intensityMap.get(x, y - 1))
                val gradient = gx + gy

                this.set(x, y, gradient)
            }
        }
    }
}
