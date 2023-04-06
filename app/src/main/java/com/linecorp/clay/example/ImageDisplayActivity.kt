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

package com.linecorp.clay.example

import android.app.Activity
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import org.jetbrains.anko.*

class ImageDisplayActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imagePath = intent.getStringExtra(Constants.KEY_IMAGE_PATH)
        ImageDisplayActivityUi(imagePath).setContentView(this)
    }
}

class ImageDisplayActivityUi(val imagePath: String) : AnkoComponent<ImageDisplayActivity> {
    lateinit var trimImageView: ImageView
    override fun createView(ui: AnkoContext<ImageDisplayActivity>) = with(ui) {
        relativeLayout {
            backgroundColor = ctx.resources.getColor(R.color.grey_777777)
            trimImageView = imageView {
                doAsync {
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    uiThread {
                        imageBitmap = bitmap
                    }
                }
            }.lparams(width = 720, height = 1280) {
                centerInParent()
            }
        }
    }
}
