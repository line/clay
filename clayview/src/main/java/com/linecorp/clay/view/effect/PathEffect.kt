package com.linecorp.clay.view.effect

import android.graphics.Bitmap
import android.graphics.Canvas
import com.linecorp.clay.drawable.PathDrawable

/**
 * The effect which draw a path on the source bitmap
 */
internal class PathEffect(val pathDrawable: PathDrawable) : Effect {
    /**
     * Draw the path on the bitmap
     *
     * @param bitmap The source bitmap,
     */
    override fun applyTo(bitmap: Bitmap) {
        val canvas = Canvas(bitmap)
        pathDrawable.draw(canvas)
    }
}
