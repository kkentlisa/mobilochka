package com.example.mobilo4ka.data.models

data class Building(val id: Int, val pixels: List<List<Int>>, val name: String? = null, val entrances: List<List<Int>>? = null)
{
    fun containsPoint(x: Int, y: Int): Boolean {
        return pixels.any { it[0] == x && it[1] == y }
    }

    fun getFirstEntrance(): Pair<Int, Int>? {
        return entrances?.firstOrNull()?.let { Pair(it[0], it[1]) }
    }

    fun getFirstPixels(): Pair<Int, Int>? {
        return pixels.firstOrNull()?.let { Pair(it[0], it[1]) }
    }
}