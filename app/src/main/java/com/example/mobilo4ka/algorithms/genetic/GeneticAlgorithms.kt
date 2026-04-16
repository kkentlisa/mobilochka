package com.example.mobilo4ka.algorithms.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import kotlin.random.Random

@RequiresApi(Build.VERSION_CODES.O)
class GeneticAlgorithm(
    private val gridMap: GridMap,
    private val buildings: List<Building>,
    private val populationSize: Int = 100,
    private val generations: Int = 150
) {
    private val aStar = AStarAlgorithm()

    fun isWalkable(x: Int, y: Int): Boolean {
        val matrix = gridMap.matrix ?: return false
        return y in matrix.indices && x in matrix[0].indices && matrix[y][x] == 0
    }

    fun buildFullPath(routeIds: List<Int>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
        val fullPath = mutableListOf<Pair<Int, Int>>()
        var currentPos = start

        routeIds.forEach { id ->
            val building = buildings.find { it.id == id }
            val targetEntrance = building?.firstEntrance

            if (targetEntrance != null) {
                val segment = aStar.findPath(
                    currentPos.first, currentPos.second,
                    targetEntrance.first, targetEntrance.second
                ) { x, y ->
                    (x == targetEntrance.first && y == targetEntrance.second) || isWalkable(x, y)
                }

                if (segment.isNotEmpty()) {
                    fullPath.addAll(segment)
                } else {
                    fullPath.add(currentPos)
                    fullPath.add(targetEntrance)
                }
                currentPos = targetEntrance
            }
        }
        return fullPath
    }
    private val distanceCache = mutableMapOf<String, Int>()

    private fun getCachedDistance(start: Pair<Int, Int>, end: Pair<Int, Int>): Int {
        val key = "${start.first},${start.second}_${end.first},${end.second}"
        return distanceCache.getOrPut(key) {
            aStar.findPath(start.first, start.second, end.first, end.second, ::isWalkable).size
        }
    }

    fun evolve(
        products: List<String>,
        start: Pair<Int, Int>,
        onStepFound: (List<Pair<Int, Int>>) -> Unit
    ): List<Int> {
        val targetIds = products.mapNotNull { prod ->
            buildings.find { it.name != null && it.menu.contains(prod) }?.id
        }.distinct()

        if (targetIds.isEmpty()) return emptyList()

        var population = List(20) { targetIds.shuffled() }
        var bestIndividual = population[0]
        var minDistance = calculateRouteDistance(bestIndividual, start)

        val totalGenerations = 100
        val animationStep = totalGenerations / 3

        repeat(totalGenerations) { generation ->
            population = population.sortedBy { calculateRouteDistance(it, start) }

            val currentBest = population[0]
            val currentDist = calculateRouteDistance(currentBest, start)

            if (currentDist < minDistance) {
                minDistance = currentDist
                bestIndividual = currentBest
            }

            if (generation % animationStep == 0) {
                val intermediatePath = buildFullPath(bestIndividual, start)
                onStepFound(intermediatePath)

                Thread.sleep(400)
            }

            val nextGeneration = mutableListOf<List<Int>>()
            nextGeneration.addAll(population.take(5))

            while (nextGeneration.size < 20) {
                val parent = population.take(10).random()
                nextGeneration.add(mutate(parent))
            }
            population = nextGeneration
        }

        return bestIndividual
    }

    private fun calculateRouteDistance(route: List<Int>, start: Pair<Int, Int>): Double {
        var total = 0.0
        var currentPos = start

        route.forEach { id ->
            val building = buildings.find { it.id == id }
            val entrance = building?.firstEntrance
            if (entrance != null) {
                val dx = (entrance.first - currentPos.first).toDouble()
                val dy = (entrance.second - currentPos.second).toDouble()
                total += Math.sqrt(dx * dx + dy * dy)
                currentPos = entrance
            }
        }
        return total
    }

    private fun mutate(route: List<Int>): List<Int> {
        if (route.size < 2) return route
        val mutated = route.toMutableList()
        val i = mutated.indices.random()
        val j = mutated.indices.random()
        val temp = mutated[i]
        mutated[i] = mutated[j]
        mutated[j] = temp
        return mutated
    }

}