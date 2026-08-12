package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.app.AppState
import com.aurax.operator.core.app.OperatorPhase
import com.aurax.operator.core.security.SafetyController

@Composable
fun SafetyCenterScreen() {
    val state = AppState.operator.value
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Safety Center", style = MaterialTheme.typography.headlineSmall)
        Text("Central emergency controls and current operator posture.")
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Operator: ${state.phase}", style = MaterialTheme.typography.titleMedium)
            Text(state.message)
            Text("Progress: ${(state.progress * 100).toInt()}%")
            if (state.abortRequested) Text("ABORT REQUESTED", color = MaterialTheme.colorScheme.error)
        } }
        Button(onClick = { SafetyController.requestAbort("Manual safety stop") }, modifier = Modifier.fillMaxWidth()) { Text("ABORT ALL ACTIONS") }
        OutlinedButton(onClick = { SafetyController.clearAbort(); AppState.setPhase(OperatorPhase.IDLE, "Ready") }, modifier = Modifier.fillMaxWidth()) { Text("Reset after abort") }
    }
}
