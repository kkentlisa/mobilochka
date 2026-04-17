package com.example.mobilo4ka.ui.screens.ants

import android.graphics.*
import androidx.compose.ui.graphics.toArgb
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.screens.astar.RouteDrawer
import com.example.mobilo4ka.ui.theme.*

class AntDrawer(mapView: MapView) : RouteDrawer(mapView) {
    var landmarks: List<Pair<Int, Int>> = emptyList()
    var selected: Set<Pair<Int, Int>> = emptySet()
    override var startPoint: Pair<Int, Int>? = null
    var route: List<Pair<Int, Int>> = emptyList()

    private val landmarkPaint = Paint().apply {
        color = AntLandmark.toArgb()
        style = Paint.Style.FILL
    }
    private val selectedPaint = Paint().apply {
        color = AntSelected.toArgb()
        style = Paint.Style.FILL
    }
    private val startPaint = Paint().apply {
        color = AntStart.toArgb()
        style = Paint.Style.STROKE
        strokeWidth = Dimens.antStartWight
    }
    private val routePaint = Paint().apply {
        color = AntRoute.toArgb()
        strokeWidth = Dimens.mapRouteWidth
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(1.5f, 0.3f), 0f)
    }

    override fun drawRoute(canvas: Canvas) {
        if (route.size >= 2) {
            val path = Path()
            path.moveTo(route[0].first.toFloat(), route[0].second.toFloat())
            for (i in 1 until route.size) {
                path.lineTo(route[i].first.toFloat() + 0.5f, route[i].second.toFloat() + 0.5f)
            }
            canvas.drawPath(path, routePaint)
        }

        landmarks.forEach { (x, y) ->
            val paint = if (selected.contains(x to y)) selectedPaint else landmarkPaint
            canvas.drawCircle(x.toFloat() + 0.5f, y.toFloat() + 0.5f, 0.8f, paint)
        }

        startPoint?.let { (x, y) ->
            canvas.drawCircle(x.toFloat() + 0.5f, y.toFloat() + 0.5f, 1.2f, startPaint)
            canvas.drawCircle(x.toFloat() + 0.5f, y.toFloat() + 0.5f, 0.8f, Paint().apply {
                color = AntStart.toArgb(); style = Paint.Style.FILL })
        }
    }
}