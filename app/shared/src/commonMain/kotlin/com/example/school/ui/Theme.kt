package com.example.school.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Brand Color Palette
val PrimaryOrange = Color(0xFFFF6B00)
val PrimaryDarkOrange = Color(0xFFE05D00)
val SecondaryNavy = Color(0xFF1A2530)
val BackgroundOffWhite = Color(0xFFF9FAFB)
val SurfaceWhite = Color(0xFFFFFFFF)
val StatusGreen = Color(0xFF10B981)
val StatusAmber = Color(0xFFF59E0B)
val TextDark = Color(0xFF1E293B)
val TextMuted = Color(0xFF64748B)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    secondary = SecondaryNavy,
    onSecondary = Color.White,
    background = BackgroundOffWhite,
    onBackground = TextDark,
    surface = SurfaceWhite,
    onSurface = TextDark
)

@Composable
fun EVotingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
