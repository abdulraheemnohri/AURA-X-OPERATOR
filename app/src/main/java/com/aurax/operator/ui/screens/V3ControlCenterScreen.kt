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

private enum class V3Center {
    OVERVIEW, MODELS, VOICE, UPGRADE, SAFETY, PRIVACY, DIAGNOSTICS
}

@Composable
fun V3ControlCenterScreen() {
    var center by remember { mutableStateOf(V3Center.OVERVIEW) }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("AURA-X NEXUS Control Center", style = MaterialTheme.typography.headlineSmall)
            Text(
                "Unified control plane for Model Hub, guarded automation, voice, local RAG, encrypted export, analytics and loopback companion controls.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(V3Center.OVERVIEW, V3Center.MODELS, V3Center.VOICE, V3Center.UPGRADE).forEach { target ->
                    FilterChip(
                        selected = center == target,
                        onClick = { center = target },
                        label = { Text(target.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(V3Center.SAFETY, V3Center.PRIVACY, V3Center.DIAGNOSTICS).forEach { target ->
                    FilterChip(
                        selected = center == target,
                        onClick = { center = target },
                        label = { Text(target.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }
        }
        when (center) {
            V3Center.OVERVIEW -> {
                item { NexusCard("Model Management", "Hugging Face browsing, GGUF downloads, resume, verification, load/unload and local lifecycle.", "READY") }
                item { NexusCard("Operator", "Accessibility automation with deterministic guardrails, confirmations and emergency abort.", "READY") }
                item { NexusCard("Voice", "Wake gate + continuous conversation state machine added; microphone/model runtime remains model/engine-gated.", "RUNTIME READY / MODEL-GATED") }
                item { NexusCard("Vision", "Screenshot and accessibility context remain available; multimodal interpretation is still model-gated.", "MODEL-GATED") }
                item { NexusCard("RAG / Memory", "Added file-backed chunking, local vectors and semantic top-K retrieval without a cloud dependency.", "READY") }
                item { NexusCard("LAN Companion", "Added a loopback-only authenticated HTTP health/about endpoint; it never binds the phone's LAN interface.", "GUARDED READY") }
                item { NexusCard("Backup", "Added AES-GCM encrypted export container and staged restore artifact handling.", "READY / STAGED RESTORE") }
                item { NexusCard("Analytics", "Added Room-backed aggregation for task success, memories, safety events and ready model counts.", "READY") }
                item { NexusCard("Quick Settings", "Added an Android Quick Settings tile for emergency abort and cockpit access.", "READY") }
                item { NexusCard("Onboarding", "Existing permission, safety and readiness surfaces remain the activation gate.", "READY") }
            }
            V3Center.MODELS -> item { ModelCenterScreen() }
            V3Center.VOICE -> item { VoiceCenterScreen() }
            V3Center.UPGRADE -> item { NexusUpgradeCenterScreen(androidx.compose.ui.platform.LocalContext.current) }
            V3Center.SAFETY -> item { SafetyCenterScreen() }
            V3Center.PRIVACY -> item { PrivacyCenterScreen() }
            V3Center.DIAGNOSTICS -> item { DiagnosticsScreen() }
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
