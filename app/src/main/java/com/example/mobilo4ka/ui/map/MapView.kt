package com.example.mobilo4ka.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withMatrix
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm

class MapView (context: Context, attrs: AttributeSet? = null): View(context, attrs) {
    var gridMap: GridMap? = null
        set(value) {
            field = value
            invalidate()
        }
    var buildings: List<Building> = emptyList()
        set(value) {
            field = value
            invalidate()
        }
    var zones: Map<String, List<List<Int>>> = emptyMap()
        set(value) {
            field = value
            invalidate()
        }

    val mapMatrix = Matrix()

    private val buildingPaint = Paint().apply {
        color = "#e6dac8".toColorInt()
        style = Paint.Style.FILL
    }

    private val grassPaint = Paint().apply { color = "#cbe88d".toColorInt() }
    private val roadCarPaint = Paint().apply { color = "#d4d4d4".toColorInt() }
    private val asphaltPaint = Paint().apply { color = "#f3efdd".toColorInt() }
    private val pathPaint = Paint().apply { color = Color.WHITE }
    private val waterPaint = Paint().apply { color = "#9ad8f7".toColorInt() }

    private val pathRoutePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 0.4f
    }

    private val linePath = android.graphics.Path()

    private val startPaint = Paint().apply { color = Color.RED }
    private val endPaint = Paint().apply { color = Color.RED }

    private val minScale = 8f
    private val maxScale = 50f
    private val matrixValues = FloatArray(9)

    private val mapWidth = 202f
    private val mapHeight = 161f

    private var startPoint: Pair<Int, Int>? = null
    private var endPoint: Pair<Int, Int>? = null
    private var currentPath: List<Pair<Int, Int>> = emptyList()

    private val moveListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            mapMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]
            val currentTransX = matrixValues[Matrix.MTRANS_X]
            val currentTransY = matrixValues[Matrix.MTRANS_Y]

            val contentWidth = mapWidth * currentScale
            val contentHeight = mapHeight * currentScale

            var newDx = -dx
            var newDy = -dy

            if (currentTransX + newDx > 0) {
                newDx = -currentTransX
            } else if (currentTransX + newDx < width - contentWidth) {
                newDx = (width - contentWidth) - currentTransX
            }

            if (contentHeight <= height) {
                val targetY = (height - contentHeight) / 2f
                val currentTransY = matrixValues[Matrix.MTRANS_Y]
                mapMatrix.postTranslate(0f, targetY - currentTransY)
                newDy = 0f
            } else {
                if (currentTransY + newDy > 0) {
                    newDy = -currentTransY
                } else if (currentTransY + newDy < height - contentHeight) {
                    newDy = (height - contentHeight) - currentTransY
                }
            }

            mapMatrix.postTranslate(newDx, newDy)
            invalidate()
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val inverse = Matrix()
            mapMatrix.invert(inverse)
            val pts = floatArrayOf(e.x, e.y)
            inverse.mapPoints(pts)

            val gridX = pts[0].toInt()
            val gridY = pts[1].toInt()

            if (gridX in 0 until mapWidth.toInt() && gridY in 0 until mapHeight.toInt()) {
                if (startPoint == null || endPoint != null) {
                    startPoint = Pair(gridX, gridY)
                    endPoint = null
                    currentPath = emptyList()
                } else {
                    endPoint = Pair(gridX, gridY)
                    calculateAStarPath()
                }
                invalidate()
            }

            performClick()
            return true
        }
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var scaleFactor = detector.scaleFactor

            mapMatrix.getValues(matrixValues)
            val currentScale = matrixValues[Matrix.MSCALE_X]

            val targetScale = currentScale * scaleFactor

            if (targetScale < minScale) {
                scaleFactor = minScale / currentScale
            } else if (targetScale > maxScale) {
                scaleFactor = maxScale / currentScale
            }

            mapMatrix.postScale(
                scaleFactor,
                scaleFactor,
                detector.focusX,
                detector.focusY
            )
            invalidate()
            return true
        }
    }

    private val scaleDetector = ScaleGestureDetector(context, scaleListener)
    private val gestureDetector = GestureDetector(context, moveListener)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    private fun isPointWalkable(x: Int, y: Int): Boolean {
        val matrix = gridMap?.matrix ?: return false
        if (y < 0 || y >= matrix.size || x < 0 || x >= matrix[0].size) return false
        return matrix[y][x] == 0
    }

    private fun calculateAStarPath() {
        val start = startPoint ?: return
        val end = endPoint ?: return
        val aStar = AStarAlgorithm()

        kotlin.concurrent.thread {
            val path = aStar.findPath(
                start.first, start.second,
                end.first, end.second,
                isWalkable = ::isPointWalkable
            )

            post {
                currentPath = path
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.withMatrix(mapMatrix) {
            zones["grass"]?.forEach { pixel ->
                val x = pixel[0]
                val y = pixel[1]
                drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, grassPaint)
            }

            zones["roadCar"]?.forEach { pixel ->
                val x = pixel[0]
                val y = pixel[1]
                drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, roadCarPaint)
            }

            zones["asphalt"]?.forEach { pixel ->
                val x = pixel[0]
                val y = pixel[1]
                drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, asphaltPaint)
            }

            zones["path"]?.forEach { pixel ->
                val x = pixel[0]
                val y = pixel[1]
                drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, pathPaint)
            }

            zones["water"]?.forEach { pixel ->
                val x = pixel[0]
                val y = pixel[1]
                drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, waterPaint)
            }

            buildings.forEach { building ->
                building.pixels.forEach { pixel ->
                    val x = pixel[0]
                    val y = pixel[1]
                    drawRect(x.toFloat(), y.toFloat(), x + 1f, y + 1f, buildingPaint)
                }
            }

            if (currentPath.isNotEmpty()) {
                linePath.reset()

                val firstPoint = currentPath[0]
                linePath.moveTo(firstPoint.first + 0.5f, firstPoint.second + 0.5f)

                for (i in 1 until currentPath.size) {
                    val point = currentPath[i]
                    linePath.lineTo(point.first + 0.5f, point.second + 0.5f)
                }

                drawPath(linePath, pathRoutePaint)
            }

            startPoint?.let {
                drawCircle(it.first + 0.5f, it.second + 0.5f, 0.5f,startPaint)
            }

            endPoint?.let {
                drawCircle(it.first + 0.5f, it.second + 0.5f, 0.5f,endPaint)
            }
        }
    }

    fun setupInitialView() {
        post {
            if (height == 0) return@post

            val scaleToFitHeight = height.toFloat() / mapHeight

            mapMatrix.reset()
            mapMatrix.postScale(scaleToFitHeight, scaleToFitHeight)

            val mapWidthInPx = mapWidth * scaleToFitHeight
            val offsetX = (width - mapWidthInPx) / 2f
            mapMatrix.postTranslate(offsetX, 0f)

            invalidate()
        }
    }
}