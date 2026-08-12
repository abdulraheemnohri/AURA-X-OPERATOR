package com.aurax.operator.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { SecurePrefs(context) }
    val models = remember { ModelRepository(context) }
    val scope = rememberCoroutineScope()
    var policy by remember { mutableStateOf(prefs.policy) }
    var modelInstalled by remember { mutableStateOf(models.isInstalled()) }
    var message by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh++ }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) runCatching { models.importPrimaryModel(uri); modelInstalled = true; message = "Local Qwen GGUF installed." }
            .onFailure { message = "Model import failed: ${it.message}" }
    }
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) scope.launch { runCatching { val csv = LogExporter.csv(context); context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }; message = "Safety log exported." }.onFailure { message = "Export failed: ${it.message}" } }
    }
    @Suppress("UNUSED_VARIABLE") val statusRefresh = refresh
    val microphone = PermissionCenter.hasMicrophone(context)
    val notifications = PermissionCenter.hasNotifications(context)
    val overlay = PermissionCenter.hasOverlay(context)
    val accessibility = PermissionCenter.isAccessibilityEnabled(context)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Text("Device-local control plane", style = MaterialTheme.typography.bodyMedium)

        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Operator readiness", style = MaterialTheme.typography.titleMedium)
            StatusRow("Accessibility service", accessibility)
            StatusRow("Floating indicator", overlay)
            StatusRow("Notifications", notifications)
            StatusRow("Microphone permission", microphone)
            Button(onClick = {
                val requested = buildList { add(Manifest.permission.RECORD_AUDIO); if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS) }
                permissionLauncher.launch(requested.toTypedArray())
            }) { Text("Request permissions") }
            OutlinedButton(onClick = { context.startActivity(PermissionCenter.accessibilitySettingsIntent()) }) { Text("Open Accessibility Settings") }
            OutlinedButton(onClick = { context.startActivity(PermissionCenter.overlaySettingsIntent(context)) }) { Text("Open Overlay Settings") }
        } }

        Card { Column(Modifier.padding(16.dp)) {
            Text("Automation policy", style = MaterialTheme.typography.titleMedium)
            listOf("OBSERVE_ONLY", "SUGGEST_ONLY", "CONFIRM_ACTIONS", "FULL_AUTO_LOW_RISK").forEach { p ->
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    RadioButton(policy == p, { policy = p; prefs.policy = p })
                    Text(p)
                }
            }
        } }

        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Local AI", style = MaterialTheme.typography.titleMedium)
            Text(if (modelInstalled) "Qwen GGUF: installed" else "Qwen GGUF: not installed")
            Text("Hugging Face: ${ModelRepository.HF_REPOSITORY}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { picker.launch(arrayOf("application/octet-stream", "application/x-gguf", "*/*")) }) { Text("Import GGUF model") }
        } }

        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Feature matrix", style = MaterialTheme.typography.titleMedium)
            FeatureCatalog.all.forEach { feature ->
                val label = when (feature.status) {
                    CapabilityStatus.READY -> "READY"
                    CapabilityStatus.REQUIRES_PERMISSION -> "PERMISSION"
                    CapabilityStatus.REQUIRES_MODEL -> "MODEL"
                    CapabilityStatus.NOT_BUNDLED -> "OPTIONAL"
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(feature.title); Text(feature.description, style = MaterialTheme.typography.bodySmall) }
                    Text(label, style = MaterialTheme.typography.labelSmall)
                }
            }
        } }

        Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Privacy & recovery", style = MaterialTheme.typography.titleMedium)
            Text("Operator actions remain local. Typed values are never written to the audit log.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { exportLauncher.launch("aura-x-safety-${System.currentTimeMillis()}.csv") }) { Text("Export Safety Logs") }
            Button(onClick = {
                val activity = context as? FragmentActivity ?: return@Button
                val executor = ContextCompat.getMainExecutor(activity)
                val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { message = "Operator session unlocked." }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { message = "Operator unlock cancelled." }
                })
                prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock AURA-X Operator").setSubtitle("Confirm before enabling deep device automation").setNegativeButtonText("Cancel").build())
            }) { Text("Biometric Operator Unlock") }
        } }
        if (message.isNotBlank()) Text(message, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusRow(label: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label); Text(if (ready) "READY" else "NEEDS ACTION") }
}
