package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TaskDetailScreen(taskId: Long? = null) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Task Detail", style = MaterialTheme.typography.headlineSmall)
        Text("Task ${taskId ?: "new"}")
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Execution timeline", style = MaterialTheme.typography.titleMedium)
            Text("Planning → safety check → confirmation → execution → verification")
            Text("Every action is locally auditable and can be aborted.", style = MaterialTheme.typography.bodySmall)
        } }
    }
}
