package com.example.mobilo4ka.ui.screens.astar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.system.SetStatusBarColor

@Composable
fun AStarScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>
){
    SetStatusBarColor(true)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    this.gridMap = gridData
                    this.buildings = buildingsData
                    this.zones = zonesData

                    setupInitialView()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}