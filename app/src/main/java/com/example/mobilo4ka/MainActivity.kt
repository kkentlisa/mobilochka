package com.example.mobilo4ka

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilo4ka.ui.card.MapViewModel
import com.example.mobilo4ka.ui.main.MainScreen
import com.example.mobilo4ka.ui.main.MainViewModel
import com.example.mobilo4ka.ui.map.MapDataViewModel
import com.example.mobilo4ka.ui.screens.ants.AntsScreen
import com.example.mobilo4ka.ui.screens.astar.AStarScreen
import com.example.mobilo4ka.ui.screens.clustering.ClusteringScreen
import com.example.mobilo4ka.ui.screens.clustering.ClusteringViewModel
import com.example.mobilo4ka.ui.screens.genetic.GeneticScreen
import com.example.mobilo4ka.ui.screens.neural.NeuralScreen
import com.example.mobilo4ka.ui.screens.neural.NeuralViewModel
import com.example.mobilo4ka.ui.screens.tree.TreeScreen
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme
import com.example.mobilo4ka.utils.LocaleHelper

class MainActivity : ComponentActivity() {
    private val mapDataViewModel: MapDataViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    @ExperimentalLayoutApi
    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !mapDataViewModel.isLoaded
        }

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.state.collectAsState()

            val context = LocalContext.current
            val localizedContext = remember(state.currentLanguage) {
                LocaleHelper.updateLocale(context, state.currentLanguage)
            }

            LaunchedEffect(state.currentLanguage) {
                mapDataViewModel.isLoaded = false
                mapDataViewModel.preloadData(localizedContext)
            }

            val registryOwner = context as? androidx.activity.result.ActivityResultRegistryOwner


            CompositionLocalProvider(
                LocalContext provides localizedContext,
                androidx.activity.compose.LocalActivityResultRegistryOwner provides registryOwner!!
            ) {
                Mobilo4kaTheme {
                    val navController = rememberNavController()

                    val neuralFactory = remember(localizedContext) {
                        viewModelFactory {
                            initializer {
                                NeuralViewModel(localizedContext)
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "main") {

                        composable("main") {
                            MainScreen(
                                state = state,
                                onToggleMenu = viewModel::toggleMenu,
                                onCloseMenu = viewModel::toggleMenu,
                                onToggleLanguage = viewModel::toggleLanguage,
                                onNavigate = { route -> navController.navigate(route) },
                            )
                        }
                        composable("ants") { AntsScreen() }
                        composable("astar") {
                            mapDataViewModel.gridData?.let { data ->
                                AStarScreen(
                                    gridData = data,
                                    buildingsData = mapDataViewModel.buildingsData,
                                    zonesData = mapDataViewModel.zonesData
                                )
                            }
                        }
                        composable("clustering") {
                            mapDataViewModel.gridData?.let { data ->
                                val clusteringViewModel: ClusteringViewModel = viewModel()
                                ClusteringScreen(
                                    gridData = data,
                                    buildingsData = mapDataViewModel.buildingsData,
                                    zonesData = mapDataViewModel.zonesData,
                                    viewModel = clusteringViewModel
                                )
                            }
                        }
                        composable("genetic") {
                            mapDataViewModel.gridData?.let { data ->
                                val mapVM: MapViewModel = viewModel()
                                GeneticScreen(
                                    gridData = data,
                                    buildingsData = mapDataViewModel.buildingsData,
                                    zonesData = mapDataViewModel.zonesData,
                                    mapViewModel = mapVM
                                )
                            }
                        }
                        composable("neural") {
                            val neuralViewModel: NeuralViewModel =
                                viewModel(factory = neuralFactory)
                            NeuralScreen(neuralViewModel)
                        }
                        composable("tree") { TreeScreen(state.currentLanguage) }
                    }
                }
            }
        }
    }
}