/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic.model

internal open class ImageMap(val width: Int, val height: Int) {
    val internalMap = IntArray(width * height)

    fun set(x: Int, y: Int, value: Int) {
        internalMap[y * width + x] = value
    }

    fun get(x: Int, y: Int): Int {
        return internalMap[y * width + x]
    }
}
