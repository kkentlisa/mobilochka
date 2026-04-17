package com.example.mobilo4ka.ui.screens.clustering

import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.ui.graphics.toArgb
import com.example.mobilo4ka.algorithms.clustering.ClusterPoint
import com.example.mobilo4ka.algorithms.clustering.ClusteringMode
import com.example.mobilo4ka.ui.theme.Cluster1
import com.example.mobilo4ka.ui.theme.Cluster2
import com.example.mobilo4ka.ui.theme.Cluster3
import com.example.mobilo4ka.ui.theme.Cluster4
import com.example.mobilo4ka.ui.theme.Cluster5
import com.example.mobilo4ka.ui.theme.Cluster6

class ClusterDrawer {
    var points: List<ClusterPoint> = emptyList()
    var mode: ClusteringMode = ClusteringMode.EUCLIDEAN

    private val clusterColors = listOf(
        Cluster1.toArgb(), Cluster2.toArgb(), Cluster3.toArgb(),
        Cluster4.toArgb(), Cluster5.toArgb(), Cluster6.toArgb()
    )

    private val fillPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 0.4f
    }

    private val regionPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    fun draw(canvas: Canvas, width: Int, height: Int) {
        if (points.isEmpty()) return

        drawRegions(canvas, width, height)

        points.forEach { point ->
            val cx = point.x.toFloat() + 0.5f
            val cy = point.y.toFloat() + 0.5f

            when (mode) {
                ClusteringMode.EUCLIDEAN -> {
                    drawSinglePoint(canvas, cx, cy, point.euclideanClusterId, 1.2f, fillPaint)
                }

                ClusteringMode.ASTAR -> {
                    drawSinglePoint(canvas, cx, cy, point.astarClusterId, 1.2f, fillPaint)
                }

                ClusteringMode.COMPARISON -> {
                    drawSinglePoint(canvas, cx, cy, point.euclideanClusterId, 1f, fillPaint)
                    drawSinglePoint(canvas, cx, cy, point.astarClusterId, 2.4f, strokePaint)
                }
            }
        }
    }

    private fun drawRegions(canvas: Canvas, width: Int, height: Int) {
        val step = 3
        val rectSize = step.toFloat() + 0.1f

        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val closest = points.minByOrNull { p ->
                    val dx = p.x - x
                    val dy = p.y - y
                    dx * dx + dy * dy
                } ?: continue

                val euclidId = closest.euclideanClusterId
                val astarId = closest.astarClusterId

                if (mode == ClusteringMode.COMPARISON) {
                    if (euclidId != -1 && astarId != -1) {
                        if (euclidId == astarId) {
                            regionPaint.color = clusterColors[euclidId % clusterColors.size]
                            regionPaint.alpha = 50
                            canvas.drawRect(
                                x.toFloat(),
                                y.toFloat(),
                                x + rectSize,
                                y + rectSize,
                                regionPaint
                            )
                        } else {
                            regionPaint.color = clusterColors[euclidId % clusterColors.size]
                            regionPaint.alpha = 50
                            canvas.drawRect(
                                x.toFloat(),
                                y.toFloat(),
                                x + rectSize,
                                y + rectSize,
                                regionPaint
                            )

                            if (x % 2 == 0) {
                                regionPaint.color = clusterColors[astarId % clusterColors.size]
                                regionPaint.alpha = 70
                                canvas.drawRect(
                                    x.toFloat(),
                                    y.toFloat(),
                                    x + rectSize,
                                    y + rectSize / 3f,
                                    regionPaint
                                )
                            }
                        }
                    }
                } else {
                    val id = if (mode == ClusteringMode.EUCLIDEAN) euclidId else astarId
                    if (id != -1) {
                        regionPaint.color = clusterColors[id % clusterColors.size]
                        regionPaint.alpha = 50
                        canvas.drawRect(
                            x.toFloat(),
                            y.toFloat(),
                            x + rectSize,
                            y + rectSize,
                            regionPaint
                        )
                    }
                }
            }
        }
    }

    private fun drawSinglePoint(
        canvas: Canvas,
        x: Float,
        y: Float,
        clusterId: Int,
        radius: Float,
        paint: Paint
    ) {
        if (clusterId == -1) return
        paint.color = clusterColors[clusterId % clusterColors.size]
        canvas.drawCircle(x, y, radius, paint)
    }
}