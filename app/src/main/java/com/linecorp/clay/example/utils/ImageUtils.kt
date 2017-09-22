/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.example.utils

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.linecorp.clay.graphic.calculateBitmapSampleSize

fun pickImageIntent() = Intent(Intent.ACTION_PICK).apply {
    type = "image/*"
}

fun resampleBitmap(imagePath: String, width: Int, height: Int): Bitmap? {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeFile(imagePath, options)
    options.inSampleSize = calculateBitmapSampleSize(options, width, height)
    options.inJustDecodeBounds = false
    return BitmapFactory.decodeFile(imagePath, options)
}
