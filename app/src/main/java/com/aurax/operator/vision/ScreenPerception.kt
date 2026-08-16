package com.aurax.operator.vision

import android.graphics.Bitmap
import com.aurax.operator.operator.ScreenContext
import com.aurax.operator.vision.ocr.OcrRuntime
import javax.inject.Inject

/**
 * Unified screen perception contract.
 * Accessibility is always available when the service is enabled; OCR is an
 * evidence source when a real runtime and screenshot are supplied.
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
    val ocrError: String? = null
) {
    val hasSensitiveContent: Boolean
        get() = (text + ocrText).any(::looksSensitive)

    companion object {
        private val sensitivePattern = Regex("(?i)\\b(password|passwd|otp|one[- ]?time|cvv|pin|verification code|secret|api key|token)\\b")
        private fun looksSensitive(value: String): Boolean = sensitivePattern.containsMatchIn(value)
    }
}

enum class PerceptionSource {
    ACCESSIBILITY,
    OCR,
    VISION
}

class AccessibilityFirstScreenPerception @Inject constructor(
    private val ocrRuntime: OcrRuntime
) : ScreenPerception {
    override suspend fun perceive(
        accessibility: ScreenContext?,
        screenshot: Bitmap?
    ): PerceivedScreen {
        val accessibilityText = accessibility?.allText.orEmpty()
        val actionableTargets = accessibility?.clickableNodes.orEmpty().mapNotNull { node ->
            node.text?.takeIf { it.isNotBlank() } ?: node.contentDescription?.takeIf { it.isNotBlank() }
        }

        val ocr = if (screenshot != null && ocrRuntime.isAvailable()) {
            ocrRuntime.recognize(screenshot)
        } else {
            null
        }

        val ocrText = ocr?.blocks?.map { it.text }.orEmpty()
        val sources = buildSet {
            if (accessibility != null) add(PerceptionSource.ACCESSIBILITY)
            if (ocrText.isNotEmpty()) add(PerceptionSource.OCR)
        }
        val textEvidenceCount = accessibilityText.size + ocrText.size
        val confidence = when {
            textEvidenceCount == 0 -> 0.5f
            accessibilityText.isNotEmpty() && ocrText.isNotEmpty() -> 1f
            else -> 0.9f
        }

        return PerceivedScreen(
            packageName = accessibility?.packageName,
            text = accessibilityText,
            actionableTargets = actionableTargets,
            ocrText = ocrText,
            confidence = confidence,
            sources = sources,
            ocrError = ocr?.error
        )
    }

    override fun isOcrAvailable(): Boolean = ocrRuntime.isAvailable()

    override fun isVisionAvailable(): Boolean = false
}
