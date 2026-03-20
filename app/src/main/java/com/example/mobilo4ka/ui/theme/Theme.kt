package com.example.mobilo4ka.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = Colors.tsuBlue,
    onPrimary = Colors.surfaceWhite,
    background = Colors.backgroundLight,
    onBackground = Colors.textDark,
    surface = Colors.surfaceWhite,
    onSurface = Colors.textDark,
    outline = Colors.tsuBlue
)

@Composable
fun Mobilo4kaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}