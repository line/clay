/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic.model

internal class IntensityMap(width: Int, height: Int) : ImageMap(width, height) {

    private fun rgbToIntensity(pixel: Int): Int =
            ((pixel shr(16)) and 0xFF) + ((pixel shr(8)) and 0xFF) + (pixel and 0xFF)

    fun setFromRgb(x: Int, y: Int, rgbPixel: Int) {
        set(x, y, rgbToIntensity(rgbPixel))
    }
}
