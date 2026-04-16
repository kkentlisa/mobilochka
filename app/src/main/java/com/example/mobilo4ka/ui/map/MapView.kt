package com.example.mobilo4ka.ui.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withMatrix
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap

class MapView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

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

    var onBuildingClicked: ((Int, Int) -> Unit)? = null

    val mapMatrix = Matrix()
    private val mapWidth = 202f
    private val mapHeight = 161f

    private var startPoint: Pair<Int, Int>? = null
    private var endPoint: Pair<Int, Int>? = null
    private var currentPath: List<Pair<Int, Int>> = emptyList()

    private var geneticRoute: List<Pair<Int, Int>> = emptyList()
    private var targetEntrances: List<Pair<Int, Int>> = emptyList()

    private val buildingPaint = Paint().apply { color = "#e6dac8".toColorInt(); style = Paint.Style.FILL }
    private val interestingBuildingPaint = Paint().apply { color = "#c4b5a0".toColorInt(); style = Paint.Style.FILL }
    private val grassPaint = Paint().apply { color = "#cbe88d".toColorInt() }
    private val roadCarPaint = Paint().apply { color = "#d4d4d4".toColorInt() }
    private val asphaltPaint = Paint().apply { color = "#f3efdd".toColorInt() }
    private val pathPaint = Paint().apply { color = Color.WHITE }
    private val waterPaint = Paint().apply { color = "#9ad8f7".toColorInt() }

    var onMapClicked: ((Int, Int) -> Unit)? = null

    private var userStartMarker: Pair<Int, Int>? = null

    private val startMarkerPaint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val pathRoutePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        isAntiAlias = true
    }

    private val geneticPathPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        isAntiAlias = true
    }

    private val pointPaint = Paint().apply { color = Color.RED; isAntiAlias = true }

    private val entrancePaint = Paint().apply {
        color = Color.parseColor("#FF4500")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val moveListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            mapMatrix.postTranslate(-dx, -dy)
            invalidate()
            return true
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val inverse = Matrix()
            if (mapMatrix.invert(inverse)) {
                val pts = floatArrayOf(e.x, e.y)
                inverse.mapPoints(pts)
                val gridX = pts[0].toInt()
                val gridY = pts[1].toInt()

                onBuildingClicked?.invoke(gridX, gridY)

                onMapClicked?.invoke(gridX, gridY)
            }
            return true
        }
    }

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mapMatrix.postScale(detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusY)
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

    override fun performClick(): Boolean { super.performClick(); return true }

    fun setStartMarker(point: Pair<Int, Int>?) {
        this.userStartMarker = point
        invalidate()
    }

    fun showGeneticRoute(route: List<Pair<Int, Int>>, entrances: List<Pair<Int, Int>> = emptyList()) {
        this.geneticRoute = route
        this.targetEntrances = entrances
        invalidate()
    }

    fun setupInitialView() {
        mapMatrix.reset()
        mapMatrix.postScale(5f, 5f)
        mapMatrix.postTranslate(50f, 50f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor("#f0f0f0".toColorInt())

        canvas.withMatrix(mapMatrix) {
            zones["grass"]?.forEach { drawRect(it[0].toFloat(), it[1].toFloat(), it[0] + 1f, it[1] + 1f, grassPaint) }
            zones["water"]?.forEach { drawRect(it[0].toFloat(), it[1].toFloat(), it[0] + 1f, it[1] + 1f, waterPaint) }
            zones["roadCar"]?.forEach { drawRect(it[0].toFloat(), it[1].toFloat(), it[0] + 1f, it[1] + 1f, roadCarPaint) }
            zones["asphalt"]?.forEach { drawRect(it[0].toFloat(), it[1].toFloat(), it[0] + 1f, it[1] + 1f, asphaltPaint) }
            zones["path"]?.forEach { drawRect(it[0].toFloat(), it[1].toFloat(), it[0] + 1f, it[1] + 1f, pathPaint) }

            buildings.forEach { building ->
                val currentPaint = if (!building.name.isNullOrBlank()) interestingBuildingPaint else buildingPaint
                building.pixels.forEach { pixel ->
                    drawRect(pixel[0].toFloat(), pixel[1].toFloat(), pixel[0] + 1f, pixel[1] + 1f, currentPaint)
                }
            }

            if (currentPath.isNotEmpty()) {
                val path = Path()
                path.moveTo(currentPath[0].first + 0.5f, currentPath[0].second + 0.5f)
                for (i in 1 until currentPath.size) path.lineTo(currentPath[i].first + 0.5f, currentPath[i].second + 0.5f)
                drawPath(path, pathRoutePaint)
            }

            if (geneticRoute.isNotEmpty()) {
                val path = Path()
                path.moveTo(geneticRoute[0].first + 0.5f, geneticRoute[0].second + 0.5f)
                geneticRoute.forEach { pt ->
                    path.lineTo(pt.first + 0.5f, pt.second + 0.5f)
                }
                canvas.drawPath(path, geneticPathPaint)
            }

            targetEntrances.forEach { entrance ->
                drawCircle(entrance.first + 0.5f, entrance.second + 0.5f, 0.7f, entrancePaint)
            }

            startPoint?.let { drawCircle(it.first + 0.5f, it.second + 0.5f, 0.6f, pointPaint) }
            endPoint?.let { drawCircle(it.first + 0.5f, it.second + 0.5f, 0.6f, pointPaint) }

            userStartMarker?.let {
                drawCircle(it.first + 0.5f, it.second + 0.5f, 0.8f, startMarkerPaint)
            }
        }
    }
}