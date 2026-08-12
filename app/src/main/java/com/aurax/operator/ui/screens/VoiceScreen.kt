package com.aurax.operator.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.app.AppState
import com.aurax.operator.voice.VoiceOutput

@Composable
fun VoiceScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val voice = remember { VoiceOutput(context) }
    var transcript by remember { mutableStateOf("") }
    val pulse by rememberInfiniteTransition(label = "voice").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "pulse"
    )
    DisposableEffect(Unit) { onDispose { voice.shutdown() } }

    Box(
        Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.radialGradient(listOf(Color(0xFF182A3A), Color(0xFF050508)), center = center, radius = size.maxDimension))
            for (i in 0 until 24) {
                val a = i * (Math.PI * 2.0 / 24.0)
                val r = 100f + (i % 3) * 28f
                drawCircle(Color(0x665EEAD4), 3f, Offset(center.x + kotlin.math.cos(a).toFloat() * r, center.y + kotlin.math.sin(a).toFloat() * r))
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("AURA-X Voice", style = MaterialTheme.typography.headlineMedium)
            Box(Modifier.size((200 * pulse).dp), contentAlignment = Alignment.Center) {
                Surface(shape = MaterialTheme.shapes.extraLarge, color = Color(0xFF162B35), shadowElevation = 18.dp) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("A", style = MaterialTheme.typography.displayLarge)
                    }
                }
            }
            AssistChip(onClick = {}, label = { Text("Operator: ${AppState.operator.value.phase}") })
            Text(if (transcript.isBlank()) "Hold-to-speak backend ready" else transcript, style = MaterialTheme.typography.bodyLarge)
            Button(onClick = {
                val text = transcript.ifBlank { "Voice output test" }
                voice.speak(text)
            }) { Text("Speak response") }
            OutlinedButton(onClick = onBack) { Text("Back") }
        }
    }
}
