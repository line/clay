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
