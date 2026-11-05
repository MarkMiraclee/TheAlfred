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
import com.example.thealfred.ui.screens.*
import com.example.thealfred.ui.theme.BatcaveBlack
import com.example.thealfred.ui.theme.TheAlfredTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = TaskRepository(database, database.templateDao())
        val factory = MissionViewModelFactory(repository, application)

        setContent {
            val viewModel: MissionViewModel = viewModel(factory = factory)
            val userProgress by viewModel.userProgress.collectAsState()

            // Пока прогресс не загружен, берём тему по умолчанию — контент в это время не рисуется
            TheAlfredTheme(currentRank = userProgress?.currentRank ?: "D") {
                // Surface больше не основной контейнер, он будет внутри AppContent
                AppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AppContent(viewModel: MissionViewModel) {
    val userProgress by viewModel.userProgress.collectAsState()
    val showReadinessCheck by viewModel.showReadinessCheck.collectAsState()

    // Определяем фон в зависимости от ранга; null (прогресс грузится) — без картинки,
    // чтобы дефолтный ранг D не мигал перед показом актуального фона
    val backgroundRes = when (userProgress?.currentRank) {
        "D" -> R.drawable.rank_d_background
        "C" -> R.drawable.rank_c_background
        "B" -> R.drawable.rank_b_background
        "A" -> R.drawable.rank_a_background
        "S" -> R.drawable.rank_s_background
        else -> 0 // 0 или null, если фон не нужен
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Устанавливаем фоновое изображение, если оно есть
        if (backgroundRes != 0) {
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop // Растягиваем с обрезкой
            )
        }

        // Основной контент приложения поверх фона
        Surface(
            modifier = Modifier.fillMaxSize(),
            // Делаем Surface прозрачным, чтобы фон был виден; пока прогресс грузится — чёрный
            color = if (backgroundRes != 0) Color.Transparent else BatcaveBlack
        ) {
            // Контент и фон ранга рисуем только после загрузки прогресса из БД:
            // до этого показываем просто чёрный экран без прыжков картинки
            if (userProgress != null) {
                when (showReadinessCheck) {
                    // Пока идёт проверка ритуала в БД — показываем только фон,
                    // чтобы не мигать ReadinessCheck у тех, кто уже сделал ритуал
                    null -> Unit
                    true -> ReadinessCheckScreen(onRitualCompleted = { viewModel.completeRitual() })
                    false -> {
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
                                onNavigateToAddTask = { navController.navigate("add_template") },
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_template") {
                            AddTemplateScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        }
                    }
                }
            }
        }
    }
}