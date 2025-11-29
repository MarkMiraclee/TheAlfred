package com.example.thealfred.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Базовая темная схема (больше не используется напрямую)
private val DarkColorScheme = darkColorScheme(
    primary = BatmanBlue,
    secondary = BatmanBlueLight,
    tertiary = WarningOrange,
    background = BatcaveBlack, 
    surface = BatcaveDark,      
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = AlertRed,
    onError = Color.White
)

// Схема для Ранга D (Пещера)
private val RankDColorScheme = DarkColorScheme.copy(
    primary = RankD_Primary,
    secondary = RankD_Primary.copy(alpha = 0.8f),
    background = RankD_Surface.copy(alpha = 0.4f),
    surface = RankD_Surface
)

// Схема для Ранга C (Пещера с компьютером)
private val RankCColorScheme = DarkColorScheme.copy(
    primary = RankC_Primary,
    secondary = RankC_Primary.copy(alpha = 0.8f),
    background = RankC_Surface.copy(alpha = 0.4f),
    surface = RankC_Surface
)

// Схема для Ранга B (Бронза)
private val RankBColorScheme = DarkColorScheme.copy(
    primary = RankB_Primary,
    secondary = RankB_Primary.copy(alpha = 0.8f),
    background = RankB_Surface.copy(alpha = 0.4f),
    surface = RankB_Surface
)

// Схема для Ранга A (Серебро)
private val RankAColorScheme = DarkColorScheme.copy(
    primary = RankA_Primary,
    secondary = RankA_Primary.copy(alpha = 0.8f),
    background = RankA_Surface.copy(alpha = 0.4f),
    surface = RankA_Surface,
    onPrimary = Color.Black, // Для лучшей читаемости на светлом фоне
    onSecondary = Color.Black // Для лучшей читаемости на светлом фоне
)

// Схема для Ранга S (Золото)
private val RankSColorScheme = DarkColorScheme.copy(
    primary = RankS_Primary,
    secondary = RankS_Primary.copy(alpha = 0.8f),
    background = RankS_Surface.copy(alpha = 0.4f),
    surface = RankS_Surface,
    onPrimary = Color.Black, // Для лучшей читаемости на светлом фоне
    onSecondary = Color.Black // Для лучшей читаемости на светлом фоне
)

@Composable
fun TheAlfredTheme(
    currentRank: String? = "D", // Ранг может быть null
    content: @Composable () -> Unit
) {
    // Логика выбора цветовой схемы в зависимости от ранга
    val colorScheme = when (currentRank) {
        "D" -> RankDColorScheme
        "C" -> RankCColorScheme
        "B" -> RankBColorScheme
        "A" -> RankAColorScheme
        "S" -> RankSColorScheme
        else -> RankDColorScheme // По умолчанию тема ранга D
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
