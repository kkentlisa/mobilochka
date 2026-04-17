package com.example.mobilo4ka.ui.screens.genetic

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.algorithms.genetic.GeneticAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.theme.AppAlpha.GHOST_BUTTON_ALPHA
import com.example.mobilo4ka.ui.theme.AppAlpha.MAP_UI_ALPHA
import com.example.mobilo4ka.ui.theme.AppAlpha.MODIFIER_WEIGH
import com.example.mobilo4ka.ui.theme.AppAlpha.SHIP_SELECTION_ALPHA
import com.example.mobilo4ka.ui.theme.Cluster1
import com.example.mobilo4ka.ui.theme.Cluster3
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.ui.theme.Line
import com.example.mobilo4ka.ui.theme.TsuBlue
import kotlinx.coroutines.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.genetic.formatTimeToMinutes
import com.example.mobilo4ka.algorithms.genetic.formatToString
import com.example.mobilo4ka.algorithms.genetic.parseTimeToMinutes
import com.example.mobilo4ka.ui.map.MapDataViewModel

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
    mapViewModel: MapViewModel = viewModel(),
    mapDataViewModel: MapDataViewModel,
    onNavigateToNeural: (Building) -> Unit
) {
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    val currentRating = mapDataViewModel.ratings[selectedBuilding?.id.toString()]
    val interestingBuildings = remember(buildingsData) {
        buildingsData.filter { !it.name.isNullOrBlank() }
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
    var geneticDrawerRef by remember { mutableStateOf<GeneticDrawer?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var showRouteDetails by remember { mutableStateOf(false) }
    var estimatedTime by remember { mutableIntStateOf(0) }

    var userStartPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isValidStart by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val allAvailableProducts = remember(buildingsData) {
        buildingsData
            .flatMap { it.menu ?: emptyList()  }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val stringShop = stringResource(R.string.shop)
    val stringRout = stringResource(R.string.rout)
    val stringTotalTime = stringResource(R.string.total_time)
    val stringStart = stringResource(R.string.start)

    var selectedProducts by remember { mutableStateOf(setOf<String>()) }
    var formattedStartTime by remember { mutableStateOf("") }
    var calculatedRouteSteps by remember { mutableStateOf<List<RouteStepInfo>>(emptyList()) }

    fun checkValidStartPoint(x: Int, y: Int): Boolean {
        return gridData.isWalkable(x, y)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(Dimens.bannerSize),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(Dimens.spacingLarge)) {
                    Text(
                        stringResource(R.string.products),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TsuBlue
                    )
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                    LazyColumn(
                        modifier = Modifier.weight(MODIFIER_WEIGH),
                        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
                        contentPadding = PaddingValues(vertical = Dimens.spacingLarge)
                    ) {
                        items(allAvailableProducts) { product ->
                            val isSelected = selectedProducts.contains(product)
                            FilterChip(
                                modifier = Modifier.fillMaxWidth(),
                                selected = isSelected,
                                onClick = {
                                    selectedProducts =
                                        if (isSelected) selectedProducts - product else selectedProducts + product
                                },
                                label = { Text(product) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            null,
                                            modifier = Modifier.size(Dimens.spacingLarge)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = TsuBlue.copy(alpha = SHIP_SELECTION_ALPHA),
                                    selectedLabelColor = TsuBlue
                                )
                            )
                        }
                    }

                    Button(
                        onClick = { scope.launch { drawerState.close() } },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = TsuBlue)
                    ) {
                        Text(
                            stringResource(R.string.apply),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Scaffold { paddingValues ->
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            this.gridMap = gridData
                            this.buildings = buildingsData
                            this.zones = zonesData

                            val drawer = GeneticDrawer(this)
                            geneticDrawerRef = drawer
                            this.geneticDrawer = drawer

                            this.onMapClicked = { x, y ->
                                if (checkValidStartPoint(x, y)) {
                                    userStartPoint = Pair(x, y)
                                    isValidStart = true
                                    drawer.updateStartPoint(Pair(x, y))
                                    calculatedRouteSteps = emptyList()
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            FilledIconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .padding(
                        start = Dimens.spacingLarge,
                        end = Dimens.spacingLarge,
                        bottom = Dimens.spacingLarge
                    )
                    .padding(top = Dimens.fabSize),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = MAP_UI_ALPHA),
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
            }

            if (!showRouteDetails) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(Dimens.spacingLarge),
                    verticalArrangement = Arrangement.spacedBy(Dimens.paddingLowerMedium)
                ) {
                    if (calculatedRouteSteps.isNotEmpty() && !isLoading) {
                        Button(
                            onClick = { showRouteDetails = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.routeInfoButtonHeight),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(alpha = GHOST_BUTTON_ALPHA),
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(R.string.show_route))
                        }
                    }

                    Button(
                        onClick = {
                            if (!isValidStart || userStartPoint == null || selectedProducts.isEmpty()) return@Button
                            isLoading = true
                            formattedStartTime = LocalTime.now().format(timeFormatter)
                            scope.launch(Dispatchers.Default) {
                                try {
                                    val ga = GeneticAlgorithm(gridData, interestingBuildings)
                                    val productsToFind = selectedProducts.toList()

                                    ga.evolve(productsToFind, userStartPoint!!) { path, ids ->
                                        scope.launch(Dispatchers.Main) {
                                            if (path.isNotEmpty()) {
                                                geneticDrawerRef?.visitedBuildingIds = ids
                                                geneticDrawerRef?.updateRoute(path)

                                                estimatedTime =
                                                    (path.size * 2 / 10).coerceAtLeast(1)

                                                val speedFactor = 10
                                                val metersPerCell = 2.0

                                                val tempSteps = mutableListOf<RouteStepInfo>()
                                                var accumulatedDistance = 0.0
                                                var lastPoint = userStartPoint!!

                                                ids.forEachIndexed { _, id ->
                                                    val building =
                                                        buildingsData.find { it.id == id }
                                                            ?: return@forEachIndexed
                                                    val targetEntrance = building.firstEntrance
                                                        ?: return@forEachIndexed

                                                    val dx =
                                                        (targetEntrance.first - lastPoint.first).toDouble()
                                                    val dy =
                                                        (targetEntrance.second - lastPoint.second).toDouble()
                                                    val stepDistance = Math.sqrt(dx * dx + dy * dy)

                                                    accumulatedDistance += stepDistance

                                                    val timeToPoint =
                                                        ((accumulatedDistance * metersPerCell) / speedFactor).toInt()
                                                            .coerceAtLeast(1)

                                                    val (status, color, hours) = getArrivalStatus(
                                                        context,
                                                        building,
                                                        timeToPoint
                                                    )
                                                    val arrivalTimeStr = LocalTime.now()
                                                        .plusMinutes(timeToPoint.toLong())
                                                        .format(timeFormatter)

                                                    val productsInThisBuilding =
                                                        productsToFind.filter { p ->
                                                            building.menu.contains(p) == true
                                                        }

                                                    tempSteps.add(
                                                        RouteStepInfo(
                                                            building = building,
                                                            arrivalTimeStr = arrivalTimeStr,
                                                            statusText = status,
                                                            statusColor = color,
                                                            workHours = hours,
                                                            productsToBuy = productsInThisBuilding,
                                                            timeToThisPoint = timeToPoint
                                                        )
                                                    )

                                                    lastPoint = targetEntrance
                                                }

                                                calculatedRouteSteps = tempSteps
                                                showRouteDetails = true
                                            }
                                            isLoading = false
                                        }
                                    }
                                } catch (_: Exception) {
                                    withContext(Dispatchers.Main) { isLoading = false }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(Dimens.routeInfoButtonHeight),
                        enabled = isValidStart && !isLoading && selectedProducts.isNotEmpty(),
                        shape = RoundedCornerShape(Dimens.paddingMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TsuBlue,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = SHIP_SELECTION_ALPHA)
                        )
                    ) {
                        when {
                            isLoading -> CircularProgressIndicator(
                                modifier = Modifier.size(Dimens.paddingExtraLarge),
                                color = MaterialTheme.colorScheme.onPrimary
                            )

                            !isValidStart -> Text(stringResource(R.string.select_point))
                            selectedProducts.isEmpty() -> Text(stringResource(R.string.select_products))
                            else -> Text(stringResource(R.string.plan_rout))
                        }
                    }
                }
            }

            if (showRouteDetails) {
                ModalBottomSheet(
                    onDismissRequest = { showRouteDetails = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimens.cornerExtraLarge)
                            .padding(bottom = Dimens.fabIconSize)
                    ) {
                        Text(
                            stringRout,
                            style = MaterialTheme.typography.headlineSmall,
                            color = TsuBlue
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringTotalTime + " " + formatTimeToMinutes(estimatedTime),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${stringStart} ${formatToString(formattedStartTime)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Dimens.paddingSmall),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            itemsIndexed(calculatedRouteSteps) { index, info ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = Dimens.paddingLowerMedium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = info.statusColor.copy(alpha = SHIP_SELECTION_ALPHA),
                                        modifier = Modifier.size(Dimens.iconSize),
                                        border = BorderStroke(
                                            Dimens.dividerThickness,
                                            info.statusColor
                                        )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text((index + 1).toString(), color = info.statusColor)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(Dimens.spacingLarge))
                                    Column {
                                        Text(
                                            info.building.name ?: stringResource(R.string.building),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            info.statusText,
                                            color = info.statusColor,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (info.productsToBuy.isNotEmpty()) {
                                            Text(
                                                stringShop + info.productsToBuy.joinToString(),
                                                color = TsuBlue,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (selectedBuilding != null) {
                BuildingBottomSheet(
                    building = selectedBuilding!!,
                    rating = currentRating,
                    onDismiss = { selectedBuilding = null },
                    onLeaveReviewClick = {
                        val buildingToRate = selectedBuilding!!
                        selectedBuilding = null
                        onNavigateToNeural(buildingToRate)
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getArrivalStatus(
    context: android.content.Context,
    building: Building,
    minutesToArrival: Int
): Triple<String, Color, String> {
    val now = LocalTime.now()
    val startMinutes = now.hour * 60 + now.minute
    val arrivalInMinutes = (startMinutes + minutesToArrival) % 1440

    val openMinutes = parseTimeToMinutes(building.openTime)
    val closeMinutes = parseTimeToMinutes(building.closeTime)

    val arrivalTime = LocalTime.of((arrivalInMinutes / 60), (arrivalInMinutes % 60))
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(java.time.format.FormatStyle.SHORT)
    val arrivalStr = arrivalTime.format(timeFormatter)

    return if (openMinutes < 1440 && closeMinutes < 1440) {
        if (arrivalInMinutes in openMinutes..closeMinutes) {
            Triple(
                "${context.getString(R.string.arrival_open)} $arrivalStr)",
                Cluster3,
                "${context.getString(R.string.open)} ${building.closeTime}"
            )
        } else {
            Triple(
                "${context.getString(R.string.arrival_close)} $arrivalStr)",
                Cluster1,
                "${context.getString(R.string.close)} ${building.closeTime}"
            )
        }
    } else {
        Triple(
            "${context.getString(R.string.arrival)} $arrivalStr",
            Line,
            context.getString(R.string.opening_hours)
        )
    }
}