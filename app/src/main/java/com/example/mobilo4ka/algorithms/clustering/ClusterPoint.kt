package com.example.mobilo4ka.algorithms.clustering

data class ClusterPoint(
    val name: String,
    val x: Int,
    val y: Int,
    var euclideanClusterId: Int = -1,
    var astarClusterId: Int = -1
)

enum class ClusteringMode {
    EUCLIDEAN, ASTAR, COMPARISON
}