package com.example.mobilo4ka.utils

import android.content.Context
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.google.gson.Gson

object LoadMapData {
    fun loadMapData(context: Context): GridMap? {
        return try {
            val jsonString = context.assets.open("grid.json").bufferedReader().use { it.readText() }
            Gson().fromJson(jsonString, GridMap::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBuildings(context: Context): List<Building> {
        return try {
            val jsonString = context.assets.open("bildings/version-ru/BuildingsWithEntrances.json").bufferedReader().use { it.readText() }
            val wrapper = Gson().fromJson(jsonString, BuildingResponse::class.java)
            wrapper.buildings
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadZones(context: Context): Map<String, List<List<Int>>> {
        return try {
            val jsonString = context.assets.open("OtherZones.json").bufferedReader().use { it.readText() }
            val wrapper = Gson().fromJson(jsonString, ZonesResponse::class.java)
            wrapper.zones
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    data class BuildingResponse(val buildings: List<Building>)
    data class ZonesResponse(val zones: Map<String, List<List<Int>>>)
}