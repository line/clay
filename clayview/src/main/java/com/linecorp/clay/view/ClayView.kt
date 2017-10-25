/*
 * Copyright (c) 2016 LINE Corporation. All rights Reserved.
 * LINE Corporation PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.linecorp.clay.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.linecorp.clay.clayview.R
import com.linecorp.clay.view.transform.TwoFingerPanTransformer
import com.linecorp.clay.view.transform.ZoomTransformer
import com.linecorp.clay.Style
import com.linecorp.clay.Style.Color.Companion.LINE_GREEN
import com.linecorp.clay.Style.Companion.DEFAULT_STROKE_WIDTH
import com.linecorp.clay.Style.Companion.DEFAULT_STROKE_WIDTH_FOR_POINT
import com.linecorp.clay.Style.Companion.TOUCH_INDICATOR_POINT_RADIUS_DP
import com.linecorp.clay.drawable.PathDrawable
import com.linecorp.clay.drawable.PathSelectDrawable
import com.linecorp.clay.drawable.PointIndicatorDrawable
import com.linecorp.clay.graphic.*
import com.linecorp.clay.graphic.model.EdgeMap
import com.linecorp.clay.view.effect.PathEffect
import com.linecorp.clay.view.state.*
import java.util.ArrayList


/**
 * The transform factory
 *
 * @param motionEvent The MotionEvent
 */
typealias TransformFactory = (MotionEvent) -> Transform

/**
 * The enhanced image view that support trimming an image easily
 */
class ClayView : BaseTransformableView {
    private val hasClosedSelection: Boolean
        get() = getPathBoundsArea(currentDrawingPath.path) > MIN_SELECT_AREA

    private val isSelectionFinished: Boolean
        get() = currentDrawingPath.isClosed && hasClosedSelection

    /**
     * return true if this view can undo
     */
    val canUndo: Boolean
        get() = drawPathHistory.isNotEmpty()

    /**
     * return true if user is touching this view
     */
    val isTouching: Boolean
        get() = editingState !is NoTouch

    //styleable properties
    /**
     * The path stroke width
     */
    var pathStrokeWidth: Float
        set(value) {
            selectionDrawable.strokeWidth = value
        }
        get() = selectionDrawable.strokeWidth

    /**
     * The path stroke color
     */
    var pathStrokeColor: Int
        set(value) {
            selectionDrawable.strokeColor = value
            startPointDrawable.color = value
            endPointDrawable.color = value
        }
        get() = selectionDrawable.strokeColor

    /**
     * The control point color
     */
    var controlPointColor: Int
        set(value) {
            selectionDrawable.controlPointColor = value
        }
        get() = selectionDrawable.controlPointColor

    /**
     * The radius of end point
     */
    var endPointRadius: Float
        set(value) {
            startPointDrawable.radius = value
            endPointDrawable.radius = value
        }
        get() = startPointDrawable.radius

    private var enableEdgeDetect = false

    /**
     * Boolean that enable edge detection (experiment), the default is false
     */
    var edgeDetect: Boolean
        set(value) {
            enableEdgeDetect = value
        }
        get() = enableEdgeDetect

    /**
     * Boolean that enable drawing the path on the cropped image, the default is false
     */
    var drawPathOnCroppedImage: Boolean = false

    private var selectionDrawable: PathSelectDrawable
    private var startPointDrawable: PointIndicatorDrawable
    private var endPointDrawable: PointIndicatorDrawable

    private var drawPathHistory = ArrayList<DrawingPath>()
    private val currentDrawingPath: DrawingPath

    private var isAnimate = false

    private var edgeMap: EdgeMap? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        currentDrawingPath = DrawingPath(context, theDisplayMatrix)
        selectionDrawable = PathSelectDrawable(currentDrawingPath, Style.Color.BLACK_OPACITY_48)
        startPointDrawable = defaultPointIndicatorDrawable()
        endPointDrawable = defaultPointIndicatorDrawable()

