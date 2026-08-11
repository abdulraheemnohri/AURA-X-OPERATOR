package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.aurax.operator.agent.execution.TaskExecutor
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var reply by remember { mutableStateOf("Ready. Give me a safe, supported command.") }
    var running by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("AURA-X Operator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Local-first automation. The floating indicator and emergency abort remain active during execution.")
        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth()) {
            Text(reply, Modifier.padding(16.dp))
        }
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
