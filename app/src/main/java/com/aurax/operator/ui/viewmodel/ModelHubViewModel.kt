package com.aurax.operator.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurax.operator.ai.model.ModelDownloadManager
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.entities.ModelEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ModelHubViewModel @Inject constructor(
    private val hub: ModelHub,
    private val downloader: ModelDownloadManager
) : ViewModel() {
    val models: StateFlow<List<ModelEntity>> = hub.models.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun download(model: ModelEntity, wifiOnly: Boolean = false) {
        viewModelScope.launch { downloader.download(model, wifiOnly) }
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
