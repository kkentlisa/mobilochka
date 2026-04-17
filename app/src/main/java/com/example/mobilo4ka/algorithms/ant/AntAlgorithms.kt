package com.example.mobilo4ka.algorithms.ant

import kotlin.math.pow
import kotlin.random.Random

class AntAlgorithm(
    val alpha: Double = 1.0,
    val beta: Double = 2.0,
    val evaporation: Double = 0.5,
    val qValue: Double = 100.0,
    val iterations: Int = 100
) {
    private val random = Random

    fun solve(dist: Array<DoubleArray>, startIndex: Int): Pair<List<Int>, Double> {
        val n = dist.size

        if (n <= 1) {
            val path = if (n == 1) listOf(startIndex) else emptyList()
            return Pair(path, 0.0)
        }

        val pheromones = Array(n) { DoubleArray(n) { 0.1 } }
        val heuristic = Array(n) { i ->
            DoubleArray(n) { j ->
                if (i != j && dist[i][j] != 0.0) 1.0 / dist[i][j] else 0.0
            }
        }

        var bestPath = emptyList<Int>()
        var bestLength = Double.POSITIVE_INFINITY

        (1..iterations).forEach { _ ->
            val allTours = mutableListOf<List<Int>>()

            (1..n).forEach { _ ->
                val tour = buildAntTour(startIndex, n, pheromones, heuristic)
                allTours.add(tour)
            }

            for (i in 0 until n) {
                for (j in 0 until n) {
                    pheromones[i][j] *= (1.0 - evaporation)
                }
            }

            for (tour in allTours) {
                val currentLen = calculateTourLength(tour, dist)

                if (currentLen < bestLength) {
                    bestLength = currentLen
                    bestPath = tour
                }

                depositPheromones(tour, currentLen, pheromones)
            }
        }

        return Pair(bestPath, bestLength)
    }

    private fun buildAntTour(start: Int, n: Int, ph: Array<DoubleArray>, heur: Array<DoubleArray>): List<Int> {
        val tour = mutableListOf<Int>()
        tour.add(start)

        val visited = BooleanArray(n)
        visited[start] = true

        (1 until n).forEach { _ ->
            val current = tour.last()
            val next = selectNextNode(current, visited, ph, heur, n)

            if (next != -1) {
                tour.add(next)
                visited[next] = true
            }
        }
        return tour
    }

    private fun selectNextNode(current: Int, visited: BooleanArray, ph: Array<DoubleArray>, heur: Array<DoubleArray>, n: Int): Int {
        val probs = DoubleArray(n)
        var sum = 0.0

        for (i in 0 until n) {
            if (!visited[i]) {
                val p = ph[current][i].pow(alpha) * heur[current][i].pow(beta)
                probs[i] = p
                sum += p
            }
        }

        if (sum == 0.0) {
            for (i in 0 until n) {
                if (!visited[i]) return i
            }
            return -1
        }

        var r = random.nextDouble() * sum
        for (i in 0 until n) {
            if (!visited[i]) {
                r -= probs[i]
                if (r <= 0) return i
            }
        }
        return -1
    }

    private fun calculateTourLength(tour: List<Int>, dist: Array<DoubleArray>): Double {
        var total = 0.0
        for (i in 0 until tour.size - 1) {
            total += dist[tour[i]][tour[i + 1]]
        }
        return total
    }

    private fun depositPheromones(tour: List<Int>, length: Double, ph: Array<DoubleArray>) {
        val bonus = qValue / length
        for (i in 0 until tour.size - 1) {
            val u = tour[i]
            val v = tour[i + 1]
            ph[u][v] += bonus
            ph[v][u] += bonus
        }
    }
}