package com.example.mobilo4ka.data.models

object LandmarkRepository {
    val landmarkIds = listOf(201, 202, 203, 204, 205, 206, 207, 208, 209, 210)

    fun getLandmarks(buildings: List<Building>): List<Building> {
        return buildings.filter { it.id in landmarkIds }
    }
}