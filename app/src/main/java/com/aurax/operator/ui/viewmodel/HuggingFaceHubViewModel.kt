package com.aurax.operator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aurax.operator.ai.model.HuggingFaceClient
import com.aurax.operator.ai.model.HuggingFaceFile
import com.aurax.operator.ai.model.HuggingFaceModel
import com.aurax.operator.ai.model.ModelDownloadWorker
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

@HiltViewModel
class HuggingFaceHubViewModel @Inject constructor(
    private val client: HuggingFaceClient,
    private val hub: ModelHub,
    private val workManager: WorkManager
) : ViewModel() {
    private val _query = MutableStateFlow("Qwen GGUF")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _models = MutableStateFlow<List<HuggingFaceModel>>(emptyList())
    val models: StateFlow<List<HuggingFaceModel>> = _models.asStateFlow()
    private val _files = MutableStateFlow<List<HuggingFaceFile>>(emptyList())
    val files: StateFlow<List<HuggingFaceFile>> = _files.asStateFlow()
    private val _selectedRepo = MutableStateFlow<String?>(null)
    val selectedRepo: StateFlow<String?> = _selectedRepo.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()
    private val _wifiOnly = MutableStateFlow(false)
    val wifiOnly: StateFlow<Boolean> = _wifiOnly.asStateFlow()
    val localModels: StateFlow<List<ModelEntity>> = hub.models.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    init { search() }

    fun setQuery(value: String) { _query.value = value }
    fun setWifiOnly(value: Boolean) { _wifiOnly.value = value }

    fun search() {
        viewModelScope.launch {
            _busy.value = true
            _message.value = ""
            runCatching { client.searchModels(_query.value.ifBlank { "GGUF" }) }
                .onSuccess { _models.value = it }
                .onFailure { _message.value = it.message ?: "Hugging Face search failed" }
            _busy.value = false
        }
    }

    fun openRepository(model: HuggingFaceModel) {
        viewModelScope.launch {
            _busy.value = true
            _message.value = ""
            _selectedRepo.value = model.id
            runCatching { client.listFiles(model.id) }
                .onSuccess { _files.value = it }
                .onFailure { _message.value = it.message ?: "Unable to list repository files" }
            _busy.value = false
        }
    }

    fun closeRepository() {
        _selectedRepo.value = null
        _files.value = emptyList()
    }

    fun download(repo: HuggingFaceModel, file: HuggingFaceFile) {
        viewModelScope.launch {
            runCatching {
                val entity: ModelEntity = hub.registerHubFile(repo, file)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(if (_wifiOnly.value) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                    .setConstraints(constraints)
                    .addTag("model-download")
                    .setInputData(Data.Builder()
                        .putString(ModelDownloadWorker.KEY_MODEL_ID, entity.id)
                        .putBoolean(ModelDownloadWorker.KEY_WIFI_ONLY, _wifiOnly.value)
                        .build())
                    .build()
                workManager.enqueueUniqueWork("model-download:${entity.id}", ExistingWorkPolicy.REPLACE, request)
                _message.value = "Download queued: ${file.path}"
            }.onFailure { _message.value = it.message ?: "Unable to queue download" }
        }
    }

    fun cancel(modelId: String) {
        workManager.cancelUniqueWork("model-download:$modelId")
        _message.value = "Download cancelled. A partial file may remain and will resume on retry."
    }

    fun load(modelId: String) {
        viewModelScope.launch {
            runCatching { hub.markLoaded(modelId) }
                .onSuccess { _message.value = "Model loaded for local inference." }
                .onFailure { _message.value = it.message ?: "Unable to load model" }
        }
    }

    fun unload(modelId: String) {
        viewModelScope.launch {
            runCatching { hub.markUnloaded(modelId) }
                .onSuccess { _message.value = "Model unloaded." }
                .onFailure { _message.value = it.message ?: "Unable to unload model" }
        }
    }

    fun remove(modelId: String) {
        viewModelScope.launch {
            runCatching { hub.remove(modelId) }
                .onSuccess { _message.value = "Model removed from local registry and storage." }
                .onFailure { _message.value = it.message ?: "Unable to remove model" }
        }
    }
}
