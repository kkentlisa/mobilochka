package com.example.mobilo4ka.ui.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.data.models.Building

@Composable
fun MapScreen(
    allBuildings: List<Building>,
    onFindBuilding: (Int, Int) -> Building?,
    onNavigateToNeural: () -> Unit
) {
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                MapView(context).apply {
                    this.onBuildingClicked = { x, y ->
                        selectedBuilding = onFindBuilding(x, y)
                    }
                }
            },
            update = { view ->
                view.buildings = allBuildings
            }
        )

        if (selectedBuilding != null) {
            BuildingBottomSheet(
                building = selectedBuilding!!,
                onDismiss = { selectedBuilding = null },
                onLeaveReviewClick = {
                    selectedBuilding = null
                    onNavigateToNeural()
                }
            )
        }
    }
}