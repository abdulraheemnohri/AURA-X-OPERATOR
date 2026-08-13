package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aurax.operator.data.entities.ModelEntity
import com.aurax.operator.ui.viewmodel.ModelHubViewModel

@Composable
fun ModelHubScreen(viewModel: ModelHubViewModel = hiltViewModel()) {
    val models by viewModel.models.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Model Hub", style = MaterialTheme.typography.headlineMedium)
            Text("Local model registry, lifecycle and integrity-aware downloads.", style = MaterialTheme.typography.bodyMedium)
        }
        items(models, key = { it.id }) { model ->
            ModelCard(
                model = model,
                onDownload = { viewModel.download(model) },
                onLoad = { viewModel.load(model) },
                onUnload = { viewModel.unload(model) },
                onDelete = { viewModel.delete(model) }
            )
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelEntity,
    onDownload: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(model.displayName, style = MaterialTheme.typography.titleLarge)
            Text("${model.category} • ${model.format} ${model.quantization}".trim())
            Text("${model.parameters} • ${model.status}${if (model.isLoaded) " • LOADED" else ""}")
            if (model.description.isNotBlank()) Text(model.description)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    model.status == "AVAILABLE" -> Button(onClick = onDownload) { Text("Download") }
                    model.status == "READY" && !model.isLoaded -> Button(onClick = onLoad) { Text("Load") }
                    model.isLoaded -> OutlinedButton(onClick = onUnload) { Text("Unload") }
                }
                if (model.isUserImported) OutlinedButton(onClick = onDelete) { Text("Delete") }
            }
        }
    }
}
