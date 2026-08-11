package com.aurax.operator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aurax.operator.core.theme.AuraColors
import com.aurax.operator.ui.components.AiOrb
import com.aurax.operator.ui.components.GlassCard

@Composable
fun HomeScreen(onChat: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your phone.\nYour operator.", style = MaterialTheme.typography.headlineLarge)
        Text("A local-first automation cockpit built around explicit user control.", color = AuraColors.TextSecondary)

        GlassCard(Modifier.fillMaxWidth(), emphasized = true) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                AiOrb()
                Spacer(Modifier.height(10.dp))
                Text("OPERATOR READY", color = AuraColors.Success, fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Observing • no action pending", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(14.dp))
                Button(onClick = onChat, modifier = Modifier.fillMaxWidth()) { Text("Start a task") }
            }
        }

        Text("Quick actions", style = MaterialTheme.typography.titleLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(listOf("Open Chrome", "Search YouTube", "Inspect screen", "Read status")) { action ->
                OutlinedButton(onClick = {}, colors = ButtonDefaults.outlinedButtonColors(contentColor = AuraColors.TextPrimary)) {
                    Text(action)
                }
            }
        }

        Text("Safety posture", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricCard("POLICY", "CONFIRM")
            MetricCard("LOCAL", "100%")
            MetricCard("ABORT", "READY")
        }

        GlassCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Before first run", style = MaterialTheme.typography.titleMedium)
                Text("Enable AccessibilityService and the floating indicator in Android Settings. AURA-X will visibly signal every automation state.", color = AuraColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String) {
    GlassCard(Modifier.weight(1f)) {
        Column {
            Text(label, fontSize = 10.sp, color = AuraColors.TextMuted)
            Spacer(Modifier.size(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
