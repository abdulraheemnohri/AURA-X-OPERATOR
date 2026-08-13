package com.aurax.operator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.theme.AuraColors

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier
            .shadow(if (emphasized) 18.dp else 8.dp, shape, clip = false)
            .background(
                if (emphasized) AuraColors.Primary.copy(alpha = 0.09f) else AuraColors.Glass,
                shape
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
            .padding(18.dp)
    ) { content() }
}