        val typedAttrs = context.theme.obtainStyledAttributes(attrs, R.styleable.ClayView, 0, 0)
        try {
            pathStrokeWidth = typedAttrs.getDimension(
                    R.styleable.ClayView_strokeWidth, DEFAULT_STROKE_WIDTH)
            pathStrokeColor = typedAttrs.getColor(R.styleable.ClayView_strokeColor, Color.WHITE)
            controlPointColor = typedAttrs.getColor(R.styleable.ClayView_controlPointColor, LINE_GREEN)
            endPointRadius = typedAttrs.getDimension(
                    R.styleable.ClayView_endPointRadius,
                    TOUCH_INDICATOR_POINT_RADIUS_DP * resources.displayMetrics.density)
            edgeDetect = typedAttrs.getBoolean(R.styleable.ClayView_edgeDetect, false)
        } finally {
            typedAttrs.recycle()
        }
        //support android:src from xml
        attrs?.let {
            checkSrcProperty(attrs)
        }
    }

    //handle setImageResource
    internal var android.widget.ImageView.imageResource: Int
        get() = throw RuntimeException("")
        set(value) {
            val drawable = resources.getDrawable(value) as? BitmapDrawable
            bitmap = drawable?.bitmap
        }

    override fun onBitmapUpdate(bitmap: Bitmap) {
        calculateImageBounds()
        clearSelection()
        if (enableEdgeDetect) {
            edgeMap = EdgeMap(bitmap)
        }
    }

    private fun checkSrcProperty(attrs: AttributeSet) {
        var attrName: String
        for (index in 0..attrs.attributeCount - 1) {
            attrName = attrs.getAttributeName(index)
            if (attrName == "src") {
                bitmap = (drawable as? BitmapDrawable)?.bitmap
            }
        }
    }

    private fun defaultPointIndicatorDrawable(): PointIndicatorDrawable {
        return PointIndicatorDrawable().apply {
            radius = TOUCH_INDICATOR_POINT_RADIUS_DP * resources.displayMetrics.density
            strokeWidth = DEFAULT_STROKE_WIDTH_FOR_POINT * resources.displayMetrics.density
            color = Color.WHITE
        }
    }

    /**
     * Get cropped image
     *
     * @param antiAlias enable antiAlias if true
     * @return cropped Image
     */
    fun getCroppedImage(antiAlias: Boolean): Bitmap? {
        if (isSelectionFinished) {
            bitmap?.let { srcBitmap ->
                val padding = (selectionDrawable.strokeWidth / resources.displayMetrics.density / 2).toInt()
                val selectedPathOnBitmap = Path(currentDrawingPath.path)
                selectedPathOnBitmap.transform(getInvertMatrix(theDisplayMatrix))
                val croppedImage = cropImage(source = srcBitmap, path = selectedPathOnBitmap,
                        antiAlias = antiAlias, padding = padding)
                if (drawPathOnCroppedImage) {
                    val translateMatrix = Matrix()
                    val selectedBounds = getPathBoundsOnBitmap(selectedPathOnBitmap, srcBitmap)
                    translateMatrix.setTranslate(-(selectedBounds.left.toFloat() - padding),
                            -(selectedBounds.top.toFloat() - padding))
                    selectedPathOnBitmap.transform(translateMatrix)
                    addPathOnCroppedImage(selectedPathOnBitmap, croppedImage)
                }

                return croppedImage
            }
        }
        return null
    }

    private fun addPathOnCroppedImage(path: Path, bitmap: Bitmap) {
        val pathDrawable = PathDrawable(path).apply {
            strokeWidth = Math.max(selectionDrawable.strokeWidth / resources.displayMetrics.density, 1f)
            strokeBorderWidth = Math.max(selectionDrawable.strokeBorderWidth
                    / resources.displayMetrics.density, 1f)
            strokeBorderColor = selectionDrawable.strokeBorderColor
            strokeColor = selectionDrawable.strokeColor
        }
        PathEffect(pathDrawable).applyTo(bitmap)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.i(TAG, "onSizeChanged, width: $w, height: $h, oldWidth: $oldw, oldHeight: $oldh")
        resetMatrix()
        createOffScreen(w, h)
        calculateImageBounds()
    }

    override fun onDraw(canvas: Canvas?) {
        //draw original imageview
        super.onDraw(canvas)
        if (currentDrawingPath.isClosed || !isTouching && !isAnimate) {
            canvas?.drawBitmap(offScreenBitmap, 0f, 0f, null)
        } else {
            drawSelectionPath(canvas)
        }
    }

    /**
     * undo path selection
     */
    fun undoSelect() {
        if (canUndo) {
            val lastDrawing = drawPathHistory.removeAt(drawPathHistory.lastIndex)
            currentDrawingPath.set(lastDrawing)
            currentDrawingPath.displayMatrix = theDisplayMatrix
        }

        updatePathDrawables()
        //also update offscreen canvas
        drawOffScreen()
        invalidate()
    }

    private fun updatePathDrawables() {
        selectionDrawable.drawingPath = currentDrawingPath
        startPointDrawable.point = currentDrawingPath.startPoint
        endPointDrawable.point = currentDrawingPath.endPoint
    }

    /**
     * Clear path selection
     */
    fun clearSelection() {
        resetOffScreen()
        drawPathHistory.forEach(DrawingPath::reset)
        drawPathHistory.clear()
        currentDrawingPath.reset()
        updatePathDrawables()
    }

    private fun saveDrawingPath() {
        drawPathHistory.add(currentDrawingPath.clone())
        updatePathDrawables()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        var needRedraw = false
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                val point = PointF(event.x, event.y)
                if (hasClosedSelection || currentDrawingPath.isClosed) {
                    startTrimming(point)
                } else {
                    clearSelection()
                    changeState(BeginTouch(point))
                }
                needRedraw = true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (editingState !is Transform && !editingState.isSteady) {
                    val transformState = customTransformFactory?.invoke(event)
                            ?: defaultTransformStateFactory(event)
                    changeState(transformState)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (editingState is BeginTouch) {
                    val beginPoint = editingState.editingObject as PointF
                    saveDrawingPath()
                    changeState(Selection(currentDrawingPath, beginPoint,
                                          pointId = event.getPointerId(0),
                                          validRect = RectF(selectionDrawable.maskBounds)).apply {
                        afterTouchEnd = {
                            currentDrawingPath.endPoint?.let { endPoint ->
                                if (currentDrawingPath.pointCount < MIN_NUMBER_OF_POINTS) {
                                    updatePathDrawables()
                                    return@let
                                }
                                //for close path, we should use bigger ratio for hit test
                                val hitPointType = currentDrawingPath.endpointHitTest(endPoint,
                                        2 * calculateHitTestRatio())
                                if (hitPointType == DrawingPath.HitPointType.START_POINT) {
                                    currentDrawingPath.close()
                                } else {
                                    currentDrawingPath.smoothify()
                                }
                            }

                            drawOffScreen()
                        }
                    })
                }
                editingState.onTouchMove(event)
                if (editingState is Selection && editingState.isSteady) {
                    startPointDrawable.point = currentDrawingPath.startPoint
                    endPointDrawable.point = currentDrawingPath.endPoint
                }
                needRedraw = true
            }

            MotionEvent.ACTION_POINTER_UP -> {
                if (editingState is Selection) {
                    val pointIndex = event.findPointerIndex((editingState as Selection).pointId)
                    Log.d(TAG, "the point index: $pointIndex, action index: ${event.actionIndex}")
                    if (pointIndex == event.actionIndex) {
                        endTouchEvent(event)
                        needRedraw = true
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_OUTSIDE,
            MotionEvent.ACTION_CANCEL -> {
                endTouchEvent(event)
                //Remove the first record, if user doesn't trimming image at all
                if (drawPathHistory.count() == 1
                    && currentDrawingPath.pointCount < MIN_POINT_COUNT_FOR_PATH) {
                    drawPathHistory.removeAt(drawPathHistory.lastIndex)
                }
                needRedraw = true
            }
        }

        if (needRedraw) {
            invalidate()
        }

        return true
    }

    private fun endTouchEvent(event: MotionEvent) {
        editingState.onTouchEnd(event)
        changeState(NoTouch())
    }

    private fun drawOffScreen() {
        resetOffScreen()
        drawSelectionPath(offScreenCanvas)
        if (currentDrawingPath.isClosed) {
            selectionDrawable.draw(offScreenCanvas)
        }
    }

    private fun drawSelectionPath(canvas: Canvas?) {
        if (!currentDrawingPath.isClosed) {
            startPointDrawable.draw(canvas)
            endPointDrawable.draw(canvas)
        }
        selectionDrawable.draw(canvas)
    }

    private fun calculateImageBounds() {
        bitmap?.let { srcBitmap ->
            resetMatrix()
            calculateBaseMatrix(srcBitmap.width, srcBitmap.height)
            updateImageMatrix(getDisplayMatrix())
            calculateMaskBounds(srcBitmap.width, srcBitmap.height)
        }
    }

    private fun calculateMaskBounds(bitmapWidth: Int, bitmapHeight: Int) {
        val displayRectF = RectF(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat())
        theDisplayMatrix.mapRect(displayRectF)
        val maskBounds = Rect()
        displayRectF.round(maskBounds)
        selectionDrawable.maskBounds = maskBounds
    }

    private fun changeState(state: EditState<Any>) {
        editingState = state
    }

    private fun centerImage() {
        val (transformScaleX, transformScaleY) = getCurrentScale(transformMatrix)
        if (transformScaleX <= 1 && transformScaleY <= 1) {
            val currentDisplayMatrix = Matrix(getDisplayMatrix())
            transformMatrix.reset()
            val targetDisplayMatrix = Matrix(getDisplayMatrix())
            val animator = prepareMatrixAnimator(currentDisplayMatrix, targetDisplayMatrix)
            animator.duration = DEFAULT_ANIMATION_DURATION
            animator.start()
        }
    }

    private fun updateImageMatrix(matrix: Matrix) {
        imageMatrix = matrix
        currentDrawingPath.displayMatrix = matrix
        //Prevent to draw start point before actually drawing the path
        if (currentDrawingPath.pointCount > MIN_POINT_COUNT_FOR_PATH) {
            startPointDrawable.point = currentDrawingPath.startPoint
            endPointDrawable.point = currentDrawingPath.endPoint
        }
    }

    private fun prepareMatrixAnimator(srcMatrix: Matrix, dstMatrix: Matrix): ValueAnimator {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        val transX: Float
        val transY: Float
        val scaleX: Float
        val scaleY: Float
        val srcValues = FloatArray(9)
        srcMatrix.getValues(srcValues)
        val dstValues = FloatArray(9)
        dstMatrix.getValues(dstValues)
        transX = dstValues[Matrix.MTRANS_X] - srcValues[Matrix.MTRANS_X]
        transY = dstValues[Matrix.MTRANS_Y] - srcValues[Matrix.MTRANS_Y]
        scaleX = dstValues[Matrix.MSCALE_X] - srcValues[Matrix.MSCALE_X]
        scaleY = dstValues[Matrix.MSCALE_Y] - srcValues[Matrix.MSCALE_Y]
        valueAnimator.addUpdateListener { animator ->
            val value = animator.animatedFraction
            val currentValues = srcValues.copyOf()
            currentValues[Matrix.MTRANS_X] = currentValues[Matrix.MTRANS_X] + transX * value
            currentValues[Matrix.MTRANS_Y] = currentValues[Matrix.MTRANS_Y] + transY * value
            currentValues[Matrix.MSCALE_X] = currentValues[Matrix.MSCALE_X] + scaleX * value
            currentValues[Matrix.MSCALE_Y] = currentValues[Matrix.MSCALE_Y] + scaleY * value
            imageMatrix.setValues(currentValues)
            updateImageMatrix(imageMatrix)
            calculateMaskLayerAndDraw()
            invalidate()
        }

        valueAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                isAnimate = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                animation?.removeAllListeners()
                //just draw offsreen at the end for better performance
                drawOffScreen()
                isAnimate = false
            }
        })

        return valueAnimator
    }

    private fun calculateMaskLayerAndDraw() {
        bitmap?.let { srcBitmap ->
            if (currentDrawingPath.isClosed) {
                calculateMaskBounds(srcBitmap.width, srcBitmap.height)
                drawOffScreen()
            }
        }
    }

    private fun startTrimming(startPoint: PointF) {
        if (currentDrawingPath.isClosed) {
            val pointIndex = currentDrawingPath.controlPointHitTest(point = startPoint,
                    ratio = 1.5f * calculateHitTestRatio())
            if (pointIndex != DrawingPath.NO_HIT) {
                currentDrawingPath.edgeMap = edgeMap
                changeState(Mold(currentDrawingPath, startPoint, pointIndex,
                                 validRect = RectF(selectionDrawable.maskBounds)).apply {
                    afterTouchMove = { drawOffScreen() }
                    afterTouchEnd = { drawOffScreen() }
                })
            }
        } else {
            val hitPointType = currentDrawingPath.endpointHitTest(startPoint, calculateHitTestRatio())
            if (hitPointType == DrawingPath.HitPointType.END_POINT) {
                changeState(BeginTouch(startPoint))
            } else if (hitPointType == DrawingPath.HitPointType.START_POINT) {
                currentDrawingPath.reverse()
                changeState(BeginTouch(startPoint))
            }
        }
    }

    /**
     * Custom transform state factory. To create a different Transform object for touch events
     */
    var customTransformFactory: TransformFactory? = null

    private val defaultTransformStateFactory: TransformFactory = { event ->
        val point1 = PointF(event.getX(0), event.getY(0))
        val point2 = PointF(event.getX(1), event.getY(1))
        val pointIds = ArrayList<Int>()
        (0..event.pointerCount - 1).mapTo(pointIds) { event.getPointerId(it) }
        val zoom = ZoomTransformer(point1, point2)
        val pan = TwoFingerPanTransformer(point1, point2)
        Transform(transformMatrix, pointIds).apply {
            addTransformer(zoom)
            addTransformer(pan)
            afterTouchMove = {
                transformMatrix.set(editingObject)
                updateImageMatrix(getDisplayMatrix())
                //support zoom in/out after path closed,
                //re-calculate the mask area impacts the draw performance, but no good solution found yet....
                calculateMaskLayerAndDraw()
            }
            afterTouchEnd = {
                bitmap?.let { srcBitmap ->
                    centerImage()
                    calculateMaskBounds(srcBitmap.width, srcBitmap.height)
                    //need to redraw offscreen to to reflect the change
                    drawOffScreen()
                }
            }
        }
    }

    private fun calculateHitTestRatio() =
            endPointDrawable.radius / currentDrawingPath.minDistanceBetweenTwoPoints

    companion object {
        private const val TAG = "ImageEditView"
        private const val MIN_SELECT_AREA = 4 // 2 x 2 area
        private const val MIN_POINT_COUNT_FOR_PATH = 2
        private const val DEFAULT_ANIMATION_DURATION = 150L //ms
        private const val MIN_NUMBER_OF_POINTS = 4
    }
}
