package com.aurax.operator.ui.screens

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.app.AppState
import com.aurax.operator.data.AppLogStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val state = AppState.operator.value
    var logs by remember { mutableStateOf(AppLogStore.read(context)) }
    var message by remember { mutableStateOf("") }
    var exportFormat by remember { mutableStateOf("txt") }

    fun refreshLogs() { logs = AppLogStore.read(context) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        if (uri != null) {
            runCatching {
                val content = when (exportFormat) {
                    "json" -> AppLogStore.exportJson(context)
                    "csv" -> AppLogStore.exportCsv(context)
                    else -> AppLogStore.exportText(context)
                }
                context.contentResolver.openOutputStream(uri)?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                message = "Export completed."
            }.onFailure { message = "Export failed: ${it.message ?: "unknown error"}" }
        }
    }

    Column(
        Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineSmall)
        Diagnostic("Android", "${Build.VERSION.RELEASE} / API ${Build.VERSION.SDK_INT}")
        Diagnostic("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
        Diagnostic("Operator phase", state.phase.name)
        Diagnostic("Current step", state.currentStep.ifBlank { "None" })
        Diagnostic("Task", state.currentTaskId?.toString() ?: "None")
        Diagnostic("Abort flag", state.abortRequested.toString())

        HorizontalDivider()
        Text("Complete app log recording", style = MaterialTheme.typography.titleLarge)
        Text(
            "AURA-X keeps a persistent app-owned diagnostic log, including lifecycle events, model failures and uncaught exceptions. It does not claim access to device-wide logcat.",
            style = MaterialTheme.typography.bodySmall
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = {
                clipboard.setText(AnnotatedString(logs))
                message = "Complete app log copied to clipboard."
            }, enabled = logs.isNotBlank(), modifier = Modifier.weight(1f)) { Text("Copy All") }
            OutlinedButton(onClick = { refreshLogs(); message = "Log refreshed." }, modifier = Modifier.weight(1f)) { Text("Refresh") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            FilterChip(selected = exportFormat == "txt", onClick = { exportFormat = "txt" }, label = { Text("TXT") })
            FilterChip(selected = exportFormat == "json", onClick = { exportFormat = "json" }, label = { Text("JSON") })
            FilterChip(selected = exportFormat == "csv", onClick = { exportFormat = "csv" }, label = { Text("CSV") })
        }
        Button(onClick = {
            val mime = when (exportFormat) {
                "json" -> "application/json"
                "csv" -> "text/csv"
                else -> "text/plain"
            }
            exportLauncher.launch("aura-x-app-log.$exportFormat")
        }, enabled = logs.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Export $exportFormat")
        }
        OutlinedButton(onClick = {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "AURA-X app log")
                putExtra(Intent.EXTRA_TEXT, logs)
            }
            context.startActivity(Intent.createChooser(send, "Share AURA-X app log"))
            message = "Share sheet opened."
        }, enabled = logs.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Share Log") }
        TextButton(onClick = {
            AppLogStore.clear(context)
            refreshLogs()
            message = "App log cleared."
        }) { Text("Clear recorded app log") }
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth().weight(1f)) {
            Text(
                logs.ifBlank { "No app log entries recorded yet." },
                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable private fun Diagnostic(name: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(name, style = MaterialTheme.typography.labelLarge)
            Text(value)
        }
    }
}
