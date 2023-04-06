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
import android.util.Log
import com.linecorp.clay.Style
import com.linecorp.clay.graphic.CatmullRomInterpolator
import com.linecorp.clay.graphic.PathInterpolator
import com.linecorp.clay.graphic.distance
import com.linecorp.clay.graphic.getCurrentScale
import com.linecorp.clay.graphic.model.EdgeMap
import java.util.ArrayList

internal class DrawingPath(val context: Context, displayMatrix: Matrix, var edgeMap: EdgeMap? = null) : Cloneable {
    val pointCount: Int
        get() = internalPoints.count()

    val controlPoints: ArrayList<PointF>
        get() = createMappedPoints(source = internalControlPoints)

    //the control point for path mold, it only has values after path closed
    private val internalControlPoints = ArrayList<PointF>()
    private val internalPoints = ArrayList<PointF>()

    //the path for display
    val path: Path = Path()

    val startPoint: PointF?
        get() {
            return if (internalStartPoint == null) null
            else createMappedPoint(internalStartPoint!!.x, internalStartPoint!!.y)
        }

    val endPoint: PointF?
        get() {
            return if (internalEndPoint == null) null
            else createMappedPoint(internalEndPoint!!.x, internalEndPoint!!.y)
        }

    val isEmpty: Boolean
        get() = internalPath.isEmpty && internalPoints.isEmpty()

    val length: Float
        get() {
            return PathMeasure(internalPath, false).length
        }

    val minDistanceBetweenTwoPoints: Float = Style.DEFAULT_MIN_DISTANCE_BETWEEN_TWO_POINTS *
            context.resources.displayMetrics.density

    var displayMatrix: Matrix
        set(value) {
            theDisplayMatrix.set(value)
            theDisplayMatrix.invert(invertMatrix)
            path.set(internalPath)
            path.transform(displayMatrix)
        }
        get() = theDisplayMatrix

    var isClosed: Boolean = false
        private set

    //create internal path for *REAL* coordinate,
    //So we can use it to re-calculate the path after display matrix changed.
    private val internalPath = Path()
    //TODO: use internalPoints to get the startPoint and endPoint later
    private var internalStartPoint: PointF? = null
    private var internalEndPoint: PointF? = null
    private var theDisplayMatrix = Matrix()
    private val invertMatrix = Matrix()

    private var pathInterpolator: PathInterpolator? = null

    init {
        this.displayMatrix = displayMatrix
    }

    fun set(drawingPath: DrawingPath) {
        Log.d(TAG, "set new drawing internalPath")
        internalPath.set(drawingPath.internalPath)
        path.set(drawingPath.path)
        internalPoints.clear()
        internalPoints.addAll(drawingPath.internalPoints)

        internalControlPoints.clear()
        internalControlPoints.addAll(drawingPath.internalControlPoints)

        pathInterpolator = drawingPath.pathInterpolator

        if (drawingPath.internalStartPoint == null) {
            internalStartPoint = null
        } else {
            internalStartPoint?.set(drawingPath.internalStartPoint!!.x, drawingPath.internalStartPoint!!.y)
        }

        if (drawingPath.internalEndPoint == null) {
            internalEndPoint = null
        } else {
            internalEndPoint?.set(drawingPath.internalEndPoint!!.x, drawingPath.internalEndPoint!!.y)
        }
        isClosed = drawingPath.isClosed
        this.edgeMap = drawingPath.edgeMap
    }

    override public fun clone(): DrawingPath {
        val original = this
        return DrawingPath(context, displayMatrix).apply {
            internalPath.set(original.internalPath)
            path.set(original.path)
            internalPoints.addAll(original.internalPoints)
            internalControlPoints.addAll(original.internalControlPoints)
            original.internalStartPoint?.let { startPoint ->
                this.internalStartPoint = PointF(startPoint.x, startPoint.y)
            }
            original.internalEndPoint?.let { endPoint ->
                this.internalEndPoint = PointF(endPoint.x, endPoint.y)
            }
            isClosed = original.isClosed
        }
    }

    fun reset() {
        Log.d(TAG, "reset drawing internalPath")
        internalPoints.clear()
        internalControlPoints.clear()
        internalPath.reset()
        path.reset()
        internalStartPoint = null
        internalEndPoint = null
        isClosed = false
        pathInterpolator = null
        edgeMap = null
    }

