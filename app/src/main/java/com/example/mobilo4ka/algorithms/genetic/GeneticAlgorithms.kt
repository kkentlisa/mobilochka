package com.example.mobilo4ka.algorithms.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlin.math.sqrt
import com.example.mobilo4ka.utils.parseTimeToMinutes

@RequiresApi(Build.VERSION_CODES.O)
class GeneticAlgorithm(
    private val gridMap: GridMap,
    private val buildings: List<Building>,
    private val populationSize: Int = 80,
    private val generations: Int = 50,
    private val speed: Double = 1.0
) {

    private val aStar = AStarAlgorithm()
    private val buildingMap = buildings.associateBy { it.id }

    data class Gene(val productIndex: Int, val buildingId: Int)

    private fun isAnyBuilding(x: Int, y: Int): Boolean {
        return buildings.any { it.containsPoint(x, y) }
    }

    private fun getEntranceForBuilding(x: Int, y: Int): Pair<Int, Int>? {
        return buildings.find { it.containsPoint(x, y) }?.getFirstEntrance()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateFitness(route: List<Gene>, start: Pair<Int, Int>): Double {
        val now = java.time.LocalTime.now()
        val startMinutes = now.hour * 60 + now.minute

        var currentTimeInMinutes = startMinutes.toDouble()
        var penalty = 0.0
        var currentPos = start

        route.forEach { gene ->
            val building = buildingMap[gene.buildingId] ?: return@forEach
            val target = building.getFirstEntrance() ?: return@forEach

            val dx = (target.first - currentPos.first).toDouble()
            val dy = (target.second - currentPos.second).toDouble()

            val distance = sqrt(dx * dx + dy * dy)
            val travelTime = distance / speed

            currentTimeInMinutes += travelTime

            val closeMinutes = parseTimeToMinutes(building.closeTime)

            if (currentTimeInMinutes > closeMinutes) {
                penalty += (currentTimeInMinutes - closeMinutes) * 1000.0
            }

            penalty += travelTime * 0.05

            currentPos = target
        }

        return currentTimeInMinutes + penalty
    }

    private fun mutateAdvanced(
        route: List<Gene>,
        options: List<List<Building>>
    ): List<Gene> {
        val mutated = route.toMutableList()

        if (Math.random() < 0.5) {
            val i = mutated.indices.random()
            val productIdx = mutated[i].productIndex
            mutated[i] = Gene(productIdx, options[productIdx].random().id)
        } else {
            val i = mutated.indices.random()
            val j = mutated.indices.random()
            mutated[i] = mutated[j].also { mutated[j] = mutated[i] }
        }

        return mutated
    }

    suspend fun evolve(
        products: List<String>,
        start: Pair<Int, Int>,
        onStepFound: suspend (List<Pair<Int, Int>>, List<Int>) -> Unit
    ): List<Int> = withContext(Dispatchers.Default) {

        val optionsPerProduct = products.map { prod ->
            buildings.filter { it.menu.contains(prod) }
        }

        if (optionsPerProduct.any { it.isEmpty() }) return@withContext emptyList()

        var population = List(populationSize) {
            optionsPerProduct.mapIndexed { index, variants ->
                Gene(index, variants.random().id)
            }.shuffled()
        }

        var bestIndividual = population.first()

        repeat(generations) { gen ->

            if (gen % 10 == 0) yield()

            population = population.sortedBy { calculateFitness(it, start) }

            if (calculateFitness(population.first(), start) <
                calculateFitness(bestIndividual, start)
            ) {
                bestIndividual = population.first()
            }

            val nextGeneration = mutableListOf<List<Gene>>()

            nextGeneration.addAll(population.take(populationSize / 5))

            while (nextGeneration.size < populationSize) {
                val parent = population.take(populationSize / 2).random()
                nextGeneration.add(mutateAdvanced(parent, optionsPerProduct))
            }

            population = nextGeneration
        }

        val bestRouteIds = bestIndividual.map { it.buildingId }.distinct()
        val finalPath = buildFullPath(bestRouteIds, start)

        withContext(Dispatchers.Main) {
            onStepFound(finalPath, bestRouteIds)
        }

        return@withContext bestRouteIds
    }

    suspend fun buildFullPath(
        routeIds: List<Int>,
        start: Pair<Int, Int>
    ): List<Pair<Int, Int>> {

        val fullPath = mutableListOf<Pair<Int, Int>>()
        var currentPos = start
        var lastBuildingId: Int? = null

        routeIds.forEach { id ->
            yield()

            val currentBuilding = buildingMap[id] ?: return@forEach
            val targetEntrance = currentBuilding.getFirstEntrance() ?: return@forEach
            val fromBuilding = lastBuildingId?.let { buildingMap[it] }

            val segment = aStar.findPath(
                startX = currentPos.first,
                startY = currentPos.second,
                targetX = targetEntrance.first,
                targetY = targetEntrance.second,
                isWalkable = { x, y ->
                    gridMap.isWalkable(x, y) ||
                            fromBuilding?.containsPoint(x, y) == true ||
                            currentBuilding.containsPoint(x, y)
                },
                isBuilding = { x, y ->
                    val isCurrentOrTarget =
                        fromBuilding?.containsPoint(x, y) == true ||
                                currentBuilding.containsPoint(x, y)

                    isAnyBuilding(x, y) && !isCurrentOrTarget
                },
                getBuildingEntrance = { x, y ->
                    getEntranceForBuilding(x, y)
                }
            )

            if (segment.isEmpty()) return emptyList()

            val filtered = if (fullPath.isNotEmpty() &&
                segment.first() == fullPath.last()
            ) segment.drop(1) else segment

            fullPath.addAll(filtered)

            currentPos = targetEntrance
            lastBuildingId = id
        }

        return fullPath
    }
}
