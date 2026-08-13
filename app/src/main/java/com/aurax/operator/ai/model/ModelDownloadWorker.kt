package com.aurax.operator.ai.model

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val hub: ModelHub,
    private val downloader: ModelDownloadManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val modelId = inputData.getString(KEY_MODEL_ID) ?: return Result.failure()
        val wifiOnly = inputData.getBoolean(KEY_WIFI_ONLY, false)
        val model = hub.get(modelId) ?: return Result.failure()
        if (isStopped) return Result.failure()

        return downloader.download(model, wifiOnly).fold(
            onSuccess = { Result.success() },
            onFailure = { error ->
                if (isStopped) Result.failure()
                else Result.failure(androidx.work.workDataOf(KEY_ERROR to (error.message ?: "Download failed")))
            }
        )
    }

    companion object {
        const val KEY_MODEL_ID = "model_id"
        const val KEY_WIFI_ONLY = "wifi_only"
        const val KEY_ERROR = "error"
    }
}
