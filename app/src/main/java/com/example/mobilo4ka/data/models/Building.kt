package com.example.mobilo4ka.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.annotations.SerializedName
import java.time.LocalTime

data class Building(
    val id: Int,
    val pixels: List<List<Int>>,
    val name: String? = null,
    @SerializedName("category")
    val category: String? = null,
    val entrances: List<List<Int>>? = null,
    val menu: List<String> = emptyList(),
    val openTime: String? = null,
    val closeTime: String? = null
) {
    val firstEntrance: Pair<Int, Int>?
        get() = entrances?.firstOrNull()?.let { Pair(it[0], it[1]) }
            ?: pixels.firstOrNull()?.let { Pair(it[0], it[1]) }

    val parsedOpenTime: LocalTime?
        @RequiresApi(Build.VERSION_CODES.O)
        get() = try {
            openTime?.let { LocalTime.parse(it) }
        } catch (e: Exception) {
            null
        }

    val parsedCloseTime: LocalTime?
        @RequiresApi(Build.VERSION_CODES.O)
        get() = try {
            closeTime?.let { LocalTime.parse(it) }
        } catch (e: Exception) {
            null
        }

    fun hasProduct(product: String): Boolean {
        return menu.any { it.equals(product, ignoreCase = true) }
    }

    fun containsPoint(x: Int, y: Int): Boolean {
        return pixels.any { it[0] == x && it[1] == y }
    }
}