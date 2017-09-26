/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic

import android.graphics.*
import android.util.Log

const private val ANGLE = 180 / Math.PI
const private val TAG = "ImageProcessUtil"

/**
 * Get invert matrix
 * @param matrix source matrix
 * @return invert matrix
 */
fun getInvertMatrix(matrix: Matrix): Matrix {
    val invertMatrix = Matrix()
    matrix.invert(invertMatrix)
    return invertMatrix
}

/**
 * Get mapped rect by matrix
 * @param srcRect Source rect
 * @param matrix Matrix for the target rect
 * @return new rect by the matrix
 */
fun getMappedRect(srcRect: Rect, matrix: Matrix): Rect {
    val dstRectF = RectF(srcRect)
    val dstRect = Rect()
    matrix.mapRect(dstRectF)
    dstRectF.round(dstRect)
    return dstRect
}

/**
 * Get path bounds
 * @param path Path
 * @return The rect of this path
 */
fun getPathBounds(path: Path): Rect {
    val selectedRectF = RectF()
    val selectedRect = Rect()
    path.computeBounds(selectedRectF, true)
    selectedRectF.round(selectedRect)
    return selectedRect
}

/**
 * Get bound area of the path
 * @param path Path
 * @return the area of this path
 */
fun getPathBoundsArea(path: Path): Int {
    val rect = getPathBounds(path)
    return rect.width() * rect.height()
}

/**
 * Get crop image by path
 * @param source Source bitmap
 * @param path Path
 * @param validRect The valid rect. The outside of valid rect would not be cropped even it is inside the path
 * @param antiAlias enable antiAlias if set true
 * @return New cropped bitmap.
 */
fun cropImage(source: Bitmap, path: Path, antiAlias: Boolean): Bitmap {
    val selectedRectOnBitmap = getPathBounds(path)
    val selectedRectWidth = Math.min(selectedRectOnBitmap.width(), source.width)
    val selectedRectHeight = Math.min(selectedRectOnBitmap.height(), source.height)
    selectedRectOnBitmap.left = Math.max(selectedRectOnBitmap.left, 0)
    selectedRectOnBitmap.top = Math.max(selectedRectOnBitmap.top, 0)
    selectedRectOnBitmap.right = Math.min(selectedRectOnBitmap.left + selectedRectWidth, source.width)
    selectedRectOnBitmap.bottom = Math.min(selectedRectOnBitmap.top + selectedRectHeight, source.height)
    val croppedDstImage = Bitmap.createBitmap(selectedRectOnBitmap.width(),
                                              selectedRectOnBitmap.height(),
                                              Bitmap.Config.ARGB_8888)
    val canvas = Canvas(croppedDstImage)
    val pathPaint = Paint()
    if (antiAlias) {
        pathPaint.flags = Paint.ANTI_ALIAS_FLAG
    }
    canvas.translate(-selectedRectOnBitmap.left.toFloat(), -selectedRectOnBitmap.top.toFloat())
    canvas.drawPath(path, pathPaint)
    pathPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(source, selectedRectOnBitmap, selectedRectOnBitmap, pathPaint)
    return croppedDstImage
}

/**
 * Get current scale in a matrix
 * @param matrix
 * @return scale pair (scaleX, scaleY)
 */
fun getCurrentScale(matrix: Matrix): Pair<Float, Float> {
    val points = FloatArray(9)
    matrix.getValues(points)

    return Pair(points[Matrix.MSCALE_X], points[Matrix.MSCALE_Y])
}

/**
 * Get the distance between two points
 * @param point1 Point1
 * @param point2 Point2
 * @return distance
 */
fun distance(point1: PointF, point2: PointF): Float {
    val dx = (point2.x - point1.x).toDouble()
    val dy = (point2.y - point1.y)

    return Math.sqrt(dx * dx + dy * dy).toFloat()
}

/**
 * Get the center point of 2 points
 * @param point1 Point1
 * @param point2 Point2
 * @return center point
 */
fun centerPoint(point1: PointF, point2: PointF): PointF {
    return PointF((point1.x + point2.x) / 2, (point1.y + point2.y) / 2)
}

/**
 * Calculate the angle between pivot and target point.
 * if counter-clockwise, return value is positive, otherwise the return value is negative
 *
 * @param point target point
 * @param pivot pivot point
 * @return angle between -90 ~ 90
 */
fun calculateAngleForRotation(point: PointF, pivot: PointF): Double {
    var sin = (point.y - pivot.y) / distance(pivot, point).toDouble()
    if (point.x < pivot.x) {
        sin = -sin
    }

    val angle = Math.asin(sin) * ANGLE
    return angle
}

/**
 * Calculate the best sample size of bitmap by the target width and height
 * @param options bitmap options
 * @param targetWidth target width
 * @param targetHeight target height
 * @return sample size, which is powers of 2
 */
fun calculateBitmapSampleSize(options: BitmapFactory.Options,
                              targetWidth: Int,
                              targetHeight: Int): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    //consider the padding
    val autoFitScale = getAutoFitScale(width, height, targetWidth, targetHeight)
    val newTargetHeight = height * autoFitScale
    val newTargetWidth = width * autoFitScale
    //sample size has to be powers of 2
    var inSampleSize = 1
    /*
     *  rule: if the area is greater than 2 * target area, increase sample size
     */
    while ((height / (inSampleSize * Math.sqrt(2.0))) >= newTargetHeight
            && (width / (inSampleSize * Math.sqrt(2.0))) >= newTargetWidth) {
        inSampleSize *= 2
    }

    Log.i(TAG, "Best the sample size of ($width, $height) for ($targetWidth, $targetHeight) is $inSampleSize")
    return inSampleSize
}

/**
 * Calculate the scale of auto-fit for base resolution to target resolution
 * @param baseWidth base width
 * @param baseHeight base height
 * @param targetWidth target width
 * @param targetHeight target height
 * @return target scale
 */
fun getAutoFitScale(baseWidth: Int, baseHeight: Int,
                    targetWidth: Int, targetHeight: Int): Float {
    val xScale: Float = targetWidth / baseWidth.toFloat()
    val yScale: Float = targetHeight / baseHeight.toFloat()
    return Math.min(xScale, yScale)
}

/**
 * Check if the bitmap is an all-transparent or all-white bitmap. This bitmap must be ARGB 8888
 *
 * @param bitmap The image bitmap to check
 * @throws IllegalArgumentException if the bitmap is not ARGB 8888
 */
fun isAllTransparentBitmap(bitmap: Bitmap): Boolean {
    val resizeBitmap: Bitmap
    val resampleResolution = 64
    if (bitmap.config != Bitmap.Config.ARGB_8888) {
        return false
    }

    //Considering the performance,
    //To resample it to small bitmap then check the pixels would have better performance than iterating all pixels directly
    if (bitmap.width >= resampleResolution && bitmap.height >= resampleResolution) {
        resizeBitmap = Bitmap.createScaledBitmap(bitmap, resampleResolution, resampleResolution, true)
    } else {
        resizeBitmap = bitmap
    }

    val pixels = IntArray(resizeBitmap.width * resizeBitmap.height)
    resizeBitmap.getPixels(pixels, 0, resizeBitmap.width, 0, 0, resizeBitmap.width, resizeBitmap.height)
    for (pixel in pixels) {
        if ((pixel and 0xFF000000.toInt()) != 0) {
            return false
        }
    }
    return true
}
