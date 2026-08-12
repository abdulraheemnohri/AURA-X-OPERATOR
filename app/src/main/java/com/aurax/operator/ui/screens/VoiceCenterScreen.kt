package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VoiceCenterScreen() {
    var pushToTalk by remember { mutableStateOf(true) }
    var ttsEnabled by remember { mutableStateOf(true) }
    var wakeWord by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Voice", style = MaterialTheme.typography.headlineSmall)
        Text("Voice processing remains local when the corresponding model is installed.")
        VoiceRow("Push to talk", pushToTalk) { pushToTalk = it }
        VoiceRow("Local TTS", ttsEnabled) { ttsEnabled = it }
        VoiceRow("Wake word", wakeWord) { wakeWord = it }
        Text("Supported recognition target: English / Urdu / Hindi", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable private fun VoiceRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Switch(checked, onChecked) }
}
