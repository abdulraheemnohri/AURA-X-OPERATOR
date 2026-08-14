package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class V3Center { OVERVIEW, MODELS, VOICE, UPGRADE, SAFETY, PRIVACY, DIAGNOSTICS, ANALYTICS }

@Composable
fun V3ControlCenterScreen() {
    var center by remember { mutableStateOf(V3Center.OVERVIEW) }

    LazyColumn(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("AURA-X NEXUS Control Center", style = MaterialTheme.typography.headlineSmall)
            Text("Unified control plane for Model Hub, guarded automation, voice, local RAG, encrypted export, analytics and companion controls.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(V3Center.OVERVIEW, V3Center.MODELS, V3Center.VOICE, V3Center.UPGRADE).forEach { target ->
                    FilterChip(selected = center == target, onClick = { center = target }, label = { Text(target.label()) })
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(V3Center.SAFETY, V3Center.PRIVACY, V3Center.DIAGNOSTICS, V3Center.ANALYTICS).forEach { target ->
                    FilterChip(selected = center == target, onClick = { center = target }, label = { Text(target.label()) })
                }
            }
        }
        when (center) {
            V3Center.OVERVIEW -> {
                item { NexusCard("Model Management", "Hugging Face browsing, GGUF downloads, resume, verification, load/unload and local lifecycle.", "READY") }
                item { NexusCard("Operator", "Accessibility automation with deterministic guardrails, confirmations and emergency abort.", "READY") }
                item { NexusCard("Voice", "Wake gate and continuous-conversation state machine; detector inference remains model-gated.", "RUNTIME READY / MODEL-GATED") }
                item { NexusCard("Vision", "Screenshot and accessibility context; multimodal interpretation remains model-gated.", "MODEL-GATED") }
                item { NexusCard("RAG / Memory", "File-backed chunking, local vectors and semantic top-K retrieval.", "READY") }
                item { NexusCard("LAN Companion", "Loopback-only authenticated companion path; no unrestricted LAN agent endpoint.", "GUARDED READY") }
                item { NexusCard("Backup", "AES-GCM encrypted export container and staged restore handling.", "READY / STAGED RESTORE") }
                item { NexusCard("Analytics", "Room-backed task, memory and safety aggregation with a functional dashboard.", "READY") }
                item { NexusCard("Quick Settings", "Emergency abort and cockpit access from Android Quick Settings.", "READY") }
                item { NexusCard("Onboarding", "Permission, safety and readiness surfaces remain the activation gate.", "READY") }
            }
            V3Center.MODELS -> item { ModelCenterScreen() }
            V3Center.VOICE -> item { VoiceCenterScreen() }
            V3Center.UPGRADE -> item { NexusUpgradeCenterScreen(androidx.compose.ui.platform.LocalContext.current) }
            V3Center.SAFETY -> item { SafetyCenterScreen() }
            V3Center.PRIVACY -> item { PrivacyCenterScreen() }
            V3Center.DIAGNOSTICS -> item { DiagnosticsScreen() }
            V3Center.ANALYTICS -> item { AnalyticsScreen() }
        }
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Implementation rule", style = MaterialTheme.typography.titleMedium)
                    Text("Only concrete runtime paths are marked READY. Native/model-dependent features stay explicitly gated.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { center = V3Center.UPGRADE }, modifier = Modifier.fillMaxWidth()) { Text("Open Upgrade Controls") }
                }
            }
        }
    }
}

private fun V3Center.label(): String = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun NexusCard(title: String, description: String, status: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(status, style = MaterialTheme.typography.labelMedium)
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
