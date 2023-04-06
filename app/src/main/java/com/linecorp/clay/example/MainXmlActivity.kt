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
