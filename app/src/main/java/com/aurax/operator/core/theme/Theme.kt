package com.aurax.operator.core.theme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
private val Scheme=darkColorScheme(primary=AuraColors.Primary,secondary=AuraColors.Secondary,background=AuraColors.Background,surface=AuraColors.Surface,onBackground=AuraColors.TextPrimary,onSurface=AuraColors.TextPrimary)
@Composable fun AuraTheme(content:@Composable()->Unit){ MaterialTheme(colorScheme=Scheme,typography=Typography(),content=content) }