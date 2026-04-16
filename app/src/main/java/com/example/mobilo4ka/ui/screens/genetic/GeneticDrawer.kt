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
        color = Color.BLUE
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
        isAntiAlias = true
    }

    private val startPointPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun updateRoute(route: List<Pair<Int, Int>>) {
        this.geneticRoute = route
        mapView.invalidate()
    }

    fun draw(canvas: Canvas) {
        if (geneticRoute.isNotEmpty()) {
            val gPath = Path()
            gPath.moveTo(geneticRoute[0].first + 0.5f, geneticRoute[0].second + 0.5f)
            geneticRoute.forEach { gPath.lineTo(it.first + 0.5f, it.second + 0.5f) }
            canvas.drawPath(gPath, geneticPathPaint)
        }

        userStartPoint?.let {
            canvas.drawCircle(it.first + 0.5f, it.second + 0.5f, 0.8f, startPointPaint)
        }
    }
}