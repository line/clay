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
