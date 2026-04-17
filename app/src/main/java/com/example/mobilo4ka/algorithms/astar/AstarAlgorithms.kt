package com.example.mobilo4ka.algorithms.astar

import java.util.PriorityQueue
import kotlin.math.abs

data class Node(
    val x: Int,
    val y: Int,
    val g: Int = 0,
    val h: Int = 0,
    val parent: Node? = null) {
    val f: Int get() = g + h
}
private val DIRECTIONS = arrayOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)
@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class AStarAlgorithm {
    private val customObstacles = mutableSetOf<Pair<Int, Int>>()

    private fun resolveTerminalPath(
        x: Int, y: Int,
        isWalkable: (Int, Int) -> Boolean,
        isBuilding: (Int, Int) -> Boolean,
        getEntrance: (Int, Int) -> Pair<Int, Int>?
    ): List<Pair<Int, Int>> {
        if (!isBuilding(x, y)) {
            return findSegment(x, y, canStep = { cx, cy -> !isBuilding(cx, cy) }, isTarget = isWalkable)
        }

        val entrance = getEntrance(x, y)
        if (entrance != null) {
            val pathInside = findSegment(x, y,
                canStep = isBuilding,
                isTarget = { cx, cy -> cx == entrance.first && cy == entrance.second })

            val pathToRoad = findSegment(entrance.first, entrance.second,
                canStep = { cx, cy -> !isBuilding(cx, cy) || (cx == entrance.first && cy == entrance.second) },
                isTarget = isWalkable)

            return if (pathInside.isNotEmpty() && pathToRoad.isNotEmpty()) {
                pathInside + pathToRoad.drop(1)
            } else emptyList()
        }

        val myBuilding = getBuildingPixels(x, y, isBuilding)
        return findSegment(x, y,
            canStep = { cx, cy -> !isBuilding(cx, cy) || (cx to cy) in myBuilding },
            isTarget = isWalkable
        )
    }

    private fun getBuildingPixels(startX: Int, startY: Int, isBuilding: (Int, Int) -> Boolean): Set<Pair<Int, Int>> {
        val start = startX to startY
        val pixels = mutableSetOf(start)
        val queue = ArrayDeque<Pair<Int, Int>>().apply { add(start) }

        while (queue.isNotEmpty()) {
            val (cx, cy) = queue.removeFirst()
            for ((dx, dy) in DIRECTIONS) {
                val next = (cx + dx) to (cy + dy)
                if (next !in pixels && isBuilding(next.first, next.second)) {
                    pixels.add(next)
                    queue.add(next)
                }
            }
        }
        return pixels
    }

    fun findSegment(
        startX: Int,
        startY: Int,
        canStep: (Int, Int) -> Boolean,
        isTarget: (Int, Int) -> Boolean
    ): List<Pair<Int, Int>> {
        val startPoint = startX to startY
        val queue = ArrayDeque<Pair<Int, Int>>().apply { add(startPoint) }
        val visited = mutableSetOf(startPoint)
        val parents = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>>()

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()

            if (isTarget(curr.first, curr.second)) {
                return generateSequence(curr) { parents[it] }.toList().asReversed()
            }

            for ((dx, dy) in DIRECTIONS) {
                val nx = curr.first + dx
                val ny = curr.second + dy
                val next = nx to ny

                val maxSearchRadius = 200
                if (abs(nx - startX) < maxSearchRadius && abs(ny - startY) < maxSearchRadius &&
                    next !in visited && next !in customObstacles && canStep(nx, ny)
                ) {
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
        getBuildingEntrance: (Int, Int) -> Pair<Int, Int>? = { _, _ -> null },
        getBuildingPixels: (Int, Int) -> Set<Pair<Int, Int>>?
    ): List<Pair<Int, Int>> {

        val fullStartPath = resolveTerminalPath(startX, startY, isWalkable, isBuilding, getBuildingEntrance)
        val endPathRaw = resolveTerminalPath(targetX, targetY, isWalkable, isBuilding, getBuildingEntrance)

        if (fullStartPath.isEmpty() || endPathRaw.isEmpty()) return emptyList()

        val fullEndPath = endPathRaw.reversed()
        val realStart = fullStartPath.last()
        val realTarget = fullEndPath.first()

        val openList = PriorityQueue<Node>(compareBy { it.f })
        val gScores = mutableMapOf<Pair<Int, Int>, Int>()

        openList.add(Node(realStart.first, realStart.second, 0, manhattan(realStart, realTarget), null))
        gScores[realStart] = 0

        var foundNode: Node? = null

        while (openList.isNotEmpty()) {
            val curr = openList.poll()
            val currPos = curr.x to curr.y

            if (curr.x == realTarget.first && curr.y == realTarget.second) {
                foundNode = curr
                break
            }

            if (curr.g > gScores.getOrDefault(currPos, Int.MAX_VALUE)) continue

            for ((dx, dy) in DIRECTIONS) {
                val nx = curr.x + dx
                val ny = curr.y + dy
                val nextPos = nx to ny

                if (!isWalkable(nx, ny) || nextPos in customObstacles) continue

                val tentativeG = curr.g + 1
                if (tentativeG < gScores.getOrDefault(nextPos, Int.MAX_VALUE)) {
                    gScores[nextPos] = tentativeG
                    openList.add(Node(nx, ny, tentativeG, manhattan(nextPos, realTarget), curr))
                }
            }
        }

        if (foundNode == null) return emptyList()

        val mainPath = generateSequence(foundNode) { it.parent }
            .map { it.x to it.y }
            .toList()
            .asReversed()

        return fullStartPath.dropLast(1) + mainPath + fullEndPath.drop(1)
    }

    private fun manhattan(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
        return abs(a.first - b.first) + abs(a.second - b.second)
    }
}