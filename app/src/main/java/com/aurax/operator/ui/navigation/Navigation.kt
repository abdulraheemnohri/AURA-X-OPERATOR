package com.aurax.operator.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Settings
import com.aurax.operator.ui.components.OperatorIndicator
import com.aurax.operator.ui.screens.ChatScreen
import com.aurax.operator.ui.screens.HomeScreen
import com.aurax.operator.ui.screens.OperatorScreen
import com.aurax.operator.ui.screens.SettingsScreen
import com.aurax.operator.ui.screens.TaskScreen

private data class Destination(val label: String, val icon: ImageVector)

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
            androidx.compose.material3.TopAppBar(
                title = { Text("AURA-X", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                actions = { OperatorIndicator(onAbort = {}) }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = com.aurax.operator.core.theme.AuraColors.Surface) {
                destinations.forEachIndexed { index, destination ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(destination.icon, destination.label) },
                        label = { Text(destination.label) }
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
                else -> SettingsScreen()
            }
        }
    }
}
