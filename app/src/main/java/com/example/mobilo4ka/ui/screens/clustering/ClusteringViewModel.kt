package com.example.mobilo4ka.ui.screens.clustering

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.algorithms.clustering.ClusterPoint
import com.example.mobilo4ka.algorithms.clustering.ClusteringData
import com.example.mobilo4ka.algorithms.clustering.ClusteringMode
import com.example.mobilo4ka.algorithms.clustering.KMeans
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClusteringUIState(
    val points: List<ClusterPoint> = emptyList(),
    val mode: ClusteringMode = ClusteringMode.EUCLIDEAN,
    val isLoading: Boolean = false,
    val showSettings: Boolean = false,
    val kValue: Int = 4
)

class ClusteringViewModel : ViewModel() {

    private val _state = MutableStateFlow(ClusteringUIState())
    val state: StateFlow<ClusteringUIState> = _state

    fun setMode(mode: ClusteringMode) {
        _state.update {
            it.copy(
                mode = mode,
                points = emptyList()
            )
        }
    }

    fun setKValue(k: Int) {
        _state.update {
            it.copy(
                kValue = k,
                points = emptyList()
            )
        }
    }

    fun setShowSettings(show: Boolean) {
        _state.update { it.copy(showSettings = show) }
    }

    fun runClustering(buildings: List<Building>, grid: GridMap) {
        val k = _state.value.kValue
        viewModelScope.launch(Dispatchers.Default) {
            _state.update { it.copy(isLoading = true) }

            val sortedPoints = ClusteringData.buildingsToPoints(buildings)
                .sortedWith(compareBy<ClusterPoint> { it.x }.thenBy { it.y })


            val step = sortedPoints.size / k
            val startCentroids = (0 until k).map { i ->
                val point = sortedPoints[i * step]
                Pair(point.x.toFloat(), point.y.toFloat())
            }

            val kMeans = KMeans(AStarAlgorithm(), grid, buildings)

            kMeans.calculate(sortedPoints, k, ClusteringMode.EUCLIDEAN, startCentroids)
            kMeans.calculate(sortedPoints, k, ClusteringMode.ASTAR, startCentroids)

            _state.update {
                it.copy(
                    points = sortedPoints,
                    isLoading = false,
                    showSettings = false
                )
            }
        }
    }

}