package com.aurax.operator.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val AuraScheme = darkColorScheme(
    primary = AuraColors.Primary,
    onPrimary = AuraColors.Background,
    primaryContainer = AuraColors.SurfaceElevated,
    onPrimaryContainer = AuraColors.TextPrimary,
    secondary = AuraColors.Secondary,
    tertiary = AuraColors.Accent,
    background = AuraColors.Background,
    onBackground = AuraColors.TextPrimary,
    surface = AuraColors.Surface,
    onSurface = AuraColors.TextPrimary,
    surfaceVariant = AuraColors.SurfaceElevated,
    onSurfaceVariant = AuraColors.TextSecondary,
    error = AuraColors.Error
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
    MaterialTheme(colorScheme = AuraScheme, typography = AuraTypography, content = content)
}
