package com.aurax.operator.vision

import android.graphics.Bitmap
import com.aurax.operator.operator.ScreenContext
import com.aurax.operator.vision.ocr.OcrRuntime
import javax.inject.Inject

/**
 * Unified screen perception contract.
 * Accessibility is authoritative for actionable targets; OCR contributes
 * independent visual text evidence when a real runtime and screenshot exist.
 */
interface ScreenPerception {
    suspend fun perceive(accessibility: ScreenContext?, screenshot: Bitmap? = null): PerceivedScreen
    fun isOcrAvailable(): Boolean
    fun isVisionAvailable(): Boolean
}

data class PerceivedScreen(
    val packageName: String?,
    val text: List<String>,
    val actionableTargets: List<String>,
    val ocrText: List<String> = emptyList(),
    val visualSummary: String? = null,
    val confidence: Float = 0f,
    val sources: Set<PerceptionSource> = emptySet(),
    val ocrError: String? = null,
    val evidenceAgreement: Float = 0f
) {
    val hasSensitiveContent: Boolean
        get() = (text + ocrText).any(::looksSensitive)

    companion object {
        private val sensitivePattern = Regex(
            "(?i)\\b(password|passwd|otp|one[- ]?time|cvv|pin|verification code|secret|api key|token)\\b"
        )
        private fun looksSensitive(value: String): Boolean = sensitivePattern.containsMatchIn(value)
    }
}

enum class PerceptionSource {
    ACCESSIBILITY,
    OCR,
    VISION
}

class AccessibilityFirstScreenPerception @Inject constructor(
    private val ocrRuntime: OcrRuntime,
    private val evidenceFusion: EvidenceFusionEngine
) : ScreenPerception {

    override suspend fun perceive(
        accessibility: ScreenContext?,
        screenshot: Bitmap?
    ): PerceivedScreen {
        val ocr = if (screenshot != null && ocrRuntime.isAvailable()) {
            ocrRuntime.recognize(screenshot)
        } else {
            null
        }

        val ocrBlocks = ocr?.blocks.orEmpty()
        val fused = evidenceFusion.fuse(accessibility, ocrBlocks)
        val ocrText = ocrBlocks.map { it.text }.filter { it.isNotBlank() }

        return PerceivedScreen(
            packageName = accessibility?.packageName,
            text = fused.text,
            actionableTargets = fused.actionableTargets,
            ocrText = ocrText,
            confidence = fused.confidence,
            sources = fused.sources,
            ocrError = ocr?.error,
            evidenceAgreement = fused.agreement
        )
    }

    override fun isOcrAvailable(): Boolean = ocrRuntime.isAvailable()

    override fun isVisionAvailable(): Boolean = false
}
