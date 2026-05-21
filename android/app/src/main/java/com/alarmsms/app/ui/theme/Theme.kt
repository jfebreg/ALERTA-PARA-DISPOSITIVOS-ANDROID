package com.alarmsms.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkRed80,
    secondary = DarkSlate80,
    tertiary = DarkGrey80,
    background = Color(0xFF1E1F22),
    surface = Color(0xFF1E1F22)
)

private val LightColorScheme = lightColorScheme(
    primary = DarkRed40,
    secondary = DarkSlate80,
    tertiary = DarkGrey80,
    background = Color(0xFFFBFDFE),
    surface = Color(0xFFFBFDFE)
)

@Composable
fun AlarmaSmsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
