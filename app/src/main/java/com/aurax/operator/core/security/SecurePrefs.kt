package com.aurax.operator.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/** Encrypted, device-local operator preferences. */
class SecurePrefs(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "aura_secure",
        MasterKey.Builder(context.applicationContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var policy: String
        get() = prefs.getString(KEY_POLICY, "CONFIRM_ACTIONS") ?: "CONFIRM_ACTIONS"
        set(value) = prefs.edit().putString(KEY_POLICY, value).apply()

    var requireBiometric: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC, value).apply()

    var indicatorAlwaysVisible: Boolean
        get() = prefs.getBoolean(KEY_INDICATOR, true)
        set(value) = prefs.edit().putBoolean(KEY_INDICATOR, value).apply()

    var blockIncognito: Boolean
        get() = prefs.getBoolean(KEY_INCOGNITO, true)
        set(value) = prefs.edit().putBoolean(KEY_INCOGNITO, value).apply()

    var hapticFeedback: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC, value).apply()

    var voiceAutoInterrupt: Boolean
        get() = prefs.getBoolean(KEY_VOICE_INTERRUPT, true)
        set(value) = prefs.edit().putBoolean(KEY_VOICE_INTERRUPT, value).apply()

    var countdownSeconds: Int
        get() = prefs.getInt(KEY_COUNTDOWN, 3).coerceIn(1, 10)
        set(value) = prefs.edit().putInt(KEY_COUNTDOWN, value.coerceIn(1, 10)).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()

    companion object {
        private const val KEY_POLICY = "policy"
        private const val KEY_BIOMETRIC = "require_biometric"
        private const val KEY_INDICATOR = "indicator_always_visible"
        private const val KEY_INCOGNITO = "block_incognito"
        private const val KEY_HAPTIC = "haptic_feedback"
        private const val KEY_VOICE_INTERRUPT = "voice_auto_interrupt"
        private const val KEY_COUNTDOWN = "countdown_seconds"
        private const val KEY_THEME = "theme_mode"
    }
}
