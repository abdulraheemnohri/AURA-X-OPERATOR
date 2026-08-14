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
import com.aurax.operator.core.theme.AuraThemeMode
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
    var biometric by remember { mutableStateOf(prefs.biometricRequired) }
    var indicator by remember { mutableStateOf(prefs.floatingIndicatorEnabled) }
    var incognito by remember { mutableStateOf(prefs.incognitoProtectionEnabled) }
    var haptics by remember { mutableStateOf(prefs.hapticFeedbackEnabled) }
    var voiceInterrupt by remember { mutableStateOf(prefs.voiceAutoInterruptEnabled) }
    var countdown by remember { mutableIntStateOf(prefs.confirmationSeconds) }
    var maxActions by remember { mutableIntStateOf(prefs.maxActionsPerTask) }
    var maxTaskSeconds by remember { mutableIntStateOf(prefs.maxTaskSeconds) }
    var temperature by remember { mutableFloatStateOf(prefs.modelTemperature) }
    var maxTokens by remember { mutableIntStateOf(prefs.modelMaxTokens) }
    var contextTokens by remember { mutableIntStateOf(prefs.modelContextTokens) }
    var sttLanguage by remember { mutableStateOf(prefs.sttLanguage) }
    var themeMode by remember { mutableStateOf(AuraThemeMode.fromStored(prefs.themeMode)) }
    var modelInstalled by remember { mutableStateOf(models.isInstalled()) }
    var message by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    var dirty by remember { mutableStateOf(false) }

    fun markDirty() { dirty = true }

    fun saveSettings() {
        prefs.policy = policy
        prefs.biometricRequired = biometric
        prefs.floatingIndicatorEnabled = indicator
        prefs.incognitoProtectionEnabled = incognito
        prefs.hapticFeedbackEnabled = haptics
        prefs.voiceAutoInterruptEnabled = voiceInterrupt
        prefs.confirmationSeconds = countdown
        prefs.maxActionsPerTask = maxActions
        prefs.maxTaskSeconds = maxTaskSeconds
        prefs.modelTemperature = temperature
        prefs.modelMaxTokens = maxTokens
        prefs.modelContextTokens = contextTokens
        prefs.sttLanguage = sttLanguage
        prefs.themeMode = themeMode.name
        dirty = false
        message = "Settings saved locally."
    }

    fun reloadSettings() {
        policy = prefs.policy
        biometric = prefs.biometricRequired
        indicator = prefs.floatingIndicatorEnabled
        incognito = prefs.incognitoProtectionEnabled
        haptics = prefs.hapticFeedbackEnabled
        voiceInterrupt = prefs.voiceAutoInterruptEnabled
        countdown = prefs.confirmationSeconds
        maxActions = prefs.maxActionsPerTask
        maxTaskSeconds = prefs.maxTaskSeconds
        temperature = prefs.modelTemperature
        maxTokens = prefs.modelMaxTokens
        contextTokens = prefs.modelContextTokens
        sttLanguage = prefs.sttLanguage
        themeMode = AuraThemeMode.fromStored(prefs.themeMode)
        dirty = false
    }

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
            Text("AURA-X Control Plane", style = MaterialTheme.typography.headlineMedium)
            Text("Configure operator autonomy, safety, local models, voice and diagnostics. Settings can be reviewed and explicitly saved locally.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            SettingsSection("Settings changes", if (dirty) "Unsaved changes are pending" else "All staged values are saved") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { saveSettings() }, enabled = dirty, modifier = Modifier.weight(1f)) { Text("Save Settings") }
                    OutlinedButton(onClick = { reloadSettings(); message = "Unsaved changes discarded." }, enabled = dirty, modifier = Modifier.weight(1f)) { Text("Discard") }
                }
                if (!dirty) Text("Saved locally and ready for the next operator session.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingsSection("Operator readiness", "Required capabilities before automation can safely act") {
                StatusRow("Accessibility service", accessibility)
                StatusRow("Floating overlay", overlay)
                StatusRow("Notifications", notifications)
                StatusRow("Microphone", microphone)
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
            SettingsSection("Automation policy", "The safety engine always overrides the selected autonomy level") {
                listOf("OBSERVE_ONLY", "SUGGEST_ONLY", "CONFIRM_ACTIONS", "FULL_AUTO_LOW_RISK").forEach { value ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = policy == value, onClick = { policy = value; markDirty() })
                        Column(Modifier.weight(1f)) {
                            Text(value.replace('_', ' '), style = MaterialTheme.typography.titleSmall)
                            Text(policyDescription(value), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
        item {
            SettingsSection("Operator safety controls", "Local guardrails and recovery defaults") {
                PreferenceSwitch("Require biometric unlock", "Protect operator sessions before deep automation", biometric) { biometric = it; markDirty() }
                PreferenceSwitch("Always show floating indicator", "Keep operator activity visibly disclosed", indicator) { indicator = it; markDirty() }
                PreferenceSwitch("Block incognito/private browsing", "Do not inspect or automate private browser sessions", incognito) { incognito = it; markDirty() }
                PreferenceSwitch("Haptic feedback", "Confirm important operator state changes", haptics) { haptics = it; markDirty() }
                Text("Confirmation countdown: ${countdown}s", style = MaterialTheme.typography.titleSmall)
                Slider(value = countdown.toFloat(), onValueChange = { countdown = it.toInt().coerceIn(1, 10); markDirty() }, valueRange = 1f..10f, steps = 8)
            }
        }
        item {
            SettingsSection("Voice controls", "Configure local voice interaction behavior") {
                PreferenceSwitch("Auto-interrupt while speaking", "Stop speech output when the user starts speaking", voiceInterrupt) { voiceInterrupt = it; markDirty() }
                CenterButton("Voice Center", "Voice model, microphone and local speech status", Icons.Default.Mic) { center = SettingsCenter.VOICE }
            }
        }
        item {
            SettingsSection("Safety controls", "Recommended defaults are enabled") {
                SettingSwitch("Biometric operator unlock", "Require local biometric authentication before protected operator sessions.", biometric) { biometric = it; markDirty() }
                SettingSwitch("Floating operator indicator", "Show the always-visible green/orange/red automation state.", indicator) { indicator = it; markDirty() }
                SettingSwitch("Incognito protection", "Refuse automation and audit capture for detected private browsing sessions.", incognito) { incognito = it; markDirty() }
                SettingSwitch("Haptic feedback", "Provide local haptic confirmation for operator state changes.", haptics) { haptics = it; markDirty() }
                SettingSwitch("Voice auto-interrupt", "Stop speech output when the user starts speaking.", voiceInterrupt) { voiceInterrupt = it; markDirty() }
                Text("Confirmation countdown: ${countdown}s", style = MaterialTheme.typography.titleSmall)
                Slider(value = countdown.toFloat(), onValueChange = { countdown = it.toInt().coerceIn(1, 10); markDirty() }, valueRange = 1f..10f, steps = 8)
                Text("Maximum actions per task: $maxActions", style = MaterialTheme.typography.titleSmall)
                Slider(value = maxActions.toFloat(), onValueChange = { maxActions = (it.toInt() / 5 * 5).coerceIn(5, 200); markDirty() }, valueRange = 5f..200f, steps = 39)
                Text("Maximum task runtime: ${maxTaskSeconds}s", style = MaterialTheme.typography.titleSmall)
                Slider(value = maxTaskSeconds.toFloat(), onValueChange = { maxTaskSeconds = (it.toInt() / 5 * 5).coerceIn(5, 900); markDirty() }, valueRange = 5f..900f, steps = 179)
                Text("Medium/high-risk actions must remain visible and abortable. Runtime limits stop runaway plans even if a tool misbehaves.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingsSection("Appearance", "Choose how the AURA-X interface is rendered") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { themeMode = mode; markDirty() },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                        )
                    }
                }
                Text("System follows Android appearance. Dark and Light force a fixed theme.", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            SettingsSection("Local AI model", "Tune llama.cpp inference without a cloud endpoint") {
                Text(if (modelInstalled) "Primary GGUF: installed" else "Primary GGUF: not installed", style = MaterialTheme.typography.titleSmall)
                Text("Recommended: Qwen 2.5 0.5B Instruct GGUF Q4_K_M", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { picker.launch(arrayOf("application/octet-stream", "application/x-gguf", "*/*")) }, modifier = Modifier.fillMaxWidth()) { Text("Import / replace GGUF") }
                if (modelInstalled) {
                    OutlinedButton(onClick = {
                        models.deletePrimaryModel()
                        modelInstalled = false
                        prefs.clearModelSelection()
                        message = "Local GGUF model removed."
                    }, modifier = Modifier.fillMaxWidth()) { Text("Remove local GGUF") }
                }
                OutlinedButton(onClick = { center = SettingsCenter.MODELS }, modifier = Modifier.fillMaxWidth()) { Text("Open Model Center") }
                Text("Temperature: ${"%.2f".format(temperature)}", style = MaterialTheme.typography.titleSmall)
                Slider(value = temperature, onValueChange = { temperature = it; markDirty() }, valueRange = 0f..1.5f, steps = 14)
                Text("Max output tokens: $maxTokens", style = MaterialTheme.typography.titleSmall)
                Slider(value = maxTokens.toFloat(), onValueChange = { maxTokens = (it.toInt() / 32 * 32).coerceIn(32, 2048); markDirty() }, valueRange = 32f..2048f, steps = 62)
                Text("Context tokens: $contextTokens", style = MaterialTheme.typography.titleSmall)
                Slider(value = contextTokens.toFloat(), onValueChange = { contextTokens = (it.toInt() / 256 * 256).coerceIn(256, 4096); markDirty() }, valueRange = 256f..4096f, steps = 14)
            }
        }
        item {
            SettingsSection("Voice models", "Keep speech assets local and explicitly selected") {
                Text("STT language", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("auto", "en", "ur", "hi").forEach { lang ->
                        FilterChip(selected = sttLanguage == lang, onClick = { sttLanguage = lang; markDirty() }, label = { Text(lang.uppercase()) })
                    }
                }
                Text(if (prefs.sttModelPath.isBlank()) "Whisper model: not selected" else "Whisper model: configured", style = MaterialTheme.typography.bodySmall)
                Text(if (prefs.ttsModelPath.isBlank()) "Piper voice: not selected" else "Piper voice: configured", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { center = SettingsCenter.VOICE }, modifier = Modifier.fillMaxWidth()) { Text("Open Voice Center") }
            }
        }
        item {
            SettingsSection("Control centers", "Deep configuration and diagnostics") {
                CenterButton("Safety Center", "Guardrails, blocked actions and emergency stop", Icons.Default.Security) { center = SettingsCenter.SAFETY }
                CenterButton("Privacy Center", "Local storage, audit trail and data controls", Icons.Default.Lock) { center = SettingsCenter.PRIVACY }
                CenterButton("Voice Center", "Speech model status and local voice configuration", Icons.Default.Mic) { center = SettingsCenter.VOICE }
                CenterButton("Diagnostics", "Permissions, services, model and runtime checks", Icons.Default.Build) { center = SettingsCenter.DIAGNOSTICS }
            }
        }
        item {
            SettingsSection("Capability matrix", "Truthful runtime status rather than placeholder feature claims") {
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
            SettingsSection("Privacy & recovery", "Auditable local operation") {
                Text("Typed values are excluded from operator audit records. Safety events and task metadata remain on-device.", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { exportLauncher.launch("aura-x-safety-${System.currentTimeMillis()}.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Export Safety Logs") }
                Button(onClick = {
                    val activity = context as? FragmentActivity ?: return@Button
                    val executor = ContextCompat.getMainExecutor(activity)
                    val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { message = "Operator session unlocked." }
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) { message = "Operator unlock cancelled." }
                    })
                    prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock AURA-X Operator").setSubtitle("Confirm before enabling deep device automation").setNegativeButtonText("Cancel").build())
                }, modifier = Modifier.fillMaxWidth()) { Text("Test Biometric Unlock") }
            }
        }
        if (message.isNotBlank()) item { Text(message, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(subtitle, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSection(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            content()
        }
    }
}

@Composable
private fun PreferenceSwitch(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(subtitle, style = MaterialTheme.typography.bodySmall) }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CenterButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(12.dp)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StatusRow(label: String, ready: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        AssistChip(onClick = {}, label = { Text(if (ready) "READY" else "REQUIRED") })
    }
}

private fun policyDescription(value: String): String = when (value) {
    "OBSERVE_ONLY" -> "Inspect and explain. Never perform automation."
    "SUGGEST_ONLY" -> "Prepare actions for review without executing them."
    "CONFIRM_ACTIONS" -> "Recommended default; policy-controlled actions use a visible countdown."
    else -> "Only low-risk actions may execute automatically; safety guardrails still win."
}
