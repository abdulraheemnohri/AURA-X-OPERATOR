package com.aurax.operator.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Encrypted, local-only preferences for operator configuration.
 * Keep policy/safety controls here so UI and runtime share one source of truth.
 */
class SecurePrefs(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "aura_secure",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var policy: String
        get() = prefs.getString(KEY_POLICY, "CONFIRM_ACTIONS") ?: "CONFIRM_ACTIONS"
        set(value) = prefs.edit().putString(KEY_POLICY, value).apply()

    var biometricRequired: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    var floatingIndicatorEnabled: Boolean
        get() = prefs.getBoolean(KEY_INDICATOR, true)
        set(value) = prefs.edit().putBoolean(KEY_INDICATOR, value).apply()

    var incognitoProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_INCOGNITO, true)
        set(value) = prefs.edit().putBoolean(KEY_INCOGNITO, value).apply()

    var hapticFeedbackEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTICS, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTICS, value).apply()

    var voiceAutoInterruptEnabled: Boolean
        get() = prefs.getBoolean(KEY_VOICE_INTERRUPT, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_INTERRUPT, value).apply()

    var confirmationSeconds: Int
        get() = prefs.getInt(KEY_COUNTDOWN, 3).coerceIn(1, 10)
        set(value) = prefs.edit().putInt(KEY_COUNTDOWN, value.coerceIn(1, 10)).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    var selectedModelPath: String
        get() = prefs.getString(KEY_MODEL_PATH, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MODEL_PATH, value).apply()

    var modelTemperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, 0.2f).coerceIn(0f, 1.5f)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value.coerceIn(0f, 1.5f)).apply()

    var modelMaxTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, 512).coerceIn(32, 2048)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value.coerceIn(32, 2048)).apply()

    var modelContextTokens: Int
        get() = prefs.getInt(KEY_CONTEXT, 2048).coerceIn(256, 4096)
        set(value) = prefs.edit().putInt(KEY_CONTEXT, value.coerceIn(256, 4096)).apply()

    var sttLanguage: String
        get() = prefs.getString(KEY_STT_LANGUAGE, "auto") ?: "auto"
        set(value) = prefs.edit().putString(KEY_STT_LANGUAGE, value).apply()

    var sttModelPath: String
        get() = prefs.getString(KEY_STT_MODEL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_STT_MODEL, value).apply()

    var ttsModelPath: String
        get() = prefs.getString(KEY_TTS_MODEL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TTS_MODEL, value).apply()

    fun clearModelSelection() {
        prefs.edit().remove(KEY_MODEL_PATH).apply()
    }

    companion object {
        private const val KEY_POLICY = "policy"
        private const val KEY_BIOMETRIC = "biometric_required"
        private const val KEY_INDICATOR = "floating_indicator"
        private const val KEY_INCOGNITO = "incognito_protection"
        private const val KEY_HAPTICS = "haptic_feedback"
        private const val KEY_VOICE_INTERRUPT = "voice_auto_interrupt"
        private const val KEY_COUNTDOWN = "confirmation_countdown"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_MODEL_PATH = "selected_model_path"
        private const val KEY_TEMPERATURE = "model_temperature"
        private const val KEY_MAX_TOKENS = "model_max_tokens"
        private const val KEY_CONTEXT = "model_context_tokens"
        private const val KEY_STT_LANGUAGE = "stt_language"
        private const val KEY_STT_MODEL = "stt_model_path"
        private const val KEY_TTS_MODEL = "tts_model_path"
    }
}
