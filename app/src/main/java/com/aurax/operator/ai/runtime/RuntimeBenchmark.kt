package com.aurax.operator.ai.runtime

import com.aurax.operator.ai.inference.GenerationRequest
import javax.inject.Inject
import javax.inject.Singleton

/** Lightweight on-device inference benchmark used for runtime model selection. */
data class RuntimeBenchmarkResult(
    val modelId: String,
    val elapsedMs: Long,
    val outputCharacters: Int,
    val estimatedTokensPerSecond: Float,
    val success: Boolean,
    val errorMessage: String? = null
)

@Singleton
class RuntimeBenchmark @Inject constructor(
    private val runtime: LocalRuntimeManager
) {
    suspend fun run(
        modelId: String,
        prompt: String = "Respond with one short sentence describing the current task."
    ): RuntimeBenchmarkResult {
        val loaded = runtime.load(modelId)
        if (loaded.isFailure) {
            return RuntimeBenchmarkResult(
                modelId = modelId,
                elapsedMs = 0L,
                outputCharacters = 0,
                estimatedTokensPerSecond = 0f,
                success = false,
                errorMessage = loaded.exceptionOrNull()?.message ?: "Unable to load model"
            )
        }

        val started = System.nanoTime()
        val generation = runtime.generate(
            GenerationRequest(
                prompt = prompt,
                maxTokens = 64,
                temperature = 0.2f,
                contextTokens = 1024
            )
        )
        val elapsedMs = ((System.nanoTime() - started) / 1_000_000L).coerceAtLeast(1L)

        return generation.fold(
            onSuccess = { output ->
                // A conservative estimate: four UTF-8-ish characters per token.
                val estimatedTokens = (output.length / 4f).coerceAtLeast(1f)
                RuntimeBenchmarkResult(
                    modelId = modelId,
                    elapsedMs = elapsedMs,
                    outputCharacters = output.length,
                    estimatedTokensPerSecond = estimatedTokens * 1000f / elapsedMs,
                    success = true
                )
            },
            onFailure = { error ->
                RuntimeBenchmarkResult(
                    modelId = modelId,
                    elapsedMs = elapsedMs,
                    outputCharacters = 0,
                    estimatedTokensPerSecond = 0f,
                    success = false,
                    errorMessage = error.message ?: "Inference failed"
                )
            }
        )
    }
}
