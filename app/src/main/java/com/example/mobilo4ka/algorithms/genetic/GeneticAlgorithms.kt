package com.example.mobilo4ka.algorithms.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import java.time.LocalTime
import java.time.Duration
import kotlin.math.max
import kotlin.random.Random

class GeneticAlgorithm(
    private val gridMap: GridMap,
    private val buildings: List<Building>,
    private val populationSize: Int = 100,
    private val generations: Int = 500,
    private val mutationRate: Double = 0.05,
    private val speedKmh: Double = 5.0
) {

    // ==================== ПЕРЕИСПОЛЬЗОВАНИЕ ИЗ ASTAR ====================

    private val aStar = AStarAlgorithm()

    private fun isWalkable(x: Int, y: Int): Boolean {
        val matrix = gridMap.matrix ?: return false
        if (y !in matrix.indices || x !in matrix[0].indices) return false
        return matrix[y][x] == 0
    }

    private fun findPath(start: Pair<Int, Int>, end: Pair<Int, Int>): List<Pair<Int, Int>> {
        return aStar.findPath(start.first, start.second, end.first, end.second, ::isWalkable)
    }

    private fun distanceBetween(p1: Pair<Int, Int>, p2: Pair<Int, Int>): Int {
        return findPath(p1, p2).size
    }

    // ==================== МОДЕЛЬ ХРОМОСОМЫ ====================

    data class Chromosome(
        val placeIds: List<Int>,
        var fitness: Double = 0.0
    )

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private fun getEntrance(building: Building): Pair<Int, Int> {
        return building.firstEntrance
            ?: throw IllegalArgumentException("У здания ${building.id} нет входа и пикселей")
    }

    private fun getBuildingById(id: Int): Building {
        return buildings.find { it.id == id }
            ?: throw IllegalArgumentException("Здание с id $id не найдено")
    }

    // ==================== РАСЧЁТ РАССТОЯНИЙ ====================

    private fun calculateTotalDistance(
        route: List<Int>,
        startPosition: Pair<Int, Int>
    ): Int {
        if (route.isEmpty()) return 0

        var totalDistance = 0
        var currentPos = startPosition

        for (buildingId in route) {
            val building = getBuildingById(buildingId)
            val entrance = getEntrance(building)
            totalDistance += distanceBetween(currentPos, entrance)
            currentPos = entrance
        }

        return totalDistance
    }

    // ==================== РАСЧЁТ ВРЕМЕНИ ПРИБЫТИЯ ====================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateArrivalTimes(
        route: List<Int>,
        startPosition: Pair<Int, Int>,
        startTime: LocalTime
    ): List<LocalTime> {
        val arrivalTimes = mutableListOf<LocalTime>()
        var currentTime = startTime
        var currentPos = startPosition

        for (buildingId in route) {
            val building = getBuildingById(buildingId)
            val entrance = getEntrance(building)
            val dist = distanceBetween(currentPos, entrance)
            val seconds = (dist / speedKmh * 3600).toLong()
            currentTime = currentTime.plusSeconds(seconds)
            arrivalTimes.add(currentTime)
            currentPos = entrance
        }

        return arrivalTimes
    }

    // ==================== FITNESS-ФУНКЦИЯ ====================

    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateFitness(
        chromosome: Chromosome,
        startPosition: Pair<Int, Int>,
        startTime: LocalTime
    ): Double {
        var penalty = 0.0

        val totalDistance = calculateTotalDistance(chromosome.placeIds, startPosition)
        val arrivalTimes = calculateArrivalTimes(chromosome.placeIds, startPosition, startTime)

        for (i in chromosome.placeIds.indices) {
            val building = getBuildingById(chromosome.placeIds[i])
            val arrival = arrivalTimes.getOrNull(i) ?: continue

            val closeTime = building.parsedCloseTime
            if (closeTime != null) {
                if (arrival.isAfter(closeTime)) {
                    penalty += 10000.0
                } else if (arrival.isAfter(closeTime.minusSeconds(1800))) {
                    val minutesLate = Duration.between(arrival, closeTime).toMinutes()
                    penalty += max(0, (30 - minutesLate)) * 10
                }
            }
        }

        // Чем меньше расстояние и штраф, тем лучше (возвращаем отрицательное значение)
        return -(totalDistance + penalty)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun evaluatePopulation(
        population: MutableList<Chromosome>,
        startPosition: Pair<Int, Int>,
        startTime: LocalTime
    ) {
        for (chromosome in population) {
            chromosome.fitness = calculateFitness(chromosome, startPosition, startTime)
        }
    }

    // ==================== ГЕНЕТИЧЕСКИЕ ОПЕРАТОРЫ ====================

    private fun initializePopulation(requiredPlaceIds: List<Int>): MutableList<Chromosome> {
        val population = mutableListOf<Chromosome>()
        repeat(populationSize) {
            val shuffledIds = requiredPlaceIds.shuffled()
            population.add(Chromosome(shuffledIds))
        }
        return population
    }

    private fun selection(population: MutableList<Chromosome>): List<Chromosome> {
        // Турнирная селекция: выбираем лучших
        val tournamentSize = (populationSize * 0.2).toInt().coerceAtLeast(2)
        val sorted = population.sortedByDescending { it.fitness }
        return sorted.take(tournamentSize)
    }

    private fun crossover(parent1: Chromosome, parent2: Chromosome): Chromosome {
        val size = parent1.placeIds.size
        if (size < 2) return parent1

        // Выбираем случайный отрезок
        val start = Random.nextInt(0, size - 1)
        val end = Random.nextInt(start + 1, size)

        val childIds = MutableList(size) { -1 }

        // Копируем отрезок от первого родителя
        for (i in start..end) {
            childIds[i] = parent1.placeIds[i]
        }

        // Заполняем остальное от второго родителя
        var parent2Index = 0
        for (i in 0 until size) {
            if (childIds[i] == -1) {
                while (parent2Index < size && childIds.contains(parent2.placeIds[parent2Index])) {
                    parent2Index++
                }
                if (parent2Index < size) {
                    childIds[i] = parent2.placeIds[parent2Index]
                    parent2Index++
                }
            }
        }

        return Chromosome(childIds)
    }

    private fun crossoverPopulation(parents: List<Chromosome>): MutableList<Chromosome> {
        val offspring = mutableListOf<Chromosome>()

        while (offspring.size < populationSize && parents.size >= 2) {
            val parent1 = parents.random()
            val parent2 = parents.random()
            val child1 = crossover(parent1, parent2)
            val child2 = crossover(parent2, parent1)
            offspring.add(child1)
            offspring.add(child2)
        }

        return offspring.take(populationSize).toMutableList()
    }

    private fun mutate(chromosome: Chromosome): Chromosome {
        val ids = chromosome.placeIds.toMutableList()
        if (ids.size < 2) return chromosome

        // Случайно меняем два места местами
        val idx1 = Random.nextInt(ids.size)
        var idx2 = Random.nextInt(ids.size)
        while (idx1 == idx2 && ids.size > 1) {
            idx2 = Random.nextInt(ids.size)
        }

        val temp = ids[idx1]
        ids[idx1] = ids[idx2]
        ids[idx2] = temp

        return Chromosome(ids)
    }

    private fun mutatePopulation(population: MutableList<Chromosome>): MutableList<Chromosome> {
        return population.map { chromosome ->
            if (Random.nextDouble() < mutationRate) {
                mutate(chromosome)
            } else {
                chromosome
            }
        }.toMutableList()
    }

    private fun preserveElites(
        oldPopulation: MutableList<Chromosome>,
        newPopulation: MutableList<Chromosome>,
        eliteCount: Int
    ) {
        val elites = oldPopulation.sortedByDescending { it.fitness }.take(eliteCount)
        for (i in 0 until eliteCount) {
            if (i < newPopulation.size) {
                newPopulation[i] = elites[i]
            } else {
                newPopulation.add(elites[i])
            }
        }
    }

    // ==================== ГЛАВНЫЙ МЕТОД ЭВОЛЮЦИИ ====================

    @RequiresApi(Build.VERSION_CODES.O)
    fun evolve(
        requiredProducts: List<String>,
        startPosition: Pair<Int, Int>,
        startTime: LocalTime
    ): Chromosome {
        // 1. Получаем ID обязательных заведений
        val requiredPlaceIds = getRequiredPlaceIds(requiredProducts)
        if (requiredPlaceIds.size <= 1) {
            return Chromosome(requiredPlaceIds)
        }

        // 2. Создаём начальную популяцию
        var population = initializePopulation(requiredPlaceIds)
        evaluatePopulation(population, startPosition, startTime)

        // 3. Основной цикл эволюции
        repeat(generations) {
            // Селекция
            val parents = selection(population)

            // Кроссинговер
            var offspring = crossoverPopulation(parents)

            // Мутация
            offspring = mutatePopulation(offspring)

            // Оценка нового поколения
            evaluatePopulation(offspring, startPosition, startTime)

            // Элитизм (сохраняем лучших)
            preserveElites(population, offspring, (populationSize * 0.1).toInt().coerceAtLeast(1))

            population = offspring
        }

        // 4. Возвращаем лучшую особь
        return population.maxByOrNull { it.fitness } ?: population.first()
    }

    // ==================== ПОСТРОЕНИЕ ПУТИ ДЛЯ ОТРИСОВКИ ====================

    fun buildFullPath(
        route: List<Int>,
        startPosition: Pair<Int, Int>
    ): List<Pair<Int, Int>> {
        val fullPath = mutableListOf<Pair<Int, Int>>()
        if (route.isEmpty()) return fullPath

        var currentPos = startPosition

        for (buildingId in route) {
            val building = getBuildingById(buildingId)
            val entrance = getEntrance(building)
            val pathSegment = findPath(currentPos, entrance)
            fullPath.addAll(pathSegment)
            currentPos = entrance
        }

        return fullPath.distinct()
    }

    // ==================== ПРОДУКТЫ → ЗАВЕДЕНИЯ ====================

    fun getRequiredPlaceIds(requiredProducts: List<String>): List<Int> {
        val productToPlaces = mutableMapOf<String, MutableList<Building>>()

        // Строим карту: продукт → список заведений
        for (building in buildings) {
            for (product in building.menu!!) {
                if (!productToPlaces.containsKey(product)) {
                    productToPlaces[product] = mutableListOf()
                }
                productToPlaces[product]?.add(building)
            }
        }

        // Собираем ID заведений для каждого продукта
        val requiredPlaceIds = mutableSetOf<Int>()
        for (product in requiredProducts) {
            val places = productToPlaces[product]
            if (!places.isNullOrEmpty()) {
                // Берём первое заведение, где есть продукт
                requiredPlaceIds.add(places[0].id)
            }
        }

        return requiredPlaceIds.toList()
    }

    fun getRequiredPlaces(requiredProducts: List<String>): List<Building> {
        val ids = getRequiredPlaceIds(requiredProducts)
        return ids.map { getBuildingById(it) }
    }
}