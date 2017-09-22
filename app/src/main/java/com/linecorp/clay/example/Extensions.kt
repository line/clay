/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.example

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import java.io.File
import java.io.FileOutputStream

fun Context.createTempImageFile(bitmap: Bitmap): File {
    return File(filesDir, "temp.webp").apply {
        FileOutputStream(absolutePath).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.WEBP, 90, outputStream)
        }
    }
}

fun Activity.screenDimension(): Point {
    return Point().apply { windowManager.defaultDisplay.getSize(this) }
}
