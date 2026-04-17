package com.example.mobilo4ka.ui.screens.genetic

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.graphics.toArgb
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.theme.*
import com.example.mobilo4ka.ui.theme.AppAlpha.GHOST_BUTTON_ALPHA
import com.example.mobilo4ka.ui.theme.AppAlpha.RADIUS_POINT
import com.example.mobilo4ka.ui.theme.AppAlpha.STROKE_WIDTH_BORDER
import com.example.mobilo4ka.ui.theme.AppAlpha.STROKE_WIDTH_PATH

class GeneticDrawer(private val mapView: MapView) {

    private var geneticRoute: List<Pair<Int, Int>> = emptyList()
    var userStartPoint: Pair<Int, Int>? = null
    var visitedBuildingIds: List<Int> = emptyList()

    private val entrancePointPaint = Paint().apply {
        color = Line.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val geneticPathPaint = Paint().apply {
        color = TsuBlue.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH_PATH
        isAntiAlias = true
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private val startPointPaint = Paint().apply {
        color = Cluster3.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val startPointBorderPaint = Paint().apply {
        color = SurfaceWhite.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH_BORDER
        isAntiAlias = true
    }

    fun updateRoute(route: List<Pair<Int, Int>>) {
        this.geneticRoute = route
        mapView.postInvalidateOnAnimation()
    }

    fun updateStartPoint(point: Pair<Int, Int>?) {
        this.userStartPoint = point
        mapView.invalidate()
    }

    fun draw(canvas: Canvas, buildingsData: List<Building>) {
        if (geneticRoute.isNotEmpty()) {
            val gPath = Path()
            gPath.moveTo(geneticRoute[0].first + GHOST_BUTTON_ALPHA, geneticRoute[0].second + GHOST_BUTTON_ALPHA)
            for (i in 1 until geneticRoute.size) {
                gPath.lineTo(geneticRoute[i].first + GHOST_BUTTON_ALPHA, geneticRoute[i].second + GHOST_BUTTON_ALPHA)
            }
            canvas.drawPath(gPath, geneticPathPaint)
        }

        visitedBuildingIds.forEach { id ->
            val building = buildingsData.find { it.id == id }
            building?.getFirstEntrance()?.let { entrance ->
                canvas.drawCircle(
                    entrance.first + GHOST_BUTTON_ALPHA,
                    entrance.second + GHOST_BUTTON_ALPHA,
                    RADIUS_POINT,
                    entrancePointPaint
                )
            }
        }

        userStartPoint?.let { point ->
            canvas.drawCircle(point.first + GHOST_BUTTON_ALPHA, point.second + GHOST_BUTTON_ALPHA, 1.0f, startPointPaint)
            canvas.drawCircle(point.first + GHOST_BUTTON_ALPHA, point.second + GHOST_BUTTON_ALPHA, 1.0f, startPointBorderPaint)
        }
    }
}