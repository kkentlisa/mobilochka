package com.example.mobilo4ka.ui.card


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.repository.BuildingRepository

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BuildingRepository(application)

    init {
        repository.loadBuildings("BuildingsWithEntrances.json")
    }

    fun findBuilding(x: Int, y: Int): Building? {
        return repository.findActiveBuildingByPoint(x, y)
    }

    fun getAllBuildings(): List<Building> {
        return repository.getAllBuildings()
    }
}