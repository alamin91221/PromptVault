package com.nilasharafi.promptvault.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand Core Colors
val BrandPurple = Color(0xFF8A2BE2)
val BrandMagenta = Color(0xFFE040FB)
val BrandVioletLight = Color(0xFF9D4EDD)
val BrandCyan = Color(0xFF00E5FF)
val BrandCyanDark = Color(0xFF00B0FF)
val BrandGold = Color(0xFFFFD700)
val BrandGoldDark = Color(0xFFFF9100)

// Dark Theme Surfaces (Deep, sleek space aesthetic)
val DarkBackground = Color(0xFF090912)
val DarkSurface = Color(0xFF131322)
val DarkSurfaceVariant = Color(0xFF1C1C30)
val DarkSurfaceElevated = Color(0xFF24233C)
val DarkBorder = Color(0xFF2B2945)
val DarkBorderHighlight = Color(0xFF433E68)

// Text Colors (Dark Theme)
val TextPrimaryDark = Color(0xFFF5F5FA)
val TextSecondaryDark = Color(0xFFA5A5BA)
val TextTertiaryDark = Color(0xFF717088)

// Light Theme Surfaces
val LightBackground = Color(0xFFF7F7FD)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEEEEFA)
val LightSurfaceElevated = Color(0xFFE6E5F5)
val LightBorder = Color(0xFFDDDDF0)

// Text Colors (Light Theme)
val TextPrimaryLight = Color(0xFF10101E)
val TextSecondaryLight = Color(0xFF5E5E77)
val TextTertiaryLight = Color(0xFF8C8CA4)

// Status / Accent Colors
val StatusSuccess = Color(0xFF00E676)
val StatusError = Color(0xFFFF3D71)
val StatusWarning = Color(0xFFFFAB00)

// Signature Gradients
val PrimaryGradient = Brush.horizontalGradient(
    listOf(Color(0xFF8A2BE2), Color(0xFFE040FB))
)

val CyanGradient = Brush.horizontalGradient(
    listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF))
)

val GoldGradient = Brush.horizontalGradient(
    listOf(Color(0xFFFFD700), Color(0xFFFF9100))
)

val DarkGlassBackground = Brush.verticalGradient(
    listOf(Color(0xEE141324), Color(0xFA090912))
)