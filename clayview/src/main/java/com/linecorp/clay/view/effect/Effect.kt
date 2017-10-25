package com.linecorp.clay.view.effect

import android.graphics.Bitmap

/**
 * The Effect is used to add additional effect on the image
 */
interface Effect {
    /**
     * Apply the effect
     *
     * @param bitmap The source bitmap,
     */
    fun applyTo(bitmap: Bitmap)
}
