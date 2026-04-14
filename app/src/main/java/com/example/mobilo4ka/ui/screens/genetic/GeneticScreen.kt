package com.example.mobilo4ka.ui.screens.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.genetic.GeneticAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GeneticScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>,
    mapViewModel: MapViewModel = viewModel()
) {
    SetStatusBarColor(true)
    val context = LocalContext.current

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .statusBarsPadding()
                        .padding(Dimens.paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_genetic),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.logoSize)
                    )
                    Text(
                        text = context.getString(R.string.algo_genetic),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                }
            }
        ) { paddingValues ->
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData

                        // ПЕРЕДАЕМ КЛИК: ищем здание через ViewModel
                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }

                        setupInitialView()
                        mapViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (resultText.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = resultText,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    resultText = ""
                    try {
                        val ga = GeneticAlgorithm(gridData, buildingsData)
                        val bestRoute = ga.evolve(
                            requiredProducts = listOf("кофе", "блинчики"),
                            startPosition = Pair(50, 50),
                            startTime = LocalTime.now()
                        )
                        val fullPath = ga.buildFullPath(bestRoute.placeIds, Pair(50, 50))
                        mapViewRef?.showGeneticRoute(fullPath)
                        val names = bestRoute.placeIds.mapNotNull { id ->
                            buildingsData.find { it.id == id }?.name
                        }
                        resultText = "Оптимальный маршрут:\n${names.joinToString(" → ")}"
                    } catch (e: Exception) {
                        resultText = "Ошибка: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Построение маршрута...")
                } else {
                    Text("Построить оптимальный маршрут")
                }
            }
        }

        if (selectedBuilding != null) {
            BuildingBottomSheet(
                building = selectedBuilding!!,
                onDismiss = { selectedBuilding = null }
            )
        }
    }
}