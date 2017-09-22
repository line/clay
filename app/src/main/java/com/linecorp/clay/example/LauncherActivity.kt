/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
