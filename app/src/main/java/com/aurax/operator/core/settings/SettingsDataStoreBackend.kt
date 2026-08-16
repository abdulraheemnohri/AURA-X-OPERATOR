package com.aurax.operator.core.settings

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val Context.auraSettingsDataStore by preferencesDataStore(
    name = "aura_settings",
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, "aura_settings_v3")) }
)

/** DataStore backend with automatic migration from the legacy settings store. */
class SettingsDataStoreBackend(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val cache = MutableStateFlow<Map<String, Any>>(emptyMap())

    init {
        scope.launch {
            cache.value = appContext.auraSettingsDataStore.data.first().asStringMap()
        }
    }

    fun contains(key: String): Boolean = cache.value.containsKey(key)
    fun getBoolean(key: String, default: Boolean) = (cache.value[key] as? Boolean) ?: default
    fun getInt(key: String, default: Int) = (cache.value[key] as? Int) ?: default
    fun getFloat(key: String, default: Float) = (cache.value[key] as? Float) ?: default
    fun getString(key: String, default: String) = (cache.value[key] as? String) ?: default

    fun putBoolean(key: String, value: Boolean) = put(booleanPreferencesKey(key), value)
    fun putInt(key: String, value: Int) = put(intPreferencesKey(key), value)
    fun putFloat(key: String, value: Float) = put(floatPreferencesKey(key), value)
    fun putString(key: String, value: String) = put(stringPreferencesKey(key), value)

    private fun <T> put(key: Preferences.Key<T>, value: T) {
        cache.value = cache.value.toMutableMap().apply { put(key.name, value as Any) }
        scope.launch {
            appContext.auraSettingsDataStore.edit { preferences -> preferences[key] = value }
        }
    }
}

private fun Preferences.asStringMap(): Map<String, Any> =
    asMap().entries.associate { (key, value) -> key.name to value }
