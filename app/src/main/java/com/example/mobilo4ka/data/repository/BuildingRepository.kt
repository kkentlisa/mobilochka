package com.example.mobilo4ka.data.repository

import android.content.Context
import com.example.mobilo4ka.data.models.Building
import org.json.JSONArray
import org.json.JSONObject

class BuildingRepository(private val context: Context) {

    private var buildings: List<Building> = emptyList()
    private val pixelToBuildingIdMap = mutableMapOf<Pair<Int, Int>, Int>()


    fun getAllBuildings(): List<Building> {
        return buildings
    }

    fun loadBuildings(fileName: String) {
        val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val buildingsArray = jsonObject.getJSONArray("buildings")

        val loadedBuildings = mutableListOf<Building>()

        for (i in 0 until buildingsArray.length()) {
            val bObj = buildingsArray.getJSONObject(i)
            val id = bObj.getInt("id")

            val pixelsArray = bObj.getJSONArray("pixels")
            for (j in 0 until pixelsArray.length()) {
                val point = pixelsArray.getJSONArray(j)
                pixelToBuildingIdMap[Pair(point.getInt(0), point.getInt(1))] = id
            }

            val building = Building(
                id = id,
                pixels = emptyList(),
                category = if (bObj.has("category")) bObj.getString("category") else null,
                entrances = if (bObj.has("entrances")) parsePoints(bObj.getJSONArray("entrances")) else null,
                name = if (bObj.has("name")) bObj.getString("name") else null,
                openTime = if (bObj.has("openTime")) bObj.getString("openTime") else null,
                closeTime = if (bObj.has("closeTime")) bObj.getString("closeTime") else null,
                menu = if (bObj.has("menu")) parseStringList(bObj.getJSONArray("menu")) else emptyList()
            )
            loadedBuildings.add(building)
        }
        buildings = loadedBuildings
    }

    private fun isInterestingBuilding(building: Building): Boolean {
        return !building.name.isNullOrBlank() ||
                !building.openTime.isNullOrBlank() ||
                building.menu.isNotEmpty()
    }

    fun findActiveBuildingByPoint(x: Int, y: Int): Building? {
        val id = pixelToBuildingIdMap[Pair(x, y)] ?: return null
        val building = buildings.find { it.id == id }

        return if (building != null && isInterestingBuilding(building)) building else null
    }

    private fun parsePoints(array: JSONArray): List<List<Int>> {
        val list = mutableListOf<List<Int>>()
        for (i in 0 until array.length()) {
            val p = array.getJSONArray(i)
            list.add(listOf(p.getInt(0), p.getInt(1)))
        }
        return list
    }

    private fun parseStringList(array: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) list.add(array.getString(i))
        return list
    }
}