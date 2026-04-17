package com.example.mobilo4ka.algorithms.clustering

import com.example.mobilo4ka.data.models.Building

object ClusteringData {
    fun buildingsToPoints(buildings: List<Building>): List<ClusterPoint> {
        return buildings
            .filter { !it.name.isNullOrBlank() }
            .map { building ->
                val entrance = building.firstEntrance

                val xCoord = entrance?.first ?: building.pixels.first()[0]
                val yCoord = entrance?.second ?: building.pixels.first()[1]

                ClusterPoint(
                    name = building.name ?: "",
                    x = xCoord,
                    y = yCoord
                )
            }
    }
}