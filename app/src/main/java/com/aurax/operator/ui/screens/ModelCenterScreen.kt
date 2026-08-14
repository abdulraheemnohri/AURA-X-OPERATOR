package com.aurax.operator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aurax.operator.ai.model.HuggingFaceFile
import com.aurax.operator.ai.model.HuggingFaceModel
import com.aurax.operator.data.entities.ModelEntity
import com.aurax.operator.ui.viewmodel.HuggingFaceHubViewModel
import java.util.Locale
import kotlin.math.max

@Composable
fun ModelCenterScreen(viewModel: HuggingFaceHubViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsState()
    val models by viewModel.models.collectAsState()
    val files by viewModel.files.collectAsState()
    val selectedRepo by viewModel.selectedRepo.collectAsState()
    val busy by viewModel.busy.collectAsState()
    val message by viewModel.message.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val localModels by viewModel.localModels.collectAsState()
    var fileFilter by remember { mutableStateOf("GGUF") }

    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Text("Model Hub", style = MaterialTheme.typography.headlineSmall)
            Text("Browse public Hugging Face repositories, inspect files, queue resumable downloads and manage local models.", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hugging Face search", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(value = query, onValueChange = viewModel::setQuery, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("Model or repository") }, leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(onClick = viewModel::search, enabled = !busy, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Search, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Search") }
                        OutlinedButton(onClick = viewModel::search, enabled = !busy) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Wi-Fi only downloads")
                        Switch(checked = wifiOnly, onCheckedChange = viewModel::setWifiOnly)
                    }
                    Text("Public Hub browsing does not require a token. Private/gated repositories are intentionally not treated as public downloads.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (selectedRepo == null) {
            item { Text("Search results (${models.size})", style = MaterialTheme.typography.titleLarge) }
            items(models, key = { it.id }) { model -> HubModelCard(model, onOpen = { viewModel.openRepository(model) }) }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) { Text(selectedRepo ?: "Repository", style = MaterialTheme.typography.titleLarge); Text("Repository files", style = MaterialTheme.typography.bodySmall) }
                    TextButton(onClick = viewModel::closeRepository) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null); Text("Back") }
                }
            }
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("GGUF", "SAFETENSORS", "ALL").forEachIndexed { index, filter ->
                        SegmentedButton(selected = fileFilter == filter, onClick = { fileFilter = filter }, shape = SegmentedButtonDefaults.itemShape(index, 3)) { Text(filter) }
                    }
                }
            }
            val visibleFiles = files.filter { file -> when (fileFilter) { "GGUF" -> file.path.endsWith(".gguf", true); "SAFETENSORS" -> file.path.endsWith(".safetensors", true); else -> true } }
            if (visibleFiles.isEmpty()) item { Text("No files match this filter.") }
            items(visibleFiles, key = { it.path }) { file ->
                HubFileCard(file, wifiOnly) {
                    models.firstOrNull { it.id == selectedRepo }?.let { viewModel.download(it, file) }
                }
            }
        }
        item { Text("Local models (${localModels.size})", style = MaterialTheme.typography.titleLarge) }
        if (localModels.isEmpty()) item { Text("No models are registered locally yet. Search the Hub or import a compatible model from storage.") }
        else items(localModels, key = { it.id }) { model ->
            LocalModelCard(
                model = model,
                onDownload = { viewModel.downloadLocal(model.id) },
                onLoad = { viewModel.load(model.id) },
                onUnload = { viewModel.unload(model.id) },
                onCancel = { viewModel.cancel(model.id) },
                onDelete = { viewModel.remove(model.id) }
            )
        }
        item {
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Model lifecycle", style = MaterialTheme.typography.titleMedium)
                    Text("AVAILABLE → DOWNLOADING → READY → LOADED. Failed downloads enter ERROR. Downloads resume from .part files when the server supports HTTP Range.", style = MaterialTheme.typography.bodySmall)
                    Text("GGUF downloads are checked for GGUF signature, expected size when available, and SHA-256 when Hugging Face exposes an LFS hash.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        if (message.isNotBlank()) item { Text(message, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun HubModelCard(model: HuggingFaceModel, onOpen: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(model.id, style = MaterialTheme.typography.titleMedium)
            Text(model.pipelineTag.ifBlank { "General model" }, style = MaterialTheme.typography.labelMedium)
            Text("${model.downloads} downloads · ${model.likes} likes", style = MaterialTheme.typography.bodySmall)
            if (model.tags.isNotEmpty()) Text(model.tags.take(8).joinToString(" · "), style = MaterialTheme.typography.bodySmall)
            Button(onClick = onOpen, modifier = Modifier.fillMaxWidth()) { Text("Browse repository") }
        }
    }
}

@Composable
private fun HubFileCard(file: HuggingFaceFile, wifiOnly: Boolean, onDownload: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(file.path, style = MaterialTheme.typography.titleSmall)
            Text(formatBytes(file.sizeBytes), style = MaterialTheme.typography.bodySmall)
            if (file.sha256.isNotBlank()) Text("SHA-256: ${file.sha256}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = onDownload, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.CloudDownload, contentDescription = null); Spacer(Modifier.width(8.dp)); Text(if (wifiOnly) "Queue Wi-Fi download" else "Download") }
        }
    }
}

@Composable
private fun LocalModelCard(model: ModelEntity, onDownload: () -> Unit, onLoad: () -> Unit, onUnload: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(model.displayName, style = MaterialTheme.typography.titleMedium)
            val progress = if (model.sizeBytes > 0L) ((model.downloadedBytes.toDouble() / model.sizeBytes.toDouble()) * 100.0).coerceIn(0.0, 100.0) else null
            Text("${model.status} · ${model.format} · ${formatBytes(model.sizeBytes)}${model.quantization.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""}${if (model.isBuiltIn) " · built-in" else ""}", style = MaterialTheme.typography.bodySmall)
            if (progress != null && model.status == "DOWNLOADING") {
                LinearProgressIndicator(progress = { (progress / 100.0).toFloat() }, modifier = Modifier.fillMaxWidth())
                Text("${progress.toInt()}% · ${formatBytes(model.downloadedBytes)} / ${formatBytes(model.sizeBytes)}", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                when {
                    model.isLoaded -> OutlinedButton(onClick = onUnload, modifier = Modifier.weight(1f)) { Text("Unload") }
                    model.status == "READY" -> Button(onClick = onLoad, modifier = Modifier.weight(1f)) { Text("Load") }
                    model.status == "DOWNLOADING" -> OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Stop, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Cancel") }
                    model.sourceUrl.isNotBlank() && !model.isLoaded -> Button(onClick = onDownload, modifier = Modifier.weight(1f)) { Icon(Icons.Default.CloudDownload, contentDescription = null); Spacer(Modifier.width(6.dp)); Text(if (model.status == "ERROR") "Retry" else "Download") }
                    else -> Spacer(Modifier.weight(1f))
                }
                if (!model.isBuiltIn && !model.isLoaded) IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete model") }
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "size unknown"
    val units = listOf("B", "KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) { value /= 1024.0; index++ }
    return String.format(Locale.US, "%.1f %s", max(0.0, value), units[index])
}
