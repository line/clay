/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.example

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewManager
import android.widget.LinearLayout
import com.linecorp.clay.example.utils.resampleBitmap
import com.linecorp.clay.view.ClayView
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView
import org.jetbrains.anko.sdk25.listeners.onClick

class MainAnkoActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageUri = intent.getParcelableExtra<Uri>(Constants.KEY_IMAGE_URI)
        val columns = arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION)
        contentResolver.query(imageUri, columns, null, null, null).use { cursor ->
            cursor.moveToFirst()
            val imagePath = cursor.getString(cursor.getColumnIndex(columns[0]))
            MainAnkoActivityUi(imagePath).setContentView(this)
        }
    }
}

class MainAnkoActivityUi(val imagePath: String) : AnkoComponent<MainAnkoActivity> {
    lateinit var mView: ClayView

    override fun createView(ui: AnkoContext<MainAnkoActivity>) = with(ui) {
        relativeLayout {
            backgroundColor = ctx.resources.getColor(R.color.grey_777777)
            mView = clayEditView {
                pathStrokeWidth = dip(6).toFloat()
                endPointRadius = dip(20).toFloat()
                controlPointColor = ui.ctx.resources.getColor(android.R.color.holo_red_dark)
                pathStrokeColor = ui.ctx.resources.getColor(R.color.orange_ffcc80)
                doAsync {
                    val screenDimension = owner.screenDimension()
                    val bitmap = resampleBitmap(imagePath, screenDimension.x, screenDimension.y)
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
                        mView.undoSelect()
                    }
                }
                button {
                    textResource = R.string.trim
                    onClick {
                        displayCroppedImage(ui)
                    }
                }
            }.lparams {
                alignParentBottom()
                centerHorizontally()
            }
        }
    }

    private fun displayCroppedImage(ui: AnkoContext<MainAnkoActivity>) {
        mView.getCroppedImage(antiAlias = true)?.let { trimmedImage ->
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


