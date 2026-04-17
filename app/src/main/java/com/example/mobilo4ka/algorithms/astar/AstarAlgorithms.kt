package com.example.mobilo4ka.algorithms.astar

import kotlin.math.abs

data class Node(
    val x: Int,
    val y: Int,
    var g: Int = 0,
    var h: Int = 0,
    var parent: Node? = null
) {
    val f: Int get() = g + h
    val pos: Pair<Int, Int> get() = Pair(x, y)
}

class AStarAlgorithm {
    private val customObstacles = mutableSetOf<Pair<Int, Int>>()

    private fun getBuildingPixels(startX: Int, startY: Int, isBuilding: (Int, Int) -> Boolean): Set<Pair<Int, Int>> {
        val pixels = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        val start = Pair(startX, startY)
        queue.add(start)
        pixels.add(start)

        val dirs = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))
        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            for (m in dirs) {
                val nx = curr.first + m.first
                val ny = curr.second + m.second
                val next = Pair(nx, ny)
                if (!pixels.contains(next) && isBuilding(nx, ny)) {
                    pixels.add(next)
                    queue.add(next)
                }
            }
        }
        return pixels
    }

    private fun findSegment(
        startX: Int,
        startY: Int,
        canStep: (Int, Int) -> Boolean,
        isTarget: (Int, Int) -> Boolean
    ): List<Pair<Int, Int>> {
        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()
        val parents = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()
        val startPoint = Pair(startX, startY)

        queue.add(startPoint)
        visited.add(startPoint)
        val directions = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            if (isTarget(curr.first, curr.second)) return reconstruct(curr, parents)

            for (move in directions) {
                val nx = curr.first + move.first
                val ny = curr.second + move.second
                val next = Pair(nx, ny)

                val maxSearchRadius = 200
                if (abs(nx - startX) < maxSearchRadius && abs(ny - startY) < maxSearchRadius &&
                    !visited.contains(next) && canStep(nx, ny) && !customObstacles.contains(next)) {
                    visited.add(next)
                    parents[next] = curr
                    queue.add(next)
                }
            }
        }
        return listOf(startPoint)
    }

    fun findPath(
        startX: Int, startY: Int,
        targetX: Int, targetY: Int,
        isWalkable: (Int, Int) -> Boolean,
        isBuilding: (Int, Int) -> Boolean,
        getBuildingEntrance: (Int, Int) -> Pair<Int, Int>? = { _, _ -> null }
    ): List<Pair<Int, Int>> {

        val fullStartPath = mutableListOf<Pair<Int, Int>>()
        if (isBuilding(startX, startY)) {
            val entrance = getBuildingEntrance(startX, startY)
            if (entrance != null) {
                val pathInside = findSegment(startX, startY,
                    { x, y -> isBuilding(x, y) },
                    { x, y -> x == entrance.first && y == entrance.second })
                fullStartPath.addAll(pathInside)
                val pathToRoad = findSegment(entrance.first, entrance.second,
                     { x, y -> !isBuilding(x, y) ||
                            (x == entrance.first && y == entrance.second) }, { x, y -> isWalkable(x, y) })
                if (pathToRoad.size > 1) fullStartPath.addAll(pathToRoad.drop(1))
            } else {
                val myBuilding = getBuildingPixels(startX, startY, isBuilding)
                val pathToRoad = findSegment(startX, startY,
                    { x, y -> !isBuilding(x, y) || myBuilding.contains(Pair(x, y)) },
                    { x, y -> isWalkable(x, y) })
                fullStartPath.addAll(pathToRoad)
            }
        } else {
            fullStartPath.addAll(findSegment(startX, startY,
                { x, y -> !isBuilding(x, y) }, { x, y -> isWalkable(x, y) }))
        }
        val realStart = fullStartPath.last()

        val fullEndPath = mutableListOf<Pair<Int, Int>>()
        if (isBuilding(targetX, targetY)) {
            val entrance = getBuildingEntrance(targetX, targetY)
            if (entrance != null) {
                val pathInside = findSegment(entrance.first, entrance.second,
                     { x, y -> isBuilding(x, y) }, { x, y -> x == targetX && y == targetY })
                val pathToRoad = findSegment(entrance.first, entrance.second,
                     { x, y -> !isBuilding(x, y) || (x == entrance.first && y == entrance.second) }, { x, y -> isWalkable(x, y) })

                fullEndPath.addAll(pathToRoad.reversed())
                if (pathInside.size > 1) fullEndPath.addAll(pathInside.drop(1))
            } else {
                val myBuilding = getBuildingPixels(targetX, targetY, isBuilding)
                val pathToRoad = findSegment(targetX, targetY,
                    { x, y -> !isBuilding(x, y) || myBuilding.contains(Pair(x, y)) }, { x, y -> isWalkable(x, y) })
                fullEndPath.addAll(pathToRoad.reversed())
            }
        } else {
            val pathToRoad = findSegment(targetX, targetY,
                { x, y -> !isBuilding(x, y) }, { x, y -> isWalkable(x, y) })
            fullEndPath.addAll(pathToRoad.reversed())
        }
        val realTarget = fullEndPath.first()

        val openList = mutableListOf(Node(realStart.first, realStart.second).apply
        { h = abs(x - realTarget.first) + abs(y - realTarget.second) })
        val closedSet = mutableSetOf<Pair<Int, Int>>()
        var foundNode: Node? = null

        while (openList.isNotEmpty()) {
            val curr = openList.minByOrNull { it.f }!!
            if (curr.x == realTarget.first && curr.y == realTarget.second) { foundNode = curr; break }
            openList.remove(curr)
            closedSet.add(curr.pos)

            for (m in listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))) {
                val nx = curr.x + m.first; val ny = curr.y + m.second
                if (!isWalkable(nx, ny) || closedSet.contains(Pair(nx, ny)) ||
                    customObstacles.contains(Pair(nx, ny))) continue

                val gScore = curr.g + 1
                val existing = openList.find { it.x == nx && it.y == ny }
                if (existing == null) {
                    openList.add(Node(nx, ny, gScore,
                        abs(nx - realTarget.first) + abs(ny - realTarget.second), curr))
                } else if (gScore < existing.g) {
                    existing.g = gScore; existing.parent = curr
                }
            }
        }

        if (foundNode == null) return emptyList()

        return fullStartPath.dropLast(1) + reconstructPath(foundNode) + fullEndPath.drop(1)
    }

    private fun reconstruct(end: Pair<Int, Int>, parents: Map<Pair<Int, Int>, Pair<Int, Int>>): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()
        var t: Pair<Int, Int>? = end
        while (t != null) { path.add(t); t = parents[t] }
        return path.reversed()
    }

    private fun reconstructPath(node: Node): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()
        var curr: Node? = node
        while (curr != null) { path.add(curr.pos); curr = curr.parent }
        return path.reversed()
    }
}