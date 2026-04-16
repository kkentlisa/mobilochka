package com.example.mobilo4ka.ui.screens.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.algorithms.genetic.GeneticAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapView
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
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

    var showRouteDetails by remember { mutableStateOf(false) }
    var routePathData by remember { mutableStateOf<List<Building>>(emptyList()) }
    var estimatedTime by remember { mutableIntStateOf(0) }

    var userStartPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Scaffold { paddingValues ->
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData
                        this.onMapClicked = { x, y -> userStartPoint = Pair(x, y) }
                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }
                        setupInitialView()
                        mapViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                update = { view -> view.setStartMarker(userStartPoint) }
            )
        }

        if (showRouteDetails) {
            if (showRouteDetails) {
                ModalBottomSheet(
                    onDismissRequest = { showRouteDetails = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 32.dp)
                    ) {
                        Text(
                            text = "Ваш маршрут",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Общее время в пути: $estimatedTime мин.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        HorizontalDivider()

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            item {
                                val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                                val startTimeStr = java.time.LocalTime.now().format(formatter)

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(text = "Начало пути", style = MaterialTheme.typography.titleMedium)
                                        Text(text = "Выход в $startTimeStr", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                            }

                            itemsIndexed(routePathData) { index, building ->
                                val currentStart = userStartPoint ?: Pair(22, 15)
                                val ga = GeneticAlgorithm(gridData, buildingsData)

                                val idsUpToThis = routePathData.take(index + 1).map { it.id }
                                val distanceToPoint = ga.calculateRouteDistance(idsUpToThis, currentStart)

                                val timeToThisPoint = (distanceToPoint * 2 / 15).toInt()
                                val (arrivalStatus, statusColor, workHours) = getArrivalStatus(building, timeToThisPoint)

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = statusColor.copy(alpha = 0.1f),
                                        modifier = Modifier.size(40.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = (index + 1).toString(),
                                                style = MaterialTheme.typography.titleMedium,
                                                color = statusColor
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(text = building.name ?: "Здание", style = MaterialTheme.typography.titleMedium)
                                        Text(text = arrivalStatus, color = statusColor, style = MaterialTheme.typography.bodyMedium)
                                        Text(text = "Режим: $workHours", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedBuilding != null) {
            BuildingBottomSheet(building = selectedBuilding!!, onDismiss = { selectedBuilding = null })
        }

        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)) {
            if (resultText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (resultText.contains("Невозможно"))
                            MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(text = resultText, modifier = Modifier.padding(12.dp))
                }
            }

            Button(
                onClick = {
                    val currentStart = userStartPoint ?: Pair(22, 15)
                    val ga = GeneticAlgorithm(gridData, interestingBuildings)

                    if (!ga.isWalkable(currentStart.first, currentStart.second)) {
                        resultText = "Невозможно построить маршрут: выберите дорожку"
                    } else {
                        isLoading = true
                        scope.launch(Dispatchers.Default) {
                            try {
                                suspend fun drawPathFast(path: List<Pair<Int, Int>>, entrances: List<Pair<Int, Int>>, step: Int) {
                                    withContext(Dispatchers.Main) {
                                        for (i in 1..path.size step step) {
                                            val partial = path.take(i)
                                            mapViewRef?.showGeneticRoute(partial, entrances.filter { partial.contains(it) })
                                            delay(1)
                                        }
                                        mapViewRef?.showGeneticRoute(path, entrances)
                                    }
                                }

                                val routeIds = ga.evolve(listOf("кофе", "блинчики"), currentStart) { intermediate ->
                                    runBlocking(Dispatchers.Main) { drawPathFast(intermediate, emptyList(), 100) }
                                }

                                if (routeIds.isNotEmpty()) {
                                    val finalPath = ga.buildFullPath(routeIds, currentStart)
                                    drawPathFast(finalPath, routeIds.mapNotNull { id -> buildingsData.find { it.id == id }?.firstEntrance }, 40)

                                    withContext(Dispatchers.Main) {
                                        routePathData = routeIds.mapNotNull { id -> buildingsData.find { it.id == id } }
                                        estimatedTime = (finalPath.size * 2 / 15).coerceAtLeast(1)
                                        resultText = ""
                                        showRouteDetails = true
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
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text(if (userStartPoint == null) "Выберите точку" else "Построить маршрут")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getArrivalStatus(building: Building, minutesToArrival: Int): Triple<String, Color, String> {
    val arrivalTime = java.time.LocalTime.now().plusMinutes(minutesToArrival.toLong())
    val open = building.parsedOpenTime
    val close = building.parsedCloseTime

    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
    val arrivalStr = arrivalTime.format(timeFormatter)

    return if (open != null && close != null) {
        if (arrivalTime.isAfter(open) && arrivalTime.isBefore(close)) {
            Triple("Успеете (прибытие в $arrivalStr)", Color(0xFF4CAF50), "Открыто до ${building.closeTime}")
        } else {
            Triple("Не успеете (прибытие в $arrivalStr)", Color(0xFFF44336), "Закрыто в это время")
        }
    } else {
        Triple("Прибытие в $arrivalStr", Color.Gray, "Часы работы не указаны")
    }
}