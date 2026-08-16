package com.aurax.operator.vision

import com.aurax.operator.operator.ScreenContext
import com.aurax.operator.vision.ocr.OcrBlock

/**
 * Fuses independent screen evidence without granting OCR authority over actions.
 * Accessibility remains the authoritative source for actionable targets.
 */
class EvidenceFusionEngine {

    fun fuse(accessibility: ScreenContext?, ocrBlocks: List<OcrBlock>): FusedScreenEvidence {
        val accessibilityText = accessibility?.allText.orEmpty()
            .map(String::normalizeEvidence)
            .filter(String::isNotBlank)
            .distinct()

        val ocrText = ocrBlocks.asSequence()
            .map { it.text.normalizeEvidence() }
            .filter(String::isNotBlank)
            .distinct()
            .toList()

        val mergedText = mergeText(accessibilityText, ocrText)
        val actionable = accessibility?.clickableNodes.orEmpty().mapNotNull { node ->
            val label = node.text?.takeIf { it.isNotBlank() }
                ?: node.contentDescription?.takeIf { it.isNotBlank() }
            label?.trim()
        }.distinct()

        val agreement = if (accessibilityText.isEmpty() || ocrText.isEmpty()) {
            0f
        } else {
            val ocrSet = ocrText.toSet()
            accessibilityText.count { it in ocrSet }.toFloat() / accessibilityText.size
        }

        val confidence = when {
            accessibilityText.isEmpty() && ocrText.isEmpty() -> 0.5f
            accessibilityText.isNotEmpty() && ocrText.isNotEmpty() -> 0.75f + (agreement * 0.25f)
            accessibilityText.isNotEmpty() -> 0.9f
            else -> 0.6f
        }.coerceIn(0f, 1f)

        return FusedScreenEvidence(
            text = mergedText,
            actionableTargets = actionable,
            agreement = agreement,
            confidence = confidence,
            sources = buildSet {
                if (accessibility != null) add(PerceptionSource.ACCESSIBILITY)
                if (ocrText.isNotEmpty()) add(PerceptionSource.OCR)
            }
        )
    }

    private fun mergeText(accessibility: List<String>, ocr: List<String>): List<String> {
        val result = LinkedHashSet<String>()
        accessibility.forEach(result::add)
        ocr.forEach { candidate ->
            if (result.none { it.similarEvidenceTo(candidate) }) result.add(candidate)
        }
        return result.toList()
    }

    private fun String.normalizeEvidence(): String =
        trim().replace(Regex("\\s+"), " ")

    private fun String.similarEvidenceTo(other: String): Boolean {
        if (equals(other, ignoreCase = true)) return true
        val a = lowercase().replace(Regex("\\W+"), "")
        val b = other.lowercase().replace(Regex("\\W+"), "")
        return a.isNotBlank() && b.isNotBlank() && (a.contains(b) || b.contains(a))
    }
}

data class FusedScreenEvidence(
    val text: List<String>,
    val actionableTargets: List<String>,
    val agreement: Float,
    val confidence: Float,
    val sources: Set<PerceptionSource>
)
