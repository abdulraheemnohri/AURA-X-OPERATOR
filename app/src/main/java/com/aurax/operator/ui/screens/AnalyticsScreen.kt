package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aurax.operator.analytics.AnalyticsEngine
import com.aurax.operator.analytics.AnalyticsSnapshot
import com.aurax.operator.data.database.AuraDatabase
import java.util.Locale

@Composable
fun AnalyticsScreen() {
    val context = LocalContext.current
    var snapshot by remember { mutableStateOf<AnalyticsSnapshot?>(null) }

    LaunchedEffect(Unit) {
        snapshot = AnalyticsEngine(AuraDatabase.get(context).dao()).snapshot()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Analytics", style = MaterialTheme.typography.headlineSmall) }
        val data = snapshot
        if (data == null) {
            item { CircularProgressIndicator() }
        } else {
            item { AnalyticsCard("Tasks", data.tasks.toString()) }
            item { AnalyticsCard("Completed", data.completed.toString()) }
            item { AnalyticsCard("Failed / aborted", data.failed.toString()) }
            item { AnalyticsCard("Success rate", String.format(Locale.US, "%.1f%%", data.successRate * 100f)) }
            item { AnalyticsCard("Memories", data.memories.toString()) }
            item { AnalyticsCard("Safety events", data.safetyEvents.toString()) }
        }
    }
}

@Composable
private fun AnalyticsCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
