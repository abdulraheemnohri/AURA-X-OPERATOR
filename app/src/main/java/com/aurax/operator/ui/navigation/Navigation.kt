package com.aurax.operator.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import com.aurax.operator.core.security.SafetyController
import com.aurax.operator.core.theme.AuraColors
import com.aurax.operator.ui.components.OperatorIndicator
import com.aurax.operator.ui.screens.*

private data class Destination(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraNavigation() {
    var selected by remember { mutableIntStateOf(0) }
    val destinations = listOf(
        Destination("Home", Icons.Default.Home),
        Destination("Chat", Icons.Default.Chat),
        Destination("Operator", Icons.Default.Security),
        Destination("Tasks", Icons.Default.Task),
        Destination("Settings", Icons.Default.Settings)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AURA-X", fontWeight = FontWeight.Bold) },
                actions = {
                    OperatorIndicator(
                        onAbort = {
                            SafetyController.requestAbort("Floating indicator stop")
                        }
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = AuraColors.Surface) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(destination.icon, contentDescription = destination.label) },
                        label = { Text(destination.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selected) {
                0 -> HomeScreen { selected = 1 }
                1 -> ChatScreen()
                2 -> OperatorScreen()
                3 -> TaskScreen()
                else -> SettingsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityNavigation(onBack: () -> Unit, screen: @Composable () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AURA-X") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            screen()
        }
    }
}
