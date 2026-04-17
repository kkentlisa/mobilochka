package com.example.mobilo4ka.ui.screens.ants

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.ant.AntAlgorithm
import com.example.mobilo4ka.algorithms.ant.MatrixBuilder
import com.example.mobilo4ka.algorithms.astar.AStarAlgorithm
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.data.models.LandmarkRepository
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AntsScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>
) {
    SetStatusBarColor(true)

    val scope = rememberCoroutineScope()

    var selectedPoints by remember { mutableStateOf(setOf<Pair<Int, Int>>()) }
    var startPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var calculatedRoute by remember { mutableStateOf(listOf<Pair<Int, Int>>()) }
    var isCalculating by remember { mutableStateOf(false) }

    val landmarksBuildings = remember(buildingsData) {
        LandmarkRepository.getLandmarks(buildingsData)
    }

    val landmarkPoints = remember(landmarksBuildings) {
        val list = mutableListOf<Pair<Int, Int>>()
        for (building in landmarksBuildings) {
            val point = building.getFirstPixels()
            if (point != null) {
                list.add(point)
            }
        }
        list
    }

    val walkablePixels = remember(zonesData) {
        val set = mutableSetOf<Pair<Int, Int>>()
        zonesData["asphalt"]?.forEach { set.add(Pair(it[0], it[1])) }
        zonesData["path"]?.forEach { set.add(Pair(it[0], it[1])) }
        set
    }

    val buildingPixelsSet = remember(buildingsData) {
        val set = mutableSetOf<Pair<Int, Int>>()
        for (b in buildingsData) {
            for (p in b.pixels) {
                set.add(Pair(p[0], p[1]))
            }
        }
        set
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val view = MapView(context)
                view.gridMap = gridData
                view.buildings = buildingsData
                view.zones = zonesData

                val drawer = AntDrawer(view)
                drawer.landmarks = landmarkPoints
                view.routeDrawer = drawer

                view.setupInitialView()

                view.onMapClicked = { clickX, clickY ->
                    if (!isCalculating) {
                        var clickedPoint: Pair<Int, Int>? = null
                        for (lp in landmarkPoints) {
                            if (Math.abs(lp.first - clickX) < 5 && Math.abs(lp.second - clickY) < 5) {
                                clickedPoint = lp
                                break
                            }
                        }

                        if (clickedPoint != null) {
                            val newSelected = selectedPoints.toMutableSet()

                            if (newSelected.contains(clickedPoint)) {
                                newSelected.remove(clickedPoint)
                                if (startPoint == clickedPoint) {
                                    startPoint = newSelected.firstOrNull()
                                }
                            } else {
                                newSelected.add(clickedPoint)
                                if (startPoint == null) {
                                    startPoint = clickedPoint
                                }
                            }

                            selectedPoints = newSelected
                            calculatedRoute = emptyList()
                        }
                    }
                }
                view
            },
            update = { view ->
                val drawer = view.routeDrawer as? AntDrawer
                if (drawer != null) {
                    drawer.selected = selectedPoints
                    drawer.startPoint = startPoint
                    drawer.route = calculatedRoute
                    view.invalidate()
                }
            }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.landmark_count) + " ${selectedPoints.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            selectedPoints = emptySet()
                            startPoint = null
                            calculatedRoute = emptyList()
                        },
                        enabled = !isCalculating && selectedPoints.isNotEmpty()
                    ) {
                        Text(stringResource(R.string.trow_off))
                    }

                    Button(
                        onClick = {
                            if (selectedPoints.size < 2 || startPoint == null) return@Button
                            isCalculating = true

                            scope.launch(Dispatchers.Default) {
                                val aStar = AStarAlgorithm()

                                val matrixBuilder = MatrixBuilder(
                                    aStar = aStar,
                                    isWalkable = { x, y -> walkablePixels.contains(Pair(x, y)) },
                                    isBuilding = { x, y -> buildingPixelsSet.contains(Pair(x, y)) },
                                    getBuildingEntrance = { x, y ->
                                        buildingsData.find { it.containsPoint(x, y) }?.getFirstEntrance()
                                    }
                                )

                                val targetBuildings = mutableListOf<Building>()
                                for (point in selectedPoints) {
                                    if (point == startPoint) continue
                                    val b = landmarksBuildings.find { it.getFirstPixels() == point }
                                    if (b != null) targetBuildings.add(b)
                                }

                                val (distMatrix, _, pathMap) = matrixBuilder.build(
                                    startPoint!!.first,
                                    startPoint!!.second,
                                    targetBuildings
                                )

                                val antAlgo = AntAlgorithm(iterations = 50)
                                val (pathIndices, _) = antAlgo.solve(distMatrix, 0)

                                val fullRoute = mutableListOf<Pair<Int, Int>>()
                                for (idx in 0 until pathIndices.size - 1) {
                                    val from = pathIndices[idx]
                                    val to = pathIndices[idx + 1]
                                    val segment = pathMap[Pair(from, to)]
                                    if (segment != null) {
                                        if (fullRoute.isEmpty()) {
                                            fullRoute.addAll(segment)
                                        } else {
                                            fullRoute.addAll(segment.drop(1))
                                        }
                                    }
                                }
                                withContext(Dispatchers.Main) {
                                    calculatedRoute = fullRoute
                                    isCalculating = false
                                }
                            }
                        },
                        enabled = !isCalculating && selectedPoints.size >= 2
                    ) {
                        Text(stringResource(R.string.set_route))
                    }
                }
            }
        }
    }
}