    /**
     * Add a line from the last point to the specified point (x,y).
     * If no moveTo() call has been made for this contour, the first point is
     * automatically set to (0,0).
     * The point would be inverted-mapped to actual point by the display matrix
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    fun lineTo(x: Float, y: Float) {
        val mappedPoint = invertMapPoint(x, y)
        val internalX = mappedPoint[0]
        val internalY = mappedPoint[1]
        Log.d(TAG, "lineTo, from display point(($x, $y), to internal point ($internalX, $internalY)")
        internalPath.lineTo(internalX, internalY)
        path.lineTo(x, y)
        internalPoints.add(PointF(internalX, internalY))
        internalEndPoint?.set(internalX, internalY)
    }

    /**
     * Set the beginning of the next contour to the point (x,y).
     * The point would be inverted-mapped to actual point by the display matrix
     *
     * @param x x-coordinate
     * @param y y-coordinate
     */
    fun moveTo(x: Float, y: Float) {
        val mappedPoint = invertMapPoint(x, y)
        val internalX = mappedPoint[0]
        val internalY = mappedPoint[1]
        Log.d(TAG, "moveTo, from display point ($x, $y), to internal point ($internalX, $internalY)")
        internalPath.moveTo(internalX, internalY)
        path.moveTo(x, y)
        val point = PointF(internalX, internalY)
        if (internalStartPoint == null) {
            //create new point instance for start point
            internalStartPoint = PointF(internalX, internalY)
        }
        internalPoints.add(point)
        internalEndPoint = PointF(point.x, point.y)
    }

    /**
     * Append a smooth line to (x, y) by using quadTo
     *
     * @param x end point x
     * @param y end point y
     */
    fun smoothTo(x: Float, y: Float, checkMinDistance: Boolean = true) {
        if (internalPoints.isNotEmpty()) {
            val lastPoint = internalPoints.last()
            val displayLastPoint = createMappedPoint(lastPoint.x, lastPoint.y)
            quadTo(displayLastPoint.x, displayLastPoint.y,
                   (x + displayLastPoint.x) / 2, (y + displayLastPoint.y) / 2, checkMinDistance)
        } else {
            quadTo(0f, 0f, x / 2, y / 2, checkMinDistance)
        }
    }

    /**
     * Add a quadratic bezier from the last point, approaching control point
     * (x1,y1), and ending at (x2,y2). If no moveTo() call has been made for
     * this contour, the first point is automatically set to (0,0). The internalPoints would be inverted-mapped to
     * actual internalPoints by the display matrix.
     *
     * @param x1 The x-coordinate of the control point on a quadratic curve
     * @param y1 The y-coordinate of the control point on a quadratic curve
     * @param x2 The x-coordinate of the end point on a quadratic curve
     * @param y2 The y-coordinate of the end point on a quadratic curve
     */
    private fun quadTo(x1: Float, y1: Float, x2: Float, y2: Float, checkMinDistance: Boolean) {
        val mappedControlPoint = invertMapPoint(x1, y1)
        val internalControlX = mappedControlPoint[0]
        val internalControlY = mappedControlPoint[1]
        val mappedPoint = invertMapPoint(x2, y2)
        val internalX = mappedPoint[0]
        val internalY = mappedPoint[1]
        val newPoint = PointF(internalX, internalY)
        val currentScale = getCurrentScale(displayMatrix)
        if (!checkMinDistance
                || internalPoints.isEmpty()
                || distance(internalPoints.last(), newPoint) >= minDistanceBetweenTwoPoints / currentScale.first) {
            Log.d(TAG, "quadTo, from display control point($x1, $y1), target point($x2, $y2), " +
                             "to internal control point ($internalControlX, $internalControlY), " +
                             "target point ($internalX, $internalY)")
            internalPath.quadTo(internalControlX, internalControlY, internalX, internalY)
            path.quadTo(x1, y1, x2, y2)
            internalPoints.add(newPoint)
        }
        internalEndPoint?.set(internalX, internalY)
    }

