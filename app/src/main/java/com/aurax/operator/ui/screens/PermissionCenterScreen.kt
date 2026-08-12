package com.aurax.operator.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.security.PermissionCenter

@Composable
fun PermissionCenterScreen() {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { refresh++ }
    @Suppress("UNUSED_VARIABLE") val tick = refresh
    val rows = listOf(
        "Accessibility service" to PermissionCenter.isAccessibilityEnabled(context),
        "Overlay" to PermissionCenter.hasOverlay(context),
        "Microphone" to PermissionCenter.hasMicrophone(context),
        "Notifications" to PermissionCenter.hasNotifications(context)
    )
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Permission Center", style = MaterialTheme.typography.headlineSmall)
        Text("AURA-X never silently enables device-control capabilities.")
        rows.forEach { (name, ready) ->
            Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(name); Text(if (ready) "READY" else "ACTION REQUIRED") } }
        }
        Button(onClick = {
            val permissions = buildList {
                add(Manifest.permission.RECORD_AUDIO)
                if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            }
            launcher.launch(permissions.toTypedArray())
        }) { Text("Request available permissions") }
        OutlinedButton(onClick = { context.startActivity(PermissionCenter.accessibilitySettingsIntent()) }) { Text("Accessibility Settings") }
        OutlinedButton(onClick = { context.startActivity(PermissionCenter.overlaySettingsIntent(context)) }) { Text("Overlay Settings") }
    }
}
