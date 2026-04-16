package com.example.mobilo4ka.ui.screens.genetic

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.mobilo4ka.ui.map.MapView

class GeneticDrawer(private val mapView: MapView) {

    private var geneticRoute: List<Pair<Int, Int>> = emptyList()
    var userStartPoint: Pair<Int, Int>? = null

    private val geneticPathPaint = Paint().apply {
        color = Color.parseColor("#2196F3")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private val startPointPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val startPointBorderPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        isAntiAlias = true
    }

    fun updateRoute(route: List<Pair<Int, Int>>) {
        this.geneticRoute = route
        mapView.invalidate()
    }

    fun updateStartPoint(point: Pair<Int, Int>?) {
        this.userStartPoint = point
        mapView.invalidate()
    }

    fun draw(canvas: Canvas) {
        // Рисуем маршрут
        if (geneticRoute.isNotEmpty()) {
            val gPath = Path()
            gPath.moveTo(geneticRoute[0].first + 0.5f, geneticRoute[0].second + 0.5f)

            for (point in geneticRoute) {
                gPath.lineTo(point.first + 0.5f, point.second + 0.5f)
            }

            canvas.drawPath(gPath, geneticPathPaint)
        }

        // Рисуем стартовую точку (зеленую)
        userStartPoint?.let { point ->
            canvas.drawCircle(
                point.first + 0.5f,
                point.second + 0.5f,
                1.5f,
                startPointBorderPaint
            )
            canvas.drawCircle(
                point.first + 0.5f,
                point.second + 0.5f,
                1.0f,
                startPointPaint
            )
        }
    }
}