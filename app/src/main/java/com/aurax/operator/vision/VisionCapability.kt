package com.aurax.operator.vision

/** Truthful capability state for the optional multimodal vision runtime. */
enum class VisionCapability {
    NOT_BUNDLED,
    MODEL_GATED,
    INITIALIZING,
    READY,
    ERROR
}

/** A vision runtime must expose availability rather than implying support from its interface alone. */
data class VisionRuntimeState(
    val capability: VisionCapability,
    val modelId: String? = null,
    val lastError: String? = null
)
