package com.aurax.operator.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.security.SecurePrefs
import java.io.File

@Composable
fun VoiceCenterScreen() {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs(context) }
    var pushToTalk by remember { mutableStateOf(true) }
    var ttsEnabled by remember { mutableStateOf(true) }
    var wakeWord by remember { mutableStateOf(false) }
    var whisperPath by remember { mutableStateOf(prefs.sttModelPath) }
    var ttsPath by remember { mutableStateOf(prefs.ttsModelPath) }
    var status by remember { mutableStateOf("") }

    val whisperPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val target = File(context.filesDir, "voice/whisper-model.bin")
            target.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
                ?: error("Unable to open selected model.")
            prefs.sttModelPath = target.absolutePath
            whisperPath = target.absolutePath
            status = "Whisper model installed locally."
        }.onFailure { status = "Whisper import failed: ${it.message}" }
    }

    val ttsPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            val target = File(context.filesDir, "voice/tts-model.bin")
            target.parentFile?.mkdirs()
            context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { output -> input.copyTo(output) } }
                ?: error("Unable to open selected voice asset.")
            prefs.ttsModelPath = target.absolutePath
            ttsPath = target.absolutePath
            status = "Local TTS asset installed."
        }.onFailure { status = "TTS import failed: ${it.message}" }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Voice", style = MaterialTheme.typography.headlineSmall)
        Text("Voice processing remains local. Whisper is used automatically when a compatible local model is configured; Android TTS remains the safe speech-output fallback.")
        VoiceRow("Push to talk", pushToTalk) { pushToTalk = it }
        VoiceRow("Local TTS", ttsEnabled) { ttsEnabled = it }
        VoiceRow("Wake word gate", wakeWord) { wakeWord = it }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Offline STT", style = MaterialTheme.typography.titleMedium)
                Text(if (whisperPath.isBlank()) "Whisper model: not configured" else "Whisper model: ${File(whisperPath).name}")
                Button(onClick = { whisperPicker.launch(arrayOf("application/octet-stream", "application/x-binary", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import Whisper model")
                }
                if (whisperPath.isNotBlank()) {
                    OutlinedButton(onClick = { prefs.sttModelPath = ""; whisperPath = ""; status = "Whisper model removed." }, modifier = Modifier.fillMaxWidth()) {
                        Text("Remove Whisper model")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("auto", "en", "ur", "hi").forEach { language ->
                        FilterChip(
                            selected = prefs.sttLanguage == language,
                            onClick = { prefs.sttLanguage = language },
                            label = { Text(language.uppercase()) }
                        )
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Speech output", style = MaterialTheme.typography.titleMedium)
                Text(if (ttsPath.isBlank()) "Optional Piper-compatible asset: not configured" else "Selected local asset: ${File(ttsPath).name}")
                OutlinedButton(onClick = { ttsPicker.launch(arrayOf("application/octet-stream", "audio/*", "*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Import local TTS asset")
                }
                if (ttsPath.isNotBlank()) {
                    OutlinedButton(onClick = { prefs.ttsModelPath = ""; ttsPath = ""; status = "Local TTS asset removed." }, modifier = Modifier.fillMaxWidth()) {
                        Text("Remove TTS asset")
                    }
                }
            }
        }

        Text("Supported recognition targets: English / Urdu / Hindi", style = MaterialTheme.typography.bodySmall)
        if (status.isNotBlank()) Text(status, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun VoiceRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked, onChecked)
    }
}
