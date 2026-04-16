package com.example.mobilo4ka.algorithms.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
class GeneticAlgorithm(
    private val gridMap: GridMap,
    private val buildings: List<Building>,
    private val populationSize: Int = 80,
    private val generations: Int = 50
) {
    private val aStar = AStarAlgorithm()
    private val buildingMap = buildings.associateBy { it.id }

    // 🔥 КЭШ расстояний (ускоряет в разы)
    private val distanceCache =
        mutableMapOf<Pair<Pair<Int, Int>, Pair<Int, Int>>, Double>()

    fun isWalkable(x: Int, y: Int): Boolean {
        val matrix = gridMap.matrix ?: return false
        return y in matrix.indices && x in matrix[0].indices && matrix[y][x] == 0
    }

    // ❗ БЕЗ suspend
    fun calculateRouteDistance(route: List<Int>, start: Pair<Int, Int>): Double {
        var score = 0.0
        var currentPos = start
        val startTime = LocalTime.now()
        var accumulatedMinutes = 0

        route.forEach { id ->
            val building = buildingMap[id] ?: return@forEach
            val entrance = building.firstEntrance ?: return@forEach

            val key = currentPos to entrance

            val dist = distanceCache.getOrPut(key) {
                val path = aStar.findPath(
                    currentPos.first,
                    currentPos.second,
                    entrance.first,
                    entrance.second
                ) { x, y ->
                    (x == entrance.first && y == entrance.second) || isWalkable(x, y)
                }

                if (path.isEmpty()) return@getOrPut 100000.0
                path.size.toDouble()
            }

            score += dist

            accumulatedMinutes += (dist * 2 / 15).toInt()
            val arrivalTime = startTime.plusMinutes(accumulatedMinutes.toLong())

            val open = building.parsedOpenTime
            val close = building.parsedCloseTime

            if (open != null && close != null) {
                if (arrivalTime.isBefore(open) || arrivalTime.isAfter(close)) {
                    score += 10000.0
                }
            }

            currentPos = entrance
        }

        return score
    }

    suspend fun evolve(
        products: List<String>,
        start: Pair<Int, Int>,
        onStepFound: suspend (List<Pair<Int, Int>>) -> Unit
    ): List<Int> = withContext(Dispatchers.Default) {

        val optionsPerProduct = products.map { prod ->
            buildings.filter { it.hasProduct(prod) }
        }

        if (optionsPerProduct.any { it.isEmpty() }) return@withContext emptyList()

        var population = List(populationSize) {
            optionsPerProduct.map { it.random().id }
        }

        var bestIndividual = population.first()
        var minScore = calculateRouteDistance(bestIndividual, start)

        repeat(generations) { generation ->
            if (generation % 5 == 0) yield()

            population = population.sortedBy {
                calculateRouteDistance(it, start)
            }

            val currentBest = population.first()
            val currentScore = calculateRouteDistance(currentBest, start)

            if (currentScore < minScore) {
                bestIndividual = currentBest
                minScore = currentScore
            }

            val nextGeneration = mutableListOf<List<Int>>()
            nextGeneration.addAll(population.take(10))

            while (nextGeneration.size < populationSize) {
                val parent = population.take(20).random()

                val mutated = if ((0..1).random() == 0) {
                    mutateOrder(parent)
                } else {
                    mutateBuildingChoice(parent, optionsPerProduct)
                }

                nextGeneration.add(mutated)
            }

            population = nextGeneration
        }

        val finalPath = buildFullPath(bestIndividual, start)

        withContext(Dispatchers.Main) {
            onStepFound(finalPath)
        }

        return@withContext bestIndividual
    }

    private fun mutateOrder(route: List<Int>): List<Int> {
        if (route.size < 2) return route
        val mutated = route.toMutableList()
        val i = mutated.indices.random()
        val j = mutated.indices.random()
        mutated[i] = mutated[j].also { mutated[j] = mutated[i] }
        return mutated
    }

    private fun mutateBuildingChoice(
        route: List<Int>,
        options: List<List<Building>>
    ): List<Int> {
        val mutated = route.toMutableList()
        val index = options.indices.random()
        mutated[index] = options[index].random().id
        return mutated
    }

    suspend fun buildFullPath(
        routeIds: List<Int>,
        start: Pair<Int, Int>
    ): List<Pair<Int, Int>> {
        val fullPath = mutableListOf<Pair<Int, Int>>()
        var currentPos = start

        routeIds.forEach { id ->
            yield()

            val building = buildingMap[id]
            val target = building?.firstEntrance ?: return@forEach

            val segment = aStar.findPath(
                currentPos.first,
                currentPos.second,
                target.first,
                target.second
            ) { x, y ->
                (x == target.first && y == target.second) || isWalkable(x, y)
            }

            fullPath.addAll(segment)
            currentPos = target
        }

        return fullPath
    }
}