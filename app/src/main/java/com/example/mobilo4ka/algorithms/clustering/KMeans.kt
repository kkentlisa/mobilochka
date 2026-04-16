package com.example.mobilo4ka.algorithms.clustering

import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import kotlin.math.pow
import kotlin.math.sqrt

class KMeans(
    private val aStar: AStarAlgorithm,
    private val gridMap: GridMap,
    private val buildings: List<Building>
) {
    private fun isWalkable(x: Int, y: Int): Boolean {
        val matrix = gridMap.matrix
        if (y < 0 || y >= matrix.size || x < 0 || x >= matrix[0].size) return false
        return gridMap.matrix[y][x] == 0
    }

    private fun isBuilding(x: Int, y: Int): Boolean {
        return buildings.any { it.containsPoint(x, y) }
    }

    private fun getBuildingEntrance(x: Int, y: Int): Pair<Int, Int>? {
        return buildings.find { it.containsPoint(x, y) }?.getFirstEntrance()
    }

    fun calculate(
        points: List<ClusterPoint>,
        k: Int,
        mode: ClusteringMode,
        startCentroids: List<Pair<Float, Float>>
    ): List<ClusterPoint> {
        if (points.isEmpty() || k <= 0) return points

        var centroids = startCentroids
        var changed = true
        var iteration = 0

        while (changed && iteration < 30) {
            changed = false
            iteration++

            points.forEach { point ->
                var minDist = Double.MAX_VALUE
                var bestClusterId = 0

                centroids.forEachIndexed { index, center ->
                    val distance = when (mode) {
                        ClusteringMode.EUCLIDEAN -> {
                            sqrt(
                                (point.x - center.first).toDouble().pow(2.0) +
                                        (point.y - center.second).toDouble().pow(2.0)
                            )
                        }

                        else -> {
                            val path = aStar.findPath(
                                point.x, point.y,
                                center.first.toInt(), center.second.toInt(),
                                ::isWalkable, ::isBuilding,
                                ::getBuildingEntrance
                            )
                            if (path.isEmpty()) {
                                10000.0
                            } else {
                                path.size.toDouble()
                            }
                        }
                    }
                    if (distance < minDist) {
                        minDist = distance
                        bestClusterId = index
                    }
                }

                if (mode == ClusteringMode.EUCLIDEAN) {
                    if (point.euclideanClusterId != bestClusterId) {
                        point.euclideanClusterId = bestClusterId
                        changed = true
                    }
                } else {
                    if (point.astarClusterId != bestClusterId) {
                        point.astarClusterId = bestClusterId
                        changed = true
                    }
                }
            }

            centroids = (0 until k).map { id ->
                val clusterMembers = points.filter {
                    (if (mode == ClusteringMode.EUCLIDEAN) it.euclideanClusterId else it.astarClusterId) == id
                }
                if (clusterMembers.isNotEmpty()) {
                    val avgX = clusterMembers.map { it.x }.average().toFloat()
                    val avgY = clusterMembers.map { it.y }.average().toFloat()
                    if (mode == ClusteringMode.ASTAR) {
                        val nearest = aStar.findNearestRoad(
                            avgX.toInt(),
                            avgY.toInt(),
                            ::isWalkable,
                            ::isBuilding
                        )
                        Pair(nearest.first.toFloat(), nearest.second.toFloat())
                    } else {
                        Pair(avgX, avgY)
                    }
                } else {
                    startCentroids[id]
                }
            }
        }
        return points
    }
}