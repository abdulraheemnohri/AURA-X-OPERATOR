package com.aurax.operator.ai.model

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the built-in primary LLM download without blocking app startup.
 *
 * Policy is explicit and local: by default downloads require an unmetered
 * network. Charging, battery, retry and automatic-download controls are user
 * configurable through [ModelDownloadSettings].
 */
@Singleton
class DefaultModelAutoDownload @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelHub: ModelHub
) {
    private val settings = ModelDownloadSettings(context)

    fun isEnabled(): Boolean = settings.automaticDownload

    fun setEnabled(enabled: Boolean) {
        settings.automaticDownload = enabled
        if (enabled) schedule() else cancel()
    }

    fun schedule() {
        if (!settings.automaticDownload) return

        val networkType = if (settings.unmeteredOnly) NetworkType.UNMETERED else NetworkType.CONNECTED
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(networkType)
            .setRequiresCharging(settings.chargingOnly)
            .setRequiresBatteryNotLow(settings.pauseBelowBatteryPercent >= 15)
            .build()

        val input = Data.Builder()
            .putString(ModelDownloadWorker.KEY_MODEL_ID, ModelHub.DEFAULT_MODEL_ID)
            .putBoolean(ModelDownloadWorker.KEY_WIFI_ONLY, settings.unmeteredOnly)
            .putBoolean(ModelDownloadWorker.KEY_AUTO_RETRY, settings.automaticRetry)
            .putInt(ModelDownloadWorker.KEY_MAX_RETRIES, settings.retryCount)
            .putInt(ModelDownloadWorker.KEY_SPEED_LIMIT_KBPS, settings.speedLimitKbps)
            .build()

        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(input)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    suspend fun scheduleIfMissingOrInvalid() {
        if (!settings.automaticDownload) return
        modelHub.seedBuiltIns()
        val model = modelHub.get(ModelHub.DEFAULT_MODEL_ID) ?: return
        val installed = model.localPath?.let { java.io.File(it).isFile } == true && model.status == "READY"
        if (!installed) schedule()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "aurax-default-model-download"
    }
}
