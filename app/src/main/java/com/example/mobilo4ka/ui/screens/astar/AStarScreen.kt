package com.example.mobilo4ka.ui.screens.astar

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.R
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapDataViewModel
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens
import com.example.mobilo4ka.utils.LocationCalibration
import com.example.mobilo4ka.utils.LocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AStarScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>,
    mapViewModel: MapViewModel = viewModel(),
    mapDataViewModel: MapDataViewModel,
    onNavigateToNeural: (Building) -> Unit
) {
    SetStatusBarColor(false)

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }

    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    val currentRating = mapDataViewModel.ratings[selectedBuilding?.id.toString()]

    var showLocationDialog by remember { mutableStateOf(false) }
    var pendingLocationPoint by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) } // ссылка на карту

    fun fetchLocation(callback: (Pair<Int, Int>) -> Unit) {
        scope.launch(Dispatchers.IO) {
            val location = locationHelper.getCurrentLocation()
            withContext(Dispatchers.Main) {
                if (location != null) {
                    val pixel = LocationCalibration.gpsToPixel(location.latitude, location.longitude)
                    callback(pixel)
                } else {
                    Toast.makeText(context, context.getString(R.string.location_not_determined), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
        } else {
            Toast.makeText(context, context.getString(R.string.permission), Toast.LENGTH_SHORT).show()
        }
    }

    fun checkPermissionsAndGetLocation(onSuccess: (Pair<Int, Int>) -> Unit) {
        val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            fetchLocation(onSuccess)
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            Toast.makeText(context, context.getString(R.string.permission_again), Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        painter = painterResource(id = R.drawable.ic_astar),
                        contentDescription = null,
                        modifier = Modifier.size(Dimens.logoSize)
                    )
                    Text(
                        text = stringResource(R.string.algo_astar),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(start = Dimens.paddingSmall)
                    )
                }
            }
        ) { paddingValues ->
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData
                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }
                        this.routeDrawer = RouteDrawer(this)
                        this.isAstarEnabled = true
                        this.isClusteringEnabled = false
                        setupInitialView()
                        mapViewRef = this
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                update = {}
            )
        }

        FloatingActionButton(
            onClick = {
                checkPermissionsAndGetLocation { pixel ->
                    pendingLocationPoint = pixel
                    showLocationDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "Моё местоположение", tint = MaterialTheme.colorScheme.onPrimary)
        }
    }

    if (showLocationDialog && pendingLocationPoint != null) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = false },
            title = { Text(stringResource(R.string.using_location)) },
            text = { Text(stringResource(R.string.choose)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val point = pendingLocationPoint!!
                        mapViewRef?.routeDrawer?.let { drawer ->
                            drawer.startPoint = point
                            drawer.endPoint = null
                            drawer.currentPath = emptyList()
                            mapViewRef?.invalidate()
                            Toast.makeText(context, context.getString(R.string.start_point), Toast.LENGTH_SHORT).show()
                        }
                        showLocationDialog = false
                        pendingLocationPoint = null
                    }
                ) {
                    Text(stringResource(R.string.start_button))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val point = pendingLocationPoint!!
                        mapViewRef?.routeDrawer?.let { drawer ->
                            if (drawer.startPoint == null) {
                                Toast.makeText(context, context.getString(R.string.first_start), Toast.LENGTH_SHORT).show()
                            } else {
                                drawer.setEndPointAndCalculate(point)
                                mapViewRef?.invalidate()
                                Toast.makeText(context, context.getString(R.string.finish_point), Toast.LENGTH_SHORT).show()
                            }
                        }
                        showLocationDialog = false
                        pendingLocationPoint = null
                    }
                ) {
                    Text(stringResource(R.string.finish_button))
                }
            }
        )
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