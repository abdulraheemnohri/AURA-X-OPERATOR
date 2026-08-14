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
    OVERVIEW, MODELS, VOICE, SAFETY, PRIVACY, DIAGNOSTICS
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
                "One control plane for Model Hub, local inference, voice, operator safety and diagnostics. Features are reported honestly: ready, permission-gated, model-gated or not bundled.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(V3Center.OVERVIEW, V3Center.MODELS, V3Center.VOICE).forEach { target ->
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
                item { NexusCard("Operator", "Accessibility-based controlled automation, deterministic guardrails, confirmations and emergency abort.", "READY") }
                item { NexusCard("Voice", "Local STT/TTS architecture with model-gated voice assets.", "MODEL-GATED") }
                item { NexusCard("Vision", "Accessibility tree and screenshot context are available; multimodal interpretation requires a compatible local model.", "MODEL-GATED") }
                item { NexusCard("RAG / Memory", "Conversation and memory persistence are available; full vector retrieval remains a separate runtime capability.", "PARTIAL") }
                item { NexusCard("Plugins", "Tool registry architecture is present; arbitrary third-party plugin execution is intentionally not enabled by default.", "GUARDED") }
                item { NexusCard("LAN Server", "No unrestricted network agent endpoint is enabled by default. Local-first operation remains the default security posture.", "NOT-BUNDLED") }
                item { NexusCard("Backup / Restore", "Local data export and safety-log export are available; full encrypted project backup is a separate release gate.", "PARTIAL") }
                item { NexusCard("Analytics", "Diagnostics and local operational telemetry exist; a full dashboard is not claimed until its data pipeline is complete.", "PARTIAL") }
                item { NexusCard("Widgets", "Android UI is the current primary surface. Home-screen widget support remains a separate capability.", "NOT-BUNDLED") }
                item { NexusCard("Onboarding", "Permission and readiness checks are exposed through Settings and Permission Center.", "READY") }
            }
            V3Center.MODELS -> item { ModelCenterScreen() }
            V3Center.VOICE -> item { VoiceCenterScreen() }
            V3Center.SAFETY -> item { SafetyCenterScreen() }
            V3Center.PRIVACY -> item { PrivacyCenterScreen() }
            V3Center.DIAGNOSTICS -> item { DiagnosticsScreen() }
        }
        item {
            Spacer(Modifier.height(8.dp))
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Implementation rule", style = MaterialTheme.typography.titleMedium)
                    Text("A UI control is only marked READY when its runtime path exists. Placeholder switches are not presented as implemented functionality.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { center = V3Center.MODELS }, modifier = Modifier.fillMaxWidth()) { Text("Open Model Hub") }
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