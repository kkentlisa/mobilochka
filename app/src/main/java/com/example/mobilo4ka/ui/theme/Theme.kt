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
    outline = Line,
    surfaceTint = BackgroundLight
)

object AppAlpha {
    const val USER_MESSAGE = 0.2f
    const val COLOR = 0.5f
    const val MAP_UI_ALPHA = 0.9f

    const val MODIFIER_WEIGH = 1f
    const val GHOST_BUTTON_ALPHA = 0.5f
    const val RADIUS_POINT = 0.6f

    const val SHIP_SELECTION_ALPHA = 0.1f
    const val STROKE_WIDTH_PATH = 0.8F
    const val STROKE_WIDTH_BORDER = 0.8F
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