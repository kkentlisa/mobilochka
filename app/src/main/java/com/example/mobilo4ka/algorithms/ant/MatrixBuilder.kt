package com.example.mobilo4ka.algorithms.ant

import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building

class MatrixBuilder(
    private val aStar: AStarAlgorithm,
    private val isWalkable: (Int, Int) -> Boolean,
    private val isBuilding: (Int, Int) -> Boolean,
    private val getBuildingEntrance: (Int, Int) -> Pair<Int, Int>?,
    private val getBuildingPixels: (Int, Int) -> Set<Pair<Int, Int>>?
) {
    fun build(
        userX: Int, userY: Int,
        landmarks: List<Building>
    ): Triple<Array<DoubleArray>, List<Pair<Int, Int>>, MutableMap<Pair<Int, Int>, List<Pair<Int, Int>>>> {
        val n = landmarks.size
        val totalPoints = n + 1
        val points = mutableListOf(userX to userY) + landmarks.mapNotNull { it.getFirstPixels() }
        val dist = Array(totalPoints) { DoubleArray(totalPoints) }
        val pathMap = mutableMapOf<Pair<Int, Int>, List<Pair<Int, Int>>>()

        for (i in 0 until totalPoints) {
            for (j in i+1 until totalPoints) {
                val path = aStar.findPath(
                    points[i].first, points[i].second,
                    points[j].first, points[j].second,
                    isWalkable,
                    isBuilding,
                    getBuildingEntrance,
                    getBuildingPixels
                )
                val distance = if (path.isNotEmpty()) path.size.toDouble() else Double.POSITIVE_INFINITY
                dist[i][j] = distance
                dist[j][i] = distance
                if (path.isNotEmpty()) {
                    pathMap[Pair(i, j)] = path
                    pathMap[Pair(j, i)] = path.reversed()
                }
            }
            dist[i][i] = 0.0
        }
        return Triple(dist, points, pathMap)
    }
}