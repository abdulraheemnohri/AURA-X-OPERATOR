package com.aurax.operator.ai.runtime

import com.aurax.operator.ai.inference.GenerationRequest
import com.aurax.operator.ai.model.ModelHub
import com.aurax.operator.data.entities.ModelEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Coordinates persisted model selection with the native local runtime. */
@Singleton
class LocalRuntimeManager @Inject constructor(
    private val modelHub: ModelHub,
    private val llama: LlamaCppRuntime
) {
    private val mutex = Mutex()
    private var loadedId: String? = null

    suspend fun load(modelId: String): Result<ModelEntity> = mutex.withLock {
        loadUnlocked(modelId)
    }

    suspend fun loadBestAvailable(): Result<ModelEntity> = mutex.withLock {
        val model = modelHub.bestReadyLlm()
            ?: return Result.failure(IllegalStateException("No ready local GGUF LLM is available"))
        loadUnlocked(model.id)
    }

    private suspend fun loadUnlocked(modelId: String): Result<ModelEntity> {
        val model = modelHub.get(modelId)
            ?: return Result.failure(IllegalArgumentException("Model not found: $modelId"))
        val path = model.localPath?.let(::File)
            ?: return Result.failure(IllegalStateException("Model has no local file: ${model.displayName}"))
        if (!path.isFile || path.length() == 0L) {
            return Result.failure(IllegalStateException("Model file is unavailable: ${path.absolutePath}"))
        }
        if (model.status != "READY") {
            return Result.failure(IllegalStateException("Model is not ready: ${model.displayName}"))
        }
        if (model.format != "GGUF") {
            return Result.failure(UnsupportedOperationException("Native runtime currently supports GGUF models only"))
        }

        if (loadedId != modelId) {
            // Native llama.cpp owns the actual model allocation. Release the previous
            // model before switching the persisted active model to avoid RAM leaks.
            llama.unloadNative()
            loadedId?.let { modelHub.markUnloaded(it) }
            modelHub.markLoaded(modelId)
            loadedId = modelId
        }
        return Result.success(modelHub.get(modelId) ?: model)
    }

    suspend fun unload() = mutex.withLock {
        llama.unloadNative()
        loadedId?.let { modelHub.markUnloaded(it) }
        loadedId = null
    }

    suspend fun generate(request: GenerationRequest): Result<String> = mutex.withLock {
        val id = loadedId ?: return Result.failure(IllegalStateException("No local model is loaded"))
        val model = modelHub.get(id) ?: return Result.failure(IllegalStateException("Loaded model no longer exists"))
        val path = model.localPath?.let(::File)
            ?: return Result.failure(IllegalStateException("Loaded model has no local path"))
        if (!path.isFile || path.length() == 0L) {
            return Result.failure(IllegalStateException("Loaded model file is unavailable"))
        }
        runCatching { llama.generateFrom(path, request) }
    }

    fun loadedModelId(): String? = loadedId
}
