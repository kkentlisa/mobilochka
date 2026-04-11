package com.example.mobilo4ka.ui.screens.astar

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.Looper
import android.widget.Toast
import androidx.compose.ui.graphics.toArgb
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.MapRoute
import android.os.Handler
class RouteDrawer (private val mapView: MapView){

    var currentPath: List<Pair<Int, Int>> = emptyList()
    var startPoint: Pair<Int, Int>? = null
    var endPoint: Pair<Int, Int>? = null

    private val pathRoutePaint = Paint().apply {
        color = MapRoute.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = Dimens.mapRouteWidth
    }

    private val pointPaint = Paint().apply {
        color = MapRoute.toArgb()
        style = Paint.Style.FILL
    }

    private val linePath = Path()

    fun drawRoute (canvas: Canvas) {
        mapView.drawBaseMap(canvas)

        if (currentPath.isNotEmpty()) {
            linePath.reset()
            val first = currentPath[0]
            linePath.moveTo(first.first + 0.5f, first.second + 0.5f)

            for (i in 1 until currentPath.size) {
                linePath.lineTo(currentPath[i].first + 0.5f, currentPath[i].second + 0.5f)
            }
            canvas.drawPath(linePath, pathRoutePaint)
        }

        startPoint?.let { canvas.drawCircle(it.first + 0.5f, it.second + 0.5f, Dimens.mapPointRadius, pointPaint) }
        endPoint?.let { canvas.drawCircle(it.first + 0.5f, it.second + 0.5f, Dimens.mapPointRadius, pointPaint) }
    }
    fun onMapClicked(gridX: Int, gridY: Int) {
        if (startPoint == null || endPoint != null) {
            startPoint = Pair(gridX, gridY)
            endPoint = null
            currentPath = emptyList()
        } else {
            endPoint = Pair(gridX, gridY)
            calculateAStarPath()
        }
        mapView.invalidate()
    }

    private fun calculateAStarPath() {
        val start = startPoint ?: return
        val end = endPoint ?: return
        val aStar = AStarAlgorithm()

        kotlin.concurrent.thread {
            val path = aStar.findPath(
                start.first, start.second,
                end.first, end.second,
                isWalkable = { x, y -> mapView.gridMap?.isWalkable(x, y) ?: false },
                isBuilding = { x, y -> mapView.buildings.any { it.containsPoint(x, y) } },
                getBuildingEntrance = { x, y -> mapView.buildings.find { it.containsPoint(x, y) }?.getFirstEntrance() }
            )

            Handler(Looper.getMainLooper()).post {
                if (path.isEmpty()) {
                    Toast.makeText(mapView.context, "Пути не существует", Toast.LENGTH_SHORT).show()
                }
                currentPath = path
                mapView.invalidate()
            }
        }
    }
}