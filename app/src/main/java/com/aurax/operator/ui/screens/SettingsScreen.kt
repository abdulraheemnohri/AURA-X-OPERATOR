package com.aurax.operator.ui.screens

import android.content.Intent
import android.provider.Settings
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

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching {
                models.importPrimaryModel(uri)
                modelInstalled = true
                message = "Local Qwen GGUF installed."
            }.onFailure { message = "Model import failed: ${it.message}" }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val csv = LogExporter.csv(context)
                    context.contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray()) }
                    message = "Safety log exported."
                }.onFailure { message = "Export failed: ${it.message}" }
            }
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("Automation policy")
        listOf("OBSERVE_ONLY", "SUGGEST_ONLY", "CONFIRM_ACTIONS", "FULL_AUTO_LOW_RISK").forEach { p ->
            Row {
                RadioButton(policy == p, {
                    policy = p
                    prefs.policy = p
                })
                Text(p, Modifier.padding(top = 12.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(if (modelInstalled) "Local Qwen model: installed" else "Local Qwen model: not installed")
        Text("Hugging Face: ${ModelRepository.HF_REPOSITORY}", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(6.dp))
        Button(onClick = { picker.launch(arrayOf("application/octet-stream", "application/x-gguf", "*/*")) }) {
            Text("Import GGUF model")
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
            Text("Open Accessibility Settings")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            })
        }) { Text("Allow Floating Indicator") }

        Spacer(Modifier.height(8.dp))
        Button(onClick = { exportLauncher.launch("aura-x-safety-${System.currentTimeMillis()}.csv") }) {
            Text("Export Safety Logs")
        }

        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            val activity = context as? FragmentActivity ?: return@Button
            val executor = ContextCompat.getMainExecutor(activity)
            val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    message = "Operator mode unlocked for this session."
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    message = "Operator unlock cancelled."
                }
            })
            prompt.authenticate(
                BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Unlock AURA-X Operator")
                    .setSubtitle("Confirm before enabling deep device automation")
                    .setNegativeButtonText("Cancel")
                    .build()
            )
        }) { Text("Biometric Operator Unlock") }

        if (message.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.bodySmall)
        }
    }
}
