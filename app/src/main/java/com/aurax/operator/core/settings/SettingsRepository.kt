package com.aurax.operator.core.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Local, type-safe runtime settings. Values are persisted on-device. */
class SettingsRepository(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("aura_settings_v3", Context.MODE_PRIVATE)

    private fun bool(key: String, default: Boolean) = prefs.getBoolean(key, default)
    private fun int(key: String, default: Int) = prefs.getInt(key, default)
    private fun float(key: String, default: Float) = prefs.getFloat(key, default)
    private fun string(key: String, default: String) = prefs.getString(key, default) ?: default

    private fun putBoolean(key: String, value: Boolean) { prefs.edit().putBoolean(key, value).apply() }
    private fun putInt(key: String, value: Int) { prefs.edit().putInt(key, value).apply() }
    private fun putFloat(key: String, value: Float) { prefs.edit().putFloat(key, value).apply() }
    private fun putString(key: String, value: String) { prefs.edit().putString(key, value).apply() }

    var activeModelId: String
        get() = string("active_model_id", "qwen2.5-0.5b-instruct-q4km")
        set(value) = putString("active_model_id", value)

    var contextLength: Int
        get() = int("context_length", 2048).coerceIn(256, 4096)
        set(value) = putInt("context_length", value.coerceIn(256, 4096))

    var temperature: Float
        get() = float("temperature", 0.2f).coerceIn(0f, 1.5f)
        set(value) = putFloat("temperature", value.coerceIn(0f, 1.5f))

    var topP: Float
        get() = float("top_p", 0.9f).coerceIn(0.1f, 1f)
        set(value) = putFloat("top_p", value.coerceIn(0.1f, 1f))

    var topK: Int
        get() = int("top_k", 40).coerceIn(1, 200)
        set(value) = putInt("top_k", value.coerceIn(1, 200))

    var maxOutputTokens: Int
        get() = int("max_output_tokens", 512).coerceIn(32, 4096)
        set(value) = putInt("max_output_tokens", value.coerceIn(32, 4096))

    var thinkingMode: String
        get() = string("thinking_mode", "BALANCED")
        set(value) = putString("thinking_mode", value)

    var cpuThreads: Int
        get() = int("cpu_threads", 4).coerceIn(1, 12)
        set(value) = putInt("cpu_threads", value.coerceIn(1, 12))

    var gpuAcceleration: Boolean
        get() = bool("gpu_acceleration", false)
        set(value) = putBoolean("gpu_acceleration", value)

    var batchSize: Int
        get() = int("batch_size", 512).coerceIn(32, 2048)
        set(value) = putInt("batch_size", value.coerceIn(32, 2048))

    var wakeWordEnabled: Boolean
        get() = bool("wake_word_enabled", false)
        set(value) = putBoolean("wake_word_enabled", value)

    var wakeWordSensitivity: Float
        get() = float("wake_word_sensitivity", 0.7f).coerceIn(0.1f, 1f)
        set(value) = putFloat("wake_word_sensitivity", value.coerceIn(0.1f, 1f))

    var sttModelId: String
        get() = string("stt_model_id", "whisper-base")
        set(value) = putString("stt_model_id", value)

    var ttsVoiceId: String
        get() = string("tts_voice_id", "default")
        set(value) = putString("tts_voice_id", value)

    var ttsSpeed: Float
        get() = float("tts_speed", 1f).coerceIn(0.5f, 2f)
        set(value) = putFloat("tts_speed", value.coerceIn(0.5f, 2f))

    var ttsPitch: Float
        get() = float("tts_pitch", 1f).coerceIn(0.8f, 1.2f)
        set(value) = putFloat("tts_pitch", value.coerceIn(0.8f, 1.2f))

    var ttsVolume: Float
        get() = float("tts_volume", 1f).coerceIn(0.1f, 1f)
        set(value) = putFloat("tts_volume", value.coerceIn(0.1f, 1f))

    var autoDetectLanguage: Boolean
        get() = bool("auto_detect_language", true)
        set(value) = putBoolean("auto_detect_language", value)

    var preferredLanguage: String
        get() = string("preferred_language", "auto")
        set(value) = putString("preferred_language", value)

    var endOfSpeechTimeout: Int
        get() = int("end_of_speech_timeout", 2).coerceIn(1, 5)
        set(value) = putInt("end_of_speech_timeout", value.coerceIn(1, 5))

    var bargeInEnabled: Boolean
        get() = bool("barge_in_enabled", true)
        set(value) = putBoolean("barge_in_enabled", value)

    var automationPolicy: String
        get() = string("automation_policy", "CONFIRM_ACTIONS")
        set(value) = putString("automation_policy", value)

    var confirmationSeconds: Int
        get() = int("confirmation_seconds", 3).coerceIn(1, 10)
        set(value) = putInt("confirmation_seconds", value.coerceIn(1, 10))

    var maxActionsPerTask: Int
        get() = int("max_actions", 30).coerceIn(1, 200)
        set(value) = putInt("max_actions", value.coerceIn(1, 200))

    var maxTaskSeconds: Int
        get() = int("max_task_seconds", 120).coerceIn(5, 900)
        set(value) = putInt("max_task_seconds", value.coerceIn(5, 900))

    var screenshotVerification: Boolean
        get() = bool("screenshot_verification", true)
        set(value) = putBoolean("screenshot_verification", value)

    var visionModelEnabled: Boolean
        get() = bool("vision_model_enabled", false)
        set(value) = putBoolean("vision_model_enabled", value)

    var countdownEnabled: Boolean
        get() = bool("countdown_enabled", true)
        set(value) = putBoolean("countdown_enabled", value)

    var abortOnScreenChange: Boolean
        get() = bool("abort_on_screen_change", true)
        set(value) = putBoolean("abort_on_screen_change", value)

    var memoryEnabled: Boolean
        get() = bool("memory_enabled", true)
        set(value) = putBoolean("memory_enabled", value)

    var autoExtractMemories: Boolean
        get() = bool("auto_extract_memories", true)
        set(value) = putBoolean("auto_extract_memories", value)

    var maxMemories: Int
        get() = int("max_memories", 5000).coerceIn(100, 50000)
        set(value) = putInt("max_memories", value.coerceIn(100, 50000))

    var memoryRetentionDays: Int
        get() = int("memory_retention_days", 365).coerceIn(1, 3650)
        set(value) = putInt("memory_retention_days", value.coerceIn(1, 3650))

    var vectorSearchEnabled: Boolean
        get() = bool("vector_search_enabled", true)
        set(value) = putBoolean("vector_search_enabled", value)

    var embeddingModelId: String
        get() = string("embedding_model_id", "hash-embedding")
        set(value) = putString("embedding_model_id", value)

    var localOnlyMode: Boolean
        get() = bool("local_only_mode", true)
        set(value) = putBoolean("local_only_mode", value)

    var internetSearchEnabled: Boolean
        get() = bool("internet_search_enabled", false)
        set(value) = putBoolean("internet_search_enabled", value)

    var searchProvider: String
        get() = string("search_provider", "duckduckgo")
        set(value) = putString("search_provider", value)

    var activityLogging: Boolean
        get() = bool("activity_logging", true)
        set(value) = putBoolean("activity_logging", value)

    var safetyLogRetention: Int
        get() = int("safety_log_retention", 90).coerceIn(7, 3650)
        set(value) = putInt("safety_log_retention", value.coerceIn(7, 3650))

    var biometricLock: Boolean
        get() = bool("biometric_lock", true)
        set(value) = putBoolean("biometric_lock", value)

    var incognitoRespected: Boolean
        get() = bool("incognito_respected", true)
        set(value) = putBoolean("incognito_respected", value)

    var passwordFilterEnabled: Boolean
        get() = bool("password_filter", true)
        set(value) = putBoolean("password_filter", value)

    var theme: String
        get() = string("theme", "SYSTEM")
        set(value) = putString("theme", value)

    var dynamicColors: Boolean
        get() = bool("dynamic_colors", false)
        set(value) = putBoolean("dynamic_colors", value)

    var animationsEnabled: Boolean
        get() = bool("animations_enabled", true)
        set(value) = putBoolean("animations_enabled", value)

    var reducedMotion: Boolean
        get() = bool("reduced_motion", false)
        set(value) = putBoolean("reduced_motion", value)

    var fontScale: Float
        get() = float("font_scale", 1f).coerceIn(0.85f, 1.5f)
        set(value) = putFloat("font_scale", value.coerceIn(0.85f, 1.5f))

    var glassmorphismIntensity: Float
        get() = float("glass_intensity", 0.8f).coerceIn(0f, 1f)
        set(value) = putFloat("glass_intensity", value.coerceIn(0f, 1f))

    var orbStyle: String
        get() = string("orb_style", "CLASSIC")
        set(value) = putString("orb_style", value)

    var performanceMode: String
        get() = string("performance_mode", "BALANCED")
        set(value) = putString("performance_mode", value)

    var batterySaverThreshold: Int
        get() = int("battery_threshold", 20).coerceIn(5, 80)
        set(value) = putInt("battery_threshold", value.coerceIn(5, 80))

    var thermalProtection: Boolean
        get() = bool("thermal_protection", true)
        set(value) = putBoolean("thermal_protection", value)

    var backgroundTasks: Boolean
        get() = bool("background_tasks", true)
        set(value) = putBoolean("background_tasks", value)

    var autoUnloadModel: Boolean
        get() = bool("auto_unload_model", true)
        set(value) = putBoolean("auto_unload_model", value)

    var autoUnloadTimeout: Int
        get() = int("auto_unload_timeout", 10).coerceIn(1, 120)
        set(value) = putInt("auto_unload_timeout", value.coerceIn(1, 120))

    var lanServerEnabled: Boolean
        get() = bool("lan_server_enabled", false)
        set(value) = putBoolean("lan_server_enabled", value)

    var lanServerPort: Int
        get() = int("lan_server_port", 8080).coerceIn(1024, 65535)
        set(value) = putInt("lan_server_port", value.coerceIn(1024, 65535))

    var lanServerAuth: Boolean
        get() = bool("lan_server_auth", true)
        set(value) = putBoolean("lan_server_auth", value)

    var remoteModelUrl: String
        get() = string("remote_model_url", "")
        set(value) = putString("remote_model_url", value)

    var debugMode: Boolean
        get() = bool("debug_mode", false)
        set(value) = putBoolean("debug_mode", value)

    var verboseLogging: Boolean
        get() = bool("verbose_logging", false)
        set(value) = putBoolean("verbose_logging", value)

    var showInternalStates: Boolean
        get() = bool("show_internal_states", false)
        set(value) = putBoolean("show_internal_states", value)

    var exportDebugInfo: Boolean
        get() = bool("export_debug_info", false)
        set(value) = putBoolean("export_debug_info", value)

    private val _revision = MutableStateFlow(0L)
    val revision: StateFlow<Long> = _revision.asStateFlow()

    fun touch() {
        _revision.value = System.currentTimeMillis()
    }
}