    /**
     * Add a cubic bezier from the last point, approaching 2 control internalPoints
     * and ending point. If no moveTo() call has been
     * made for this contour, the first point is automatically set to (0,0).
     * The internalPoints would be inverted-mapped to actual internalPoints by the display matrix.
     *
     * @param controlPoint1X The x-coordinate of the 1st control point on a cubic curve
     * @param controlPoint1Y The y-coordinate of the 1st control point on a cubic curve
     * @param controlPoint2X The x-coordinate of the 2nd control point on a cubic curve
     * @param controlPoint2Y The y-coordinate of the 2nd control point on a cubic curve
     * @param endPointX The x-coordinate of the end point on a cubic curve
     * @param endPointY The y-coordinate of the end point on a cubic curve
     */
    private fun cubicTo(controlPoint1X: Float, controlPoint1Y: Float,
                controlPoint2X: Float, controlPoint2Y: Float,
                endPointX: Float, endPointY: Float) {
        val mappedControlPoint1 = invertMapPoint(controlPoint1X, controlPoint1Y)
        val internalControlPoint1X = mappedControlPoint1[0]
        val internalControlPoint1Y = mappedControlPoint1[1]

        val mappedControlPoint2 = invertMapPoint(controlPoint2X, controlPoint2Y)
        val internalControlPoint2X = mappedControlPoint2[0]
        val internalControlPoint2Y = mappedControlPoint2[1]

        val mappedEndPoint = invertMapPoint(endPointX, endPointY)
        val internalEndPointX = mappedEndPoint[0]
        val internalEndPointY = mappedEndPoint[1]

        internalPath.cubicTo(internalControlPoint1X, internalControlPoint1Y,
                             internalControlPoint2X, internalControlPoint2Y,
                             internalEndPointX, internalEndPointY)
        path.cubicTo(controlPoint1X, controlPoint1Y,
                     controlPoint2X, controlPoint2Y,
                     endPointX, endPointY)

        internalEndPoint?.set(endPointX, endPointY)
    }

    /**
     * close the path
     */
    fun close() {
        if (internalPoints.count() <= 1) {
            Log.w(TAG, "Path could not be closed because it contains only one point")
            return
        }
        val startPoint = internalPoints[0]
        internalEndPoint?.set(startPoint.x, startPoint.y)
        val displayStartPoint = createMappedPoint(startPoint.x, startPoint.y)
        smoothTo(displayStartPoint.x, displayStartPoint.y)
        isClosed = true

        setupControlPoints()
        smoothify()

        Log.d(TAG, "close drawing path, start(${internalStartPoint?.x}, ${internalStartPoint?.y}), " +
                 "end(${internalEndPoint?.x}, ${internalEndPoint?.y})")
    }

    /**
     * Check if the point hit the end internalPoints of drawing path, The internalPoints would be inverted-mapped to actual internalPoints
     * by the display matrix
     *
     * @param point the hit-test point
     * @param ratio the ratio of hit test radius, bigger is easier to hit
     * @return null if the point doesn't hit any any point. Otherwise return start point or end point
     */
    fun endpointHitTest(point: PointF, ratio: Float = 1f): HitPointType {
        Log.d(TAG, "endpoint hit test, point(${point.x}, ${point.y}), internalStartPoint(${internalStartPoint?.x}, " +
                 "${internalStartPoint?.y}), internalEndPoint(${internalEndPoint?.x}, ${internalEndPoint?.y})")

        internalStartPoint?.let { start ->
            val displayStartPoint = createMappedPoint(start.x, start.y)
            if (distance(point, displayStartPoint) <= minDistanceBetweenTwoPoints * ratio) {
                return HitPointType.START_POINT
            }
        }
        internalEndPoint?.let { end ->
            val displayEndPoint = createMappedPoint(end.x, end.y)
            if (distance(point, displayEndPoint) <= minDistanceBetweenTwoPoints * ratio) {
                return HitPointType.END_POINT
            }
        }
        return HitPointType.NONE
    }

    /**
     * Hit test the control point, return the index of control point
     *
     * @param point touch point
     * @param ratio the ratio for hit test, more bigger is more easily to hit
     * @return the index of point in the path, return -1 if the touch point doesn't hit any control point
     */
    fun controlPointHitTest(point: PointF, ratio: Float = 1f): Int {
        if (internalControlPoints.isEmpty()) {
            return NO_HIT
        }

        val radius = minDistanceBetweenTwoPoints * ratio / 2
        val touchRect = RectF(point.x - radius, point.y - radius,
                              point.x + radius, point.y + radius)
        invertMatrix.mapRect(touchRect)
        for (index in 0..internalControlPoints.count() - 1) {
            val currentPoint = internalControlPoints[index]
            if (touchRect.contains(currentPoint.x, currentPoint.y)) {
                return index
            }
        }
        return NO_HIT
    }

