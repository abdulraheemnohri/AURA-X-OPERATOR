package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.app.AuraApplication
import com.aurax.operator.data.entities.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TaskScreen() {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    LaunchedEffect(Unit) {
        tasks = withContext(Dispatchers.IO) { (context.applicationContext as AuraApplication).db.dao().tasks() }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Tasks", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        if (tasks.isEmpty()) {
            Text("No tasks yet.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(task.status, style = MaterialTheme.typography.labelLarge)
                            Text(task.input)
                            if (task.log.isNotBlank()) Text(task.log, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
