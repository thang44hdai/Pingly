package com.thangnq.pingly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4355C7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE3FF),
    background = Color(0xFFF9FAFF),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB8C3FF),
    primaryContainer = Color(0xFF2B398E),
    background = Color(0xFF111318),
    surface = Color(0xFF1B1D24),
)

@Composable
fun PinglyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
