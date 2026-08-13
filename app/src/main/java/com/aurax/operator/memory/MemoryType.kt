package com.aurax.operator.memory

enum class MemoryType {
    PREFERENCE, FACT, EPISODIC, STRATEGY, PROCEDURAL,
    DOCUMENT, CONVERSATION, TEMPORAL, RELATIONSHIP
}

data class ScoredMemory(
    val key: String,
    val value: String,
    val type: MemoryType,
    val timestamp: Long,
    val accessCount: Int = 0,
    val userRating: Int? = null
)

object MemoryImportance {
    fun score(memory: ScoredMemory, nowMs: Long = System.currentTimeMillis()): Float {
        val ageDays = ((nowMs - memory.timestamp).coerceAtLeast(0L) / 86_400_000f)
        val recency = kotlin.math.exp(-ageDays / 30f)
        val frequency = kotlin.math.log10((memory.accessCount + 1).toFloat()) / 2f
        val rating = (memory.userRating?.coerceIn(0, 5)?.div(5f) ?: 0.5f)
        val typeBoost = when (memory.type) {
            MemoryType.PREFERENCE -> 1.2f
            MemoryType.RELATIONSHIP -> 1.3f
            MemoryType.STRATEGY -> 1.1f
            else -> 1.0f
        }
        return (recency * 0.4f + frequency.coerceIn(0f, 1f) * 0.3f + rating * 0.2f + typeBoost * 0.1f)
            .coerceIn(0f, 1f)
    }
}
