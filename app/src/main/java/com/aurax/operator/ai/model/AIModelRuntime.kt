package com.aurax.operator.ai.model

import com.aurax.operator.ai.inference.GenerationRequest

/**
 * Runtime abstraction shared by the native and test model backends.
 * Tuning methods are intentionally part of the runtime contract so the
 * performance controller can change inference pressure without knowing the
 * concrete backend.
 */
interface AIModelRuntime {
    suspend fun generate(request: GenerationRequest): String
    fun isReady(): Boolean

    fun setThreads(value: Int) = Unit
    fun setContextLength(value: Int) = Unit
    fun setMaxOutputTokens(value: Int) = Unit
    fun setBatchSize(value: Int) = Unit

    fun threads(): Int = 4
    fun contextLength(): Int = 2048
    fun maxOutputTokens(): Int = 512
    fun batchSize(): Int = 512
}
