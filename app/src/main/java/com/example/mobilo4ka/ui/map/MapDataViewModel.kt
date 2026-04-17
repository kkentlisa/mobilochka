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

    fun preloadData(context: Context) {
        if (isLoaded) return

        viewModelScope.launch {
            val data = withContext(Dispatchers.IO) {
                val grid = LoadMapData.loadMapData(context)
                val buildings = LoadMapData.loadBuildings(context)
                val zones = LoadMapData.loadZones(context)
                Triple(grid, buildings, zones)
            }

            gridData = data.first
            buildingsData = data.second
            zonesData = data.third
            isLoaded = true
        }
    }
}