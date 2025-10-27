package com.example.foodhelp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = ComponentAccent,
    secondary = ComponentAccent,
    background = AppBackground,
    surface = SurfaceBackground,
    onPrimary = AppBackground,
    onSurface = SelectedToggle

)

@Composable
fun MyApplicationTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}