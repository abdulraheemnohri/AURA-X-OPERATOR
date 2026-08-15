package com.aurax.operator.ai.vision

import android.graphics.Bitmap

/**
 * Interface for vision model runtime.
 * Supports multimodal understanding (image + text).
 */
interface VisionRuntime {
    
    /**
     * Analyzes an image and returns a structured result.
     * @param imageInput The image and optional prompt to analyze.
     * @return VisionResult containing description, labels, and confidence.
     */
    suspend fun analyze(imageInput: ImageInput): VisionResult
    
    /**
     * Checks if the vision runtime is available (model loaded).
     */
    fun isAvailable(): Boolean
    
    /**
     * Loads a vision model from the specified path.
     * @param modelPath Path to the vision model file.
     * @return True if loaded successfully, false otherwise.
     */
    fun load(modelPath: String): Boolean
    
    /**
     * Unloads the vision model to free resources.
     */
    fun unload()
    
    /**
     * Gets the current status of the vision runtime.
     */
    fun getStatus(): VisionRuntimeStatus
}

/**
 * Data class for vision analysis input.
 */
data class ImageInput(
    val bitmap: Bitmap,
    val prompt: String? = null
)

/**
 * Data class for vision analysis result.
 */
data class VisionResult(
    val description: String,
    val labels: List<String>,
    val confidence: Float,
    val error: String? = null
)

/**
 * Enum for vision runtime status.
 */
enum class VisionRuntimeStatus {
    NOT_LOADED,
    LOADING,
    READY,
    ERROR
}

/**
 * Exception for vision runtime errors.
 */
class VisionRuntimeException(message: String) : Exception(message)