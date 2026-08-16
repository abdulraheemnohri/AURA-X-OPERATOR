package com.aurax.operator.ai.runtime

/**
 * Contract for a real local text-embedding runtime.
 *
 * Implementations must return model-derived vectors. Hashing, TF/IDF and other
 * lexical heuristics are not embeddings and must not implement this contract.
 */
interface EmbeddingRuntime {
    suspend fun embed(text: String): FloatArray

    fun isRuntimeAvailable(): Boolean
    fun isModelAvailable(): Boolean

    fun isOperational(): Boolean = isRuntimeAvailable() && isModelAvailable()

    val dimension: Int
}
