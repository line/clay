/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view.state

import android.graphics.PointF

internal class BeginTouch(touchedPoint: PointF) : EditState<PointF>(PointF(touchedPoint.x, touchedPoint.y))
