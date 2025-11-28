package com.example.thealfred

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thealfred.data.AppDatabase
import com.example.thealfred.data.TaskRepository
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.MissionViewModelFactory
import com.example.thealfred.ui.screens.*
import com.example.thealfred.ui.theme.TheAlfredTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao(), database.templateDao()) 
        val factory = MissionViewModelFactory(repository, application)

        setContent {
            val viewModel: MissionViewModel = viewModel(factory = factory)
            val userProgress by viewModel.userProgress.collectAsState()

            TheAlfredTheme(currentRank = userProgress.currentRank) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MissionViewModel) {
    val showReadinessCheck by viewModel.showReadinessCheck.collectAsState()

    if (showReadinessCheck) {
        ReadinessCheckScreen(onRitualCompleted = { viewModel.completeRitual() })
    } else {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "daily_briefing",
        ) {
            composable("daily_briefing") {
                DailyBriefingScreen(
                    viewModel = viewModel,
                    onNavigateToCompleted = { navController.navigate("completed_tasks") },
                    onNavigateToJournal = { navController.navigate("batcave_log") },
                    onNavigateToTemplates = { navController.navigate("templates") }, 
                    onNavigateToAddTask = { navController.navigate("add_task") }
                )
            }
            composable("add_task") {
                AddTaskScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("batcave_log") {
                BatcaveLogScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("completed_tasks") {
                CompletedTasksScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("templates") { 
                TemplatesScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}