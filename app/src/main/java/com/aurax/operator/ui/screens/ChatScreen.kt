package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.agent.execution.TaskExecutor
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("Ready. Give me a safe, supported command.") }
    var running by remember { mutableStateOf(false) }
    var showVoice by remember { mutableStateOf(false) }

    if (showVoice) {
        Surface(Modifier.fillMaxSize()) {
            VoiceScreen(onBack = { showVoice = false })
        }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text("AURA-X Operator", style = MaterialTheme.typography.headlineSmall)
                Text("Local-first automation with visible safety controls.", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { showVoice = true }) {
                Icon(Icons.Default.Mic, contentDescription = "Voice mode")
            }
        }
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Operator response", style = MaterialTheme.typography.titleMedium)
                Text(reply)
            }
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                enabled = !running,
                placeholder = { Text("Try: open Chrome and search weather") }
            )
            Spacer(Modifier.width(8.dp))
            Button(
                enabled = text.isNotBlank() && !running,
                onClick = {
                    val input = text.trim()
                    text = ""
                    running = true
                    scope.launch {
                        reply = TaskExecutor(context).execute(input)
                        running = false
                    }
                }
            ) { Text(if (running) "Running…" else "Send") }
        }
    }
}
