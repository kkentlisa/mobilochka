package com.example.mobilo4ka

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilo4ka.ui.main.MainScreen
import com.example.mobilo4ka.ui.main.MainViewModel
import com.example.mobilo4ka.ui.screens.ants.AntsScreen
import com.example.mobilo4ka.ui.screens.astar.AStarScreen
import com.example.mobilo4ka.ui.screens.clustering.ClusteringScreen
import com.example.mobilo4ka.ui.screens.clustering.ClusteringViewModel
import com.example.mobilo4ka.ui.screens.genetic.GeneticScreen
import com.example.mobilo4ka.ui.screens.neural.NeuralScreen
import com.example.mobilo4ka.ui.screens.neural.NeuralViewModel
import com.example.mobilo4ka.ui.screens.tree.TreeScreen
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme
import com.example.mobilo4ka.utils.LoadMapData

class MainActivity : ComponentActivity() {
    @ExperimentalLayoutApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val gridData = LoadMapData.loadMapData(this)
        val buildingsData = LoadMapData.loadBuildings(this)
        val zonesData = LoadMapData.loadZones(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            Mobilo4kaTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                val neuralFactory = viewModelFactory {
                    initializer {
                        val app =
                            this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as android.app.Application
                        NeuralViewModel(app.applicationContext)
                    }
                }

                NavHost(navController = navController, startDestination = "main") {

                    composable("main") {
                        MainScreen(
                            state = state,
                            onToggleMenu = viewModel::toggleMenu,
                            onCloseMenu = viewModel::toggleMenu,
                            onNavigate = { route -> navController.navigate(route) },
                        )
                    }
                    composable("ants") { AntsScreen() }
                    composable("astar") {
                        gridData?.let { data ->
                            AStarScreen(
                                gridData = data,
                                buildingsData = buildingsData,
                                zonesData = zonesData
                            )
                        }
                    }
                    composable("clustering") {
                        gridData?.let { data ->
                            val clusteringViewModel: ClusteringViewModel = viewModel()
                            ClusteringScreen(
                                gridData = data,
                                buildingsData = buildingsData,
                                zonesData = zonesData,
                                viewModel = clusteringViewModel
                            )
                        }
                    }
                    composable("genetic") { GeneticScreen() }
                    composable("neural") {
                        val neuralViewModel: NeuralViewModel = viewModel(factory = neuralFactory)
                        NeuralScreen(neuralViewModel)
                    }
                    composable("tree") { TreeScreen() }

                }

            }
        }
    }
}