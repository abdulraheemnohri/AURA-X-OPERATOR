package com.aurax.operator.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurax.operator.core.theme.AuraColors

@Composable
fun AiOrb(modifier: Modifier = Modifier, active: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "aura-orb")
    val scale = transition.animateFloat(1f, 1.08f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")
    val rotation = transition.animateFloat(0f, 360f, infiniteRepeatable(tween(9000), RepeatMode.Restart), label = "orbit")

    Box(modifier.size(132.dp).scale(if (active) scale.value else 1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize().rotate(rotation.value)) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(AuraColors.Primary.copy(alpha = .42f), AuraColors.Secondary.copy(alpha = .12f), AuraColors.Background)
                ),
                radius = size.minDimension * .46f
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(AuraColors.Primary, AuraColors.Secondary, AuraColors.Accent, AuraColors.Primary)),
                startAngle = 20f,
                sweepAngle = 275f,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Canvas(Modifier.size(82.dp)) {
            drawCircle(Brush.radialGradient(listOf(AuraColors.Primary, AuraColors.Secondary.copy(alpha = .45f))))
        }
        Text("A", color = AuraColors.TextPrimary, fontSize = 30.sp)
    }
}
