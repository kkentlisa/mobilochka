package com.example.mobilo4ka.ui.screens.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.algorithms.genetic.GeneticAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking


//Нужно вместо уведомления снизу, сделать нижний лист на котором будет указан маршрут, время прохождения его
//Также нужно брать время, то есть смотреть по закрытию и открытию
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GeneticScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>,
    mapViewModel: MapViewModel = viewModel()
) {
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    val interestingBuildings = remember(buildingsData) {
        buildingsData.filter { !it.name.isNullOrBlank() }
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    var userStartPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Scaffold(
            topBar = {

            }
        ) { paddingValues ->
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData

                        this.onMapClicked = { x, y ->
                            userStartPoint = Pair(x, y)
                        }

                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }
                        setupInitialView()
                        mapViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                update = { view ->
                    view.setStartMarker(userStartPoint)
                }
            )
        }

        if (selectedBuilding != null) {
            BuildingBottomSheet(
                building = selectedBuilding!!,
                onDismiss = { selectedBuilding = null }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (resultText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Text(text = resultText, modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = {
                    val currentStart = userStartPoint ?: Pair(22, 15)
                    val ga = GeneticAlgorithm(gridData, interestingBuildings)

                    if (!ga.isWalkable(currentStart.first, currentStart.second)) {
                        resultText = "Невозможно построить маршрут: выберите другую координату"
                    } else {
                        isLoading = true
                        val products = listOf("кофе", "блинчики")

                        scope.launch(Dispatchers.Default) {
                            try {
                                suspend fun drawPathFast(path: List<Pair<Int, Int>>, entrances: List<Pair<Int, Int>>, step: Int) {
                                    withContext(Dispatchers.Main) {
                                        for (i in 1..path.size step step) {
                                            val partialPath = path.take(i)
                                            val visibleEntrances = entrances.filter { partialPath.contains(it) }
                                            mapViewRef?.showGeneticRoute(partialPath, visibleEntrances)

                                            delay(1)
                                        }
                                        mapViewRef?.showGeneticRoute(path, entrances)
                                    }
                                }

                                val routeIds = ga.evolve(products, currentStart) { intermediatePath ->
                                    runBlocking(Dispatchers.Main) {
                                        drawPathFast(intermediatePath, emptyList(), 50)
                                    }
                                }

                                if (routeIds.isNotEmpty()) {
                                    val finalPath = ga.buildFullPath(routeIds, currentStart)
                                    val entrancePoints = routeIds.mapNotNull { id ->
                                        interestingBuildings.find { it.id == id }?.firstEntrance
                                    }

                                    drawPathFast(finalPath, entrancePoints, 20)

                                    withContext(Dispatchers.Main) {
                                        val names = routeIds.mapNotNull { id ->
                                            interestingBuildings.find { it.id == id }?.name
                                        }
                                        resultText = "Маршрут построен: ${names.joinToString(" -> ")}"
                                    }
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) { resultText = "Ошибка: ${e.localizedMessage}" }
                            } finally {
                                withContext(Dispatchers.Main) { isLoading = false }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(if (userStartPoint == null) "Выберите точку на карте" else "Построить маршрут")
                }
            }
        }
    }
}