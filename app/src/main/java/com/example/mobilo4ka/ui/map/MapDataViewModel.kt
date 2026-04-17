package com.example.mobilo4ka.ui.map

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.utils.LoadMapData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapDataViewModel : ViewModel() {
    var gridData by mutableStateOf<GridMap?>(null)
    var buildingsData by mutableStateOf<List<Building>>(emptyList())
    var zonesData by mutableStateOf<Map<String, List<List<Int>>>>(emptyMap())

    var isLoaded by mutableStateOf(false)

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
}