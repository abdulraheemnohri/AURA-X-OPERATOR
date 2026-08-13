package com.aurax.operator.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class AuraThemeMode {
    SYSTEM, DARK, LIGHT;

    companion object {
        fun fromStored(value: String): AuraThemeMode =
            entries.firstOrNull { it.name == value.uppercase() } ?: SYSTEM
    }
}

private val AuraDarkScheme = darkColorScheme(
    primary = AuraColors.Primary,
    onPrimary = AuraColors.Background,
    primaryContainer = AuraColors.SurfaceElevated,
    onPrimaryContainer = AuraColors.TextPrimary,
    secondary = AuraColors.Secondary,
    onSecondary = AuraColors.Background,
    secondaryContainer = AuraColors.SurfaceElevated,
    tertiary = AuraColors.Accent,
    background = AuraColors.Background,
    onBackground = AuraColors.TextPrimary,
    surface = AuraColors.Surface,
    onSurface = AuraColors.TextPrimary,
    surfaceVariant = AuraColors.SurfaceElevated,
    onSurfaceVariant = AuraColors.TextSecondary,
    error = AuraColors.Error
)

private val AuraLightScheme = lightColorScheme(
    primary = ColorTokens.LightPrimary,
    onPrimary = ColorTokens.LightOnPrimary,
    primaryContainer = ColorTokens.LightPrimaryContainer,
    onPrimaryContainer = ColorTokens.LightOnPrimaryContainer,
    secondary = ColorTokens.LightSecondary,
    onSecondary = ColorTokens.LightOnSecondary,
    secondaryContainer = ColorTokens.LightSecondaryContainer,
    onSecondaryContainer = ColorTokens.LightOnSecondaryContainer,
    tertiary = ColorTokens.LightTertiary,
    onTertiary = ColorTokens.LightOnTertiary,
    background = ColorTokens.LightBackground,
    onBackground = ColorTokens.LightOnBackground,
    surface = ColorTokens.LightSurface,
    onSurface = ColorTokens.LightOnSurface,
    surfaceVariant = ColorTokens.LightSurfaceVariant,
    onSurfaceVariant = ColorTokens.LightOnSurfaceVariant,
    error = AuraColors.Error
)

private object ColorTokens {
    val LightPrimary = androidx.compose.ui.graphics.Color(0xFF4656C8)
    val LightOnPrimary = androidx.compose.ui.graphics.Color.White
    val LightPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFDDE1FF)
    val LightOnPrimaryContainer = androidx.compose.ui.graphics.Color(0xFF111A5A)
    val LightSecondary = androidx.compose.ui.graphics.Color(0xFF006A61)
    val LightOnSecondary = androidx.compose.ui.graphics.Color.White
    val LightSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF9CF2E7)
    val LightOnSecondaryContainer = androidx.compose.ui.graphics.Color(0xFF00201D)
    val LightTertiary = androidx.compose.ui.graphics.Color(0xFF9B405F)
    val LightOnTertiary = androidx.compose.ui.graphics.Color.White
    val LightBackground = androidx.compose.ui.graphics.Color(0xFFF8F8FC)
    val LightOnBackground = androidx.compose.ui.graphics.Color(0xFF191A20)
    val LightSurface = androidx.compose.ui.graphics.Color(0xFFF8F8FC)
    val LightOnSurface = androidx.compose.ui.graphics.Color(0xFF191A20)
    val LightSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE3E2EA)
    val LightOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF46464F)
}

private val AuraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val AuraTypography = Typography(
    headlineLarge = androidx.compose.ui.text.TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineMedium = androidx.compose.ui.text.TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun AuraTheme(mode: AuraThemeMode = AuraThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        AuraThemeMode.SYSTEM -> isSystemInDarkTheme()
        AuraThemeMode.DARK -> true
        AuraThemeMode.LIGHT -> false
    }
    MaterialTheme(
        colorScheme = if (dark) AuraDarkScheme else AuraLightScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
