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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class RouteStepInfo(
    val building: Building,
    val arrivalTimeStr: String,
    val statusText: String,
    val statusColor: Color,
    val workHours: String,
    val productsToBuy: List<String>,
    val timeToThisPoint: Int
)

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
    var geneticDrawerRef by remember { mutableStateOf<GeneticDrawer?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showRouteDetails by remember { mutableStateOf(false) }
    var estimatedTime by remember { mutableIntStateOf(0) }

    var userStartPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isValidStart by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    var calculatedRouteSteps by remember { mutableStateOf<List<RouteStepInfo>>(emptyList()) }

    // Функция проверки, находится ли точка на проходимой области
    fun checkValidStartPoint(x: Int, y: Int): Boolean {
        return gridData.isWalkable(x, y)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Scaffold { paddingValues ->
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData

                        // Создаем GeneticDrawer
                        val drawer = GeneticDrawer(this)
                        geneticDrawerRef = drawer

                        this.onMapClicked = { x, y ->
                            val isValid = checkValidStartPoint(x, y)
                            if (isValid) {
                                userStartPoint = Pair(x, y)
                                isValidStart = true
                                drawer.updateStartPoint(Pair(x, y))
                            } else {
                                userStartPoint = null
                                isValidStart = false
                                drawer.updateStartPoint(null)
                            }
                        }

                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }

                        setupInitialView()
                        mapViewRef = this
                    }
                },
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            )
        }

        if (showRouteDetails) {
            ModalBottomSheet(
                onDismissRequest = { showRouteDetails = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
                    Text("Ваш маршрут", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Общее время в пути: $estimatedTime мин.", style = MaterialTheme.typography.bodyLarge)

                    HorizontalDivider()

                    LazyColumn(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 16.dp)) {
                        item {
                            val startTimeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Начало пути", style = MaterialTheme.typography.titleMedium)
                                    Text("Выход в $startTimeStr", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                        }

                        itemsIndexed(calculatedRouteSteps) { index, info ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = info.statusColor.copy(alpha = 0.1f),
                                    modifier = Modifier.size(40.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, info.statusColor)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text((index + 1).toString(), color = info.statusColor)
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(info.building.name ?: "Здание", style = MaterialTheme.typography.titleMedium)
                                    Text(info.statusText, color = info.statusColor, style = MaterialTheme.typography.bodyMedium)
                                    Text("Режим: ${info.workHours}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    if (info.productsToBuy.isNotEmpty()) {
                                        Text("Купить: ${info.productsToBuy.joinToString()}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
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

        // Кнопка внизу
        Column(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = {
                    if (!isValidStart || userStartPoint == null) return@Button

                    isLoading = true

                    scope.launch(Dispatchers.Default) {
                        try {
                            val ga = GeneticAlgorithm(gridData, interestingBuildings)
                            val productsToFind = listOf("кофе", "блинчики")

                            val resultIds = ga.evolve(productsToFind, userStartPoint!!) { finalPath ->
                                scope.launch(Dispatchers.Main) {
                                    if (finalPath.isNotEmpty()) {
                                        geneticDrawerRef?.updateRoute(finalPath)
                                        estimatedTime = (finalPath.size * 2 / 15).coerceAtLeast(1)
                                    }
                                }
                            }

                            if (resultIds.isNotEmpty()) {
                                val tempSteps = mutableListOf<RouteStepInfo>()
                                resultIds.distinct().forEachIndexed { index, id ->
                                    val building = buildingsData.find { it.id == id } ?: return@forEachIndexed
                                    val idsUpToThis = resultIds.take(index + 1)

                                    val distance = ga.calculateRouteDistance(idsUpToThis, userStartPoint!!)
                                    val timeToPoint = (distance * 2 / 15).toInt()

                                    val (status, color, hours) = getArrivalStatus(building, timeToPoint)
                                    val arrivalTime = LocalTime.now().plusMinutes(timeToPoint.toLong())
                                        .format(DateTimeFormatter.ofPattern("HH:mm"))

                                    tempSteps.add(RouteStepInfo(
                                        building = building,
                                        arrivalTimeStr = arrivalTime,
                                        statusText = status,
                                        statusColor = color,
                                        workHours = hours ?: "Не указано",
                                        productsToBuy = productsToFind.filter { p -> building.menu.contains(p) },
                                        timeToThisPoint = timeToPoint
                                    ))
                                }

                                withContext(Dispatchers.Main) {
                                    calculatedRouteSteps = tempSteps
                                    showRouteDetails = true
                                    isLoading = false
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    isLoading = false
                                }
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = isValidStart && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isValidStart && !isLoading)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Построение маршрута...")
                    }
                    !isValidStart -> Text("Выберите точку на дорожке")
                    else -> Text("Построить маршрут")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getArrivalStatus(building: Building, minutesToArrival: Int): Triple<String, Color, String> {
    val arrivalTime = LocalTime.now().plusMinutes(minutesToArrival.toLong())
    val open = building.parsedOpenTime
    val close = building.parsedCloseTime
    val arrivalStr = arrivalTime.format(DateTimeFormatter.ofPattern("HH:mm"))

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