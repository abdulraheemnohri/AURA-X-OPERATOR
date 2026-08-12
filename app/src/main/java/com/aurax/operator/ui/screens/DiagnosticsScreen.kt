package com.aurax.operator.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.app.AppState

@Composable
fun DiagnosticsScreen() {
    val state = AppState.operator.value
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineSmall)
        Diagnostic("Android", "${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        Diagnostic("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
        Diagnostic("Operator phase", state.phase.name)
        Diagnostic("Current step", state.currentStep.ifBlank { "None" })
        Diagnostic("Task", state.currentTaskId?.toString() ?: "None")
        Diagnostic("Abort flag", state.abortRequested.toString())
    }
}

@Composable private fun Diagnostic(name: String, value: String) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp)) { Text(name, style = MaterialTheme.typography.labelLarge); Text(value) } }
}
