/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.transform

import android.graphics.PointF

/**
 * The interface of Transformer
 */
interface Transformer<in T> {
    /**
     * Do transform by touch points
     *
     * @param target the target to transform
     * @param points the list of touch points
     */
    fun transform(target: T, points: List<PointF>)
}
