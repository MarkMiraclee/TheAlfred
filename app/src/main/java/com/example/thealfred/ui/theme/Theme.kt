package com.example.thealfred.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Базовая темная схема (Ранги D, C)
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

// Схема для Ранга B (Бронза)
private val RankBColorScheme = DarkColorScheme.copy(
    primary = RankB_Primary,
    secondary = RankB_Primary.copy(alpha = 0.8f),
    background = RankB_Surface.copy(alpha = 0.4f), // МЕНЯЕМ ФОН!
    surface = RankB_Surface // И МЕНЯЕМ ЦВЕТ КАРТОЧЕК
)

// Схема для Ранга A (Серебро)
private val RankAColorScheme = DarkColorScheme.copy(
    primary = RankA_Primary,
    secondary = RankA_Primary.copy(alpha = 0.8f),
    background = RankA_Surface.copy(alpha = 0.4f), // МЕНЯЕМ ФОН!
    surface = RankA_Surface
)

// Схема для Ранга S (Золото)
private val RankSColorScheme = DarkColorScheme.copy(
    primary = RankS_Primary,
    secondary = RankS_Primary.copy(alpha = 0.8f),
    background = RankS_Surface.copy(alpha = 0.4f), // МЕНЯЕМ ФОН!
    surface = RankS_Surface 
)

@Composable
fun TheAlfredTheme(
    currentRank: String = "D", // По умолчанию ранг D
    content: @Composable () -> Unit
) {
    // Логика выбора цветовой схемы в зависимости от ранга
    val colorScheme = when (currentRank) {
        "B" -> RankBColorScheme
        "A" -> RankAColorScheme
        "S" -> RankSColorScheme
        else -> DarkColorScheme // Для рангов D и C используется базовая схема
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
