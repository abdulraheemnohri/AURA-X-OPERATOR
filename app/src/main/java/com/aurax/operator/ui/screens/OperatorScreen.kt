package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.security.PermissionCenter
import com.aurax.operator.operator.AuraAccessibilityService
import com.aurax.operator.operator.NotificationSnapshotStore
import com.aurax.operator.operator.OperatorRuntime

@Composable
fun OperatorScreen() {
    val context = LocalContext.current
    val service = AuraAccessibilityService.instance
    val state by service?.windowState?.collectAsState() ?: remember { mutableStateOf(null) }
    val indicator by OperatorRuntime.indicator.collectAsState()
    val countdown by OperatorRuntime.countdown.collectAsState()
    val notifications by NotificationSnapshotStore.notifications.collectAsState()
    var notificationAccess by remember { mutableStateOf(PermissionCenter.isNotificationAccessEnabled(context)) }

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Operator", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Status: $indicator")
        countdown?.let { Text("ACTION IN ${it.remainingSeconds}s — tap the floating indicator or press Volume Down to abort") }
        Spacer(Modifier.height(12.dp))

        when (val current = state) {
            is com.aurax.operator.operator.WindowState.Changed -> {
                Text("Package: ${current.packageName}")
                Text("Window: ${current.className}")
                Text("Clickable elements: ${current.context.clickableElements.size}")
                Text("Password field: ${current.context.hasPasswordField}")
                Text("Sensitive text: ${current.context.hasSensitiveText}")
                Text("Private browsing: ${current.context.isPrivateBrowsing}")
                Spacer(Modifier.height(8.dp))
                Text(current.context.allText.take(5000), style = MaterialTheme.typography.bodySmall)
            }
            else -> Text("AccessibilityService is not enabled.")
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))
        Text("Notification reader", style = MaterialTheme.typography.titleLarge)
        Text("Optional, user-granted access. Notification contents stay in memory and protected packages are filtered.", style = MaterialTheme.typography.bodySmall)
        if (!notificationAccess) {
            OutlinedButton(onClick = {
                context.startActivity(PermissionCenter.notificationAccessSettingsIntent())
                notificationAccess = true
            }, modifier = Modifier.fillMaxWidth()) { Text("Grant Notification Access") }
        } else {
            Text("Notification access: enabled (${notifications.size} visible)", style = MaterialTheme.typography.bodyMedium)
            notifications.take(12).forEach { item ->
                ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Column(Modifier.padding(10.dp)) {
                        Text(item.title.ifBlank { item.packageName }, style = MaterialTheme.typography.titleSmall)
                        if (item.text.isNotBlank()) Text(item.text.take(500), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Button(onClick = { OperatorRuntime.abort() }) { Text("ABORT ALL ACTIONS") }
    }
}
