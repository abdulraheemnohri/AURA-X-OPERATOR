package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aurax.operator.operator.AuraAccessibilityService
import com.aurax.operator.operator.OperatorRuntime

@Composable
fun OperatorScreen() {
    val service = AuraAccessibilityService.instance
    val state by service?.windowState?.collectAsState() ?: remember { mutableStateOf(null) }
    val indicator by OperatorRuntime.indicator.collectAsState()
    val countdown by OperatorRuntime.countdown.collectAsState()

    Column(
        Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
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
        Button(onClick = { OperatorRuntime.abort() }) { Text("ABORT ALL ACTIONS") }
    }
}
