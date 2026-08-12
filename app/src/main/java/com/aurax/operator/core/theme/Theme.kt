package com.aurax.operator.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AuraScheme = darkColorScheme(
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
fun AuraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AuraScheme,
        typography = AuraTypography,
        shapes = AuraShapes,
        content = content
    )
}
