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

package com.linecorp.clay.graphic.model

internal class IntensityMap(width: Int, height: Int) : ImageMap(width, height) {

    private fun rgbToIntensity(pixel: Int): Int =
            ((pixel shr(16)) and 0xFF) + ((pixel shr(8)) and 0xFF) + (pixel and 0xFF)

    fun setFromRgb(x: Int, y: Int, rgbPixel: Int) {
        set(x, y, rgbToIntensity(rgbPixel))
    }
}