    /**
     * Update the position of control point and generate the new trimming path
     * It only works when the path is closed
     *
     * @param index the index of control point
     * @param x The new x-coordinate
     * @param y the new y-coordinate
     */
    fun updateControlPoint(index: Int, x: Float, y: Float) {
        if (!isClosed) {
            return
        }

        val mappedPoint = createInvertMappedPoint(x, y)

        edgeMap?.let { edgeMap ->
            //Find the pixel with highest gradient in edge map
            val max = edgeMap.maxInRegion(mappedPoint.x.toInt(), mappedPoint.y.toInt(), SNAP_POINT_RADIUS)
            mappedPoint.set(max.x.toFloat(), max.y.toFloat())
        }

        internalControlPoints[index].set(mappedPoint.x, mappedPoint.y)
        (pathInterpolator as? CatmullRomInterpolator)?.invalidPoint(index)
        smoothify()
    }

    /**
     * Reverse the drawing path, exchange the start point and endpoint
     */
    fun reverse() {
        if (internalPoints.count() < 2) {
            Log.w(TAG, "the length of drawing path is less than 2, don't need to reverse")
            return
        }

        internalPoints.reverse()
        quadToSmoothify()
    }

    private fun quadToSmoothify() {
        internalPath.rewind()
        //rebuild the path
        val firstPoint = internalPoints[0]
        internalPath.moveTo(firstPoint.x, firstPoint.y)
        internalStartPoint?.set(firstPoint.x, firstPoint.y)
        for (i in 1..internalPoints.count() - 1) {
            internalPath.quadTo(internalPoints[i - 1].x,
                    internalPoints[i - 1].y,
                    (internalPoints[i].x + internalPoints[i - 1].x) / 2,
                    (internalPoints[i].y + internalPoints[i - 1].y) / 2)
        }
        resetDisplayPath()
        val lastPoint = internalPoints.last()
        internalEndPoint?.set(lastPoint.x, lastPoint.y)
    }

    //map the point to display point
    private fun mapPoint(x: Float, y: Float): FloatArray {
        val mappedPoint = FloatArray(2)
        mappedPoint[0] = x
        mappedPoint[1] = y
        displayMatrix.mapPoints(mappedPoint)
        return mappedPoint
    }

    //map the display point to the point where it should be when the display matrix is identity matrix
    private fun invertMapPoint(x: Float, y: Float): FloatArray {
        val mappedPoint = FloatArray(2)
        mappedPoint[0] = x
        mappedPoint[1] = y
        invertMatrix.mapPoints(mappedPoint)
        return mappedPoint
    }

    private fun createInvertMappedPoint(x: Float, y: Float): PointF {
        val mappedPoint = invertMapPoint(x, y)
        return PointF(mappedPoint[0], mappedPoint[1])
    }

    private fun createMappedPoint(x: Float, y: Float): PointF {
        val mappedPoint = mapPoint(x, y)
        return PointF(mappedPoint[0], mappedPoint[1])
    }

    private fun setupControlPoints() {
        if (!isClosed) {
            return
        }

        internalControlPoints.clear()

        for (index in 0..internalPoints.count() - 1 step CONTROL_POINT_FACTOR) {
            val point = internalPoints[index]
            internalControlPoints.add(PointF(point.x, point.y))
        }
    }

    /**
     * Smoothify the drawing path, using CatmullRom splines to draw better Bezier Path
     */
    fun smoothify() {
        if (isClosed) {
            if (pathInterpolator == null) {
                pathInterpolator = CatmullRomInterpolator(
                        points = internalControlPoints, closed = isClosed, alpha = SMOOTH_FACTOR)
            }

            pathInterpolator?.interpolator()?.let { smoothPath ->
                internalPath.set(smoothPath)
                resetDisplayPath()
                internalPath.close()
            }
        } else {
            quadToSmoothify()
        }
    }

    private fun resetDisplayPath() {
        path.rewind()
        path.set(internalPath)
        path.transform(displayMatrix)
        if (isClosed) {
            path.close()
        }
    }

    private fun createMappedPoints(source: ArrayList<PointF>): ArrayList<PointF> {
        val mappedPoints = ArrayList<PointF>()
        source.mapTo(mappedPoints) { pointF ->
            createMappedPoint(pointF.x, pointF.y)
        }
        return mappedPoints
    }

    enum class HitPointType {
        START_POINT,
        END_POINT,
        NONE
    }

    companion object {
        const private val TAG = "DrawingPath"
        const private val SMOOTH_FACTOR = 0.8f
        const private val CONTROL_POINT_FACTOR = 4
        const private val SNAP_POINT_RADIUS = 3
        const val NO_HIT = -1
    }
}
