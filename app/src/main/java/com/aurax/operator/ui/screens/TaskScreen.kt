package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.aurax.operator.app.AuraApplication
import com.aurax.operator.data.entities.TaskEntity

@Composable
fun TaskScreen() {
    val context = LocalContext.current
    val db = (context.applicationContext as AuraApplication).db
    val tasks by db.dao().observeTasks().collectAsState(initial = emptyList())
    val running = tasks.count { it.status.equals("RUNNING", true) }
    val completed = tasks.count { it.status.equals("COMPLETED", true) }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Task history", style = MaterialTheme.typography.headlineMedium)
        Text("Live execution state, results and audit-friendly logs.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TaskMetric(Modifier.weight(1f), "TOTAL", tasks.size.toString())
            TaskMetric(Modifier.weight(1f), "RUNNING", running.toString())
            TaskMetric(Modifier.weight(1f), "DONE", completed.toString())
        }
        Spacer(Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(tasks, key = { it.id }) { task -> TaskCard(task) }
            }
        }
    }
}

@Composable
private fun TaskCard(task: TaskEntity) {
    val normalized = task.status.uppercase()
    val icon = when {
        normalized.contains("COMPLETE") || normalized == "SUCCESS" -> Icons.Default.CheckCircle
        normalized.contains("FAIL") || normalized.contains("ERROR") -> Icons.Default.ErrorOutline
        normalized.contains("RUN") || normalized.contains("PENDING") -> Icons.Default.HourglassTop
        else -> Icons.Default.HourglassTop
    }
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(task.input, style = MaterialTheme.typography.titleMedium)
                AssistChip(onClick = {}, label = { Text(task.status) })
                if (task.log.isNotBlank()) Text(task.log, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TaskMetric(modifier: Modifier, label: String, value: String) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun EmptyState() {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No tasks yet", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text("Start a task from Chat. Execution will appear here automatically.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
