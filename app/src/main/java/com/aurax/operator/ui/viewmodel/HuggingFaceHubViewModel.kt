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

    fun setQuery(value: String) { _query.value = value }

    fun search() {
        viewModelScope.launch {
            _busy.value = true
            _message.value = ""
            runCatching { client.searchModels(_query.value) }
                .onSuccess { _models.value = it }
                .onFailure { _message.value = it.message ?: "Hugging Face search failed" }
            _busy.value = false
        }
    }

    fun openRepository(model: HuggingFaceModel) {
        viewModelScope.launch {
            _busy.value = true
            _selectedRepo.value = model.id
            runCatching { client.listFiles(model.id) }
                .onSuccess { _files.value = it }
                .onFailure { _message.value = it.message ?: "Unable to list repository files" }
            _busy.value = false
        }
    }

    fun download(repo: HuggingFaceModel, file: HuggingFaceFile, wifiOnly: Boolean) {
        viewModelScope.launch {
            runCatching {
                val entity: ModelEntity = hub.registerHubFile(repo, file)
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
                    .build()
                val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
                    .setConstraints(constraints)
                    .setInputData(Data.Builder().putString(ModelDownloadWorker.KEY_MODEL_ID, entity.id).putBoolean(ModelDownloadWorker.KEY_WIFI_ONLY, wifiOnly).build())
                    .build()
                workManager.enqueueUniqueWork("model-download:${entity.id}", ExistingWorkPolicy.REPLACE, request)
                _message.value = "Download queued: ${file.path}"
            }.onFailure { _message.value = it.message ?: "Unable to queue download" }
        }
    }

    fun cancel(modelId: String) { workManager.cancelUniqueWork("model-download:$modelId") }
}
