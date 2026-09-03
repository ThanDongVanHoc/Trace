package com.traceapp.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Color(0xFF5A39D6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAE4FF),
    onPrimaryContainer = Color(0xFF1B0758),
    secondary = Color(0xFF7357C8),
    secondaryContainer = Color(0xFFECE4FF),
    tertiary = Color(0xFF006C51),
    tertiaryContainer = Color(0xFF9EF2D1),
    background = Color(0xFFF9F7FC),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFE9E6EF),
    outline = Color(0xFF79747E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCCBEFF),
    onPrimary = Color(0xFF2D007C),
    primaryContainer = Color(0xFF4420A5),
    secondary = Color(0xFFD0BCFF),
    tertiary = Color(0xFF82D5B6),
    background = Color(0xFF121116),
    surface = Color(0xFF19171D),
    surfaceVariant = Color(0xFF49454F),
)

private val TraceTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
)

private val TraceShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
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
    MaterialTheme(
        colorScheme = colorScheme,
        typography = TraceTypography,
        shapes = TraceShapes,
        content = content,
    )
}
