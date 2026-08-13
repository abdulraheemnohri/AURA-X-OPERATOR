package com.aurax.operator.performance

import com.aurax.operator.ai.model.AIModelRuntime
import com.aurax.operator.ai.model.ModelPreset

/** Applies a bounded preset to any runtime. */
/** Applies a safe preset to any runtime. Backends may override tuning hooks when supported. */
object RuntimeTuning {
    fun applyPreset(runtime: AIModelRuntime, preset: ModelPreset) {
        when (preset) {
            ModelPreset.ULTRA_FAST -> {
                runtime.setThreads(2)
                runtime.setContextLength(512)
                runtime.setMaxOutputTokens(256)
                runtime.setBatchSize(256)
            }
            ModelPreset.BALANCED -> {
                runtime.setThreads(4)
                runtime.setContextLength(2048)
                runtime.setMaxOutputTokens(512)
                runtime.setBatchSize(512)
            }
            ModelPreset.QUALITY -> {
                runtime.setThreads(4)
                runtime.setContextLength(4096)
                runtime.setMaxOutputTokens(1024)
                runtime.setBatchSize(768)
            }
            ModelPreset.MAX_QUALITY -> {
                runtime.setThreads(6)
                runtime.setContextLength(8192)
                runtime.setMaxOutputTokens(2048)
                runtime.setBatchSize(1024)
            }
            ModelPreset.CUSTOM -> Unit
        }
    }
}
