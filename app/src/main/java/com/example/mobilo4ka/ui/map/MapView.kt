package com.example.mobilo4ka.ui.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.View
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withMatrix

class MapView (context: Context, attrs: AttributeSet? = null): View(context, attrs){
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

    private val grassPaint = Paint().apply {
        color = "#cbe88d".toColorInt()
    }

    private val roadCarPaint = Paint().apply {
        color = "#d4d4d4".toColorInt()
    }

    private val asphaltPaint = Paint().apply {
        color = "#f3efdd".toColorInt()
    }

    private val pathPaint = Paint().apply {
        color = Color.WHITE
    }

    private val waterPaint = Paint().apply {
        color = "#9ad8f7".toColorInt()
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
        }
    }
    fun setupInitialView() {
        mapMatrix.reset()
        mapMatrix.postScale(5f, 5f)
        mapMatrix.postTranslate(0f, 800f)
        invalidate()
    }
}