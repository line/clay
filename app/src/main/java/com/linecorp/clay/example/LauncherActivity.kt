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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.linecorp.clay.example.utils.pickImageIntent
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class LauncherActivity : Activity() {

    lateinit var useXmlButton: Button
    lateinit var useAnkoButton: Button

    companion object {
        const val GALLERY_PIC_FOR_ANKO = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        useAnkoButton = findViewById(R.id.use_anko) as Button
        useXmlButton = findViewById(R.id.use_xml) as Button

        useAnkoButton.setOnClickListener {
            LauncherActivityPermissionsDispatcher.choosePhotoWithCheck(this)
        }

        useXmlButton.setOnClickListener {
            startActivity(Intent(this, MainXmlActivity::class.java))
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    fun choosePhoto() {
        startActivityForResult(pickImageIntent(), GALLERY_PIC_FOR_ANKO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray?) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LauncherActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            GALLERY_PIC_FOR_ANKO -> {
                checkNotNull(data)
                data?.data?.let { imageUri ->
                    startActivity(Intent(this, MainAnkoActivity::class.java).apply {
                        putExtra(Constants.KEY_IMAGE_URI, imageUri)
                    })
                }
            }
        }
    }
}
