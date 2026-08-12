package com.aurax.operator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurax.operator.core.theme.AuraColors
import com.aurax.operator.ui.components.AiOrb
import com.aurax.operator.ui.components.GlassCard

@Composable
fun HomeScreen(onChat: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AURA-X", style = MaterialTheme.typography.labelLarge, color = AuraColors.Secondary)
                Text("Your phone.\nYour operator.", style = MaterialTheme.typography.headlineLarge)
                Text("Local-first automation with visible control.", color = AuraColors.TextSecondary)
            }
            IconButton(onClick = onChat) { Icon(Icons.Default.Chat, contentDescription = "Open chat") }
        }

        GlassCard(Modifier.fillMaxWidth(), emphasized = true) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(
                    Brush.radialGradient(listOf(AuraColors.PrimaryGlow, AuraColors.SurfaceElevated))
                ).padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    AiOrb()
                    Spacer(Modifier.height(8.dp))
                    AssistChip(onClick = {}, label = { Text("●  OPERATOR READY") })
                    Spacer(Modifier.height(8.dp))
                    Text("Observing • no action pending", style = MaterialTheme.typography.titleMedium)
                    Text("All actions remain guarded and abortable.", style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = onChat, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start a task")
                    }
                }
            }
        }

        Text("Quick actions", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 12.dp)) {
            items(
                listOf(
                    QuickAction("Ask AURA-X", "Chat", Icons.Default.Chat),
                    QuickAction("Inspect screen", "Operator", Icons.Default.Visibility),
                    QuickAction("Voice mode", "Voice", Icons.Default.Mic),
                    QuickAction("Safety", "Guardrails", Icons.Default.Security)
                )
            ) { action ->
                ElevatedCard(Modifier.width(148.dp)) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(action.icon, contentDescription = null, tint = AuraColors.Secondary)
                        Text(action.title, style = MaterialTheme.typography.titleSmall)
                        Text(action.subtitle, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
                        TextButton(onClick = onChat) { Text("Open") }
                    }
                }
            }
        }

        Text("Safety posture", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard(Modifier.weight(1f), "POLICY", "CONFIRM", Icons.Default.VerifiedUser)
            MetricCard(Modifier.weight(1f), "LOCAL", "ON DEVICE", Icons.Default.PhoneAndroid)
            MetricCard(Modifier.weight(1f), "ABORT", "READY", Icons.Default.StopCircle)
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("First-run checklist", style = MaterialTheme.typography.titleMedium)
                ChecklistRow("AccessibilityService", "Required for UI inspection and guarded actions")
                ChecklistRow("Overlay access", "Required for the persistent abort indicator")
                ChecklistRow("Local model", "Optional; import a compatible GGUF from Settings")
            }
        }
    }
}

private data class QuickAction(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
private fun MetricCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    GlassCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = AuraColors.Secondary)
            Text(label, fontSize = 10.sp, color = AuraColors.TextSecondary)
            Text(value, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun ChecklistRow(title: String, detail: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp), tint = AuraColors.Success)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = AuraColors.TextSecondary)
        }
    }
}
