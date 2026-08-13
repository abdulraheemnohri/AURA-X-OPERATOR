package com.aurax.operator.ai.inference

/**
 * Runtime generation controls. Values are clamped so a malformed UI/config value
 * cannot request an unbounded native inference workload.
 */
data class GenerationRequest(
    val prompt: String,
    val maxTokens: Int = 256,
    val temperature: Float = 0.2f,
    val contextTokens: Int = 2048
) {
    init {
        require(prompt.isNotBlank()) { "Prompt must not be blank" }
    }

    val safeMaxTokens: Int get() = maxTokens.coerceIn(32, 2048)
    val safeTemperature: Float get() = temperature.coerceIn(0f, 1.5f)
    val safeContextTokens: Int get() = contextTokens.coerceIn(256, 4096)
}
