@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aurax.operator.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.aurax.operator.ai.model.ModelRepository
import com.aurax.operator.core.capabilities.CapabilityStatus
import com.aurax.operator.core.capabilities.FeatureCatalog
import com.aurax.operator.core.security.PermissionCenter
import com.aurax.operator.core.security.SecurePrefs
import com.aurax.operator.data.LogExporter
import kotlinx.coroutines.launch

private enum class SettingsCenter(val title: String) {
    ROOT("Settings"), SAFETY("Safety Center"), PRIVACY("Privacy Center"), MODELS("Model Center"), VOICE("Voice Center"), DIAGNOSTICS("Diagnostics")
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs(context) }
    val models = remember { ModelRepository(context) }
    val scope = rememberCoroutineScope()
    var center by remember { mutableStateOf(SettingsCenter.ROOT) }
    var policy by remember { mutableStateOf(prefs.policy) }
    var modelInstalled by remember { mutableStateOf(models.isInstalled()) }
    var message by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }

    if (center != SettingsCenter.ROOT) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text(center.title) },
                navigationIcon = {
                    IconButton(onClick = { center = SettingsCenter.ROOT }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            Box(Modifier.fillMaxSize()) {
                when (center) {
                    SettingsCenter.SAFETY -> SafetyCenterScreen()
                    SettingsCenter.PRIVACY -> PrivacyCenterScreen()
                    SettingsCenter.MODELS -> ModelCenterScreen()
                    SettingsCenter.VOICE -> VoiceCenterScreen()
                    SettingsCenter.DIAGNOSTICS -> DiagnosticsScreen()
                    SettingsCenter.ROOT -> Unit
                }
            }
        }
        return
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh++ }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching {
            models.importPrimaryModel(uri)
            modelInstalled = true
            message = "Local GGUF model installed."
        }.onFailure { message = "Model import failed: ${it.message ?: "unknown error"}" }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch {
            runCatching {
                val csv = LogExporter.csv(context)
                context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                message = "Safety log exported."
            }.onFailure { message = "Export failed: ${it.message ?: "unknown error"}" }
        }
    }
    @Suppress("UNUSED_VARIABLE") val statusRefresh = refresh
    val microphone = PermissionCenter.hasMicrophone(context)
    val notifications = PermissionCenter.hasNotifications(context)
    val overlay = PermissionCenter.hasOverlay(context)
    val accessibility = PermissionCenter.isAccessibilityEnabled(context)

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Control plane", style = MaterialTheme.typography.headlineMedium)
            Text("Tune operator behavior, permissions, local AI and privacy from one place.", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            SettingsSection("Operator readiness", "Everything AURA-X needs before it can safely act") {
                StatusRow("Accessibility service", accessibility)
                StatusRow("Floating indicator", overlay)
                StatusRow("Notifications", notifications)
                StatusRow("Microphone", microphone)
                Spacer(Modifier.height(4.dp))
                Button(onClick = {
                    val requested = buildList {
                        add(Manifest.permission.RECORD_AUDIO)
                        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionLauncher.launch(requested.toTypedArray())
                }, modifier = Modifier.fillMaxWidth()) { Text("Request permissions") }
                OutlinedButton(onClick = { context.startActivity(PermissionCenter.accessibilitySettingsIntent()) }, modifier = Modifier.fillMaxWidth()) { Text("Open Accessibility Settings") }
                OutlinedButton(onClick = { context.startActivity(PermissionCenter.overlaySettingsIntent(context)) }, modifier = Modifier.fillMaxWidth()) { Text("Open Overlay Settings") }
            }
        }

        item {
            SettingsSection("Automation policy", "Choose how much autonomy the operator receives") {
                listOf("OBSERVE_ONLY", "SUGGEST_ONLY", "CONFIRM_ACTIONS", "FULL_AUTO_LOW_RISK").forEach { value ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = policy == value, onClick = { policy = value; prefs.policy = value })
                        Column(Modifier.weight(1f)) {
                            Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall)
                            Text(policyDescription(value), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            SettingsSection("Local AI", "No cloud endpoint is required by the operator core") {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Primary GGUF")
                        Text(if (modelInstalled) "Installed and ready for local inference" else "Not installed", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Recommended family: Qwen 2.5 0.5B Instruct GGUF", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { picker.launch(arrayOf("application/octet-stream", "application/x-gguf", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Import GGUF model") }
                OutlinedButton(onClick = { center = SettingsCenter.MODELS }, modifier = Modifier.fillMaxWidth()) { Text("Open Model Center") }
            }
        }

        item {
            SettingsSection("Control centers", "Deep configuration and diagnostics") {
                CenterButton("Safety Center", "Guardrails, blocked actions and abort state", Icons.Default.Security) { center = SettingsCenter.SAFETY }
                CenterButton("Privacy Center", "Local storage, audit trail and data controls", Icons.Default.Lock) { center = SettingsCenter.PRIVACY }
                CenterButton("Voice Center", "Voice permissions and local speech status", Icons.Default.Mic) { center = SettingsCenter.VOICE }
                CenterButton("Diagnostics", "Build, permissions, service and runtime checks", Icons.Default.Build) { center = SettingsCenter.DIAGNOSTICS }
            }
        }

        item {
            SettingsSection("Capability matrix", "A truthful view of what is ready on this device") {
                FeatureCatalog.all.forEach { feature ->
                    val label = when (feature.status) {
                        CapabilityStatus.READY -> "READY"
                        CapabilityStatus.REQUIRES_PERMISSION -> "PERMISSION"
                        CapabilityStatus.REQUIRES_MODEL -> "MODEL"
                        CapabilityStatus.NOT_BUNDLED -> "OPTIONAL"
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(feature.title, style = MaterialTheme.typography.titleSmall)
                            Text(feature.description, style = MaterialTheme.typography.bodySmall)
                        }
                        AssistChip(onClick = {}, label = { Text(label) })
                    }
                }
            }
        }

        item {
            SettingsSection("Privacy & recovery", "Keep the operator auditable and recoverable") {
                Text("Typed values are intentionally excluded from the operator audit log. Safety events and task metadata stay on-device.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { exportLauncher.launch("aura-x-safety-${System.currentTimeMillis()}.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Export Safety Logs") }
                Button(onClick = {
                    val activity = context as? FragmentActivity ?: return@Button
                    val executor = ContextCompat.getMainExecutor(activity)
                    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { message = "Operator session unlocked." }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { message = "Operator unlock cancelled." }
                    })
                    prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock AURA-X Operator").setSubtitle("Confirm before enabling deep device automation").setNegativeButtonText("Cancel").build())
                }, modifier = Modifier.fillMaxWidth()) { Text("Biometric Operator Unlock") }
            }
        }

        if (message.isNotBlank()) item { Text(message, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun SettingsSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), content = {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            content()
        })
    }
}

@Composable
private fun CenterButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null)
    }
}

@Composable
private fun StatusRow(label: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        AssistChip(onClick = {}, label = { Text(if (ready) "READY" else "NEEDS ACTION") })
    }
}

private fun policyDescription(value: String): String = when (value) {
    "OBSERVE_ONLY" -> "Inspect and explain. Never perform automation."
    "SUGGEST_ONLY" -> "Prepare actions for review without executing them."
    "CONFIRM_ACTIONS" -> "Recommended default; policy-controlled actions use a visible countdown."
    else -> "Only low-risk actions may execute automatically; safety guardrails still win."
}
