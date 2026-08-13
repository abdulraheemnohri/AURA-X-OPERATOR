package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.app.AuraApplication
import com.aurax.operator.data.LogExporter
import kotlinx.coroutines.launch

@Composable
fun PrivacyCenterScreen() {
    val context = LocalContext.current
    val db = (context.applicationContext as AuraApplication).db
    val scope = rememberCoroutineScope()
    var redactLogs by remember { mutableStateOf(true) }
    var privateModeProtection by remember { mutableStateOf(true) }
    var localOnly by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Privacy Center", style = MaterialTheme.typography.headlineSmall)
        Text("AURA-X keeps operator state, conversations and memories on-device. Cloud inference is not used by the local runtime.")
        SwitchRow("Local-only processing", localOnly) { localOnly = it }
        SwitchRow("Redact sensitive audit values", redactLogs) { redactLogs = it }
        SwitchRow("Protect private/incognito screens", privateModeProtection) { privateModeProtection = it }

        OutlinedButton(
            onClick = {
                scope.launch {
                    runCatching {
                        val csv = LogExporter.csv(context)
                        context.openFileOutput("aura-x-safety-${System.currentTimeMillis()}.csv", 0).use {
                            it.write(csv.toByteArray())
                        }
                        message = "Safety log exported to app-private storage."
                    }.onFailure { message = "Export failed: ${it.message ?: "unknown error"}" }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export operator logs") }

        OutlinedButton(
            onClick = {
                scope.launch {
                    db.dao().clearMessages()
                    message = "Conversation history cleared."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear conversations") }

        OutlinedButton(
            onClick = {
                scope.launch {
                    db.dao().clearMemories()
                    message = "Local memories cleared."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Clear local memories") }

        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.padding(top = 12.dp))
        Switch(checked, onChecked)
    }
}
