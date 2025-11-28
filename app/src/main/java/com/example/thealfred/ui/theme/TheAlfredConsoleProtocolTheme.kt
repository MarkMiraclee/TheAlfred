package com.example.thealfred.ui.theme

import androidx.compose.runtime.Composable

@Composable
fun TheAlfredConsoleProtocolTheme(
    currentRank: String = "D",
    content: @Composable () -> Unit
) {
    TheAlfredTheme(
        currentRank = currentRank,
        content = content
    )
}
