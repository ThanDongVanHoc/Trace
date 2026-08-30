package com.traceapp.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF5B34D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF1C0052),
    secondary = Color(0xFF665A73),
    background = Color(0xFFFCF9FF),
    surface = Color(0xFFFCF9FF),
    surfaceVariant = Color(0xFFE8E0EB),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCEBDFF),
    onPrimary = Color(0xFF2D007C),
    primaryContainer = Color(0xFF4420A5),
    background = Color(0xFF141218),
    surface = Color(0xFF141218),
)

@Composable
fun TraceTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
