/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.example

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewManager
import android.widget.LinearLayout
import com.linecorp.clay.example.utils.resampleBitmap
import com.linecorp.clay.view.ClayView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.listeners.onCheckedChange
import org.jetbrains.anko.sdk25.listeners.onClick
import java.io.File
import java.io.FileOutputStream

class MainAnkoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUri = intent.getParcelableExtra<Uri>(Constants.KEY_IMAGE_URI)
        doAsync {
            val imagePath = checkImageSource(imageUri)
            uiThread {
                MainAnkoActivityUi(imagePath).setContentView(this@MainAnkoActivity)
            }
        }
    }

    fun deleteTempFileIfExist() {
        File(filesDir, TEMP_CLOUD_IMAGE_FILENAME).apply {
            if (exists()) { delete() }
        }
    }

    private fun checkImageSource(imageUri: Uri): String {
        var imagePath = ""
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION)
        contentResolver.query(imageUri, columns, null, null, null).use { cursor ->
            cursor.moveToFirst()
            imagePath = cursor.getString(cursor.getColumnIndex(columns[0])) ?: run {
                return if (imageUri.toString().startsWith("content://com.google.android.apps.photos.content")) {
                    writeCloudImageToFile(imageUri)
                } else {
                    Log.e(TAG, "The image doesn't come from Google Photo, "
                            + "need to add the support for $imageUri")
                    ""
                }
            }
        }

        return imagePath
    }

    private fun writeCloudImageToFile(uri: Uri): String {
        val tempImageFile = File(filesDir, TEMP_CLOUD_IMAGE_FILENAME)
        val buf = ByteArray(4096) //4k
        val inputStream = contentResolver.openInputStream(uri)
        FileOutputStream(tempImageFile.absolutePath).use { outStream ->
            while (inputStream.read(buf) > 0) { outStream.write(buf) }
        }
        return tempImageFile.absolutePath
    }

    companion object {
        private const val TAG = "MainAnkoActivity"
        private const val TEMP_CLOUD_IMAGE_FILENAME = "temp_cloud_image"
    }
}

class MainAnkoActivityUi(val imagePath: String) : AnkoComponent<MainAnkoActivity> {
    lateinit var clayView: ClayView

    override fun createView(ui: AnkoContext<MainAnkoActivity>) = with(ui) {
        relativeLayout {
            backgroundColor = ctx.resources.getColor(R.color.grey_777777)
            clayView = clayEditView {
                pathStrokeWidth = dip(6).toFloat()
                endPointRadius = dip(20).toFloat()
                controlPointColor = ui.ctx.resources.getColor(android.R.color.holo_green_dark)
                pathStrokeColor = ui.ctx.resources.getColor(android.R.color.white)
                doAsync {
                    val screenDimension = owner.screenDimension()
                    val bitmap = resampleBitmap(imagePath, screenDimension.x, screenDimension.y)
                    owner.deleteTempFileIfExist()
                    uiThread {
                        this@clayEditView.bitmap = bitmap
                    }
                }
            }.lparams(width = matchParent, height = matchParent)

            linearLayout {
                orientation = LinearLayout.HORIZONTAL
                button {
                    textResource = R.string.undo
                    onClick {
                        clayView.undoSelect()
                    }
                }
                button {
                    textResource = R.string.trim
                    onClick {
                        displayCroppedImage(ui)
                    }
                }
                checkBox {
                    textResource = R.string.show_path
                    onCheckedChange { _, isChecked ->
                        clayView.drawPathOnCroppedImage = isChecked
                    }
                }
            }.lparams {
                alignParentBottom()
                centerHorizontally()
            }
        }
    }

    private fun displayCroppedImage(ui: AnkoContext<MainAnkoActivity>) {
        clayView.getCroppedImage(antiAlias = true)?.let { trimmedImage ->
            doAsync {
                val tempFile = ui.owner.createTempImageFile(trimmedImage)
                uiThread {
                    ui.startActivity<ImageDisplayActivity>(
                            Constants.KEY_IMAGE_PATH to tempFile.absolutePath)
                }
            }
        }
    }
}

inline fun ViewManager.clayEditView(theme: Int = 0, init: ClayView.() -> Unit) =
        ankoView(::ClayView, theme, init)


