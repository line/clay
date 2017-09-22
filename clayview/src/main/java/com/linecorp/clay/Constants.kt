/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay


internal class Style {
    class Color {
        companion object {
            const val LINE_GREEN = 0xFF33C659.toInt()
            const val BLACK_OPACITY_48 = 0x7A000000
            const val OPACITY_50 = 0x7F
            const val OPACITY_100 = 0xFF
        }
    }
    
    companion object {
        const val DEFAULT_MIN_DISTANCE_BETWEEN_TWO_POINTS = 8f
        const val TOUCH_INDICATOR_POINT_RADIUS_DP = 20f
        const val DEFAULT_STROKE_WIDTH_FOR_POINT = 3f
        const val DEFAULT_STROKE_WIDTH = 20f
        const val DEFAULT_BORDER_WIDTH = 2f
    }
}
