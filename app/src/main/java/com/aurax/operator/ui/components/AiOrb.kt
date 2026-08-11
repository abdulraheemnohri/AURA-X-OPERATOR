package com.aurax.operator.ui.components
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurax.operator.core.theme.AuraColors
@Composable fun AiOrb(modifier:Modifier=Modifier){val t=rememberInfiniteTransition(label="orb");val s=t.animateFloat(1f,1.12f,infiniteRepeatable(tween(1500),RepeatMode.Reverse),label="scale");Box(modifier.size(120.dp).scale(s.value).background(Brush.radialGradient(listOf(AuraColors.Primary,AuraColors.Secondary.copy(alpha=.25f),AuraColors.Background)),CircleShape),contentAlignment=Alignment.Center){Text("A",color=AuraColors.TextPrimary,fontSize=36.sp)}}