@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.aurax.operator.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aurax.operator.core.security.SafetyController
import com.aurax.operator.core.theme.AuraColors
import com.aurax.operator.ui.components.OperatorIndicator
import com.aurax.operator.ui.screens.*

private data class Destination(val label: String, val icon: ImageVector)

@Composable
fun AuraNavigation() {
    var selected by remember { mutableIntStateOf(0) }
    val destinations = listOf(
        Destination("Home", Icons.Default.Home),
        Destination("Chat", Icons.AutoMirrored.Filled.Chat),
        Destination("Operator", Icons.Default.Security),
        Destination("Tasks", Icons.Default.Task),
        Destination("Models", Icons.Default.Memory),
        Destination("Nexus", Icons.Default.Hub),
        Destination("Settings", Icons.Default.Settings)
    )

    Scaffold(
        containerColor = AuraColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AURA-X", fontWeight = FontWeight.Bold)
                        Text("Operator cockpit", style = MaterialTheme.typography.labelSmall, color = AuraColors.TextSecondary)
                    }
                },
                actions = {
                    OperatorIndicator(onAbort = { SafetyController.requestAbort("Floating indicator stop") })
                    Spacer(Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AuraColors.Background)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AuraColors.Surface, tonalElevation = 8.dp) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) },
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selected) {
                0 -> HomeScreen { selected = 1 }
                1 -> ChatScreen()
                2 -> OperatorScreen()
                3 -> TaskScreen()
                4 -> ModelHubScreen()
                5 -> V3ControlCenterScreen()
                else -> SettingsScreen()
            }
        }
    }
}

@Composable
fun UtilityNavigation(onBack: () -> Unit, screen: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AURA-X") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) { screen() }
    }
}