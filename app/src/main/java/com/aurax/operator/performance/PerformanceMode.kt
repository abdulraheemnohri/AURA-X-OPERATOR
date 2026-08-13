package com.aurax.operator.performance

import com.aurax.operator.ai.model.AIModelRuntime

enum class PerformanceMode { BATTERY_SAVER, BALANCED, PERFORMANCE, CUSTOM;
    fun apply(runtime: AIModelRuntime) = when (this) {
        BATTERY_SAVER -> { runtime.setThreads(2); runtime.setContextLength(1024); runtime.setMaxOutputTokens(256); runtime.setBatchSize(256) }
        BALANCED -> { runtime.setThreads(4); runtime.setContextLength(2048); runtime.setMaxOutputTokens(512); runtime.setBatchSize(512) }
        PERFORMANCE -> { runtime.setThreads(6); runtime.setContextLength(4096); runtime.setMaxOutputTokens(1024); runtime.setBatchSize(1024) }
        CUSTOM -> Unit
    }
}
