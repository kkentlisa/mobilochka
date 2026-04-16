package com.example.mobilo4ka.algorithms.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap

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

    fun calculateRouteDistance(route: List<Int>, start: Pair<Int, Int>): Double {
        var totalDistance = 0.0
        var currentPos = start
        val startTime = java.time.LocalTime.now()
        var accumulatedMinutes = 0

        route.forEach { id ->
            val building = buildings.find { it.id == id }
            val entrance = building?.firstEntrance
            if (entrance != null) {
                val dx = (entrance.first - currentPos.first).toDouble()
                val dy = (entrance.second - currentPos.second).toDouble()
                val dist = Math.sqrt(dx * dx + dy * dy)
                totalDistance += dist

                accumulatedMinutes += (dist * 2 / 15).toInt()
                val arrivalTime = startTime.plusMinutes(accumulatedMinutes.toLong())

                val open = building.parsedOpenTime
                val close = building.parsedCloseTime
                if (open != null && close != null) {
                    if (arrivalTime.isBefore(open) || arrivalTime.isAfter(close)) {
                        totalDistance += 10000.0
                    }
                }
                currentPos = entrance
            }
        }
        return totalDistance
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

        var population = List(populationSize) { targetIds.shuffled() }
        var bestIndividual = population[0]
        var minDistance = calculateRouteDistance(bestIndividual, start)

        val animationStep = (generations / 3).coerceAtLeast(1)

        repeat(generations) { generation ->
            population = population.sortedBy { calculateRouteDistance(it, start) }
            if (calculateRouteDistance(population[0], start) < minDistance) {
                bestIndividual = population[0]
                minDistance = calculateRouteDistance(bestIndividual, start)
            }

            if (generation % animationStep == 0) {
                onStepFound(buildFullPath(bestIndividual, start))
                Thread.sleep(200)
            }

            val nextGeneration = mutableListOf<List<Int>>()
            val eliteSize = (populationSize * 0.1).toInt().coerceAtLeast(1)
            nextGeneration.addAll(population.take(eliteSize))

            while (nextGeneration.size < populationSize) {
                val parent = population.take(populationSize / 2).random()
                nextGeneration.add(mutate(parent))
            }
            population = nextGeneration
        }
        return bestIndividual
    }

    fun buildFullPath(routeIds: List<Int>, start: Pair<Int, Int>): List<Pair<Int, Int>> {
        val fullPath = mutableListOf<Pair<Int, Int>>()
        var currentPos = start
        routeIds.forEach { id ->
            val building = buildings.find { it.id == id }
            val targetEntrance = building?.firstEntrance
            if (targetEntrance != null) {
                val segment = aStar.findPath(currentPos.first, currentPos.second, targetEntrance.first, targetEntrance.second) { x, y ->
                    (x == targetEntrance.first && y == targetEntrance.second) || isWalkable(x, y)
                }
                fullPath.addAll(segment)
                currentPos = targetEntrance
            }
        }
        return fullPath
    }

    private fun mutate(route: List<Int>): List<Int> {
        if (route.size < 2) return route
        val mutated = route.toMutableList()
        val i = mutated.indices.random(); val j = mutated.indices.random()
        val temp = mutated[i]; mutated[i] = mutated[j]; mutated[j] = temp
        return mutated
    }
}