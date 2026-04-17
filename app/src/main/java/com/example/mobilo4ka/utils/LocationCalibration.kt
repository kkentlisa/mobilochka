package com.example.mobilo4ka.utils

import com.example.mobilo4ka.ui.theme.Dimens

object LocationCalibration {
     var topLeftLat: Double = 56.472768
    var topLeftLon: Double = 84.940970
    var bottomRightLat: Double = 56.466787
    var bottomRightLon: Double = 84.54130

    val mapWidthPx: Int = Dimens.mapWidhtSize.toInt()
    val mapHeightPx: Int = Dimens.mapHeightSize.toInt()

    fun gpsToPixel(latitude: Double, longitude: Double): Pair<Int, Int> {
        val x = ((longitude - topLeftLon) / (bottomRightLon - topLeftLon)) * mapWidthPx
        val y = ((latitude - topLeftLat) / (bottomRightLat - topLeftLat)) * mapHeightPx
        return x.toInt().coerceIn(0, mapWidthPx) to y.toInt().coerceIn(0, mapHeightPx)
    }

}