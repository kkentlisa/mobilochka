package com.example.mobilo4ka.ui.card


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.repository.BuildingRepository

class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BuildingRepository(application)

    fun loadBuildingsByLanguage(isRu: Boolean) {
        val path = if (isRu) {
            "bildings/version-ru/BuildingsWithEntrances.json"
        } else {
            "bildings/version-en/BuildingsWithEntrances.json"
        }
        repository.loadBuildings(path)
    }

    fun findBuilding(x: Int, y: Int): Building? {
        return repository.findActiveBuildingByPoint(x, y)
    }
}