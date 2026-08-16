package com.aurax.operator.ui.screens

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aurax.operator.analytics.AnalyticsEngine
import com.aurax.operator.data.BackupManager
import com.aurax.operator.data.database.AuraDatabase
import com.aurax.operator.memory.KnowledgeBaseManager
import com.aurax.operator.network.LanServerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import java.util.UUID

@Composable
fun NexusUpgradeCenterScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences("aura_settings_v3", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    var wake by remember { mutableStateOf(prefs.getBoolean("wake_word_enabled", false)) }
    var rag by remember { mutableStateOf(prefs.getBoolean("rag_enabled", true)) }
    var lan by remember { mutableStateOf(prefs.getBoolean("lan_server_enabled", false)) }
    var backupPassword by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Ready") }
    var analytics by remember { mutableStateOf("No snapshot loaded") }
    val kb = remember { KnowledgeBaseManager(context) }
    val authToken = remember {
        prefs.getString("lan_auth_token", null).takeUnless { it.isNullOrBlank() } ?: UUID.randomUUID().toString().also {
            prefs.edit().putString("lan_auth_token", it).apply()
        }
    }
    val server = remember { LanServerManager(scope = scope, port = prefs.getInt("lan_server_port", 8080), authEnabled = true, token = authToken) }

    val restorePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null || backupPassword.length < 8) return@rememberLauncherForActivityResult
        scope.launch {
            status = withContext(Dispatchers.IO) {
                runCatching {
                    val stagedInput = File(context.cacheDir, "restore_input_${System.currentTimeMillis()}.aura")
                    context.contentResolver.openInputStream(uri)?.use { input -> stagedInput.outputStream().use { output -> input.copyTo(output) } }
                        ?: error("Unable to read selected backup.")
                    val imported = BackupManager(context).restoreBackup(stagedInput, backupPassword).getOrThrow()
                    stagedInput.delete()
                    "Backup restored safely: $imported new records merged."
                }.getOrElse { "Restore failed: ${it.message}" }
            }
        }
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("NEXUS Upgrade Center", style = MaterialTheme.typography.headlineSmall)
        Text("New runtime controls for voice state, local RAG, guarded companion mode, encrypted backup and analytics.")

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Voice & Conversation", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Wake phrase gate")
                    Switch(checked = wake, onCheckedChange = { wake = it; prefs.edit().putBoolean("wake_word_enabled", it).apply() })
                }
                Text("The wake detector is an engine-neutral gate; always-listening still requires an installed detector/runtime model.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Memory & RAG", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Local RAG index")
                    Switch(checked = rag, onCheckedChange = { rag = it; prefs.edit().putBoolean("rag_enabled", it).apply() })
                }
                Text("Indexed chunks: ${kb.chunkCount()}")
                Button(onClick = {
                    scope.launch {
                        status = withContext(Dispatchers.IO) {
                            runCatching {
                                kb.ingestText("nexus_note", "AURA-X NEXUS local knowledge index is enabled. Preferences remain on-device.")
                                "Local knowledge seeded."
                            }.getOrElse { "Knowledge seed failed: ${it.message}" }
                        }
                    }
                }) { Text("Seed local knowledge") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Loopback Companion", style = MaterialTheme.typography.titleMedium)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Enable loopback server")
                    Switch(checked = lan, onCheckedChange = { value ->
                        lan = value
                        prefs.edit().putBoolean("lan_server_enabled", value).apply()
                        if (value) server.start().onSuccess { status = "Loopback server started on 127.0.0.1:${prefs.getInt("lan_server_port", 8080)}" }.onFailure { status = it.message ?: "Start failed" }
                        else { server.stop(); status = "Loopback server stopped" }
                    })
                }
                OutlinedTextField(value = server.authToken, onValueChange = {}, readOnly = true, label = { Text("Auth token") }, modifier = Modifier.fillMaxWidth())
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(value = backupPassword, onValueChange = { backupPassword = it }, label = { Text("Password (8+ chars)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
                Button(enabled = backupPassword.length >= 8, onClick = {
                    scope.launch {
                        status = withContext(Dispatchers.IO) {
                            runCatching { BackupManager(context).createBackup(backupPassword) }
                                .fold({ "Backup created: ${it.name}" }, { "Backup failed: ${it.message}" })
                        }
                    }
                }) { Text("Create encrypted backup") }
                Button(enabled = backupPassword.length >= 8, onClick = {
                    restorePicker.launch(arrayOf("application/octet-stream", "application/x-aura-backup", "*/*"))
                }, modifier = Modifier.fillMaxWidth()) { Text("Restore encrypted backup") }
                Text("Restore merges records; it does not wipe current conversations, memories or safety history.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Analytics", style = MaterialTheme.typography.titleMedium)
                Button(onClick = {
                    scope.launch {
                        val snapshot = withContext(Dispatchers.IO) { AnalyticsEngine(AuraDatabase.get(context).dao()).snapshot() }
                        analytics = "Tasks ${snapshot.tasks} · Completed ${snapshot.completed} · Failed ${snapshot.failed} · Success ${(snapshot.successRate * 100).toInt()}% · Memories ${snapshot.memories} · Safety ${snapshot.safetyEvents}"
                    }
                }) { Text("Refresh analytics") }
                Text(analytics, style = MaterialTheme.typography.bodySmall)
            }
        }

        Text(status, color = MaterialTheme.colorScheme.primary)
    }
}
