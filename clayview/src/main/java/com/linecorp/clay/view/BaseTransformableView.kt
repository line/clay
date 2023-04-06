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

package com.linecorp.clay.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.ImageView
import com.linecorp.clay.graphic.getAutoFitScale
import com.linecorp.clay.view.state.EditState
import com.linecorp.clay.view.state.NoTouch

/**
 * The base class of matrix-based image view
 */
open class BaseTransformableView : ImageView {

    //backing properties
    @Volatile
    private var _bitmap: Bitmap? = null

    /**
     * The bitmap data for the image
     */
    var bitmap: Bitmap?
        set(value) {
            this._bitmap = value
            super.setImageBitmap(value)
            value?.let { newBitmap ->
                onBitmapUpdate(newBitmap)
            }
        }
        get() = this._bitmap

    /**
     * The base matrix
     */
    protected val baseMatrix = Matrix()

    /**
     * The transform matrix which reflects zooming, rotation and panning
     */
    protected var transformMatrix = Matrix()

    /**
     * The display matrix, it is the final matrix that maps to actual dimension.
     * It is calculated by transformMatrix * baseMatrix
     */
    protected val theDisplayMatrix = Matrix()

    protected var offScreenBitmap: Bitmap? = null
    protected var offScreenCanvas: Canvas? = null

    /**
     * The internal editing state, begins from NoTouch
     */
    protected var editingState: EditState<Any> = NoTouch()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        scaleType = ScaleType.MATRIX
    }

    open protected fun onBitmapUpdate(bitmap: Bitmap) {

    }

    //Overwrite ImageView.setImageBitmap to redirect to bitmap property
    override fun setImageBitmap(imageBitmap: Bitmap?) {
        this.bitmap = imageBitmap
    }

    /**
     * reset all matrix
     */
    protected fun resetMatrix() {
        baseMatrix.reset()
        transformMatrix.reset()
        theDisplayMatrix.reset()
    }

    /**
     * Create the offscreen bitmap and canvas
     */
    protected fun createOffScreen(width: Int, height: Int) {
        offScreenBitmap?.recycle()
        offScreenBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        offScreenCanvas = Canvas(offScreenBitmap)
        resetOffScreen()
    }

    /**
     * Reset the offscreen canvas
     */
    protected fun resetOffScreen() {
        offScreenCanvas?.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    }

    /**
     * Calculate the base matrix
     *
     * @param bitmapWidth the width of the bitmap
     * @param bitmapHeight the height of the bitmap
     */
    protected fun calculateBaseMatrix(bitmapWidth: Int, bitmapHeight: Int) {
        //Some images look a little small on modern devices, so try to display 2x at least to trim easier
        //It can also avoid that the image which comes from camera looks smaller because of resampling
        val expectedScale = Math.min(Math.sqrt(2.0).toFloat(),
                                     getAutoFitScale(bitmapWidth, bitmapHeight, width, height))
        val newBitmapHeight = bitmapHeight * expectedScale
        val newBitmapWidth = bitmapWidth * expectedScale
        val deltaX = (width - newBitmapWidth) / 2
        val deltaY = (height - newBitmapHeight) / 2
        baseMatrix.postScale(expectedScale, expectedScale)
        baseMatrix.postTranslate(deltaX, deltaY)
    }

    /**
     * Get the display matrix
     */
    protected fun getDisplayMatrix(): Matrix {
        theDisplayMatrix.set(baseMatrix)
        theDisplayMatrix.postConcat(transformMatrix)
        return theDisplayMatrix
    }
}
