package com.example.mobilo4ka.ui.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import androidx.compose.ui.graphics.toArgb
import com.example.mobilo4ka.algorithms.clustering.ClusterPoint
import com.example.mobilo4ka.algorithms.clustering.ClusteringMode
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.screens.astar.RouteDrawer
import com.example.mobilo4ka.ui.screens.clustering.ClusterDrawer
import com.example.mobilo4ka.ui.theme.*

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

    var routeDrawer: RouteDrawer? = null
    var isAstarEnabled: Boolean = false

    private val clusterDrawer = ClusterDrawer()
    var isClusteringEnabled: Boolean = false

    fun updateClustering(points: List<ClusterPoint>, mode: ClusteringMode) {
        clusterDrawer.points = points
        clusterDrawer.mode = mode
        invalidate()
    }

    val mapMatrix = Matrix()

    private val buildingPaint = Paint().apply {
        color = MapBuilding.toArgb()
        style = Paint.Style.FILL
    }
    private val grassPaint = Paint().apply { color = MapGrass.toArgb() }
    private val roadCarPaint = Paint().apply { color = MapRoadCar.toArgb() }
    private val asphaltPaint = Paint().apply { color = MapAsphalt.toArgb() }
    private val pathPaint = Paint().apply { color = MapPath.toArgb() }
    private val waterPaint = Paint().apply { color = MapWater.toArgb() }


    private val minScale = Dimens.mapMinScale
    private val maxScale = Dimens.mapMaxScale
    private val matrixValues = FloatArray(9)

    private val mapWidth = Dimens.mapWidhtSize
    private val mapHeight = Dimens.mapHeightSize

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
            if (!isAstarEnabled) return false

            val inverse = Matrix()
            mapMatrix.invert(inverse)
            val pts = floatArrayOf(e.x, e.y)
            inverse.mapPoints(pts)

            val gridX = pts[0].toInt()
            val gridY = pts[1].toInt()

            if (gridX in 0 until mapWidth.toInt() && gridY in 0 until mapHeight.toInt()) {
                routeDrawer?.onMapClicked(gridX, gridY)
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

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.concat(mapMatrix)

        drawBaseMap(canvas)

        routeDrawer?.drawRoute(canvas)

        gridMap?.let { map ->
            if (isClusteringEnabled) clusterDrawer.draw(canvas, map.width, map.height)
        }
        canvas.restore()

    }

    fun drawBaseMap(canvas: Canvas) {
        drawZone(canvas, "grass", grassPaint)
        drawZone(canvas, "roadCar", roadCarPaint)
        drawZone(canvas, "asphalt", asphaltPaint)
        drawZone(canvas, "path", pathPaint)
        drawZone(canvas, "water", waterPaint)

        buildings.forEach { building ->
            building.pixels.forEach { pixel ->
                val x = pixel[0].toFloat()
                val y = pixel[1].toFloat()
                canvas.drawRect(x, y, x + 1f, y + 1f, buildingPaint)
            }
        }
    }

    private fun drawZone(canvas: Canvas, key: String, paint: Paint) {
        zones[key]?.forEach { p ->
            canvas.drawRect(p[0].toFloat(), p[1].toFloat(), p[0] + 1f, p[1] + 1f, paint)
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