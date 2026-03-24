package com.example.mobilo4ka

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobilo4ka.ui.main.MainScreen
import com.example.mobilo4ka.ui.main.MainViewModel
import com.example.mobilo4ka.ui.screens.ants.AntsScreen
import com.example.mobilo4ka.ui.screens.astar.AStarScreen
import com.example.mobilo4ka.ui.screens.clustering.ClusteringScreen
import com.example.mobilo4ka.ui.screens.genetic.GeneticScreen
import com.example.mobilo4ka.ui.screens.neural.NeuralScreen
import com.example.mobilo4ka.ui.screens.tree.TreeScreen
import com.example.mobilo4ka.ui.theme.Mobilo4kaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Mobilo4kaTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                NavHost(navController = navController, startDestination = "main") {

                    composable("main"){
                        MainScreen (
                            state = state,
                            onToggleMenu = viewModel::toggleMenu,
                            onNavigate = {route -> navController.navigate(route)},
                        )
                    }
                    composable("ants") { AntsScreen() }
                    composable("astar") { AStarScreen() }
                    composable("clustering") { ClusteringScreen() }
                    composable("genetic") { GeneticScreen() }
                    composable("neural") { NeuralScreen() }
                    composable("tree") { TreeScreen() }

                }

            }
        }
    }
}