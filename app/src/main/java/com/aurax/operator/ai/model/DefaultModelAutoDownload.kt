package com.aurax.operator.ai.model

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the built-in primary LLM download without blocking app startup.
 * Downloads are unique/idempotent and default to unmetered networks so a
 * first-run model download cannot silently consume a user's mobile data.
 */
@Singleton
class DefaultModelAutoDownload @Inject constructor(
    private val context: Context,
    private val modelHub: ModelHub
) {
    fun isEnabled(): Boolean = prefs().getBoolean(KEY_ENABLED, true)

    fun setEnabled(enabled: Boolean) {
        prefs().edit().putBoolean(KEY_ENABLED, enabled).apply()
        if (enabled) schedule() else cancel()
    }

    fun schedule() {
        if (!isEnabled()) return

        val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setInputData(
                Data.Builder()
                    .putString(ModelDownloadWorker.KEY_MODEL_ID, ModelHub.DEFAULT_MODEL_ID)
                    .putBoolean(ModelDownloadWorker.KEY_WIFI_ONLY, true)
                    .build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.UNMETERED)
                    .build()
            )
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                30,
                TimeUnit.SECONDS
            )
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
        if (!isEnabled()) return
        modelHub.seedBuiltIns()
        val model = modelHub.get(ModelHub.DEFAULT_MODEL_ID) ?: return
        val installed = model.localPath?.let { java.io.File(it).isFile } == true && model.status == "READY"
        if (!installed) schedule()
    }

    private fun prefs() = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        const val UNIQUE_WORK_NAME = "aurax-default-model-download"
        const val PREFS_NAME = "aurax_model_download_settings"
        const val KEY_ENABLED = "auto_download_default_model"
    }
}
