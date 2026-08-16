package com.aurax.operator.vision

import android.graphics.Rect
import com.aurax.operator.operator.ScreenContext
import com.aurax.operator.operator.UiElement
import com.aurax.operator.vision.ocr.OcrBlock
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

/**
 * Combines accessibility evidence with OCR evidence without granting OCR
 * authority over actionable UI. Accessibility remains the source of truth for
 * actions; OCR increases confidence and supplies visual text that the tree
 * cannot expose.
 */
class EvidenceFusionEngine @Inject constructor() {

    fun fuse(accessibility: ScreenContext?, ocrBlocks: List<OcrBlock>): FusedScreenEvidence {
        val accessibilityText = accessibility?.allText
            ?.split('|')
            ?.map(String::trim)
            ?.filter(String::isNotBlank)
            .orEmpty()
        val ocrText = ocrBlocks.map { it.text.trim() }.filter(String::isNotBlank)

        val agreement = agreementScore(accessibilityText, ocrText)
        val mergedText = mergeText(accessibilityText, ocrText)
        val actionable = accessibility?.clickableElements
            ?.mapNotNull(::actionableLabel)
            ?.distinctBy(::normalize)
            .orEmpty()

        val sources = buildSet {
            if (accessibility != null) add(PerceptionSource.ACCESSIBILITY)
            if (ocrBlocks.isNotEmpty()) add(PerceptionSource.OCR)
        }

        val confidence = when {
            accessibility != null && ocrBlocks.isNotEmpty() ->
                ((0.65f + agreement * 0.35f).coerceIn(0f, 1f))
            accessibility != null -> 0.65f
            ocrBlocks.isNotEmpty() -> 0.35f
            else -> 0f
        }

        return FusedScreenEvidence(
            text = mergedText,
            actionableTargets = actionable,
            confidence = confidence,
            agreement = agreement,
            sources = sources
        )
    }

    private fun mergeText(accessibilityText: List<String>, ocrText: List<String>): List<String> {
        val result = mutableListOf<String>()
        result += accessibilityText
        ocrText.forEach { candidate ->
            if (result.none { similar(it, candidate) }) result += candidate
        }
        return result
    }

    private fun agreementScore(accessibilityText: List<String>, ocrText: List<String>): Float {
        if (accessibilityText.isEmpty() || ocrText.isEmpty()) return 0f
        val matches = ocrText.count { ocr -> accessibilityText.any { acc -> similar(acc, ocr) } }
        return (matches.toFloat() / ocrText.size).coerceIn(0f, 1f)
    }

    private fun actionableLabel(element: UiElement): String? =
        listOf(element.text, element.contentDesc, element.resourceId)
            .firstOrNull { !it.isNullOrBlank() }

    private fun similar(a: String, b: String): Boolean {
        val left = normalize(a)
        val right = normalize(b)
        if (left.isEmpty() || right.isEmpty()) return false
        if (left == right || left.contains(right) || right.contains(left)) return true
        val leftTokens = left.split(' ').toSet()
        val rightTokens = right.split(' ').toSet()
        val intersection = leftTokens.intersect(rightTokens).size
        val union = leftTokens.union(rightTokens).size
        return union > 0 && intersection.toFloat() / union >= 0.6f
    }

    private fun normalize(value: String): String =
        value.lowercase().replace(Regex("\\s+"), " ").trim()
}

data class FusedScreenEvidence(
    val text: List<String>,
    val actionableTargets: List<String>,
    val confidence: Float,
    val agreement: Float,
    val sources: Set<PerceptionSource>
)
