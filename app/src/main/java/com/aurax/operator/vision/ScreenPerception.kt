package com.aurax.operator.vision

import com.aurax.operator.operator.ScreenContext

/**
 * Unified screen perception contract.
 * Accessibility remains the authoritative source until OCR/vision runtimes are available.
 */
interface ScreenPerception {
    suspend fun perceive(accessibility: ScreenContext?): PerceivedScreen
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
    val sources: Set<PerceptionSource> = emptySet()
) {
    val hasSensitiveContent: Boolean
        get() = text.any(::looksSensitive) || ocrText.any(::looksSensitive)

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

class AccessibilityFirstScreenPerception : ScreenPerception {
    override suspend fun perceive(accessibility: ScreenContext?): PerceivedScreen {
        if (accessibility == null) return PerceivedScreen(null, emptyList(), emptyList())
        return PerceivedScreen(
            packageName = accessibility.packageName,
            text = accessibility.allText,
            actionableTargets = accessibility.clickableNodes.mapNotNull { node ->
                node.text?.takeIf { it.isNotBlank() } ?: node.contentDescription?.takeIf { it.isNotBlank() }
            },
            confidence = if (accessibility.allText.isNotEmpty()) 1f else 0.5f,
            sources = setOf(PerceptionSource.ACCESSIBILITY)
        )
    }

    override fun isOcrAvailable(): Boolean = false
    override fun isVisionAvailable(): Boolean = false
}
