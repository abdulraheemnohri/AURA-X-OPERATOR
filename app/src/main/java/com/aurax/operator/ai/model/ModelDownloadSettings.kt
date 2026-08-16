package com.aurax.operator.ai.model

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Typed, local model-download policy.
 *
 * This is intentionally separate from model metadata: the registry describes
 * what a model is, while this store describes when AURA-X is allowed to fetch it.
 */
class ModelDownloadSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision.asStateFlow()

    var automaticDownload: Boolean
        get() = prefs.getBoolean(KEY_AUTO, true)
        set(value) = put { it.putBoolean(KEY_AUTO, value) }

    var unmeteredOnly: Boolean
        get() = prefs.getBoolean(KEY_UNMETERED, true)
        set(value) = put { it.putBoolean(KEY_UNMETERED, value) }

    var chargingOnly: Boolean
        get() = prefs.getBoolean(KEY_CHARGING, false)
        set(value) = put { it.putBoolean(KEY_CHARGING, value) }

    var pauseBelowBatteryPercent: Int
        get() = prefs.getInt(KEY_BATTERY, 20).coerceIn(5, 80)
        set(value) = put { it.putInt(KEY_BATTERY, value.coerceIn(5, 80)) }

    var maximumParallelDownloads: Int
        get() = prefs.getInt(KEY_PARALLEL, 1).coerceIn(1, 2)
        set(value) = put { it.putInt(KEY_PARALLEL, value.coerceIn(1, 2)) }

    var speedLimitKbps: Int
        get() = prefs.getInt(KEY_SPEED, 0).coerceIn(0, 102400)
        set(value) = put { it.putInt(KEY_SPEED, value.coerceIn(0, 102400)) }

    var automaticRetry: Boolean
        get() = prefs.getBoolean(KEY_RETRY, true)
        set(value) = put { it.putBoolean(KEY_RETRY, value) }

    var retryCount: Int
        get() = prefs.getInt(KEY_RETRY_COUNT, 3).coerceIn(0, 10)
        set(value) = put { it.putInt(KEY_RETRY_COUNT, value.coerceIn(0, 10)) }

    private inline fun put(block: (android.content.SharedPreferences.Editor) -> Unit) {
        prefs.edit().also(block).apply()
        _revision.value = System.currentTimeMillis()
    }

    companion object {
        const val PREFS = "aurax_model_download_policy"
        private const val KEY_AUTO = "automatic_download"
        private const val KEY_UNMETERED = "unmetered_only"
        private const val KEY_CHARGING = "charging_only"
        private const val KEY_BATTERY = "pause_below_battery"
        private const val KEY_PARALLEL = "maximum_parallel_downloads"
        private const val KEY_SPEED = "speed_limit_kbps"
        private const val KEY_RETRY = "automatic_retry"
        private const val KEY_RETRY_COUNT = "retry_count"
    }
}
