package com.aurax.operator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.aurax.operator.ai.model.ModelDownloadWorker
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ModelHubViewModel @Inject constructor(
    private val hub: ModelHub,
    private val workManager: WorkManager
) : ViewModel() {
    val models: StateFlow<List<ModelEntity>> = hub.models.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val availableStorageBytes: StateFlow<Long> = models.map { hub.availableStorageBytes() }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        hub.availableStorageBytes()
    )

    fun download(model: ModelEntity, wifiOnly: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putString(ModelDownloadWorker.KEY_MODEL_ID, model.id)
                    .putBoolean(ModelDownloadWorker.KEY_WIFI_ONLY, wifiOnly)
                    .build()
            )
            .build()

        workManager.enqueueUniqueWork(
            "model-download:${model.id}",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancelDownload(model: ModelEntity) {
        workManager.cancelUniqueWork("model-download:${model.id}")
    }

    fun load(model: ModelEntity) {
        viewModelScope.launch { hub.markLoaded(model.id) }
    }

    fun unload(model: ModelEntity) {
        viewModelScope.launch { hub.markUnloaded(model.id) }
    }

    fun delete(model: ModelEntity) {
        viewModelScope.launch { hub.remove(model.id) }
    }
}
