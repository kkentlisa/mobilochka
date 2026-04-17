package com.example.mobilo4ka.ui.map

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.toColorInt
import com.example.mobilo4ka.algorithms.clustering.ClusterPoint
import com.example.mobilo4ka.algorithms.clustering.ClusteringMode
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.screens.astar.RouteDrawer
import com.example.mobilo4ka.ui.screens.clustering.ClusterDrawer
import com.example.mobilo4ka.ui.screens.genetic.GeneticDrawer
import com.example.mobilo4ka.ui.theme.*

class MapView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var gridMap: GridMap? = null
        set(value) { field = value; invalidate() }
    var buildings: List<Building> = emptyList()
        set(value) { field = value; invalidate() }
    var zones: Map<String, List<List<Int>>> = emptyMap()
        set(value) { field = value; invalidate() }

    var routeDrawer: RouteDrawer? = null
    var geneticDrawer: GeneticDrawer? = null
    private val clusterDrawer = ClusterDrawer()

    var isAstarEnabled: Boolean = false
    var isClusteringEnabled: Boolean = false

    var onBuildingClicked: ((Int, Int) -> Unit)? = null
    var onMapClicked: ((Int, Int) -> Unit)? = null

    val mapMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private val minScale = Dimens.mapMinScale
    private val maxScale = Dimens.mapMaxScale
    private val mapWidth = Dimens.mapWidhtSize
    private val mapHeight = Dimens.mapHeightSize

    private val buildingPaint = Paint().apply { color = MapBuilding.toArgb() }
    private val interestingBuildingPaint = Paint().apply { color = "#c4b5a0".toColorInt() }
    private val grassPaint = Paint().apply { color = MapGrass.toArgb() }
    private val roadCarPaint = Paint().apply { color = MapRoadCar.toArgb() }
    private val asphaltPaint = Paint().apply { color = MapAsphalt.toArgb() }
    private val pathPaint = Paint().apply { color = MapPath.toArgb() }
    private val waterPaint = Paint().apply { color = MapWater.toArgb() }

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

            if (currentTransX + newDx > 0) newDx = -currentTransX
            else if (currentTransX + newDx < width - contentWidth) newDx = (width - contentWidth) - currentTransX

            if (contentHeight <= height) {
                val targetY = (height - contentHeight) / 2f
                mapMatrix.postTranslate(0f, targetY - currentTransY)
                newDy = 0f
            } else {
                if (currentTransY + newDy > 0) newDy = -currentTransY
                else if (currentTransY + newDy < height - contentHeight) newDy = (height - contentHeight) - currentTransY
            }

            mapMatrix.postTranslate(newDx, newDy)
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

                if (isAstarEnabled && gridX in 0 until mapWidth.toInt() && gridY in 0 until mapHeight.toInt()) {
                    routeDrawer?.onMapClicked(gridX, gridY)
                }
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

            if (targetScale < minScale) scaleFactor = minScale / currentScale
            else if (targetScale > maxScale) scaleFactor = maxScale / currentScale

            mapMatrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
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


    fun showGeneticRoute(route: List<Pair<Int, Int>>, extra: List<Any>? = null) {
        geneticDrawer?.updateRoute(route)
    }

    fun setStartMarker(point: Pair<Int, Int>?) {
        geneticDrawer?.userStartPoint = point
        invalidate()
    }

    fun updateClustering(points: List<ClusterPoint>, mode: ClusteringMode) {
        clusterDrawer.points = points
        clusterDrawer.mode = mode
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.concat(mapMatrix)

        drawBaseMap(canvas)

        routeDrawer?.drawRoute(canvas)

        geneticDrawer?.draw(canvas)

        if (isClusteringEnabled) {
            gridMap?.let { clusterDrawer.draw(canvas, it.width, it.height) }
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
            val paint = if (!building.name.isNullOrBlank()) interestingBuildingPaint else buildingPaint
            building.pixels.forEach { pixel ->
                canvas.drawRect(pixel[0].toFloat(), pixel[1].toFloat(), pixel[0] + 1f, pixel[1] + 1f, paint)
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
            val offsetX = (width - (mapWidth * scaleToFitHeight)) / 2f
            mapMatrix.postTranslate(offsetX, 0f)
            invalidate()
        }
    }
}