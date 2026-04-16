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
}

class AStarAlgorithm {

    private val customObstacles = mutableSetOf<Pair<Int, Int>>()

    fun toggleObstacle(x: Int, y: Int) {
        val point = Pair(x, y)
        if (customObstacles.contains(point)) {
            customObstacles.remove(point)
        } else {
            customObstacles.add(point)
        }
    }

    fun clearObstacles() {
        customObstacles.clear()
    }

    private fun getHeuristic(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return abs(x1 - x2) + abs(y1 - y2)
    }

    fun findNearestRoad(
        x: Int, y: Int,
        isWalkable: (Int, Int) -> Boolean,
        isBuilding: (Int, Int) -> Boolean
    ): Pair<Int, Int> {
        if (isWalkable(x, y)) return Pair(x, y)

        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = mutableSetOf<Pair<Int, Int>>()

        val startPoint = Pair(x, y)
        queue.add(startPoint)
        visited.add(startPoint)

        val directions = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()

            if (isWalkable(curr.first, curr.second)) {
                return curr
            }

            for (move in directions) {
                val nx = curr.first + move.first
                val ny = curr.second + move.second
                val nextPoint = Pair(nx, ny)

                if (abs(nx - x) < 100 && abs(ny - y) < 100 &&
                    !visited.contains(nextPoint) &&
                    !isBuilding(nx, ny) &&
                    !customObstacles.contains(nextPoint)) {

                    visited.add(nextPoint)
                    queue.add(nextPoint)
                }
            }
        }
        return startPoint
    }

    fun findPath(
        startX: Int, startY: Int,
        targetX: Int, targetY: Int,
        isWalkable: (Int, Int) -> Boolean,
        isBuilding: (Int, Int) -> Boolean,
        getBuildingEntrance: (Int, Int) -> Pair<Int, Int>? = { _, _ -> null },
    ): List<Pair<Int, Int>> {

        val startEntrance = getBuildingEntrance.invoke(startX, startY)
        val realStart = startEntrance ?: findNearestRoad(startX, startY, isWalkable, isBuilding)

        val targetEntrance = getBuildingEntrance.invoke(targetX, targetY)
        val realTarget = targetEntrance ?: findNearestRoad(targetX, targetY, isWalkable, isBuilding)

        val openList = mutableListOf<Node>()
        val closedSet = mutableSetOf<Pair<Int, Int>>()

        val startNode = Node(realStart.first, realStart.second)

        startNode.h = getHeuristic(realStart.first, realStart.second, realTarget.first, realTarget.second)
        openList.add(startNode)

        var foundTargetNode: Node? = null

        while (openList.isNotEmpty()) {
            var currentNode = openList[0]
            for (node in openList) {
                if (node.f < currentNode.f) {
                    currentNode = node
                }
            }

            if (currentNode.x == realTarget.first && currentNode.y == realTarget.second) {
                foundTargetNode = currentNode
                break
            }

            openList.remove(currentNode)
            closedSet.add(Pair(currentNode.x, currentNode.y))

            val neighbors = listOf(
                Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)
            )

            for (move in neighbors) {
                val nx = currentNode.x + move.first
                val ny = currentNode.y + move.second

                if (!isWalkable(nx, ny) || closedSet.contains(Pair(nx, ny)) || customObstacles.contains(Pair(nx, ny))) continue

                val gScore = currentNode.g + 1

                var existingNode: Node? = null
                for (node in openList) {
                    if (node.x == nx && node.y == ny) {
                        existingNode = node
                        break
                    }
                }

                if (existingNode == null) {
                    val newNode = Node(nx, ny, g = gScore)
                    newNode.h = getHeuristic(nx, ny, realTarget.first, realTarget.second)
                    newNode.parent = currentNode
                    openList.add(newNode)
                } else if (gScore < existingNode.g) {
                    existingNode.g = gScore
                    existingNode.parent = currentNode
                }
            }
        }
        if (foundTargetNode == null) return emptyList()

        val path = reconstructPath(foundTargetNode).toMutableList()

        if (realStart != Pair(startX, startY)) {
            path.add(0, Pair(startX, startY))
        }

        if (realTarget != Pair(targetX, targetY)) {
            path.add(Pair(targetX, targetY))
        }

        return path
    }

    private fun reconstructPath(node: Node): List<Pair<Int, Int>> {
        val path = mutableListOf<Pair<Int, Int>>()
        var curr: Node? = node
        while (curr != null) {
            path.add(Pair(curr.x, curr.y))
            curr = curr.parent
        }
        return path.reversed()
    }
}