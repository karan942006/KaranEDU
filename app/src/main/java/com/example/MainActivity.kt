package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.KaranEDUViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Programmatic Firebase Service Initialization
        com.example.data.FirebaseService.initialize(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize the centralized ViewModel
                val viewModel: KaranEDUViewModel = viewModel()
                val profile by viewModel.userProfile.collectAsState()
                
                val navController = rememberNavController()

                // Dynamic routing checking if profile has already been configured on Room DB
                LaunchedEffect(profile) {
                    if (profile != null) {
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            viewModel = viewModel,
                            onNavigateToDashboard = {
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("dashboard") {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToChat = { navController.navigate("chat") },
                            onNavigateToQuiz = { navController.navigate("quiz") },
                            onNavigateToPlanner = { navController.navigate("planner") },
                            onNavigateToProgress = { navController.navigate("progress") },
                            onNavigateToFlashcards = { navController.navigate("flashcards") },
                            onNavigateToLogin = {
                                navController.navigate("login") {
                                    popUpTo("dashboard") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("chat") {
                        ChatScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("quiz") {
                        QuizScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("planner") {
                        PlannerScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("progress") {
                        ProgressScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("flashcards") {
                        FlashcardScreen(
                            viewModel = viewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
