package com.aurax.operator.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurax.operator.core.theme.AuraColors

@Composable
fun OperatorIndicator(
    blocked: Boolean = false,
    acting: Boolean = false,
    onAbort: () -> Unit = {}
) {
    val color = when {
        blocked -> AuraColors.Error
        acting -> AuraColors.Warning
        else -> AuraColors.Success
    }
    val pulse = rememberInfiniteTransition(label = "indicator")
        .animateFloat(.55f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse")
    Row(
        modifier = Modifier
            .background(AuraColors.SurfaceElevated.copy(alpha = .94f), RoundedCornerShape(18.dp))
            .clickable(onClick = onAbort),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(10.dp).alpha(pulse.value).background(color, androidx.compose.foundation.shape.CircleShape)
            )
        }
        Text(
            when { blocked -> "BLOCKED"; acting -> "ACTING • TAP TO STOP"; else -> "OBSERVING" },
            color = AuraColors.TextPrimary,
            fontSize = 11.sp
        )
    }
}
