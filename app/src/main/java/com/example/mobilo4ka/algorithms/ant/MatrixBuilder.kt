package com.example.mobilo4ka.algorithms.ant

import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building

class MatrixBuilder(
    private val aStar: AStarAlgorithm,
    private val isWalkable: (Int, Int) -> Boolean,
    private val isBuilding: (Int, Int) -> Boolean,
    private val getBuildingEntrance: (Int, Int) -> Pair<Int, Int>?,
    private val getBuildingPixels: (Int, Int) -> Pair<Int, Int>?
) {
    fun build(landmarks: List<Building>): Pair<Array<DoubleArray>, List<Pair<Int, Int>>> {
        val n = landmarks.size
        val dist = Array(n) { DoubleArray(n) }

        val points = landmarks.mapNotNull { building ->
            building.getFirstPixels()
        }

        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val path = aStar.findPath(
                    startX = points[i].first,
                    startY = points[i].second,
                    targetX = points[j].first,
                    targetY = points[j].second,
                    isWalkable = isWalkable,
                    isBuilding = isBuilding,
                    getBuildingEntrance = getBuildingEntrance,
                )

                val distance = if (path.isNotEmpty()) path.size.toDouble() else Double.POSITIVE_INFINITY
                dist[i][j] = distance
                dist[j][i] = distance
            }
            dist[i][i] = 0.0
        }

        return Pair(dist, points)
    }
}