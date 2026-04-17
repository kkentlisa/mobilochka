package com.example.mobilo4ka.ui.screens.clustering

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobilo4ka.R
import com.example.mobilo4ka.algorithms.clustering.ClusteringMode
import com.example.mobilo4ka.data.models.Building
import com.example.mobilo4ka.data.models.GridMap
import com.example.mobilo4ka.ui.card.BuildingBottomSheet
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.map.MapView
import com.example.mobilo4ka.ui.system.SetStatusBarColor
import com.example.mobilo4ka.ui.theme.Dimens


@Composable
@ExperimentalLayoutApi
@ExperimentalMaterial3Api
fun ClusteringScreen(
    gridData: GridMap,
    buildingsData: List<Building>,
    zonesData: Map<String, List<List<Int>>>,
    viewModel: ClusteringViewModel = viewModel(),
    mapViewModel: MapViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }

    SetStatusBarColor(false)

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .statusBarsPadding()
                    .padding(Dimens.paddingLarge),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clustering),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.logoSize)
                )
                Text(
                    text = stringResource(R.string.algo_clustering),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(Dimens.paddingSmall)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                factory = { context ->
                    MapView(context).apply {
                        this.gridMap = gridData
                        this.buildings = buildingsData
                        this.zones = zonesData
                        this.onBuildingClicked = { x, y ->
                            selectedBuilding = mapViewModel.findBuilding(x, y)
                        }
                        this.isAstarEnabled = false
                        this.isClusteringEnabled = true
                        setupInitialView()
                    }
                },
                update = { mapView ->
                    mapView.updateClustering(state.points, state.mode)
                    mapView.invalidate()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!state.showSettings) {

                FloatingActionButton(
                    onClick = { viewModel.setShowSettings(true) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.paddingLarge)
                        .size(Dimens.fabSize),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.clustering_settings),
                        modifier = Modifier.size(Dimens.fabIconSize)
                    )
                }
            }

            if (state.showSettings) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.setShowSettings(false)
                        }
                ) {
                    ElevatedCard(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth()
                            .padding(Dimens.paddingLarge)
                            .clickable(enabled = false) {},
                        shape = RoundedCornerShape(Dimens.cardCornerRadius),
                        colors = CardDefaults.cardColors(
                            MaterialTheme.colorScheme.surface.copy(
                                alpha = 0.98f
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(
                                    horizontal = Dimens.paddingExtraLarge,
                                    vertical = Dimens.paddingLarge
                                ),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.clustering_settings),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                IconButton(onClick = { viewModel.setShowSettings(false) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.close_settings)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                            Text(
                                text = stringResource(R.string.clusters_count, state.kValue),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Slider(
                                value = state.kValue.toFloat(),
                                onValueChange = { viewModel.setKValue(it.toInt()) },
                                valueRange = 2f..6f,
                                steps = 3,
                                thumb = {
                                    Surface(
                                        modifier = Modifier.size(Dimens.sliderThumbSize),
                                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                        color = MaterialTheme.colorScheme.primary,
                                        shadowElevation = Dimens.shadowHeight
                                    ) { }
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(Dimens.sliderTrackSize),
                                        colors = SliderDefaults.colors(
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.background,
                                            activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(
                                                alpha = 0.8f
                                            ),
                                            inactiveTickColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.height(Dimens.paddingExtraSmall))

                            val modes = listOf(
                                ClusteringMode.EUCLIDEAN to stringResource(R.string.mode_euclidean),
                                ClusteringMode.ASTAR to stringResource(R.string.mode_astar),
                                ClusteringMode.COMPARISON to stringResource(R.string.mode_comparison)
                            )

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
                            ) {
                                modes.forEach { (m, label) ->
                                    FilterChip(
                                        selected = state.mode == m,
                                        onClick = { viewModel.setMode(m) },
                                        label = {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        shape = RoundedCornerShape(Dimens.cardCornerRadius),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.background,
                                            labelColor = MaterialTheme.colorScheme.onBackground
                                        ),
                                        border = null
                                    )
                                }
                            }

                            if (state.mode == ClusteringMode.COMPARISON) {
                                Spacer(modifier = Modifier.height(Dimens.paddingLarge))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(Dimens.clusterLegend)
                                                .background(
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.paddingExtraSmall))
                                        Text(
                                            text = stringResource(R.string.legend_euclidean),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(Dimens.paddingLarge))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(Dimens.clusterLegend)
                                                .border(
                                                    Dimens.borderSize,
                                                    MaterialTheme.colorScheme.primary,
                                                    CircleShape
                                                )
                                        )
                                        Spacer(modifier = Modifier.width(Dimens.paddingExtraSmall))
                                        Text(
                                            text = stringResource(R.string.legend_astar),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                            Button(
                                onClick = {
                                    viewModel.runClustering(
                                        buildingsData,
                                        gridData
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(Dimens.clusterButton),
                                enabled = !state.isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(Dimens.buttonRadius)
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Dimens.fabIconSize),
                                        color = MaterialTheme.colorScheme.surface
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.calculate_clusters),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
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
}