package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ModelCenterScreen() {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Local AI Models", style = MaterialTheme.typography.headlineSmall)
        Text("Model files stay on-device. Download/import is intentionally separate from automation permissions.")
        ModelRow("Qwen2.5 0.5B Instruct", "Primary planner / intent model", "GGUF")
        ModelRow("Whisper Base", "Local multilingual speech recognition", "Whisper")
        ModelRow("Piper", "Local text-to-speech voice", "Piper")
        ModelRow("Vision model", "Optional screenshot understanding", "Optional")
    }
}

@Composable private fun ModelRow(name: String, description: String, format: String) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(description)
        Text(format, style = MaterialTheme.typography.labelMedium)
    } }
}
