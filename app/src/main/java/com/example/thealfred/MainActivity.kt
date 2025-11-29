package com.example.thealfred

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thealfred.data.AppDatabase
import com.example.thealfred.data.TaskRepository
import com.example.thealfred.ui.MissionViewModel
import com.example.thealfred.ui.MissionViewModelFactory
import com.example.thealfred.ui.ReadinessCheckState
import com.example.thealfred.ui.screens.*
import com.example.thealfred.ui.theme.TheAlfredTheme
import com.example.thealfred.ui.utils.LocaleHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Загружаем язык при старте
        LocaleHelper.loadLocale(this)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database.taskDao(), database.templateDao())
        val factory = MissionViewModelFactory(repository, application)

        setContent {
            val viewModel: MissionViewModel = viewModel(factory = factory)
            val userProgress by viewModel.userProgress.collectAsState()

            // Pass the rank to the theme, defaulting to "D"
            TheAlfredTheme(currentRank = userProgress?.currentRank ?: "D") {
                AppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MissionViewModel) {
    val userProgress by viewModel.userProgress.collectAsState()
    val readinessState by viewModel.readinessCheckState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // First, check if userProgress is loaded.
        if (userProgress == null) {
            // While loading, show a neutral background
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {}
        } else {
            // Once loaded, determine and show the rank-based background
            val backgroundRes = when (userProgress!!.currentRank) {
                "C" -> R.drawable.rank_c_background
                "B" -> R.drawable.rank_b_background
                "A" -> R.drawable.rank_a_background
                "S" -> R.drawable.rank_s_background
                else -> R.drawable.rank_d_background
            }
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Then, show the actual content on a transparent surface
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                when (readinessState) {
                    is ReadinessCheckState.Loading -> {
                        // While waiting for the ritual decision, show an empty screen (the background is already there)
                        Box(modifier = Modifier.fillMaxSize())
                    }
                    is ReadinessCheckState.Show -> {
                        ReadinessCheckScreen(onRitualCompleted = { viewModel.completeRitual() })
                    }
                    is ReadinessCheckState.Hide -> {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = "daily_briefing",
                        ) {
                            composable("daily_briefing") {
                                DailyBriefingScreen(
                                    viewModel = viewModel,
                                    onNavigateToCompleted = { navController.navigate("completed_tasks") },
                                    onNavigateToTemplates = { navController.navigate("templates") },
                                    onNavigateToAddTask = { navController.navigate("add_task") },
                                    onNavigateToSettings = { navController.navigate("settings") }
                                )
                            }
                            composable("add_task") { AddTaskScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }) }
                            composable("batcave_log") { BatcaveLogScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }) }
                            composable("completed_tasks") { CompletedTasksScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }) }
                            composable("templates") { TemplatesScreen(viewModel = viewModel, onNavigateToAddTask = { navController.navigate("add_template") }, onNavigateBack = { navController.popBackStack() }) }
                            composable("add_template") { AddTemplateScreen(viewModel = viewModel, onNavigateBack = { navController.popBackStack() }) }
                            composable("settings") { SettingsScreen(onNavigateBack = { navController.popBackStack() }) }
                        }
                    }
                }
            }
        }
    }
}
