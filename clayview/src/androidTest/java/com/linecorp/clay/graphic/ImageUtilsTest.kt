/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic

import android.graphics.*
import org.junit.Test

import android.support.test.runner.AndroidJUnit4

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImageProcessUtilsTest {

    @Test
    @Throws(Exception::class)
    fun testInvertMatrix() {
        val matrix = Matrix()
        matrix.setScale(2f, 2f)
        var points = FloatArray(9)

        val invertedMatrix = getInvertMatrix(matrix)
        invertedMatrix.getValues(points)
        assertThat(points[0], equalTo(0.5f))
        assertThat(points[4], equalTo(0.5f))
    }

    @Test
    @Throws(Exception::class)
    fun testMappedRect() {
        val matrix = Matrix()
        val rect = Rect(100, 200, 200, 300)
        matrix.setScale(2f, 2f)
        val mappedRect = getMappedRect(rect, matrix)
        assertThat(mappedRect.left, equalTo(200))
        assertThat(mappedRect.top, equalTo(400))
        assertThat(mappedRect.width(), equalTo(200))
        assertThat(mappedRect.height(), equalTo(200))
    }

    @Test
    @Throws(Exception::class)
    fun testPathBounds() {
        var path = createExamplePath(100f, 100f)
        val pathRect = getPathBounds(path)

        assertThat(pathRect.left, equalTo(0))
        assertThat(pathRect.top, equalTo(0))
        assertThat(pathRect.right, equalTo(100))
        assertThat(pathRect.bottom, equalTo(100))
    }

    @Test
    @Throws(Exception::class)
    fun testCropImage() {
        val bitmap = createSampleGradientBitmap()
        val path = createExamplePath(50f, 50f)

        val croppedImage = Bitmap.createBitmap(bitmap, 0, 0, 50, 50)
        val croppedImageFromPath = cropImage(bitmap, path, false)
        assertThat(croppedImage.sameAs(croppedImageFromPath), equalTo(true))
    }

    @Test
    @Throws(Exception::class)
    fun testPathArea() {
        val path = createExamplePath(100f, 100f)
        val area = getPathBoundsArea(path)

        assertThat(area, equalTo(100 * 100))
    }

    @Test
    @Throws(Exception::class)
    fun testScale() {
        val matrix = Matrix()
        matrix.postScale(2f, 3f)
        matrix.postTranslate(5f, 10f)
        val (scaleX, scaleY) =  getCurrentScale(matrix)
        assertThat(scaleX, equalTo(2f))
        assertThat(scaleY, equalTo(3f))
    }

    @Test
    @Throws(Exception::class)
    fun testDistance() {
        val theDistance = distance(PointF(0f, 10f), PointF(10f, 0f))
        assertThat(theDistance, equalTo(Math.sqrt(10.toDouble() * 10 + 10 * 10).toFloat()))
    }

    @Test
    @Throws(Exception::class)
    fun testCenterPoint() {
        val centerPoint = centerPoint(PointF(0f, 10f), PointF(10f, 0f))
        assertThat(centerPoint.x, equalTo(5f))
        assertThat(centerPoint.y, equalTo(5f))
    }

    @Test
    @Throws(Exception::class)
    fun testAngle() {
        val angle1 = calculateAngleForRotation(PointF(10f, 0f), PointF(0f, 0f))
        val angle2 = calculateAngleForRotation(PointF(-10f, 0f), PointF(0f, 0f))
        val angle3 = calculateAngleForRotation(PointF(0f, 10f), PointF(0f, 0f))
        val angle4 = calculateAngleForRotation(PointF(0f, -10f), PointF(0f, 0f))

        val angle5 = calculateAngleForRotation(PointF(10f, 10f), PointF(0f, 0f))
        val angle6 = calculateAngleForRotation(PointF(-10f, 10f), PointF(0f, 0f))
        val angle7 = calculateAngleForRotation(PointF(-10f, -10f), PointF(0f, 0f))
        val angle8 = calculateAngleForRotation(PointF(10f, -10f), PointF(0f, 0f))

        assertThat(angle1, equalTo(0.0))
        assertThat(angle2, equalTo(-0.0))
        assertThat(angle3, equalTo(90.0))
        assertThat(angle4, equalTo(-90.0))

        assertThat(Math.abs(angle5 - 45.0), lessThan(0.0001))
        assertThat(Math.abs(angle6 - -45.0), lessThan(0.0001))
        assertThat(Math.abs(angle7 - 45.0), lessThan(0.0001))
        assertThat(Math.abs(angle8 - -45.0), lessThan(0.0001))
    }

    @Test
    @Throws(Exception::class)
    fun testSampleSize() {
        val options = BitmapFactory.Options()
        options.outHeight = 8000
        options.outWidth = 8000
        var sampleSize = calculateBitmapSampleSize(options, 1080, 1920)
        assertThat(sampleSize, equalTo(8))

        options.outHeight = 1500
        options.outWidth = 4000
        sampleSize = calculateBitmapSampleSize(options, 1080, 1920)
        assertThat(sampleSize, equalTo(4))

        options.outHeight = 4530
        options.outWidth = 3280
        sampleSize = calculateBitmapSampleSize(options, 1080, 1920)
        assertThat(sampleSize, equalTo(4))

        options.outHeight = 1200
        options.outWidth = 2000
        sampleSize = calculateBitmapSampleSize(options, 1080, 1920)
        assertThat(sampleSize, equalTo(2))
    }

    @Test
    fun testAutoFitScale() {
        val scale1 = getAutoFitScale(2000, 2000, 500, 500)
        assertThat(scale1, equalTo(0.25f))

        val scale2 = getAutoFitScale(2000, 1000, 500, 500)
        assertThat(scale2, equalTo(0.25f))

        val scale3 = getAutoFitScale(1000, 2000, 500, 1000)
        assertThat(scale3, equalTo(0.5f))
    }

    @Test
    fun testAllTransparentBitmap() {
        val bitmap1 = createSampleGradientBitmap()
        val result1 = isAllTransparentBitmap(bitmap1)

        val bitmap2 = createSampleTransparentBitmap()
        val result2 = isAllTransparentBitmap(bitmap2)

        val bitmap3 = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565)
        val result3 = isAllTransparentBitmap(bitmap3)

        assertFalse(result1)
        assertTrue(result2)
        assertFalse(result3)
    }

    private fun createSampleTransparentBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        return bitmap
    }

    private fun createSampleGradientBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val gradient = LinearGradient(0f, 0f, 100f, 100f, Color.RED, Color.GREEN, Shader.TileMode.CLAMP)
        paint.shader = gradient
        canvas.drawRect(Rect(0, 0, 100, 100), paint)
        return bitmap
    }

    private fun createExamplePath(width: Float, height: Float): Path {
        val path = Path()
        path.moveTo(0f, 0f)
        path.lineTo(width, 0f)
        path.lineTo(width, height)
        path.lineTo(0f, height)
        path.lineTo(0f, 0f)
        return path
    }
}
