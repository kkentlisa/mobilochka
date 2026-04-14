package com.example.mobilo4ka.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColorScheme = lightColorScheme(
    primary = TsuBlue,
    onPrimary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark,
    outline = TsuBlue
)

object AppAlpha {
    const val USER_MESSAGE = 0.2f

    const val COLOR = 0.5f
}

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