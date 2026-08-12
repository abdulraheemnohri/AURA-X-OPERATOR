package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.ai.model.ModelRepository

@Composable
fun ModelCenterScreen() {
    val context = LocalContext.current
    val repository = remember { ModelRepository(context) }
    var status by remember { mutableStateOf(repository.status()) }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Local AI Models", style = MaterialTheme.typography.headlineSmall)
        Text("Model files stay on-device. Import, validation and removal are local operations.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Primary planner", style = MaterialTheme.typography.titleMedium)
                Text("Qwen2.5 0.5B Instruct · GGUF Q4_K_M")
                Text(if (status.isValid) "READY · ${status.sizeMb} MB" else "NOT READY · ${status.error ?: "unknown state"}")
                if (status.sha256 != null) {
                    Text("SHA-256", style = MaterialTheme.typography.labelMedium)
                    Text(status.sha256, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { status = repository.status() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Recheck model integrity")
                }
            }
        }

        ModelRow("Whisper Base", "Local multilingual speech recognition", "Whisper · model dependent")
        ModelRow("Piper", "Local text-to-speech voice", "Piper · voice model dependent")
        ModelRow("Vision model", "Optional screenshot understanding", "Optional · model dependent")

        Text(
            "AURA-X never treats a filename as proof that a model is usable. The primary GGUF is checked for the GGUF file signature, minimum size and SHA-256 integrity before it is accepted.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ModelRow(name: String, description: String, format: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium)
            Text(description)
            Text(format, style = MaterialTheme.typography.labelMedium)
        }
    }
}
