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

    private fun getHeuristic(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        return abs(x1 - x2) + abs(y1 - y2)
    }

    fun findPath(
        startX: Int, startY: Int,
        targetX: Int, targetY: Int,
        isWalkable: (Int, Int) -> Boolean
    ): List<Pair<Int, Int>> {

        val openList = mutableListOf<Node>()
        val closedSet = mutableSetOf<Pair<Int, Int>>()

        val startNode = Node(startX, startY)

        startNode.h = getHeuristic(startX, startY, targetX, targetY)
        openList.add(startNode)

        while (openList.isNotEmpty()) {
            var currentNode = openList[0]
            for (node in openList) {
                if (node.f < currentNode.f) {
                    currentNode = node
                }
            }

            if (currentNode.x == targetX && currentNode.y == targetY) {
                return reconstructPath(currentNode)
            }

            openList.remove(currentNode)
            closedSet.add(Pair(currentNode.x, currentNode.y))

            val neighbors = listOf(
                Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0)
            )

            for (move in neighbors) {
                val nx = currentNode.x + move.first
                val ny = currentNode.y + move.second

                if (!isWalkable(nx, ny) || closedSet.contains(Pair(nx, ny))) continue

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
                    newNode.h = getHeuristic(nx, ny, targetX, targetY)
                    newNode.parent = currentNode
                    openList.add(newNode)
                } else if (gScore < existingNode.g) {
                    existingNode.g = gScore
                    existingNode.parent = currentNode
                }
            }
        }
        return emptyList()
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