package com.aurax.operator.ai.model

import android.content.Context
import com.aurax.operator.core.settings.SettingsDataStoreBackend
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Typed, local model-download policy.
 *
 * The policy now shares the application's Preferences DataStore backend with
 * the rest of the settings surface. A one-time migration preserves values
 * from the legacy model-download SharedPreferences store.
 */
class ModelDownloadSettings(context: Context) {
    private val appContext = context.applicationContext
    private val store = SettingsDataStoreBackend(appContext)
    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision.asStateFlow()

    init {
        migrateLegacyPreferences()
    }

    var automaticDownload: Boolean
        get() = store.getBoolean(KEY_AUTO, true)
        set(value) = putBoolean(KEY_AUTO, value)

    var unmeteredOnly: Boolean
        get() = store.getBoolean(KEY_UNMETERED, true)
        set(value) = putBoolean(KEY_UNMETERED, value)

    var chargingOnly: Boolean
        get() = store.getBoolean(KEY_CHARGING, false)
        set(value) = putBoolean(KEY_CHARGING, value)

    var pauseBelowBatteryPercent: Int
        get() = store.getInt(KEY_BATTERY, 20).coerceIn(5, 80)
        set(value) = putInt(KEY_BATTERY, value.coerceIn(5, 80))

    var maximumParallelDownloads: Int
        get() = store.getInt(KEY_PARALLEL, 1).coerceIn(1, 2)
        set(value) = putInt(KEY_PARALLEL, value.coerceIn(1, 2))

    var speedLimitKbps: Int
        get() = store.getInt(KEY_SPEED, 0).coerceIn(0, 102400)
        set(value) = putInt(KEY_SPEED, value.coerceIn(0, 102400))

    var automaticRetry: Boolean
        get() = store.getBoolean(KEY_RETRY, true)
        set(value) = putBoolean(KEY_RETRY, value)

    var retryCount: Int
        get() = store.getInt(KEY_RETRY_COUNT, 3).coerceIn(0, 10)
        set(value) = putInt(KEY_RETRY_COUNT, value.coerceIn(0, 10))

    private fun putBoolean(key: String, value: Boolean) {
        store.putBoolean(key, value)
        touch()
    }

    private fun putInt(key: String, value: Int) {
        store.putInt(key, value)
        touch()
    }

    private fun touch() {
        _revision.value = System.currentTimeMillis()
    }

    private fun migrateLegacyPreferences() {
        val legacy = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!legacy.getBoolean(KEY_MIGRATED, false) && legacy.contains(KEY_AUTO)) {
            store.putBoolean(KEY_AUTO, legacy.getBoolean(KEY_AUTO, true))
            store.putBoolean(KEY_UNMETERED, legacy.getBoolean(KEY_UNMETERED, true))
            store.putBoolean(KEY_CHARGING, legacy.getBoolean(KEY_CHARGING, false))
            store.putInt(KEY_BATTERY, legacy.getInt(KEY_BATTERY, 20).coerceIn(5, 80))
            store.putInt(KEY_PARALLEL, legacy.getInt(KEY_PARALLEL, 1).coerceIn(1, 2))
            store.putInt(KEY_SPEED, legacy.getInt(KEY_SPEED, 0).coerceIn(0, 102400))
            store.putBoolean(KEY_RETRY, legacy.getBoolean(KEY_RETRY, true))
            store.putInt(KEY_RETRY_COUNT, legacy.getInt(KEY_RETRY_COUNT, 3).coerceIn(0, 10))
            legacy.edit().putBoolean(KEY_MIGRATED, true).apply()
            touch()
        }
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
        private const val KEY_MIGRATED = "datastore_migrated"
    }
}
