package com.aurax.operator.ai.runtime

/**
 * Local embedding contract used by semantic memory and RAG.
 *
 * Implementations must return real model embeddings. A lexical/hash vector is
 * intentionally not accepted as an embedding implementation.
 */
interface EmbeddingRuntime {
    suspend fun embed(text: String): FloatArray

    fun isRuntimeAvailable(): Boolean

    fun isModelAvailable(): Boolean

    fun isOperational(): Boolean = isRuntimeAvailable() && isModelAvailable()
}

/** Explicit unavailable state until a real local embedding backend is installed. */
class UnavailableEmbeddingRuntime : EmbeddingRuntime {
    override suspend fun embed(text: String): FloatArray =
        error("Local embedding runtime is unavailable")

    override fun isRuntimeAvailable(): Boolean = false

    override fun isModelAvailable(): Boolean = false
}
