package com.aurax.operator.ai.vision

import android.graphics.Bitmap
import com.aurax.operator.operator.accessibility.AccessibilityTree
import com.aurax.operator.operator.ocr.OcrEngine

/**
 * Manages vision-related tasks, including multimodal understanding,
 * OCR, and accessibility tree fusion.
 */
class VisionManager(
    private val visionRuntime: VisionRuntime,
    private val ocrEngine: OcrEngine,
    private val accessibilityTree: AccessibilityTree
) {
    
    /**
     * Analyzes the screen using vision, OCR, and accessibility tree.
     * @param bitmap Screenshot of the current screen.
     * @return ScreenContext containing fused vision, OCR, and accessibility data.
     */
    suspend fun analyzeScreen(bitmap: Bitmap): ScreenContext {
        return if (visionRuntime.isAvailable()) {
            // Use vision model for multimodal understanding
            val visionResult = visionRuntime.analyze(ImageInput(bitmap))
            ScreenContext(
                vision = visionResult,
                ocr = ocrEngine.extractText(bitmap),
                accessibilityTree = accessibilityTree.getTree()
            )
        } else {
            // Fallback to OCR + Accessibility
            ScreenContext(
                vision = null,
                ocr = ocrEngine.extractText(bitmap),
                accessibilityTree = accessibilityTree.getTree()
            )
        }
    }
    
    /**
     * Checks if vision capabilities are available.
     */
    fun isVisionAvailable(): Boolean = visionRuntime.isAvailable()
    
    /**
     * Loads a vision model from the specified path.
     */
    fun loadVisionModel(modelPath: String): Boolean = visionRuntime.load(modelPath)
    
    /**
     * Unloads the vision model.
     */
    fun unloadVisionModel() = visionRuntime.unload()
    
    /**
     * Gets the status of the vision runtime.
     */
    fun getVisionStatus(): VisionRuntimeStatus = visionRuntime.getStatus()
}

/**
 * Data class for fused screen context.
 */
data class ScreenContext(
    val vision: VisionResult?,
    val ocr: String,
    val accessibilityTree: String
)
