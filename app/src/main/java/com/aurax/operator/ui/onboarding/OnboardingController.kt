package com.aurax.operator.ui.onboarding

import android.content.Context
import android.content.SharedPreferences

class OnboardingController(context: Context) {
    enum class Step { WELCOME, PERMISSIONS, SAFETY, MODEL, VOICE, COMPLETE }
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("aura_onboarding_v3", Context.MODE_PRIVATE)
    var completed: Boolean get() = prefs.getBoolean("completed", false) private set(value) { prefs.edit().putBoolean("completed", value).apply() }
    var step: Step get() = runCatching { Step.valueOf(prefs.getString("step", Step.WELCOME.name) ?: Step.WELCOME.name) }.getOrDefault(Step.WELCOME) private set(value) { prefs.edit().putString("step", value.name).apply() }
    fun advance() { step = when(step) { Step.WELCOME->Step.PERMISSIONS; Step.PERMISSIONS->Step.SAFETY; Step.SAFETY->Step.MODEL; Step.MODEL->Step.VOICE; Step.VOICE->Step.COMPLETE; Step.COMPLETE->Step.COMPLETE }; if(step==Step.COMPLETE) completed=true }
    fun back() { step = when(step) { Step.WELCOME->Step.WELCOME; Step.PERMISSIONS->Step.WELCOME; Step.SAFETY->Step.PERMISSIONS; Step.MODEL->Step.SAFETY; Step.VOICE->Step.MODEL; Step.COMPLETE->Step.VOICE }; completed=false }
    fun skipToComplete() { step=Step.COMPLETE; completed=true }
    fun reset() { prefs.edit().clear().apply() }
}
