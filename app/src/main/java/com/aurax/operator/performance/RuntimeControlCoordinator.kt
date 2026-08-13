package com.aurax.operator.performance

import android.content.Context
import com.aurax.operator.ai.model.AIModelRuntime
import com.aurax.operator.ai.model.ModelPreset
import com.aurax.operator.core.settings.SettingsRepository

class RuntimeControlCoordinator(context: Context) {
    private val settings = SettingsRepository(context.applicationContext)
    private val thermal = ThermalManager(context.applicationContext)

    fun apply(runtime: AIModelRuntime) {
        thermal.refresh()
        val preset = runCatching { ModelPreset.valueOf(settings.performanceMode.uppercase()) }.getOrDefault(ModelPreset.BALANCED)
        if (settings.thermalProtection && !thermal.canRunHeavyInference()) {
            runtime.setThreads(1)
            runtime.setContextLength(512)
            runtime.setMaxOutputTokens(128)
            runtime.setBatchSize(128)
            return
        }
        RuntimeTuning.applyPreset(runtime, preset)
        runtime.setThreads(settings.cpuThreads)
        runtime.setContextLength(settings.contextLength)
        runtime.setMaxOutputTokens(settings.maxOutputTokens)
        runtime.setBatchSize(settings.batchSize)
    }

    fun thermalState(): ThermalManager.State { thermal.refresh(); return thermal.state.value }
    fun settings(): SettingsRepository = settings
}
