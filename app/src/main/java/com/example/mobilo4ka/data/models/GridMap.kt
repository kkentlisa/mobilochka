package com.example.mobilo4ka.data.models

data class GridMap(
    val width: Int,
    val height: Int,
    val matrix: Array<IntArray>
) {
    fun isWalkable(x: Int, y: Int): Boolean {
        if (x < 0 || x >= width || y < 0 || y >= height) return false
        return matrix[y][x] == 0
    }
}