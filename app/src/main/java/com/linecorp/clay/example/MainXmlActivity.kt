/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.example

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox

import com.linecorp.clay.view.ClayView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.sdk25.listeners.onCheckedChange
import org.jetbrains.anko.uiThread

class MainXmlActivity : Activity() {

    lateinit var clayView: ClayView
    lateinit var trimButton: Button
    lateinit var undoButton: Button
    lateinit var showPathAfterTrimmigCheck: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clayView = findViewById(R.id.clay) as ClayView
        undoButton = findViewById(R.id.undo) as Button
        trimButton = findViewById(R.id.trim) as Button
        showPathAfterTrimmigCheck = findViewById(R.id.show_path) as CheckBox

        undoButton.setOnClickListener {
            clayView.undoSelect()
        }

        trimButton.setOnClickListener {
            clayView.getCroppedImage(antiAlias = true)?.let { croppedImage ->
                loadCroppedImageAsync(croppedImage)
            }
        }

        showPathAfterTrimmigCheck.onCheckedChange { _, isChecked ->
            clayView.drawPathOnCroppedImage = isChecked
        }
    }

    private fun loadCroppedImageAsync(image: Bitmap) {
        doAsync {
            val tempFile = createTempImageFile(image)
            uiThread {
                startActivity(Intent(this@MainXmlActivity, ImageDisplayActivity::class.java).apply {
                    putExtra(Constants.KEY_IMAGE_PATH, tempFile.absolutePath)
                })
            }
        }
    }
}
