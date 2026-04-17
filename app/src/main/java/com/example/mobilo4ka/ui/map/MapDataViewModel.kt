package com.example.mobilo4ka.ui.map

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.utils.LoadMapData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapDataViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("ratings_prefs", Context.MODE_PRIVATE)
    var gridData by mutableStateOf<GridMap?>(null)
    var buildingsData by mutableStateOf<List<Building>>(emptyList())
    var zonesData by mutableStateOf<Map<String, List<List<Int>>>>(emptyMap())

    var isLoaded by mutableStateOf(false)

    var ratings by mutableStateOf<Map<String, Float>>(
        prefs.all.keys.filter { it.endsWith("_sum") }.associate { key ->
            val id = key.removeSuffix("_sum")
            val sum = prefs.getInt("${id}_sum", 0)
            val count = prefs.getInt("${id}_count", 0)
            val avg = if (count > 0) sum.toFloat() / count else 0f
            id to String.format("%.1f", avg).replace(",", ".").toFloat()

        }
    )
        private set

    private fun getBuildingsPath(language: String): String {
        return if (language == "en") {
            "bildings/version-en/BuildingsWithEntrances.json"
        } else {
            "bildings/version-ru/BuildingsWithEntrances.json"
        }
    }

    fun preloadData(context: Context, language: String) {
        val path = getBuildingsPath(language)

        viewModelScope.launch {
            val buildings = withContext(Dispatchers.IO) {
                LoadMapData.loadBuildings(context, path)
            }
            buildingsData = buildings

            if (!isLoaded) {
                val (grid, zones) = withContext(Dispatchers.IO) {
                    val g = LoadMapData.loadMapData(context)
                    val z = LoadMapData.loadZones(context)
                    Pair(g, z)
                }
                gridData = grid
                zonesData = zones
                isLoaded = true
            }
        }
    }

    fun saveRating(buildingId: String, newRating: Int) {
        val currentSum = prefs.getInt("${buildingId}_sum", 0)
        val currentCount = prefs.getInt("${buildingId}_count", 0)

        val newSum = currentSum + newRating
        val newCount = currentCount + 1
        val average = newSum.toFloat() / newCount

        prefs.edit().apply {
            putInt("${buildingId}_sum", newSum)
            putInt("${buildingId}_count", newCount)
            apply()
        }

        val formattedRating = String.format("%.1f", average).replace(",", ".").toFloat()
        ratings = ratings + (buildingId to formattedRating)
    }
}