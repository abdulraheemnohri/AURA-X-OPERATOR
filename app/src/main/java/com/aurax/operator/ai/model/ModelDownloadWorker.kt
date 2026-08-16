package com.aurax.operator.ai.model

import android.content.Context
import android.os.BatteryManager
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

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
        val autoRetry = inputData.getBoolean(KEY_AUTO_RETRY, true)
        val maxRetries = inputData.getInt(KEY_MAX_RETRIES, 3).coerceIn(0, 10)
        val model = hub.get(modelId) ?: return Result.failure()
        if (isStopped) return Result.failure()

        val settings = ModelDownloadSettings(applicationContext)
        val battery = applicationContext.getSystemService(BatteryManager::class.java)
        val percent = battery?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        if (percent >= 0 && percent < settings.pauseBelowBatteryPercent && !isCharging()) {
            return Result.retry()
        }

        return try {
            ModelDownloadConcurrencyGate.withPermit(settings.maximumParallelDownloads) {
                downloader.download(model, wifiOnly)
            }.fold(
                onSuccess = { Result.success() },
                onFailure = { error ->
                    when {
                        isStopped -> Result.failure()
                        !autoRetry -> Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Download failed")))
                        runAttemptCount >= maxRetries -> Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Download failed after retries")))
                        isTransient(error) -> Result.retry()
                        else -> Result.failure(workDataOf(KEY_ERROR to (error.message ?: "Download failed")))
                    }
                }
            )
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            Result.failure(workDataOf(KEY_ERROR to "Download cancelled"))
        }
    }

    private fun isCharging(): Boolean {
        val battery = applicationContext.getSystemService(BatteryManager::class.java) ?: return false
        return battery.isCharging
    }

    private fun isTransient(error: Throwable): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (current is SocketTimeoutException ||
                current is ConnectException ||
                current is UnknownHostException ||
                current is IOException
            ) return true
            current = current.cause
        }
        return false
    }

    companion object {
        const val KEY_MODEL_ID = "model_id"
        const val KEY_WIFI_ONLY = "wifi_only"
        const val KEY_AUTO_RETRY = "auto_retry"
        const val KEY_MAX_RETRIES = "max_retries"
        const val KEY_SPEED_LIMIT_KBPS = "speed_limit_kbps"
        const val KEY_ERROR = "error"
    }
}
