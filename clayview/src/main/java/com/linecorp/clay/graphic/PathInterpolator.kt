/*
 * Copyright (c) 2017 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.graphic

import android.graphics.Path
import android.graphics.PointF
import android.util.Log

abstract internal class PathInterpolator(val points: ArrayList<PointF>, val closed: Boolean) {
    /**
     * Interpolate path
     *
     * @param points the points of the path
     * @param closed if the path is closed
     * @return new path
     */
    abstract fun interpolator(): Path?
}

/**
 * The Hermite spline interpolator
 */
internal class HermiteInterpolator(points: ArrayList<PointF>, closed: Boolean) : PathInterpolator(points, closed) {
    override fun interpolator(): Path? {
        if (points.count() < 2) {
            return null
        }

        val numberOfPoints = if (closed) points.count() else points.count() - 1
        var currentPoint = points[0]
        var previousPoint = if (closed) points[points.lastIndex - 1] else null
        var nextPoint: PointF? = points[1]

        val path = Path()
        path.moveTo(currentPoint.x, currentPoint.y)

        for (index in 0..numberOfPoints - 1) {
            if (nextPoint == null) {
                Log.d("HermiteInerpolator", "next point is null, cannot calculate the curve")
                break
            }
            val endPoint = nextPoint
            var mX: Float
            var mY: Float
            if (previousPoint == null) {
                mX = (nextPoint.x - currentPoint.x) * 0.5f
                mY = (nextPoint.y - currentPoint.y) * 0.5f
            } else {
                mX = 0.5f * ((nextPoint.x - currentPoint.x) + (currentPoint.x - previousPoint.x))
                mY = 0.5f * ((nextPoint.y - currentPoint.y) + (currentPoint.y - previousPoint.y))
            }

            val controlPoint1 = PointF(currentPoint.x + mX / 3f, currentPoint.y + mY / 3f)

            previousPoint = currentPoint
            currentPoint = nextPoint
            val nextIndex = index + 2
            nextPoint = if (closed) {
                points[nextIndex % points.count()]
            } else if (nextIndex < points.count()) {
                points[nextIndex % points.count()]
            } else {
                null
            }

            if (nextPoint == null) {
                mX = (currentPoint.x - previousPoint.x) * 0.5f
                mY = (currentPoint.y - previousPoint.y) * 0.5f
            } else {
                mX = 0.5f * ((nextPoint.x - currentPoint.x) + (currentPoint.x - previousPoint.x))
                mY = 0.5f * ((nextPoint.y - currentPoint.y) + (currentPoint.y - previousPoint.y))
            }
            val controlPoint2 = PointF(currentPoint.x - mX / 3.0f, currentPoint.y - mY / 3.0f)
            path.cubicTo(controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    endPoint.x, endPoint.y)
        }
        return path
    }
}

/**
 * The CatmullRom spline interpolator
 */
internal class CatmullRomInterpolator(points: ArrayList<PointF>, closed: Boolean,
                             val alpha: Float, enableCache: Boolean = true) :
        PathInterpolator(points, closed) {
    companion object {
        private const val EPSILON = 1.0e-5
    }
    private val beizerControlPointsCache: Array<Pair<PointF, PointF>?>?

    init {
        if (enableCache) {
            beizerControlPointsCache = Array(points.size) { null }
        } else {
            beizerControlPointsCache = null
        }
    }

    /**
     * If enableCache is true, when the point changed, need to call invalidPoint to recalculate the control points
     */
    fun invalidPoint(index: Int) {
        if (beizerControlPointsCache == null) {
            return
        }

        val pointsCount = beizerControlPointsCache.count()
        val prevIndex = if (index - 1 < 0) pointsCount - 1 else index - 1
        val prevPrevIndex = if (prevIndex - 1 < 0) pointsCount - 1 else prevIndex - 1
        val nextIndex = (index + 1) % pointsCount
        beizerControlPointsCache[index] = null
        beizerControlPointsCache[prevIndex] = null
        beizerControlPointsCache[prevPrevIndex] = null
        beizerControlPointsCache[nextIndex] = null
    }

    override fun interpolator(): Path? {
        val pointsCount = points.count()
        if (pointsCount < 4) {
            return null
        }

        val path = Path()

        val endIndex = if (closed) pointsCount else pointsCount - 2
        val alphaValue = alpha.coerceIn(0f, 1f)
        val startIndex = if (closed) 0 else 1
        val controlPoint1 = PointF()
        val controlPoint2 = PointF()
        for (index in startIndex..endIndex - 1) {
            val nextIndex = (index + 1) % pointsCount
            val nextNextIndex = (nextIndex + 1) % pointsCount
            val prevIndex = if (index - 1 < 0) pointsCount - 1 else index - 1
            val point0 = points[prevIndex]
            val point1 = points[index]
            val point2 = points[nextIndex]
            val point3 = points[nextNextIndex]

            if (beizerControlPointsCache != null && beizerControlPointsCache[index] != null) {
                controlPoint1.set(beizerControlPointsCache[index]?.first)
                controlPoint2.set(beizerControlPointsCache[index]?.second)
            } else {
                val delta1 = (point1 sub point0).length().toDouble()
                val delta2 = (point2 sub point1).length().toDouble()
                val delta3 = (point3 sub point2).length().toDouble()

                calculateCatmullRomControlPoint(controlPoint = controlPoint1, prevPoint = point0, currentPoint = point1,
                        nextPoint = point2, delta1 = delta1, delta2 = delta2, alpha = alphaValue)

                calculateCatmullRomControlPoint(controlPoint = controlPoint2, prevPoint = point3, currentPoint = point2,
                        nextPoint = point1, delta1 = delta3, delta2 = delta2, alpha = alphaValue)
            }

            if (index == startIndex) {
                path.moveTo(point1.x, point1.y)
            }
            path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, point2.x, point2.y)
            beizerControlPointsCache?.let {
                beizerControlPointsCache[index] =
                        Pair(PointF(controlPoint1.x, controlPoint1.y), PointF(controlPoint2.x, controlPoint2.y))
            }
        }

        if (closed) {
            path.close()
        }
        return path
    }


    private fun calculateCatmullRomControlPoint(controlPoint: PointF, prevPoint: PointF, currentPoint: PointF,
                                                nextPoint: PointF, delta1: Double, delta2: Double, alpha: Float) {
        if (Math.abs(delta1) < EPSILON) {
            controlPoint.set(currentPoint)
        } else {
            controlPoint.set(nextPoint mul Math.pow(delta1, 2 * alpha.toDouble()).toFloat())
            controlPoint.set(controlPoint sub
                    (prevPoint mul Math.pow(delta2, 2 * alpha.toDouble()).toFloat()))
            controlPoint.set(controlPoint add
                    (currentPoint mul
                            (2 * Math.pow(delta1, 2 * alpha.toDouble()) +
                                    3 * Math.pow(delta1, alpha.toDouble()) *
                                            Math.pow(delta2, alpha.toDouble()) +
                                    Math.pow(delta2, 2 * alpha.toDouble())).toFloat()))
            controlPoint.set(controlPoint mul 1.0f /
                    (3 * Math.pow(delta1, alpha.toDouble()) *
                            (Math.pow(delta1,
                                    alpha.toDouble()) + Math.pow(delta2, alpha.toDouble()))).toFloat())
        }

    }
}

