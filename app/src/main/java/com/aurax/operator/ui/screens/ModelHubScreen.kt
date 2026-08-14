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
import androidx.compose.material3.LinearProgressIndicator
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
import com.aurax.operator.ui.components.GlassCard
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
            Text("Hugging Face models, downloads, lifecycle and integrity checks.", style = MaterialTheme.typography.bodyMedium)
        }
        items(models, key = { it.id }) { model ->
            ModelCard(
                model = model,
                onDownload = { viewModel.download(model) },
                onCancel = { viewModel.cancelDownload(model) },
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
    onCancel: () -> Unit,
    onLoad: () -> Unit,
    onUnload: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(model.displayName, style = MaterialTheme.typography.titleLarge)
            Text("${model.category} • ${model.format} ${model.quantization}".trim())
            Text("${model.parameters} • ${model.status}${if (model.isLoaded) " • LOADED" else ""}")
            if (model.description.isNotBlank()) Text(model.description)

            if (model.status == "DOWNLOADING") {
                val total = model.sizeBytes
                val progress = if (total > 0L) {
                    (model.downloadedBytes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else null
                if (progress != null) LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text("Downloaded ${formatBytes(model.downloadedBytes)}${if (total > 0L) " / ${formatBytes(total)}" else ""}")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when {
                    model.status == "AVAILABLE" -> Button(onClick = onDownload) { Text("Download") }
                    model.status == "DOWNLOADING" -> OutlinedButton(onClick = onCancel) { Text("Cancel") }
                    model.status == "ERROR" -> Button(onClick = onDownload) { Text("Retry Download") }
                    model.status == "READY" && !model.isLoaded -> Button(onClick = onLoad) { Text("Load") }
                    model.isLoaded -> OutlinedButton(onClick = onUnload) { Text("Unload") }
                }
                if (model.isUserImported && !model.isLoaded && model.status != "DOWNLOADING") {
                    OutlinedButton(onClick = onDelete) { Text("Delete") }
                }
            }
        }
    }
}

private fun formatBytes(value: Long): String = when {
    value >= 1024L * 1024L * 1024L -> "%.2f GB".format(value / (1024.0 * 1024.0 * 1024.0))
    value >= 1024L * 1024L -> "%.1f MB".format(value / (1024.0 * 1024.0))
    value >= 1024L -> "%.1f KB".format(value / 1024.0)
    else -> "$value B"
}
