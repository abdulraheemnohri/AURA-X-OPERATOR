package com.aurax.operator.voice

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

/** Lightweight wake-phrase gate. Real-time audio model integration can plug into the same API. */
class WakeWordDetector(
    initialPhrase: String = "hey aura",
    initialSensitivity: Float = 0.7f
) {
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _detectedCount = MutableStateFlow(0)
    val detectedCount: StateFlow<Int> = _detectedCount.asStateFlow()

    var phrase: String = initialPhrase.trim().lowercase()
    var sensitivity: Float = initialSensitivity.coerceIn(0.1f, 1f)

    fun setEnabled(value: Boolean) {
        _enabled.value = value
    }

    fun onTranscript(text: String): Boolean {
        if (!_enabled.value) return false
        val normalized = text.trim().lowercase()
        if (normalized.isBlank() || phrase.isBlank()) return false
        val score = similarity(normalized, phrase)
        val detected = normalized.contains(phrase) || score >= max(0.65f, sensitivity)
        if (detected) _detectedCount.value += 1
        return detected
    }

    private fun similarity(a: String, b: String): Float {
        val left = a.split(Regex("\\s+")).toSet()
        val right = b.split(Regex("\\s+")).toSet()
        if (left.isEmpty() || right.isEmpty()) return 0f
        return left.intersect(right).size.toFloat() / right.size.toFloat()
    }
}
