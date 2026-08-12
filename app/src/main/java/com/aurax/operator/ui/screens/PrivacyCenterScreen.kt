package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyCenterScreen() {
    var redactLogs by remember { mutableStateOf(true) }
    var privateModeProtection by remember { mutableStateOf(true) }
    var localOnly by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Privacy Center", style = MaterialTheme.typography.headlineSmall)
        Text("AURA-X is designed for local execution and explicit user control.")
        SwitchRow("Local-only processing", localOnly) { localOnly = it }
        SwitchRow("Redact sensitive audit values", redactLogs) { redactLogs = it }
        SwitchRow("Protect private/incognito screens", privateModeProtection) { privateModeProtection = it }
        OutlinedButton(onClick = { }) { Text("Export operator logs") }
        OutlinedButton(onClick = { }) { Text("Clear local memories") }
    }
}

@Composable private fun SwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, modifier = Modifier.padding(top = 12.dp)); Switch(checked, onChecked) }
}
