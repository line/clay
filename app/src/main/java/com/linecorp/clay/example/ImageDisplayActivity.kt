/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
