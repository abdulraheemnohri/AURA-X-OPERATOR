package com.aurax.operator.voice

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WakeWordDetectorTest {
    @Test
    fun disabledDetectorNeverTriggers() {
        val detector = WakeWordDetector("hey aura")
        assertFalse(detector.onTranscript("hey aura"))
    }

    @Test
    fun matchingPhraseTriggersWhenEnabled() {
        val detector = WakeWordDetector("hey aura")
        detector.setEnabled(true)
        assertTrue(detector.onTranscript("please hey aura start listening"))
    }

    @Test
    fun sensitivityAndTranscriptCanBeUsedAsGate() {
        val detector = WakeWordDetector("open aura", 0.6f)
        detector.setEnabled(true)
        assertTrue(detector.onTranscript("open aura"))
        assertFalse(detector.onTranscript("unrelated phrase"))
    }
}
